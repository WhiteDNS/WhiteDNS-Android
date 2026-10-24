# WhiteDNS Android 1.6.12-beta1

Fix CottenMax authentication succeeding while transfer verification remains at
90% when a large resolver list includes many unreachable candidates.

- Authenticated paths receive real-traffic capacity before bounded, rotating
  resolver checks. All 4,785 bundled candidates remain eligible; startup does
  not require waiting for the whole list. Healthy paths keep their configured
  congestion and in-flight windows.
- Large query-name sizing trials carry padding instead of application data.
  Resolver verification errors identify stream opening, challenge writing, or
  reply reading.
- CottenMax scans give slow successful authentication a fresh transfer-proof
  deadline. An optional large size measurement cannot reject a proven small
  transfer.
- Add optional download forward error correction (FEC) in Advanced CottenMax
  options. Reed-Solomon recovery shards can repair missing download packets
  while normal retransmission remains available. FEC is off by default and
  uses extra bandwidth when enabled. It requires an updated server with
  CottenMax FEC support; older servers require FEC to remain off. Data shards
  can be set to 2–16 (default 4), recovery shards to 1–4 (default 1).
- Allow up to 4,096 desired verified discovery resolvers and 4,096 total
  in-flight queries as optional advanced limits. Defaults remain 16 and 256;
  per-path query windows and discovery concurrency keep their existing limits.
- The APK builds all four native ABIs from the corrected, explicitly pinned
  CottenDNS source and checks the packaged core bytes against the fresh build.

Use CottenMax mode when scanning for Max. Successful ordinary DNS lookups or
Normal-mode scans do not prove Max compatibility. A working resolver must
forward the tunnel subtree and pass both authenticated handshake and byte-exact
transfer on the current network. No public resolver list is guaranteed to work
on every ISP. Keep known working results while testing other candidates.

The existing Max-capable e50a43e server remains compatible with FEC off. Keep
the current server key, domain, and Router route. Upgrade the server before
enabling the new download FEC option.

This is a signed prerelease; stable 1.6.12 remains available as before. The
earlier CI debug APK also called 1.6.12-beta1 contained the same core as stable;
this prerelease contains the resolver scheduling fix and optional FEC support.
Official signed APKs retain the normal application identity and signing key,
so official-app profiles are retained. The earlier debug app uses a separate
application identity and stores its profiles separately. Export/import profile
links to move settings from that debug app to the official app.

Validation reproduces the old failure with a simulated 4,785-candidate pool
and confirms authenticated transfer with the corrected scheduler. Regression
checks cover fairness across all candidates, small and full query windows,
size-trial isolation, and scan deadlines. This does not replace validation on
the affected Samsung phones and Shatel connection.
