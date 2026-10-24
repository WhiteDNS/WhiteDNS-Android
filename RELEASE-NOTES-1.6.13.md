# WhiteDNS Android 1.6.13

Fix CottenMax connections that authenticate but remain at 90% transfer
verification when a large resolver list includes many unreachable candidates.
This stable release uses the same corrected CottenDNS core as the signed
1.6.12-beta1 prerelease: `7db7b73658c791cce62300d29647e4d369d6e1e4`.

- Prioritize authenticated paths for real traffic while checking other
  resolvers in bounded, rotating batches. All 4,785 bundled candidates remain
  eligible; startup does not wait for the entire list. Healthy paths retain
  their configured congestion and in-flight windows.
- Keep application data out of large query-name size trials. Verification
  errors identify stream opening, challenge writing, or reply reading.
- Give a slow successful scan handshake a fresh transfer-proof deadline.
  Optional large-size measurements cannot reject a proven small transfer.
- Add optional download forward error correction (FEC) in Advanced CottenMax
  options. Reed-Solomon recovery shards repair missing download packets while
  normal retransmission remains available. FEC is off by default and uses
  extra bandwidth when enabled. Enabling it requires an updated server with
  CottenMax FEC support. Data shards support 2-16 (default 4), recovery shards
  support 1-4 (default 1).
- Raise the optional desired verified discovery-resolver target and total
  in-flight query limit to 4,096. Defaults stay at 16 and 256. Per-path query
  windows and discovery concurrency keep their existing limits.

Select CottenMax mode when scanning for Max-compatible resolvers. Ordinary DNS
lookups and Normal-mode scans do not prove Max compatibility: each resolver
must forward the tunnel subtree and pass authenticated handshake and exact
byte transfer on the current network. Keep known working results while
testing additional candidates.

Existing Max-capable servers, including e50a43e, remain compatible with FEC
off. Keep the current server key, domain, and Router route. Upgrade the server
before enabling download FEC.

Official signed APKs retain the normal application identity and signing key,
preserving existing official-app profiles, including those from the signed
1.6.12-beta1 prerelease. Earlier CI debug APKs use a separate application
identity and store their settings separately; export/import profile links to
move those settings to the official app. The previous beta tag remains
available.

Validation of this core includes a simulated 4,785-candidate pool, resolver
fairness, small and full query windows, size-trial isolation, scan deadlines,
and FEC recovery regressions. The signed beta passed native engine checks,
Android JVM tests, four-ABI builds, signing, and packaged-core verification.
Physical validation on the reported Samsung phones and Shatel connection is
still needed.
