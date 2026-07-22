# CottenDNS — Engineering Changes & Design Notes

A technical walkthrough of the changes made to CottenDNS, written for network
engineers who want to evaluate the design. It explains **what problem each change
solves**, **how it works**, **how it is wired into the data path**, and **why it
helps** on hostile DNS networks. Honest caveats are called out where they exist.

---

## 0. System model (read this first)

CottenDNS tunnels TCP over DNS. The client exposes a local SOCKS5/TCP
listener, chops each stream into DNS-safe packets, and sends them as the QNAME
labels of DNS **queries** through one or more recursive resolvers to an
authoritative CottenDNS server. The server reassembles the stream, makes the
real outbound connection, and returns downstream data inside DNS **answers**
(TXT/CNAME/A/NULL/HTTPS records).

```
app ──TCP──> client ──DNS query (UDP/53)──> resolver(s) ──> CottenDNS server ──> internet
app <──TCP── client <──DNS answer──────────── resolver(s) <───────────────────────┘
```

Two structural facts drive almost every design decision below:

1. **MTU is a session-global, server-negotiated property.** A single
   `SESSION_INIT` fixes one upload MTU and one download MTU for the entire
   session. The server sizes *every* download packet to that one download MTU,
   and those answers return through whichever resolver carried the poll. You
   therefore cannot run different resolvers at different MTUs inside one session
   without either multiple sessions or per-poll renegotiation.
2. **Loss is bidirectional and path-dependent.** The resolver↔server and
   resolver↔client legs are lossy, asymmetric, rate-limited, and the resolver
   itself silently truncates oversized answers. The reliability layer must treat
   loss as normal, not exceptional.

The transport is UDP/53 by default, with an automatic fallback to
**DNS-over-TCP/53** when UDP is filtered (§10). Everything that follows is built
around the two facts above and applies to both transports.

---

## 1. Reliability core: ARQ correctness & efficiency

**Problem.** The selective-repeat ARQ (per-stream windows, ACK/NACK, RTO
retransmission) had two scaling issues: (a) background session sweeps iterated a
full session table, and (b) the retransmit checker rescanned the whole send
buffer on every tick even when nothing was due.

**What changed.**
- **Active-ID sweeps.** Session housekeeping now iterates an `activeIDs` set
  maintained at insert/remove instead of scanning the whole (now 65535-slot)
  session array. O(active) instead of O(capacity).
- **O(1) retransmit deadline hint.** Each ARQ keeps `minRetransmitAt`, a provable
  lower bound on the earliest moment any buffered packet could need action (RTO
  due-time or TTL expiry). A tick where `now < minRetransmitAt` skips the entire
  send-buffer scan. The hint is invalidated at every send/dispatch and recomputed
  after each real scan, so it can never skip a due retransmit.

**Why it helps.** The reliability layer stays cheap as the number of concurrent
sessions/streams grows, which matters once the session space is widened (§3).

---

## 2. Forward Error Correction (FEC) on the download path

**Problem.** Under heavy loss, ARQ recovers by retransmitting — but each
retransmit costs a full resolver round-trip (often hundreds of ms). At 30–75%
loss the tunnel spends most of its time waiting for retransmits. We want the
client to *reconstruct* lost downstream packets without a round-trip.

**Mechanism — Reed-Solomon over packet blocks.**
- `internal/fec/fec.go`: block codec. `EncodePackets(packets, parityShards)`
  turns `N` data packets into `N + K` equal-size shards; any `N` of the `N+K`
  shards reconstruct the block. `ParityForLoss(dataShards, lossFrac)` sizes `K`
  for a target loss.
- `internal/fec/stream.go`: a stateful streaming layer. `Encoder` buffers data
  units and emits framed shards at each block boundary (`Flush()` bounds latency
  when the stream pauses); `Decoder` collects shards and returns the recovered
  data units once a block is decodable. Shard frame header (9 bytes):
  `blockID(4) | shardIndex(1) | dataShards(1) | parityShards(1) | shardSize(2)`.
- `internal/vpnproto/fec_unit.go`: each block element is one data packet
  serialized as `seq(2) | fragID(1) | payload`, so a recovered unit can be
  replayed into ARQ exactly as if its `STREAM_DATA` had arrived.

**How it is wired (server → client), default-off, byte-identical when disabled.**
- New packet type `PACKET_FEC_SHARD = 0x38`, flagged `valid | stream` (carries a
  StreamID for routing, no seq/frag of its own).
- **Server** (`internal/udpserver/stream_server.go`): when a stream has FEC on,
  `STREAM_DATA`/`STREAM_RESEND` popped from the transmit queue at *dequeue time*
  are folded into the stream's `fec.Encoder` and emitted as `PACKET_FEC_SHARD`
  frames through the **same** transmit queue. ARQ above is untouched — it still
  tracks, dedups, and retransmits the underlying data packets, providing the
  backstop when a block is lost beyond recovery. A trailing partial block is
  flushed when the queue drains so a paused stream's tail is not stuck below a
  block boundary.
- **Client** (`internal/client/stream_client.go`): a per-stream `fec.Decoder`
  ingests shards routed by StreamID and replays each recovered unit into the
  stream's ARQ via `ReceiveData`. ARQ dedups by sequence number, so a unit that
  also arrived directly is a harmless no-op; a recovered one saves a retransmit.

**Why it helps.** FEC converts loss into redundancy paid *up front* instead of
latency paid *per loss*. With `block=4, parity=12` a block survives losing 12 of
16 shards — i.e. **~75% shard loss** — with no round-trip. ARQ remains the
correctness backstop, so FEC is a pure latency/throughput optimization, never a
reliability risk.

**Validation.** Codec + stream tests prove reconstruction at 75% loss; a
server-side integration test drives the live `feedFECData → flushFEC → popFECShard`
path through a `fec.Decoder` with 50% shard loss; an end-to-end test echoes 64 KB
with FEC forced on through the real binaries.

---

## 3. Loss-triggered (automatic) FEC

**Problem.** Always-on FEC wastes bandwidth on healthy links; manually toggling
it per deployment is impractical. We want FEC that switches on only when a stream
is actually losing packets, and scales its strength to the loss.

**Mechanism — server-autonomous, zero protocol/ARQ change.** The server's
`PushTXPacket` is the single funnel for both `STREAM_DATA` (originals) and
`STREAM_RESEND` (retransmits), so each stream can measure *its own* download loss
from the retransmit rate over a sliding window (64 sends) with no new signaling:

```
loss ≈ retransmits / (originals + retransmits)   (per window, per stream)
```

