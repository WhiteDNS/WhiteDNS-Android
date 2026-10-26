# WhiteDNS Android 1.6.15

CottenMax now offers **Full** and **Low** traffic profiles. Full remains the
default. Low lets you choose a 10–90% reduction target, with 35% selected by
default, and also limits connection and resolver-discovery bursts. The reduction
is approximate: actual speed depends on the resolver, network and workload.

Select the profile and reduction in the connection's advanced CottenMax options.
Your choice is saved with the profile and preserved through profile links and
configuration import/export. Existing CottenMax servers can accept either mode.

This version retains the 1.6.14 long-domain handshake fix and the earlier Max
resolver-recovery and optional download-FEC fixes. It updates the CottenDNS core
to `77bf87db28af6b64a4b6c404074982235272ed9b`; existing domains, keys and
subscriptions remain valid.

The organization CI builds and signs APKs for all supported Android ABIs,
checks the packaged DNS engines and VPN native libraries, and runs the Android
and engine test suites. Device and restricted-network results can vary.
