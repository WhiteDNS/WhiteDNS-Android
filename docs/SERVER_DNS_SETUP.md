# CottenDns Server — Domain & DNS Setup Guide

How to point *any* domain at your CottenDns tunnel server and see how the DNS
records actually carry traffic. Worked example uses Cloudflare, but any DNS host
that supports **subdomain NS records** works.

Placeholders used below — replace with your own:

| Placeholder | Meaning | Example |
|---|---|---|
| `example.com` | a domain you own | `example.com` |
| `v.example.com` | the **tunnel subdomain** clients use | `v.example.com` |
| `ns1.example.com` | hostname of your tunnel server | `ns1.example.com` |
| `203.0.113.10` | **public IP** of your VPS running the server | your VPS IP |

---

## 1. The one idea that makes it work: **NS delegation**

A DNS tunnel works because **your VPS becomes the authoritative nameserver for a
subdomain**. You are telling the entire DNS system:

> "For anything ending in `v.example.com`, don't answer it yourself — go ask the
> server at `203.0.113.10`."

That "go ask that server" instruction is an **NS record**. It is *not* the same
as the tunnel's data record types (TXT/CNAME/A/…). Two different jobs:

- **NS record** = infrastructure. Delegates the subdomain to your VPS. Set once.
- **TXT / CNAME / A / NULL / HTTPS** = the actual bytes. Every tunnel packet is a
  query *to* your delegated server, answered in one of these record types.

---

## 2. The DNS records to create (Cloudflare)

In Cloudflare DNS for `example.com`, create **exactly two** records:

| Type | Name | Value | Proxy status |
|---|---|---|---|
| `A` | `ns1` | `203.0.113.10` | **DNS only (grey cloud)** ← required |
| `NS` | `v` | `ns1.example.com` | n/a |

- The `A` record must be **grey-cloud / DNS only**. Cloudflare's orange-cloud
  proxy only handles HTTP/HTTPS — it will **not** pass UDP/53 DNS traffic. If it's
  proxied, resolvers hit Cloudflare instead of your VPS and the tunnel dies.
- The `NS` record delegates `v.example.com` (and everything under it) to
  `ns1.example.com`, which resolves to your VPS via the `A` record above.

That's it. No glue records needed — `ns1.example.com` lives in the same zone.

---

## 3. How a single tunnel query travels (the actual mechanics)

```
                              (1) query for
                          <data>.v.example.com
   ┌──────────┐  TXT?  ┌────────────────────┐  "who is authoritative
   │  Client  │ ─────► │  Recursive resolver │   for v.example.com?"
   │ (phone / │        │  (8.8.8.8, ISP, …)  │
   │  app)    │        └─────────┬───────────┘
   └────▲─────┘                  │ (2) NS says: ns1.example.com → 203.0.113.10
        │                        ▼
        │              ┌───────────────────────┐
        │  (4) answer  │  YOUR VPS  :53 (UDP)   │  (3) recursor asks your
        │  as a TXT /  │  CottenDns server      │      server directly
        └───────────── │  authoritative for     │
           CNAME / A / │  v.example.com         │
           NULL /HTTPS └───────────────────────┘
```

1. The client encodes upload data into the **query name** and asks a normal
   recursive resolver for, e.g., a **TXT** record of `<data>.v.example.com`.
2. The resolver doesn't have it cached, sees the **NS delegation**, and learns
   your VPS is authoritative.
3. It forwards the query to **your VPS on UDP/53** (or TCP/53 fallback).
4. Your CottenDns server decodes the upload, does the real work (SOCKS/DNS), and
   encodes the **download** data into the answer — a **TXT** record by default,
   or CNAME / A / NULL / HTTPS depending on the client's `QUERY_TYPES`.

The resolver relays that answer back to the client. Repeat thousands of times per
second = a tunnel. **No traffic ever leaves the DNS system**, which is why it
passes filtered networks.

---

## 4. Which record types carry data (and when to switch)

Your server encodes tunnel responses into these — all switchable from the client
config (`QUERY_TYPES` / delivery mode), no server change needed:

| Record | Server flag | When to use |
|---|---|---|
| **TXT** | on by default | **primary** — highest capacity, use unless blocked |
| **CNAME** | on by default | when TXT is filtered |
| **A** | `A_RECORD_DATA_DELIVERY = true` | extra anti-fingerprint channel (~766 B/resp) |
| **NULL** | on by default | small frames on hostile networks |
| **HTTPS/SVCB** | on by default | modern carrier, sometimes passes when TXT doesn't |