When loss crosses `FEC_AUTO_LOSS_THRESHOLD`, the stream turns FEC on with parity
= `ParityForLoss(block, loss)`, clamped to `[FEC_PARITY, FEC_AUTO_MAX_PARITY]`.
One or two clean windows relax parity toward the base without flapping. After
three consecutive below-threshold windows, FEC fully disengages and the stream
returns to raw ARQ packets with **zero FEC bandwidth or Reed-Solomon CPU
overhead**. The encoder object is retained only to keep block IDs monotonic if
loss later re-engages FEC; its partial block and queued shards are discarded,
and ARQ remains the delivery backstop. A dequeue/disengage race sends the packet
raw immediately instead of waiting for its retransmission timer.

**Config.** `FEC_AUTO_ENABLED` (default true), `FEC_AUTO_LOSS_THRESHOLD` (0.3),
`FEC_AUTO_MAX_PARITY` (0 → auto-caps at 4× block). `FEC_DOWNLOAD_ENABLED`
(always-on, fixed parity) takes precedence when set.

**Why it helps.** Healthy links stay lean; links that start losing packets
self-protect within ~64 packets, transparently to the client (which already
handles raw data and shards interchangeably). It directly targets the project's
goal of remaining usable at very high loss without operator intervention.

**Caveat.** The loss signal is the retransmit rate, which is a proxy for true
path loss; it reacts at window granularity, not instantaneously.

---

## 4. More transport channels, accepted by the server by default

**Problem.** Carrying everything over TXT is a fingerprint, and answering a
non-TXT query (e.g. `A`) with a TXT record is protocol-incoherent and gets
dropped by strict resolvers. We want the client to be able to *rotate the DNS
record type* it queries, and the server to answer with a matching record type.

**Mechanism.**
- **Query-type rotation (client → server).** The client rotates `QUERY_TYPES`
  (e.g. `["TXT","CNAME","NULL","HTTPS"]`) per query. The tunnel payload always
  rides in the QNAME labels, so the server reads it regardless of record type.
  The server's domain matcher accepts **any** tunnel-transport query type by
  default (`IsTunnelTransportQueryType`), so the client can switch delivery
  method with no server reconfiguration.
- **Response RR-type matching (server → client).**
  `BuildVPNResponsePacketMatchingQuery` picks an answer encoding that matches the
  question:
  - `TXT` → TXT chunks (default; highest capacity over recursive resolvers).
  - `NULL` → the frame verbatim in the answer RDATA
    (`internal/dnsparser/transport_rrchannels.go`).
  - `HTTPS`/`SVCB` → the frame inside a service-binding SvcParam (private key),
    with a root TargetName — looks like an ordinary service record.
  - `A` → IPv4 A-records (`internal/dnsparser/transport_arecord.go`,
    index byte + 3 data bytes/record, reorder-safe, ~766 B cap, opt-in).
  - other types → CNAME target, with automatic fallback to TXT for large frames.
  The client's `ExtractVPNResponseMatching` auto-detects and decodes whichever
  channel was used, so no negotiation is required.

**Why it helps.** It breaks the "all-TXT" fingerprint, lets the client adapt to
resolvers/paths that handle some record types better than others, and keeps the
answer RR-type a legal match for the question (important for resolvers that
validate that). IPv6/AAAA is intentionally not used as a data channel because the
target networks commonly block IPv6.

**Validation.** Round-trip tests for NULL/HTTPS/SVCB/A; an end-to-end test runs
the client rotating `["TXT","CNAME","NULL","HTTPS"]` and echoes 64 KB intact.

---

## 5. Dynamic encryption: server auto-detects the client's method

**Problem.** Pinning one cipher per deployment is brittle; we want a client to
change its encryption method without the server being reconfigured.

**Mechanism.** Methods: 0 None, 1 XOR, 2 ChaCha20, 3/4/5 AES-128/192/256-GCM
(3–5 are AEAD). With `ENCRYPTION_AUTO_DETECT` (default true), the server builds a
codec set and trial-decrypts each inbound frame, **AEAD methods first** (they
authenticate, so they cannot be mis-detected), falling back to the unauthenticated
ciphers. The first codec that yields a valid frame is used.

**Why it helps.** A client can pick a rarer/stronger cipher (or rotate) and the
server simply reads it. AEAD-first ordering avoids false positives from the
unauthenticated ciphers.

---

## 6. Larger session space

The session ID was widened from **uint8 (256)** to **uint16 (65535)** across the
wire header, `SESSION_INIT`/`SESSION_ACCEPT` payloads, the server session store,
and ARQ. This removes the 256-concurrent-session ceiling so a single server can
host far more users — which is why the ARQ/sweep efficiency work in §1 matters.

---

## 7. Adaptive per-group MTU (the core throughput change)

**Problem.** The client measured each resolver's viable MTU but then applied the
**global minimum** across all of them. One slow resolver (small payload limit)
dragged every resolver down to its MTU, wasting throughput on the majority that
could carry much larger packets.

Recall the constraint from §0: a session has **one** MTU. So we cannot simply run
each resolver at its own MTU within one session. The design works *with* that
constraint.

### 7.1 Loss-aware measurement
`MTU_PROBE_SAMPLES > 1` switches probing from "accept if any retry passes" to
**loss-aware**: each candidate MTU is probed K times and accepted only if
measured loss ≤ `MTU_MAX_LOSS`. This yields a real per-resolver loss curve and a
robust MTU edge instead of a brittle single-success edge.

To bound probe cost on large resolver fleets, the sampler is **coarse-then-refine**:
it early-exits a candidate the moment the verdict is locked — once enough
successes make the loss budget unbeatable, or once failures exceed it — instead
of always sending all K probes.

**Loss reporting fix.** Early-exit probing must not make the UI lie. A candidate
can be rejected after the first failed probe when the configured loss budget is
zero; reporting `failures / sampled` made that look like **100% loss** even when
the configured budget was, for example, 1 failure out of 6 probes. The client now
reports `failures / MTU_PROBE_SAMPLES` for loss-aware candidates while keeping the
same early-stop verdict. That preserves fast startup and lets operators see
intermediate values (16.7%, 25%, 40%, ...) instead of only 0%/100%.

**Caveat.** `MTU_PROBE_SAMPLES = 1` intentionally keeps the legacy pass/fail
mode, so it can still only report 0% on pass or 100% on failure. Meaningful loss
percentages require `MTU_PROBE_SAMPLES > 1`.

### 7.2 Throughput-optimal operating point (joint upload+download)
Instead of the global minimum, the client picks the operating point that
maximizes aggregate throughput. For each resolver's own `(upload, download)` as a
candidate floor, it forms the pool that sustains **both** and scores it:

```
score(U, D) = (U + D) × (number of resolvers with upload ≥ U and download ≥ D)
```

