# WhiteDNS Android 1.6.16

Refresh the embedded CottenDNS engine with long-uptime DNS cache and connection corrections for Normal and CottenMax.

- Expire cached DNS replies at their authoritative TTL and preserve their true expiry across restarts. Cache hits no longer keep stale answers alive.
- Return transient DNS failures to callers without retaining them as ordinary cached answers.
- Keep DNSSEC/EDNS request policies separate in the DNS cache and pending work. Preserve each caller's transaction ID and question case when delivering replies.
- Keep pending DNS work bounded and match replies to the correct active request.
- Retain the existing CottenMax Full and Low profiles and the current server protocol.

Update corresponding CottenDNS servers to receive their independent UDP resource cleanup corrections. Existing profile domains and shared keys remain compatible.

The release workflow rebuilds all four Android native ABIs, runs core and Android unit tests, verifies the packaged core provenance, and signs the universal and per-ABI APKs with the existing release key.
