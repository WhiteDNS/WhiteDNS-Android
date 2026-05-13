<p align="center">
  <img src="app/src/main/play_store_512.png" width="128" alt="WhiteDNS logo">
</p>

# WhiteDNS

WhiteDNS is an Android application for running a local DNS tunneling client with proxy and VPN modes.

[Documentation](./docs/README.md) | [User guide](./docs/WHITE_DNS_USER_GUIDE.md) | [Troubleshooting](./docs/WHITE_DNS_TROUBLESHOOTING.md) | [QA plan](./docs/WHITE_DNS_FIELD_QA.md) | [راهنمای فارسی](./README.fa.md)

> **NOTICE:** WhiteDNS is source-available proprietary software. The code is published for transparency, review, and contribution to this official project only. You may not copy the app into a separate product, publish modified builds, repackage APKs, redistribute binaries, clone the branding, or reuse the WhiteDNS name, logo, icon, design, or visual identity.

> **APP STORE WARNING:** WhiteDNS does not have any publication on Google Play. Any WhiteDNS APK, listing, or package found on Google Play or another app marketplace is not an official release from this project and may be modified, outdated, or unsafe. Use only this repository and the official Telegram channel for project updates.

Official channel: [https://t.me/whitedns](https://t.me/whitedns)

## Credits

WhiteDNS is backed by the [MasterDNS Client](https://github.com/masterking32/MasterDnsVPN) project and uses StormDNS, a fork from MasterDNS, from [nullroute1970/StormDNS](https://github.com/nullroute1970/StormDNS).

The Android VPN path also packages `tun2proxy`; see [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md) for third-party license details.

## Features

- Android client for WhiteDNS / StormDNS based DNS tunneling.
- Proxy mode with local SOCKS5 support and optional HTTP proxy bridge.
- VPN mode using Android `VpnService` and packaged `tun2proxy` native libraries.
- Built-in and custom server profile support.
- `stormdns://` profile import and export helpers.
- Resolver profile management with validation and default resolver assets.
- Split tunnel options for VPN routing.
- Runtime connection logs, resolver state, progress, and traffic statistics.
- Foreground service notifications for long-running proxy and VPN sessions.
- Jetpack Compose UI with Material 3 components.

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
|       |   |-- jniLibs/        # packaged native StormDNS and tun2proxy libraries
|       |   `-- res/            # app icons, strings, themes, XML resources
|       |-- test/
|       `-- androidTest/
|-- gradle/
|-- third_party/
|   `-- StormDNS/       # pinned StormDNS source used for native client builds
|-- build.gradle.kts
|-- settings.gradle.kts
`-- THIRD_PARTY_NOTICES.md
```

## Local Development Build

These instructions are for local review, testing, and contribution to the official WhiteDNS project only. They do not grant permission to publish, redistribute, re-sign, or upload APKs.

Requirements:

- Android Studio or Android SDK command line tools.
- JDK 17.
- Go matching the version in `third_party/StormDNS/go.mod`.
- Android SDK platform for `compileSdk = 36`.
- Android NDK `26.3.11579264`.
- Android NDK `29.0.14206865` for rebuilding the StormDNS native client.

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