The winning `(U, D)` balances per-packet size against resolver count in both
directions: a few slow resolvers cannot throttle the session, **and** a single
fast outlier cannot strand the crowd. (`selectMTUOperatingPoint` in
`internal/client/mtu_cluster.go`.)

### 7.3 Three explicit resolver states: active / reserve / invalid
MTU testing now classifies every resolver into one of three states:

| State | Condition | Role |
|---|---|---|
| **active** | `IsValid && !Backup` | in the data pool; carries traffic |
| **reserve** | `IsValid && Backup` | sustains *less* than the session MTU; held as failover |
| **invalid** | `!IsValid` | failed probing |

Crucially, resolvers that cannot sustain the operating MTU are **not discarded** —
they are kept as **reserves**. The balancer
(`internal/client/balancer.go`) selects primaries during normal operation and
**automatically falls back to reserves only when no primary remains** (one choke
point in `rebuildValidIndices`, so every selection strategy inherits it). A
`[RESOLVER STATES] active=X reserve=Y invalid=Z` summary is logged after testing.

### 7.4 Re-clustering on degradation, with hysteresis
At session (re)establishment, `recomputeMTUOperatingPoint` re-derives the
operating point over the **surviving** resolvers (primary + reserve). If the fast
pool has died, surviving reserves are **promoted at a viable lower MTU** instead
of stranding the session at an MTU nothing left can carry. To avoid thrashing the
session MTU when resolvers flap, a **hysteresis** rule
(`mtuShouldAdoptOperatingPoint`) only changes the MTU when there is no current
point, the current point is *stranded* (no survivor sustains it), or a new point
is *materially* better (> 12.5% larger download MTU). The server honors whatever
MTU the client negotiates in the new `SESSION_INIT`
(`applyMTUFromSessionInit`, validated server-side).

### 7.5 MTU-weighted balancing
A new balancing strategy (`RESOLVER_BALANCING_STRATEGY = 5`) selects active-pool
resolvers with probability proportional to their download MTU, so a resolver that
can carry 4000-byte answers receives ~4× the traffic of one capped at 1000.

**Why all of this helps.** On a realistic mixed fleet (say 40 resolvers at 4000 B
+ 10 at 1000 B), the old behavior ran everyone at 1000 B. The new behavior runs
the session at 4000 B over the 40-resolver pool (≈4× the per-query payload), keeps
the 10 slow ones as reserves, weights traffic toward the fastest resolvers, and —
if those 40 die — automatically drops to 1000 B on the survivors instead of going
dark. It stays **one session per client** (no extra server session pressure),
which is why it scales to the larger session space in §6.

**Caveat.** Re-derivation happens at session (re)establishment (a deliberate
race-free design), so promotion of reserves occurs on the next restart after
primary loss (which a stalled session triggers via inactivity/timeout), not
instantaneously. Hysteresis keeps that bounded and stable.

---

## 8. Caching is a background accelerator, never a gate

**Problem.** Log-based fast-start reused cached per-resolver MTUs to skip the full
scan — but if the user changed their resolver list, new resolvers (absent from
the cache) were silently ignored.

**What changed.** Log-mode start is now **hybrid**: it trusts the cache for
resolvers that have an entry but **always probes any resolver in the current list
that has none** (`scanConnectionsWithoutPreknownMTU`). The cache is written in the
background while running (`appendResolverCacheEntry`) and only *accelerates* known
resolvers at startup; it can never drop a new/changed resolver list. Per-resolver
loss is also persisted (`UPLOSS=/DOWNLOSS=`, backward-compatible) so the UI is
consistent across restarts; resolver tiers are **re-derived** on load (more
correct than persisting a possibly-stale flag).

---

## 9. DPI-resistance & duplication (threat model: passive DPI)

- **Query-type rotation** (§4) breaks the all-TXT fingerprint.
- **Type-matched responses** (§4) keep answers protocol-coherent.
- **Domain-diverse duplication** (`DUPLICATION_PREFER_DISTINCT_DOMAINS`): when a
  packet is duplicated for loss resistance, copies are spread across multiple
  tunnel domains rather than hammering one.
- **Adaptive duplication** (`ADAPTIVE_DUPLICATION`): the client raises the upload
  duplication count toward a target delivery probability based on the measured
  aggregate loss (`ceil(ln(1-target)/ln(lossFrac))`, capped), then hands off to
  FEC on the download side for the heavy-loss regime.

These target a **passive** DPI threat model (pattern/fingerprint observation), not
an active prober.

---

## 10. Transport diversity: DNS-over-TCP/53 fallback

**Problem.** The only data path used to be plain **UDP/53**. Highly restrictive
networks frequently filter, truncate, or hijack UDP/53 while still allowing
TCP/53. (DoH/DoT on 443 are often blocked outright in these environments, so they
are deliberately *not* the chosen fallback.)

**Server.** A DNS-over-TCP listener runs on the **same host:port** as UDP
(`server_tcp.go`), reading RFC 1035 §4.2.2 length-prefixed messages and replying
length-prefixed, routed through the **exact same** transport-agnostic
`safeHandlePacket`. Default on (`TCP_LISTENER_ENABLED`), connection-capped,
load-shedding, graceful shutdown — so all tunnel logic (sessions, FEC, channels,
encryption) is shared with UDP, no duplication.

**Client.** Client-wide transport via `RESOLVER_TRANSPORT = auto | udp | tcp`:
- **`auto` (default)** probes over UDP first; if **zero** resolvers pass MTU
  testing, it flips to TCP and **re-probes the whole fleet over TCP/53**. On a
  UDP-working network TCP is never attempted (zero cost).
- A `queryExchanger` abstraction makes the probe, session-init, and health paths
  transport-agnostic.
- A persistent **per-resolver TCP connection manager** (`tcp_data.go`) serves the
  high-throughput data plane (a handshake-per-query would be far too slow). Each
  connection's read loop feeds the **existing `rxChannel`**, so the inbound path
  (`handleInboundPacket`) treats TCP and UDP responses identically. Lazy dial,
  re-dial on failure, clean shutdown.

**Why it helps.** It changes what is *possible* on UDP-blocked networks, not just
what is faster. Because TCP wraps the whole DNS message, **every response channel
(TXT/CNAME/A/NULL/HTTPS) works unchanged over TCP** — validated end-to-end.

**Survival-path hardening.** The TCP listener now has explicit guardrails for
long-lived fallback use: `TCP_MAX_CONNS_PER_IP`, `TCP_MAX_QUERIES_PER_CONN`,
`TCP_READ_IDLE_TIMEOUT_SECONDS`, and `TCP_WRITE_TIMEOUT_SECONDS`. The defaults
keep persistent DNS-over-TCP useful (`TCP_MAX_QUERIES_PER_CONN = 0` means
unlimited) while bounding connection floods and idle clients. This is important
because TCP/53 is not a secondary convenience path in censored networks; it may
be the only viable transport.

