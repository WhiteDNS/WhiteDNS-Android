<p align="center">
  <img src="app/src/main/play_store_512.png" width="128" alt="WhiteDNS logo">
</p>

# WhiteDNS

<p align="center">
  <a href="https://github.com/WhiteDNS/WhiteDNS-Android/releases/latest"><strong>⬇ Download latest release / دانلود آخرین نسخه</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/WhiteDNS/WhiteDNS-Android/releases/download/1.6.8/WhiteDNS-1.6.8-universal.apk">Android APK 1.6.8 — all architectures</a>
</p>

**[All releases / همهٔ نسخه‌ها](https://github.com/WhiteDNS/WhiteDNS-Android/releases)**

WhiteDNS is an Android application for running a local DNS tunneling client with proxy and VPN modes.

> **NOTICE:** WhiteDNS is source-available proprietary software. The code is published for transparency, review, and contribution to this official project only. You may not copy the app into a separate product, publish modified builds, repackage APKs, redistribute binaries, clone the branding, or reuse the WhiteDNS name, logo, icon, design, or visual identity.

> **APP STORE WARNING:** WhiteDNS does not have any publication on Google Play. Any WhiteDNS APK, listing, or package found on Google Play or another app marketplace is not an official release from this project and may be modified, outdated, or unsafe. Use only this repository and the official Telegram channel for project updates.

Official channel: [https://t.me/whitedns](https://t.me/whitedns)

## Credits

WhiteDNS is backed by the [MasterDNS Client](https://github.com/masterking32/MasterDnsVPN) project and supports [StormDNS](https://github.com/nullroute1970/StormDNS) and [CottenDns](https://github.com/WhiteDNS/CottenDns) client engines, plus [Lantern Soar](https://github.com/getlantern/soar).

The Android VPN path also packages `tun2proxy`; see [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for third-party license details.

## CottenMax beta on the current Android interface

The `beta/cottenmax-20261021` branch combines the latest main interface with the reviewed CottenDNS core [`e50a43ef`](https://github.com/WhiteDNS/CottenDNS/commit/e50a43ef75ad10ae2ffbfa106d13c50de3e77624), including [PR #15](https://github.com/WhiteDNS/CottenDNS/pull/15). [Debug builds and test results](https://github.com/WhiteDNS/WhiteDNS-Android/actions?query=branch%3Abeta%2Fcottenmax-20261021) are available as workflow artifacts; these are beta builds, with no stable app release published by this update.

Edit a CottenDNS connection profile, keep the same domain and key, and select **CottenMax (beta)**. The Master/Storm compatibility preset stays in Normal mode. Save, disconnect and reconnect.

- Normal and Max coexist on an updated Max-enabled CottenDNS server. If Max cannot connect, switch back to Normal using the same profile.
- UDP, TCP and automatic UDP/TCP selection apply to actual tunnel connections and Max resolver scanning. Encrypted DNS, padding, standby domains, discovery and query shaping remain optional.
- Scans show testing and verified DNS paths. Max connection readiness waits for the core's authenticated tunnel-transfer marker.
- Max options stay inside CottenDNS. The current main Soar engine, subscriptions and other engine controls remain available.

## Features

- Android client for WhiteDNS DNS tunneling with StormDNS, CottenDns and Soar engines.
- Proxy mode with local SOCKS5 support and optional HTTP proxy bridge.
- VPN mode using Android `VpnService` and packaged `tun2proxy` native libraries.
- Built-in and custom server profile support.
- `stormdns://` and `soar://` profile import/export; tap a connection card to select it.
- HTTP/HTTPS subscription import and refresh, with stable profiles, quota/expiry snapshots and automatic due refresh while disconnected.
- Resolver profile management with validation and default resolver assets.
- Scan tab accepts the default list, a resolver file, or DNS servers typed and pasted
  by hand, and reports each manual server's reachability and latency from the current
  network before scanning. See [docs/MANUAL_SCAN.md](docs/MANUAL_SCAN.md).
- Split tunnel options for VPN routing.
- Runtime connection logs, resolver state, progress, and traffic statistics.
- Foreground service notifications for long-running proxy and VPN sessions.
- Jetpack Compose UI with Material 3 components.

## Soar server and Iran profiles

See [docs/SOAR.md](docs/SOAR.md) for IR/GLOBAL discovery, adaptive settings,
network limits and the [guided server installer](https://github.com/WhiteDNS/Soar-Server).

## Subscriptions

In Profiles → Connection → Import, paste an HTTPS subscription URL or scan its
QR code and confirm. WhiteDNS accepts plain or base64 lists of its supported
profile URI formats. Use Refresh subscriptions while disconnected. Due sources
also refresh on app open/resume when no tunnel is running; Android background
refresh is not promised. A failed response keeps saved profiles. Refresh keeps
profile selection, connection mode and manually selected resolver overrides.
The summary shows usage, quota and expiry as last retrieved, not live counters.

Subscription URLs are credentials. Prefer HTTPS. HTTP requires explicit consent
for that source because its address and keys are unencrypted. Certificate checks
remain enabled and HTTPS cannot redirect to HTTP. Requests are bounded to 1 MiB,
128 profiles and three redirects. Source URLs are excluded from diagnostics and
direct profile exports. Provider updates replace the provider-owned settings;
deleting every profile of a subscription stops its automatic refresh.

The [WhiteDNS Soar panel](https://github.com/WhiteDNS/Soar-Server) provides one
subscription per account; [the full guide](https://github.com/WhiteDNS/Soar-Server/blob/main/docs/SUBSCRIPTIONS.md)
explains token/key rotation and backups. Profile-list format compatibility does
not add support for unrelated Xray/V2Ray protocols.

## Project Structure

```text
.
|-- app/
|   |-- build.gradle.kts
|   `-- src/
|       |-- main/
|       |   |-- AndroidManifest.xml
|       |   |-- assets/
|       |   |-- java/shop/whitedns/client/
|       |   |   |-- model/      # settings, profiles, validation, profile links
|       |   |   |-- proxy/      # foreground proxy service and HTTP bridge
|       |   |   |-- runtime/    # runtime state, traffic, progress parsing
|       |   |   |-- storm/      # StormDNS config and process management
|       |   |   |-- ui/         # Compose UI, theme, view model
|       |   |   `-- vpn/        # Android VPN service and tun2proxy management
|       |   |-- jniLibs/        # packaged native StormDNS, CottenDns, and tun2proxy libraries
|       |   `-- res/            # app icons, strings, themes, XML resources
|       |-- test/
|       `-- androidTest/
|-- gradle/
|-- third_party/
|   |-- StormDNS/       # pinned StormDNS source used for native client builds
|   `-- CottenDns/      # pinned CottenDns source used for native client builds
|-- build.gradle.kts
|-- settings.gradle.kts
`-- THIRD_PARTY_NOTICES.md
```

## Local Development Build

These instructions are for local review, testing, and contribution to the official WhiteDNS project only. They do not grant permission to publish, redistribute, re-sign, or upload APKs.

Requirements:

- Android Studio or Android SDK command line tools.
- JDK 17.
- Go 1.26.1 for the pinned native client modules.
- Android SDK platform for `compileSdk = 36`.
- Android NDK `26.3.11579264`.
- Android NDK `29.0.14206865` for rebuilding the StormDNS and CottenDns native clients.

Build and test a local debug copy:

```bash
git submodule update --init --recursive
./gradlew testDebugUnitTest
make debug
```

The debug APK uses package `shop.whitedns.client.debug` and the app label
`WhiteDNS Debug`, so QA can install it next to the production app without
uninstalling the original WhiteDNS build.

If Go is installed outside your shell `PATH`, pass it explicitly:

```bash
make debug GO=/path/to/go
```

Release builds are produced only by the official WhiteDNS maintainers. Do not publish APKs, AABs, modified builds, re-signed packages, keystores, signing passwords, or local SDK files.

## License

WhiteDNS is source-available proprietary software.

Community contributions are welcome through the official repository, but this project is not open-source.

You may view the code and submit contributions, but you may not fork it into another app, redistribute builds, repackage APKs, sell modified versions, clone the project, or reuse the WhiteDNS name, logo, icon, design, or branding.

See:

- [LICENSE](./LICENSE.MD)
- [CONTRIBUTING.md](./CONTRIBUTING.md)
- [CLA.md](./CLA.md)
- [TRADEMARK.MD](./TRADEMARK.MD)