> If a network blocks TXT, change the client's delivery mode to CNAME or HTTPS —
> same server, same domain, different record type. That flexibility is your
> evasion lever.

Note: `AAAA`/IPv6 is intentionally **not** a carrier — the target networks block
IPv6 (this is also why the Android client sinkholes IPv6).

---

## 5. Server config essentials (`server_config.toml`)

Only the fields that must match your domain/setup — see
`third_party/CottenDns/server_config.toml.simple` for the full annotated file.

```toml
# Must match the client's DOMAINS exactly.
DOMAIN = ["v.example.com"]

# Listen on all interfaces, standard DNS port. TCP/53 fallback is on by default.
UDP_HOST = "0.0.0.0"
UDP_PORT = 53
TCP_LISTENER_ENABLED = true

# Encryption. Method 0..5 (0=None,1=XOR,2=ChaCha20,3=AES-128,4=AES-192,5=AES-256).
# Auto-detect lets the client pick any method on the same key.
DATA_ENCRYPTION_METHOD = 5
ENCRYPTION_AUTO_DETECT = true
ENCRYPTION_KEY_FILE = "encrypt_key.txt"   # put your shared secret in this file

PROTOCOL_TYPE = "SOCKS5"                    # client chooses destinations
```

Put your shared secret in `encrypt_key.txt` next to the binary, then run:

```bash
./cottendns-server -config server_config.toml
```

The server must run as root (or with `CAP_NET_BIND_SERVICE`) to bind port 53.

---

## 6. Matching client config

The Android app / client must use the **same domain and key**:

- `DOMAINS = ["v.example.com"]`  (matches server `DOMAIN`)
- `ENCRYPTION_KEY` = the exact contents of the server's `encrypt_key.txt`
- `DATA_ENCRYPTION_METHOD` = any of 0..5 (server auto-detects)
- `QUERY_TYPES` = `["TXT"]` to start; add/switch to `CNAME`/`HTTPS`/`A`/`NULL` if TXT is filtered

In the app this is the custom-server profile: domain `v.example.com`, the
encryption key, and encryption method.

---

## 7. Verify it before touching the app

From any machine, confirm the delegation and that your server answers:

```bash
# 1) Delegation exists — should list ns1.example.com
dig NS v.example.com +short

# 2) ns host resolves to your VPS
dig A ns1.example.com +short          # -> 203.0.113.10

# 3) Your server actually answers a TXT query on 53 (ask it directly)
dig TXT test.v.example.com @203.0.113.10 +short
dig TXT test.v.example.com @203.0.113.10 +tcp +short   # TCP/53 fallback

# 4) End-to-end through a public resolver (delegation working globally)
dig TXT test.v.example.com @1.1.1.1 +short
```

If (3) answers but (4) doesn't, the delegation/propagation isn't live yet.
If neither (3) answers, the server isn't reachable on UDP/53 (see gotchas).

---

## 8. Gotchas (in order of how often they bite)

1. **UDP/53 blocked on the VPS.** #1 failure. Open inbound `udp/53` (and `tcp/53`)
   in the provider firewall *and* the OS firewall. Some hosts block 53 entirely —
   pick a provider that doesn't.
2. **`A` record left orange-cloud (proxied).** Must be **DNS only**.
3. **Port 53 already in use** by `systemd-resolved` on the VPS. Disable/relocate it
   so CottenDns can bind `0.0.0.0:53`.
4. **Key/domain mismatch** between client and server — the tunnel silently fails
   to establish. `DOMAIN` and the encryption key must match exactly.
5. **Propagation delay** — a fresh NS delegation can take minutes to be visible to
   all resolvers. Test directly against your VPS IP first (step 3 above).
6. **Registrar-locked NS** — a few registrars restrict subdomain NS on cheap DNS
   plans. Cloudflare allows it on all plans.

---

## TL;DR

- **Any domain works.** You need **two records**: an `A` (DNS-only) for your
  server host, and an **`NS`** delegating the tunnel subdomain to it.
- Data rides on **TXT** (default), with **CNAME / A / NULL / HTTPS** as switchable
  fallbacks — NS is only for delegation, not data.
- Run the server on reachable **UDP/53** (+ TCP/53 fallback), match `DOMAIN` and
  the encryption key on both sides, verify with `dig`, then point the app at it.