---

## 11. Intelligent rate limiting (redistribution, not a global throttle)

**Problem.** Over-sending to a resolver past its rate limit is self-defeating: it
returns REFUSED/SERVFAIL (wasted round-trips + ARQ retransmits), silently drops
queries (full RTO stalls), or — worst — flags/blocks the client IP. You never had
that throughput; pushing harder only manufactures errors and risk.

**Mechanism.** A per-resolver AIMD pacer (`resolver_pacer.go`). The client already
sees the overload signal (`RCODE != 0` → `trackResolverFailure`, plus timeouts) at
one choke point, `recordResolverHealthEvent`. On a throttle signal the resolver
enters an exponentially-growing **cooldown window** and is deprioritized in
selection (`orderByPacing` at the data-plane spread and control-packet
selection); sustained success additively shrinks the window back to zero.

**Why it doesn't hurt throughput.** It is **redistribution**: the client's total
capacity is the sum of each resolver's sustainable rate; the pacer keeps each
under its own ceiling and shifts overflow to resolvers with headroom, so the
*aggregate* goes up and stays stable. It is **self-gating** (healthy resolvers,
interval 0, are never paced — does nothing on a clean network) and **never idles**
(a paced resolver is still used as a fallback when nothing else is free). It also
lowers the burst fingerprint. Default on (`RESOLVER_RATE_LIMIT_ENABLED`).

---

## 12. QNAME reshaping (anti-fingerprint, desync-proof)

**Problem.** The encoded payload used to ride as a single chain of uniformly
**maximum-length (63-char)** labels under one domain — a classic DNS-tunnel
fingerprint.

**Mechanism.** `QNAME_LABEL_LENGTH` (1..63) controls the target label length;
labels are split shorter and **jittered** per query (`qname_shape.go`), so the
query name looks more like ordinary multi-label subdomains.

**Why it can never desync client and server.** The receiver recovers the payload
by **concatenating all labels and stripping the dots** (server `stripLabelDots`,
client CNAME/A decoders) — label *boundaries are irrelevant to decoding*. The
sender may split however it likes. The single invariant that must hold — the
client's capacity math agreeing with how many labels the builder emits — is
centralized in `qnameLabelCount`, shared by the wire builder, `encodedQNameLen`,
and `CalculateMaxEncodedQNameChars`, so a name can never exceed 253 bytes. The
default (63) is **byte-identical** to the legacy greedy split; reshaping is opt-in.

**Trade-off.** Shorter labels mean more dots, i.e. less payload per query — a
throughput/stealth knob the operator tunes (hence default 63).

---

## 13. How the pieces fit together (hostile-network stack)

```
Transport:                   UDP/53, auto-fallback to DNS-over-TCP/53 when UDP is blocked
Upload  (client → server):   adaptive duplication ── across diverse domains
Both:                        ARQ (ACK/NACK, RTO, retransmit)  ← correctness backstop
Download(server → client):   auto-FEC (Reed-Solomon)  ← reconstruct without round-trip
Path selection:              adaptive per-group MTU + reserves + MTU-weighted balancing
Rate control:                per-resolver AIMD pacing (redistribute off throttling resolvers)
Anti-fingerprint:            query-type rotation + type-matched responses + QNAME reshaping
```

Each layer degrades independently: FEC reduces retransmits, ARQ guarantees
eventual delivery, duplication protects uploads, pacing avoids throttle/IP-blocks,
the MTU/reserve logic keeps the path usable as resolvers come and go, and the TCP
fallback keeps the tunnel alive when UDP/53 is filtered.

---

## 14. Paired operational presets

**Problem.** Operators had to hand-tune many interacting knobs for different
network conditions. That is error-prone: a "fast" client profile can accidentally
request compression or packet behavior the server does not allow, and a
TCP-heavy client can be paired with a server config that is not tuned for
long-lived DNS-over-TCP connections.

**Mechanism.** Both config loaders now understand `CONFIG_PRESET`, with the same
valid names on both sides:

| Preset | Client intent | Server intent |
|---|---|---|
| `default` | Existing explicit config values | Existing explicit config values |
| `speed` | Lower base duplication, MTU-weighted selection, LZ4, loss-aware MTU probing | Higher request/batch headroom, all compression types allowed, auto-FEC threshold tuned for moderate loss |
| `survival` | More duplication, smaller QNAME/EDNS shape, lower MTU ceilings, stricter loss-aware probing | Earlier auto-FEC, duplicated control blocks, longer TCP idle tolerance |
| `tcp-survival` | Force `RESOLVER_TRANSPORT = "tcp"` and keep duplication modest for persistent TCP/53 | Keep TCP listener enabled with higher connection caps and longer idle timeout |

Preset application is **non-destructive**: explicit values in the TOML file (or
CLI overrides) win over preset defaults. That means a bundled preset can be used
as a base profile while still letting an operator override one knob safely.

**Bundled pairs.**

```
client_config.speed.toml        + server_config.speed.toml
client_config.survival.toml     + server_config.survival.toml
client_config.tcp-survival.toml + server_config.tcp-survival.toml
```

The release packagers include these files plus `CONFIG_PRESETS.md`, so the
profiles are available in built artifacts, not only in the source tree.

**Validation.** Config tests cover preset parsing, explicit-value precedence,
CLI/override preset application, and all bundled preset TOML files. The shipped
template test accepts the expected placeholder client-key error but fails if any
preset is malformed before that point.

---

## 15. Validation summary

- **Unit tests** across `fec`, `vpnproto`, `dnsparser`, `udpserver`, `client`,
  `config` — including FEC reconstruction at 75% loss, auto-FEC enable/scale on
  loss, joint operating-point selection, reserve promotion, hysteresis, hybrid
  cache selection, MTU-weighted bias, loss-aware MTU reporting, the transport
  channels, the AIMD pacer (throttle/recover, redistribution ordering), TCP
  framing, config presets, and QNAME-shaping round-trip/bounds.
- **Server-side validation** that the server honors and clamps the client's
  per-session MTU (including a lowered value re-derived after primary-pool loss),
  the DNS-over-TCP framing/pipelining, and TCP connection guardrails.
- **End-to-end tests** (real client + server binaries over loopback), each a
  byte-exact 64 KB echo: baseline; encryption auto-detect; FEC-on download;
  query-type rotation over the new channels (UDP); full tunnel over **TCP/53**;
  **TCP/53 + NULL/HTTPS/CNAME** together; and **reshaped QNAME + TCP/53 + the
  non-TXT channels** stacked.
- **Cross-compilation** verified for linux/amd64, linux/arm64, darwin/arm64,
  windows/amd64, android/arm64.

---

## 16. Config quick reference (new/changed keys)

**Server (`server_config.toml`):**
- `TCP_LISTENER_ENABLED` (true) / `TCP_MAX_CONNS` (2048) — DNS-over-TCP/53 listener.
- `TCP_MAX_CONNS_PER_IP` (128) / `TCP_MAX_QUERIES_PER_CONN` (0) /
  `TCP_READ_IDLE_TIMEOUT_SECONDS` (30.0) / `TCP_WRITE_TIMEOUT_SECONDS` (15.0) —
  TCP/53 survival-path guardrails.
- `CONFIG_PRESET` (`default`, `speed`, `survival`, `tcp-survival`) — paired
  operational profile; explicit TOML/CLI values still win.
- `ENCRYPTION_AUTO_DETECT` (true) — trial-decrypt the client's cipher.
- `A_RECORD_DATA_DELIVERY` (false) — answer A queries with A-record data.
- `FEC_DOWNLOAD_ENABLED` (false) / `FEC_BLOCK_SIZE` (4) / `FEC_PARITY` (4) —
  always-on FEC.
- `FEC_AUTO_ENABLED` (true) / `FEC_AUTO_LOSS_THRESHOLD` (0.3) /
  `FEC_AUTO_MAX_PARITY` (0=auto) — loss-triggered FEC.
- `DOT_LISTENER_ENABLED` (false) / `DOT_LISTEN_PORT` (853) — DNS-over-TLS listener.
- `DOH_LISTENER_ENABLED` (false) / `DOH_LISTEN_PORT` (443) / `DOH_PATH`
  (`/dns-query`) — DNS-over-HTTPS listener. Both are only needed to point clients
  **directly** at this server; public-resolver DoT/DoH needs no server change.
- `DOH_COEXIST_MODE` (`auto`) — `auto`/`behind` never bind the TLS port (the panel
  keeps :443 and forwards DoH to `DOH_BEHIND_PORT`); `front` takes :443 and
  SNI-splices everything else to `DOH_SHARE_BACKEND`.
- `DOH_BEHIND_PORT` (8453) / `DOH_SHARE_BACKEND` ("") / `DOH_SHARE_PROXY_PROTOCOL`
  (false) — :443 coexistence with a co-hosted panel.
- `TLS_CERT_FILE` / `TLS_KEY_FILE` / `ACME_ENABLED` (true) / `ACME_CACHE_DIR` /
  `ACME_EMAIL` — TLS material, resolved cert/key → ACME → self-signed.
- `ENCRYPTED_MAX_CONNS` (0 = ¾ of `TCP_MAX_CONNS`) — ceiling on DoT/DoH
  connections so they cannot starve the plain TCP/53 survival path.
- `DOH_MAX_INFLIGHT` / `DOH_MAX_INFLIGHT_BYTES` /
  `DOH_REQUESTS_PER_SECOND_PER_IP` / `DOH_REQUEST_BURST_PER_IP` /
  `DOH_TRUSTED_PROXY_CIDRS` — DoH-specific flood budgets.

**Client (`client_config.toml`):**
- `CONFIG_PRESET` (`default`, `speed`, `survival`, `tcp-survival`) — paired
  operational profile; explicit TOML/CLI values still win.
- `RESOLVER_TRANSPORT` (auto) — `auto` (UDP, fall back to TCP/53 if UDP finds no
  resolvers) | `udp` | `tcp` | `dot` | `doh`. `auto` never escalates into the
  encrypted transports; picking `dot`/`doh` still falls back to UDP then TCP/53.
- `RESOLVER_TLS_SERVER_NAME` ("") — SNI/certificate name for `dot`/`doh`. Leave
  empty for public resolvers: the engine then verifies against the resolver's own
  identity, and Cloudflare/Google/Quad9 carry their IP as a certificate SAN. Set it
  only when pointing at your own DoT/DoH server.
- `RESOLVER_TLS_PIN` ("") — base64 SHA-256 of the server certificate's
  SubjectPublicKeyInfo. Replaces CA validation, so a self-signed server is trusted
  exactly and nothing else is; pinning the SPKI survives certificate renewal.
- `RESOLVER_TLS_INSECURE_SKIP_VERIFY` (false) — last resort; the payload stays
  AEAD-encrypted regardless, but an unverified hop can be transparently intercepted.
- `RESOLVER_DOT_PORT` (853) / `RESOLVER_DOH_PORT` (443) / `RESOLVER_DOH_PATH`
  (`/dns-query`) — where the resolver entry's IP is contacted. Client-wide, not
  per-resolver.
- `RESOLVER_RATE_LIMIT_ENABLED` (true) — per-resolver adaptive pacing.
- `QNAME_LABEL_LENGTH` (63) — QNAME label reshaping (smaller = shorter, jittered
  labels; lower fingerprint, less capacity).
- `DNS_RANDOMIZE_QUERY_ID` (true) — random DNS transaction ID per query instead of
  a sequential counter. Client-only; the server echoes the ID without validating
  it, so no server change is needed.
- `DNS_EDNS_COOKIE` (true) — add an RFC 7873 EDNS Client Cookie to each query's OPT
  record so it looks like a modern stub on the client→resolver leg. The recursive
  resolver terminates EDNS, so the cookie never reaches the server (client-only).
- `DNS_QNAME_CASE_RANDOMIZATION` (false) — DNS 0x20 mixed-case QNAME. The server
  lowercases the name before decoding (`writeLowerASCIILabel` at parse time), so it
  is server-transparent and cannot desync. Modest anti-detection value (does not
  reduce label entropy); opt-in.
- `EDNS_UDP_SIZE` (4096, clamped to [512, 4096]) — advertised requestor UDP payload
  size in the OPT record. Smaller looks more like a modern stub but can cap answer
  size and hurt throughput. Client-only.
- `RESOLVER_IGNORE_INJECTED_NXDOMAIN` (true) — on-path DNS-poisoning hardening. A
  forged NXDOMAIN (no tunnel payload) is treated as injection noise: it does not
  consume the pending query sample, so the genuine authoritative answer is still
  scored as a success, and the resolver is never throttled or disabled for it.
  Genuine unreachability is still caught by the pending sample timing out (a signal
  injection cannot forge). Client-only; zero extra queries/bytes. It actively
  *recovers* throughput under poisoning by keeping working resolvers in the pool.
- `QUERY_TYPES` — DNS record types to rotate (TXT/CNAME/A/NULL/HTTPS/SVCB/…).
- `MTU_PROBE_SAMPLES` (1) / `MTU_MAX_LOSS` (0.0) — loss-aware probing.
- `MTU_ADAPTIVE_GROUPING` (true) / `MTU_GROUP_GAP_RATIO` (0.25) — adaptive MTU.
- `RESOLVER_BALANCING_STRATEGY` — 1 random, 2 round-robin, 3 least-loss,
  4 lowest-latency, **5 MTU-weighted**.
- `DUPLICATION_PREFER_DISTINCT_DOMAINS`, `ADAPTIVE_DUPLICATION`,
  `ADAPTIVE_DUPLICATION_TARGET_DELIVERY`.

---

## 17. Encrypted resolver transports (DoT / DoH)

**Problem.** Sections 10 and 12 harden *what* the queries look like, but the
client→resolver leg is still plaintext DNS on port 53. A censor does not have to
break the tunnel's encryption to act on it: the volume, timing and shape of
plaintext DNS on 53 is enough to fingerprint, throttle or poison. On networks that
treat *any* heavy 53 traffic as suspicious, the tunnel is visible even when its
payload is not readable.

**What was added.** Two optional resolver transports:

- **DoT** — DNS-over-TLS (RFC 7858), normally port 853.
- **DoH** — DNS-over-HTTPS (RFC 8484), normally port 443.

Both encrypt the client→resolver hop, so on the wire the tunnel looks like a
device using an encrypted DNS provider.

**These are a disguise, not a security layer.** The tunnel payload is already
AEAD-encrypted end to end (section 5). TLS here buys traffic *shape*, not payload
secrecy — worth being precise about, because it decides how much the certificate
trust model actually matters.

### 17.1 Opt-in only, but not a commitment

`RESOLVER_TRANSPORT` gains `dot` and `doh`. Two deliberate rules:

- **Nothing ever escalates into them.** `auto` still means UDP → TCP/53 only.
  These transports disguise a *working* hop rather than rescue a broken one, so
  entering them is always an explicit operator decision.
- **Choosing one is still not a commitment.** Blocking 853/443 is a common
  censorship response, so if the chosen transport cannot carry the tunnel the
  client walks down on its own. A blocked TLS port degrades to the survival path
  instead of failing to connect.

```
dot  ─► UDP ─► TCP/53          udp  ─► (no fallback)
doh  ─► UDP ─► TCP/53          tcp  ─► (no fallback)
auto ─► UDP ─► TCP/53
```

The chain is `resolverTransportChain()`; the walk is in `RunInitialMTUTests`,
which re-probes the whole fleet on each step down.

### 17.2 How they are wired into the data path

The active transport is now an enum behind one interface
(`streamDataTransport`) instead of a `useTCP` boolean, so the dispatcher does not
grow a branch per transport:

- **DoT reuses the TCP data plane verbatim.** DoT *is* DNS-over-TCP framing
  inside TLS, so only the dial differs: `tcpDataManager` took a pluggable dialer,
  and connection pooling, the 2-byte length framing, the read loop and the
  `rxChannel` hand-off are shared with TCP/53 line for line.
- **DoH is genuinely different** and gets its own transport: one HTTP POST per
  query, HTTP/2 multiplexing over pooled connections, and answers pushed into the
  **same `rxChannel` the UDP reader feeds** — so `handleInboundPacket` treats every
  transport identically. In-flight POSTs are bounded (256) and a saturated burst is
  shed rather than queued: ARQ retransmits, and shedding stops a burst from opening
  unbounded sockets.

### 17.3 Public resolvers need no server change

The important operational point. Two distinct deployments:

```
A) public resolver  (no server change, no redeploy)
   client ──DoH/DoT──► 1.1.1.1 ──plain DNS + your NS delegation──► your server

B) direct           (needs DOT_/DOH_LISTENER_ENABLED)
   client ──DoH/DoT──► your server
```

In (A) the encryption covers exactly the hop that gets fingerprinted, and the
resolver reaches the tunnel server through normal delegation as it always did.
The server's listeners are **only** for (B), and default to off.

Resolvers are still configured the same way — a list of IPs. The endpoint is built
from the entry plus the transport's own port/path, so `1.1.1.1` becomes
`https://1.1.1.1:443/dns-query`. Cloudflare, Google and Quad9 publish certificates
carrying their **IP as a SAN**, so (A) validates with no configuration at all.

*Caveat:* the transport and its port/path are client-wide, not per-resolver. You
cannot run one resolver over DoH while another stays on UDP, and providers using a
different path cannot be mixed in one profile. The hedging is sequential
(fallback), not parallel.

### 17.4 Certificate trust

Three modes, best first:

1. **`RESOLVER_TLS_PIN`** — base64 SHA-256 of the server certificate's
   SubjectPublicKeyInfo. Pinning *replaces* chain validation, so a self-signed or
   private-CA server is trusted exactly and nothing else is. Pinning the SPKI
   rather than the certificate lets the server renew without breaking clients.
2. **Default** — normal hostname/CA verification. With `RESOLVER_TLS_SERVER_NAME`
   unset the engine verifies against the resolver's own identity, which is what
   makes public resolvers work unconfigured.
3. **`RESOLVER_TLS_INSECURE_SKIP_VERIFY`** — last resort, off by default.

Because the payload is already AEAD-encrypted, even (3) never exposes tunnel data.
Pinning is still preferred: an unverified hop can be transparently intercepted,
and the interception itself is a censorship signal.

### 17.5 Server listeners, and sharing :443 with a panel

Both listeners share the *exact* transport-agnostic packet handler used by
UDP/TCP, so no tunnel logic is duplicated. DoT additionally reuses the TCP accept
loop (`serveDNSOverStream`), inheriting its framing, per-IP caps and load shedding
unchanged — it is "TCP/53 in a TLS coat" as far as the server is concerned.

TLS material resolves **cert/key → ACME → self-signed**, so an enabled listener
always comes up. ACME is wrapped so an issuance/renewal failure at handshake time
falls back to the generated certificate instead of dropping encrypted DNS.

Only one process can bind :443, which matters when a panel (3x-ui, Hiddify, …) is
on the box. `DOH_COEXIST_MODE` picks the model:

- **`auto` (default) → model A.** CottenDNS **never binds the TLS port at all**.
  It serves cleartext HTTP/1.1+h2c on `DOH_BEHIND_PORT`, and the panel's own front
  (Xray fallback / nginx / Caddy) forwards the DoH route in. Because the panel
  still owns the handshake, every inbound it supports keeps working untouched —
  VMess/VLESS/Trojan, xhttp/gRPC/raw/ws/tls, CDN-fronted. WireGuard is UDP and is
  unaffected either way.
- **`front` → model B.** CottenDNS owns :443, terminates TLS, and SNI-splices
  every connection that is not for `DOMAIN` to `DOH_SHARE_BACKEND` untouched.

`auto` resolves to A deliberately: model B would opportunistically claim :443, and
then a panel installed *later* would fail to bind it. Owning the port is therefore
always an explicit decision, never a default. The SNI router carries the same
bias — a ClientHello it cannot parse is forwarded to the backend rather than
swallowed, so a detection bug never takes 443 from the co-hosted service.

### 17.6 Not letting the disguise starve the survival path

DoT/DoH are optional extras; DNS-over-TCP/53 is the fallback the tunnel depends
on. Sharing one connection budget between them would let a flood of the optional
listeners consume the headroom the survival path needs.

`connectionBudget` therefore supports a **parent** link: DoT/DoH reserve from a
child budget capped at `ENCRYPTED_MAX_CONNS` (default ¾ of `TCP_MAX_CONNS`) whose
reservations also consume parent capacity. However hard the encrypted listeners
are flooded, the remaining quarter stays available for plain TCP/53. UDP/53 is a
separate path and is unaffected by any of these budgets.

DoH additionally carries its own request-rate, in-flight and byte ceilings, plus
trusted-proxy handling: behind a reverse proxy the rate limiter keys on the
*forwarded* client address, and the per-IP **connection** cap is disabled, since
otherwise every user would share the proxy's single ceiling.

### 17.7 Why it helps

On a network that fingerprints plaintext 53, the tunnel stops looking like a DNS
anomaly and starts looking like a phone using Cloudflare — while the fallback
chain guarantees that turning the disguise on can never leave a user worse off
than before.

---

## 18. Native sessions, dynamic compatibility, and server scalability

This pass raised the capacity available to native CottenDNS clients without
turning the server into a native-only endpoint. The server still determines the
packet/session layout from the packet it receives and continues to answer legacy
clients on their historical format.

### 18.1 Native capacity without a legacy-client flag day

- Native clients use the two-byte session-ID layout and can use the complete
  16-bit session namespace.
- Legacy one-byte session frames remain parseable. Candidate validation uses
  packet semantics and existing session state instead of assuming that every
  client was upgraded at once.
- Session init, reuse, close, stream setup, data, ACK/NACK, DNS, and encrypted
  packet tests cover both layouts. UDP, TCP/53, DoT, and DoH all enter the same
  transport-independent packet handler after framing is removed.
- `MAX_ACTIVE_SESSIONS` (default 2048) is deliberately separate from the 65535
  ID slots. The wider namespace prevents collisions; the live-session cap
  prevents an init flood from turning the namespace into an unbounded memory
  commitment.

The result is dynamic compatibility: a server can give a native client its
larger session space while an old client continues to work without changing a
server mode or receiving a new mandatory field.

### 18.2 Optional server policy, never a hidden client throttle

The server can append resource ceilings to `SESSION_ACCEPT` for cooperating
clients: duplication, setup duplication, upload/download MTU, RX/TX workers,
minimum ping interval, packets per batch, ARQ window/NACK gap, compression
threshold, and initial RTO.

Every policy value defaults to zero (no stated limit). If all values are zero,
the policy block is omitted and `SESSION_ACCEPT` remains byte-for-byte compatible
with earlier clients. A native client publishes a received policy atomically and
clamps values at their actual use sites, avoiding a race with already-running
send, ping, and stream goroutines. Removing a policy on a later session also
clears the old snapshot rather than leaving a client permanently throttled.

These controls are overload safeguards, not speed knobs. The maximum-speed
configuration leaves them at zero. In particular, lowering the packets-per-batch
ceiling can create *more* DNS queries, so it must not be used as a generic rate
limit.

### 18.3 Bounded ingress and fair overload behavior

The UDP front door was split into cheap admission and bounded processing:

1. A reader parses the DNS question, checks the delegated domain, resolves the
   dynamic codec/header layout, and decrypts enough to validate the tunnel frame.
2. Only admitted frames consume bounded worker-queue space. The prepared parse
   result is retained, so workers do not repeat DNS parsing, codec trials, or
   decryption.
3. Decompression, session mutation, and response generation remain on bounded
   workers.

`MAX_INGRESS_QUEUE_BYTES` bounds queued backing buffers in addition to the
request-count limit. The default 4096-byte packet-pool ceiling is far above a DNS
query while avoiding the old worst case where every queue slot retained a
65535-byte array.

Ingress has separate control and data lanes. Control traffic receives reserved
capacity so ACK/NACK, setup, ping, and close packets can make progress while data
is busy. However, duplicated control packets cannot permanently occupy the data
lane: control spills into that lane only while no data is waiting. This preserves
recovery under overload without letting duplication reduce a user's useful data
rate. Drop counters, queue gauges, and rate-limited overload logs make saturation
observable.

### 18.4 UDP receive scaling without single-flow slowdown

On platforms with `SO_REUSEPORT`, the server opens multiple kernel receive
queues, but intentionally not one socket per reader. The count is:

```
max(1, min(UDP_READERS / 2, runtime.NumCPU()))
```

At least two readers therefore share each socket. Since the kernel hashes a
resolver flow to one reuse-port socket, a busy flow can still be drained by more
than one decrypt loop instead of being pinned to one reader. Platforms without
reuse-port, single-reader configurations, or partial bind failures fall back to
one shared socket with all readers attached. TCP/53, DoT, and DoH retain their
own connection budgets and are not coupled to this UDP-only socket topology.

---

## 19. Native-client recovery, connectivity, and throughput pass

The client data path was audited across UDP, TCP/53, DoT, and DoH. The changes
below do not add fields to DNS packets and do not require a server upgrade.

### 19.1 Runtime path and MTU recovery

Initial discovery is no longer the only time the client can choose a working
path. A runtime recovery controller can repeat resolver/transport/MTU discovery
when:

- session initialization repeatedly fails;
- the ping watchdog sees no useful progress; or
- every mature active resolver has reached its timeout window.

Recovery has a 15-second cooldown so several symptoms collapse into one scan.
Explicit `udp` and `tcp` settings remain explicit: recovery re-probes the same
transport and its usable MTU rather than silently changing the user's choice.
`auto`, `dot`, and `doh` retain their configured fallback chains. Successful
recovery resets session state and reconnects on the newly measured path.

Session-init racing and tunneled exchanges are cancellable. Once one attempt
wins, losing requests stop consuming sockets, HTTP requests, and queue space.

### 19.2 Persistent, priority-aware stream transports

- **DoH:** one long-lived HTTP/2-capable client/transport is shared for the
  client's lifetime instead of creating and closing a client per DNS query.
  Sixteen bounded workers drain separate control and data queues (64 and 256
  entries), preserving control progress while bounding memory and concurrency.
- **TCP/53 and DoT:** eight bounded send workers use two persistent connection
  stripes per resolver. Control and data queues are separated, and immediate
  dial/write failures are reported to resolver health instead of waiting for a
  later generic timeout.
- Stream transports no longer allocate unused UDP sockets. All transports feed
  the same inbound channel and packet handler, preserving identical ARQ/session
  semantics.

Queue admission is intentionally bounded. A shed request is recoverable through
ARQ; an unbounded backlog would instead make every user slow long after the
original burst ended.

### 19.3 Path-specific carrier selection

DNS record-type selection now learns per resolver/domain path. A resolver that
handles TXT well but drops HTTPS records no longer biases every other resolver,
and a failure on one domain/path does not globally suppress a useful carrier.
Aggregate telemetry is retained for operator visibility, while the actual choice
uses the path-local history.

### 19.4 Duplication cooperates with FEC

The speed preset starts upload and download data duplication at one copy. The
adaptive controller can still add redundancy when measured loss justifies it,
and explicit user values remain accepted. When recent downstream FEC is present,
ACK/NACK duplication is capped at two: FEC has already paid redundancy up front,
so multiplying recovery control packets further would consume bandwidth without
improving delivery. Setup/control reliability remains protected independently.

This is the key rule behind the new defaults: duplication is a loss response,
not a permanent speed multiplier. On a healthy or bandwidth-limited link, extra
copies reduce useful throughput.

### 19.5 Smaller allocations and compression decisions

- Receive buffers are pooled and sized from the discovered download MTU plus
  safety space, with an 8192-byte floor and 65535-byte hard ceiling. Direct or
  legacy paths without a discovered MTU retain the safe maximum.
- Compression estimates entropy before invoking ZSTD/LZ4/ZLIB. Payloads above
  the 7.6 bits/byte threshold are already effectively compressed or encrypted,
  so skipping the codec saves CPU and avoids expansion.
- Watchdog defaults were reduced from five minutes to 30 seconds; the speed and
  survival presets use 20 and 15 seconds. A dead resolver/path is therefore
  rediscovered on a useful timescale for unstable mobile networks.

### 19.6 Operational telemetry

Periodic traffic statistics now include the active transport, control/data
queue depths, RX/TX admission drops, recovery count, and stream dial/write
failures. These distinguish four very different problems that previously looked
like generic loss: resolver failure, local queue saturation, connection failure,
and actual tunnel packet loss.

---

## 20. Android native-engine integration (`v1.5.1c-rc13`)

The Android `cottendns-engine-ui` branch vendors the same native client recovery
and transport changes while preserving its Android-specific fast-connect flow,
resolver injection/progress logging, and executable/TOML boundary.

Android-side behavior was aligned with the engine:

- New/fresh profiles and launch-request fallback values use upload duplication
  1, download duplication 1, and a 30-second watchdog.
- Auto-tune profiles start data duplication at 1 and let the native adaptive
  controller raise it from measured loss instead of imposing a permanent
  3-30-copy bandwidth penalty.
- Native CottenDNS profiles emit adaptive duplication and distinct-domain
  preference. Compatibility mode explicitly leaves adaptive duplication off,
  preserving legacy-client behavior and user-selected settings.
- The app parses and displays the new transport, queue, drop, recovery, and
  stream-connection telemetry.

The Android vendored Go engine passed `go test ./...`, `go vet ./...`, and the
native client build. The release workflow then rebuilt all Android ABIs with the
NDK, ran Android unit tests, produced signed split/universal APKs, verified their
signatures, and published checksums in both Android repositories.

---

## 21. Reuse-port CI correction and final validation

The reuse-port socket-count test originally asserted the obsolete design of one
socket per UDP reader. Production had already moved to the deliberate
`UDP_READERS/2`, CPU-capped topology described in section 18.4. GitHub's two-core
runner therefore opened the correct two sockets for four readers while the stale
test expected four.

The test now derives its expectation from `udpSocketCount`; production network
code was not weakened or changed. Validation after the correction included:

- the complete Go test suite in both CottenDNS mirrors;
- race-sensitive ARQ, client, and UDP-server tests;
- `go vet`, static analysis, and client/server builds;
- the live end-to-end tunnel test;
- installer, Compose, container-health, and legacy-upgrade contracts;
- the complete cross-platform release matrix and multi-architecture server
  container publication; and
- signed Android `v1.5.1c-rc13` releases in both Android repositories.

---

## 22. Sessionful generic UDP and Android full-VPN routing

The native SOCKS5 listener now supports generic UDP destinations in addition to
the optimized DNS/53 path. Each UDP association is represented by a normal ARQ
stream, so it inherits DNS-MTU fragmentation, retransmission, path balancing,
fair stream scheduling, and duplicate suppression rather than adding a second
reliability protocol.

Datagram boundaries are carried by a two-byte length followed by the standard
SOCKS address, port, and payload. The one-time association marker is a reserved
address type: an older server rejects it cleanly, while upgraded peers attach a
sessionful UDP adapter. The server retains a connected UDP socket per target so
QUIC, voice, and games keep a stable remote five-tuple. Target fan-out is capped,
idle endpoints are reclaimed, and literal and DNS-resolved addresses are both
checked against the public-address policy to prevent access to loopback,
private, link-local, multicast, and benchmark networks. IPv4 and IPv6 are
supported. DNS/53 remains on its lower-overhead cache-aware request path and no
longer tears down the association merely because the first lookup is pending.
When the server is configured with an external SOCKS5 upstream, it negotiates a
real upstream UDP ASSOCIATE and keeps its TCP control connection alive; generic
UDP never bypasses the operator's selected egress mode.

The default local SOCKS UDP association idle time is now 120 seconds. Explicit
client settings remain authoritative and are still bounded to the existing safe
range.

The Android full-VPN integration was updated alongside the protocol:

- tun2proxy was upgraded from v0.7.21 to the verified v0.8.1 release interface;
- the runner requests 1024 sessions, 300-second TCP lifetime, 120-second UDP
  lifetime, IPv6, virtual DNS, and fatal-error exit reporting;
- readiness waits for a stable native runner instead of reporting success as
  soon as its Java thread is started;
- network capability churn no longer kills CottenDNS; a real default-network
  identity change gets a recovery grace period and outbound probes before a
  restart is permitted;
- normal stop/revoke work is moved off the Android main thread, startup jobs are
  joined before teardown, and `onRevoke` performs explicit cleanup;
- the JNI-owned TUN descriptor retains close-on-exec, avoiding inheritance by
  child processes;
- IPv6 is routed end-to-end instead of intentionally sinkholed;
- split-tunnel include mode with no apps and any unfulfillable include/exclude
  selection fail closed instead of silently broadening VPN scope; and
- connection verification reports a failed outbound probe as failure rather
  than calling an unverified route ready.

---

*All changes keep ARQ as the correctness backstop; every optimization above is
designed to fail safe — if FEC, MTU grouping, a carrier, or a transport channel
does not help on a given path, the tunnel still delivers through the surviving
resolver/path combination.*
