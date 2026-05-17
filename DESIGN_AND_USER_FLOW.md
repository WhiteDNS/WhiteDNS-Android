# WhiteDNS — Design and User Flow

This document describes the visual design language, feature surface, and end-to-end user flow of WhiteDNS across both the Android client (`/WhiteDNS`) and the cross-platform desktop client (`/whitedns-desktop`). It is a reference for how the product is shaped and how a user moves through it, not a build or contributor guide.

---

## 1. Product Summary

WhiteDNS is a DNS tunneling client that turns a remote StormDNS server into a local network tunnel. It provides two operating modes:

- **Proxy mode** — runs a local SOCKS5 listener (with an optional HTTP proxy bridge) so apps can opt in by pointing at it.
- **VPN mode** — captures system-wide traffic via the platform VPN API and routes it through the StormDNS tunnel.

Both modes are driven by the same underlying StormDNS native binary; the UI is a thin control plane that selects a profile, configures parameters, starts the binary, and renders the runtime state it streams back.

### Target Platforms

| Surface          | Path                    | Stack                                                |
|------------------|-------------------------|------------------------------------------------------|
| Android          | `/WhiteDNS`             | Kotlin, Jetpack Compose, Material 3, foreground services, Android `VpnService` + `tun2proxy` |
| Desktop (mac/win/linux) | `/whitedns-desktop` | Electron + TypeScript main, React + react-native-web renderer, Zustand store |

---

## 2. Design Language

### 2.1 Visual Identity (Android)

The Android client is the canonical, fully-designed surface. Defined in `app/src/main/java/shop/whitedns/client/ui/WhiteDnsTheme.kt`.

**Theme:** Dark, Material 3, single fixed color scheme (no light mode). The app sets status and navigation bar colors to match its background so the chrome blends into the canvas.

**Palette — semantic roles:**

| Role            | Hex        | Usage                                            |
|-----------------|-----------|--------------------------------------------------|
| Background      | `#0D0F14` | App canvas                                       |
| Surface         | `#161A23` | Cards, dialogs                                   |
| Surface Alt     | `#111420` | Secondary cards, input fields                    |
| Dropdown Surface| `#1C2030` | Menus and selectors                              |
| Border / Divider| `#1E2330` / `#252B3D` | Hairlines and section breaks            |
| Accent          | `#6C5CE7` | Primary actions, brand purple                    |
| Accent Pressed  | `#5A4BD1` | Pressed / container variant                      |
| Success         | `#00D68F` | Connected state, validated values                |
| Warning         | `#FBBF24` | Performance / attention banners                  |
| Error           | `#FF6B6B` | Failures, destructive actions                    |
| Ink             | `#EDEEF2` | Primary text                                     |
| Muted / Pale    | `#C2C8E1` / `#ADB5D3` | Secondary text                            |
| Disabled        | `#717A9E` | Disabled controls                                |

**Typography:** Material 3 defaults overridden with a tight, slightly bolder scale tuned for compact dense screens — headline 24sp/bold, title 17sp/bold, body 14–15sp normal, label 11–14sp medium with stronger letter spacing on small variants.

**Shape & motion:**
- Corners: `RoundedCornerShape` cards (~16dp), pill controls for primary buttons.
- Animations: `AnimatedVisibility` with `fadeIn + expandVertically` (~180–220ms) for inline reveals; `spring` for the connect button morph; `infiniteRepeatable` for the connecting-state ring.
- Banners (notification-permission, battery-optimization, full-VPN-warning) animate in/out rather than appearing instantly.

### 2.2 Visual Identity (Desktop)

The desktop client uses `react-native-web` primitives with `StyleSheet.create`. It is intentionally lighter-weight: a **light** theme with iOS-style accent colors. Defined inline per screen.

| Role        | Hex        | Notes                                  |
|-------------|-----------|----------------------------------------|
| Background  | `#f5f5f5` | App canvas                             |
| Surface     | `#ffffff` | Cards, header, tab bar                 |
| Border      | `#e5e5e5` / `#ddd` | Hairlines                       |
| Accent      | `#007AFF` | Primary actions, active tab indicator  |
| Success     | `#34C759` | Connected dot, submit buttons          |
| Danger      | `#FF3B30` | Disconnect, delete, disconnected dot   |
| Text Primary| `#000`    | Titles                                 |
| Text Muted  | `#666` / `#999` | Labels, empty states              |
| Logs Mono   | `Menlo`   | Log message text                       |

The two clients deliberately diverge: Android prioritizes a polished, branded dark experience for daily mobile use; desktop is a utility-grade panel where the same StormDNS binary is wrapped in a minimal cross-platform shell.

### 2.3 Layout Pattern

Both clients share the same top-level layout:

```
+----------------------------------+
| Header / Status                  |
+----------------------------------+
|                                  |
|  Active tab content (scrolls)    |
|                                  |
+----------------------------------+
| Bottom Tab Bar                   |
+----------------------------------+
```

Tabs (in display order):

| Android        | Desktop        | Purpose                                |
|----------------|----------------|----------------------------------------|
| Profiles       | Profiles       | CRUD for connection + resolver profiles|
| Connect        | Connect        | Start/stop, live status, traffic, info |
| Logs           | Logs           | Streaming output from the binary       |

---

## 3. Feature Surface

### 3.1 Connection Profiles

A **connection profile** is one named server target. It bundles:

- Display name
- Server mode (`custom` for user-entered servers; built-in pool exists on Android via `StormDnsBuiltInPool`)
- Server domain
- Encryption key
- Encryption method — one of: `0` None, `1` XOR, `2` ChaCha20, `3` AES-128-GCM, `4` AES-192-GCM, `5` AES-256-GCM
- A linked resolver profile id
- Connection mode (`proxy` or `vpn`)

**Sources:** Manual entry, `stormdns://` link import, or — on Android — the built-in profile pool.

**Sharing:** Both clients support exporting a profile as a `stormdns://` link and importing one back. Android adds a share-sheet handoff to system apps via `FileProvider`.

### 3.2 Resolver Profiles

A **resolver profile** is a named list of DNS resolver IPs (one per line). The selected resolver profile feeds the StormDNS client at startup. Resolver text is validated (`ResolverTextValidation`) so invalid entries are flagged and the normalized list is stored.

The Android client ships default resolver assets and the desktop client lets users hand-author the list.

### 3.3 Operating Modes

**Proxy mode** (both platforms):
- Local SOCKS5 server bound to `listenIp:listenPort` (default `127.0.0.1:10886`).
- Optional HTTP proxy bridge on `httpProxyPort` (default `10887`) that forwards CONNECT/HTTP requests through the SOCKS5 socket.
- Optional SOCKS5 username/password authentication.
- On Android, served by `WhiteDnsProxyService` — a foreground service in a `:proxy` subprocess with a `dataSync` notification.

**VPN mode** (Android primary; desktop scaffolded):
- Android uses `VpnService` + a packaged `tun2proxy` native library that points the system tun back at the local SOCKS5 listener.
- A **split tunnel** UI lets users include or exclude specific installed apps (`SplitTunnelSettingsPanel`, `SplitTunnelAppDialog`).
- A dismissible `FullVpnPerformanceWarning` is shown the first time a user picks VPN mode.
- Foreground service `WhiteDnsVpnService` runs in a `:vpn` subprocess with the `systemExempted` foreground type.

### 3.4 Runtime Telemetry

While connected, both clients render structured telemetry parsed line-by-line from the StormDNS binary's stdout/stderr stream:

- **Connection progress** — `phase`, `percent`, `completed/total`, `valid`, `rejected` resolver counts during startup probing.
- **Resolver runtime state** — active, standby, and valid resolver lists (the live pool the tunnel is rotating through).
- **Traffic stats** — cumulative `downloadBytes`/`uploadBytes`, instantaneous and peak speeds, total data usage, connected app count.
- **Logs** — every line emitted by the binary, plus Android service-side messages, surfaced verbatim in the Logs tab.

Parsers live in `src/main/parsers/*` (desktop) and `runtime/parseStormDns*` (Android).

### 3.5 Tunnel & Performance Controls (Android — full set)

Exposed under an "Advanced" panel on the Connect tab:

- Balancing strategy, upload/download duplication, upload/download compression, base-encoding toggle.
- MTU min/max, MTU test retries/timeouts/parallelism (separate values for resolvers and logs lookups).
- Worker count (RX/TX, tunnel process), channel size, queue capacity, dispatcher idle poll interval.
- Tunnel packet timeout, ping watchdog, traffic warmup (probe count + interval).
- Session-init retry curve: base, step, linear-after, max, busy-retry interval.
- Local DNS toggle + port (default 53).
- Log level (`DEBUG` / `INFO` / `WARN` / `ERROR`).

Desktop exposes the same fields in `WhiteDnsSettings` (`src/shared/models.ts`) but ships fewer controls in the current Profiles UI; values fall back to defaults from `createDefaultSettings()`.

### 3.6 Platform Integrations (Android)

- **Notification permission banner** — shown when VPN mode is selected and `POST_NOTIFICATIONS` is not granted.
- **Battery optimization banner** — prompts the user to allow `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` for reliable background runs.
- **FileProvider** — exporting logs as a text file and sharing `stormdns://` profile links.
- **Foreground service notifications** — long-running indicator while proxy or VPN is active.
- **Network IP discovery** — `findDeviceNetworkIpAddress()` substitutes the LAN IP when `listenIp` is `0.0.0.0`, so the user sees an address other devices can reach.

---

## 4. Architecture (How a Connection Actually Happens)

### 4.1 Android

```
Compose UI ──> WhiteDnsViewModel ──> WhiteDnsProxyService (:proxy)  ┐
                                  └─> WhiteDnsVpnService   (:vpn)   │
                                                                    ▼
                                                          StormDNS native binary
                                                                    │
                                              broadcast/log events ◄┘
                                  resolver/progress/traffic parsers ◄┘
```

- `WhiteDnsViewModel` owns `uiState`, settings persistence (`WhiteDnsSettingsStore`), and a coroutine pipeline that subscribes to proxy/VPN events plus a stats refresh loop.
- Service events arrive via either direct listeners (`WhiteDnsProxyEvents` / `WhiteDnsVpnEvents`) or broadcasts (`WhiteDnsProxyService.BroadcastAction`), and are funneled into `handleRuntimeLog` / `handleRuntimeReady` / `handleProxyFailure`.
- The view model parses each log line through `parseStormDnsConnectionProgressLine`, `parseStormDnsResolverStateLine`, and `parseStormDnsTrafficStatsLine`, throttles UI updates, and reconciles them with `Android TrafficStats` for traffic deltas.

### 4.2 Desktop

```
React renderer (Zustand store)
      │  IPC (preload bridge)
      ▼
Electron main: ConnectionManager
      ├── StormDnsProcessManager  ──> StormDNS binary (stdio)
      ├── HttpProxyBridge          (optional HTTP -> SOCKS5)
      ├── TrafficWarmup            (probes the SOCKS5 port)
      ├── RuntimeStateStore        (status / lifecycle)
      └── SettingsStore            (persisted JSON)
```

`ConnectionManager.connect()` — defined in `src/main/ConnectionManager.ts:32` — runs this sequence:

1. Validate that a connection profile and a non-empty resolver profile are selected.
2. `setStatus(CONNECTING)`; mark runtime state as starting.
3. `stormDnsProcessManager.start(...)` with the selected profile + settings; pipe every output line into `handleBinaryOutput`.
4. `waitForPort(listenPort, 10s)` — TCP-probe until the SOCKS5 listener responds.
5. If `httpProxyEnabled`, instantiate `HttpProxyBridge` (with optional auth) and `await start()`.
6. `setStatus(CONNECTED)`; mark runtime ready; emit `ready`.
7. If `trafficWarmupEnabled`, fire N probes through the SOCKS5 port at 100ms intervals.

`disconnect()` clears the warmup timer, stops the binary (1.5s grace), stops the HTTP bridge, and emits `status-change`.

---

## 5. End-to-End User Flow

The following describes a first-run user landing on either client. Steps are equivalent unless flagged otherwise.

### 5.1 First Launch

1. App opens to the **Connect** tab (Android) / **Connect** tab (desktop). Status reads **Disconnected**.
2. (Android) If applicable, the **Notification permission** and **Battery optimization** banners appear above the connect button.
3. The connect button is enabled only when at least one resolver profile has resolvers — otherwise tapping it surfaces a "resolver required" hint and routes the user toward Profiles.

### 5.2 Creating a Connection Profile

1. User opens the **Profiles** tab → *Connection Profiles* sub-tab.
2. Taps **+ New Profile**.
3. Fills in: name, server domain, encryption key, encryption method (Android exposes the picker; desktop currently fixes method `0` in the form, with the model supporting all six).
4. Saves. The profile becomes available in the Connect tab's profile dropdown.

Alternative: user receives a `stormdns://` link, opens the **Import** action, and the profile is parsed via `WhiteDnsProfileLinks` / `src/shared/profileLinks.ts` and added to the list.

### 5.3 Creating / Selecting a Resolver Profile

1. User switches to **Profiles → Resolver Profiles**.
2. Taps **+ New Resolver Profile**.
3. Pastes one IP per line (e.g. `8.8.8.8`, `1.1.1.1`). Validation strips invalids and normalizes the text.
4. Saves; selects this resolver as the one linked to the active connection profile.

### 5.4 Connecting

1. User returns to **Connect**. Selects:
   - Connection profile (dropdown).
   - Mode: **Proxy** or **VPN** (segmented control; Android only — desktop is proxy-first).
   - (Optional, Android) Adjusts split-tunnel app inclusions if VPN.
   - (Optional) Opens **Advanced** to tune workers, MTU, retries, log level, etc.
2. Taps **Connect**. The button morphs into a connecting indicator.
3. Behind the scenes:
   - The OS may prompt for VPN consent (Android, first VPN run only).
   - The StormDNS binary launches and begins probing resolvers.
   - The Connect screen shows live **Connection Progress**: phase, percent, valid/rejected counts.
4. When the binary signals ready and the SOCKS5 port is listening:
   - Status flips to **Connected** (green dot).
   - The connect button turns into a red **Disconnect**.
   - The card expands to show:
     - **Traffic Statistics** — download/upload totals + live speeds.
     - **Resolvers** — active resolver IPs (top N).
     - **Connection info** (Android) — proxy/HTTP addresses, protocol, mode.

### 5.5 Live Use

- **Apps connect** via the SOCKS5 endpoint (`127.0.0.1:10886` by default), the HTTP bridge port if enabled, or — under VPN mode — automatically because the system tun captures their traffic.
- **Logs tab** streams every binary line for diagnosis. Android adds an export-to-text-file action that hands the log off through `FileProvider`.
- **Resolver state** updates as the rotation pool changes; failing resolvers are dropped to standby and replaced.
- (Android) The foreground service notification persists and reflects the active mode.

### 5.6 Disconnecting

1. User taps **Disconnect**.
2. Client signals the StormDNS process to stop, with a short grace window.
3. HTTP bridge and traffic-warmup timers are torn down.
4. Status returns to **Disconnected**; runtime stats clear; logs remain in the Logs tab until the next session.

### 5.7 Error & Edge Paths

- **No resolvers** → connect blocked with inline message; user pushed to Profiles.
- **Port already in use / binary refuses to bind** → `waitForPort` times out → `failed` event → status reverts to Disconnected with an error log entry.
- **VPN consent denied** (Android) → service does not start; failure surfaced in logs.
- **Battery optimization not whitelisted** (Android) → banner remains visible after reconnect; tapping it deep-links to system settings.
- **Notification permission missing in VPN mode** (Android) → banner remains until granted; service still runs but with reduced visibility.
- **Network IP unavailable** → `displayProxyIpAddress` falls back to `listenIp` so the address card never blanks out.

---

## 6. State Model Cheat Sheet

`WhiteDnsSettings` is the single persisted blob on each platform. Its core dimensions:

- **Identity** — `selectedConnectionProfileId`, `connectionProfiles[]`, `selectedResolverProfileId`, `resolverProfiles[]`.
- **Server target** — `customServerDomain`, `customServerEncryptionKey`, `customServerEncryptionMethod`, `serverMode`.
- **Mode** — `connectionMode` (`proxy` | `vpn`), `protocolType` (`SOCKS5`).
- **Listen** — `listenIp`, `listenPort`, `httpProxyEnabled`, `httpProxyPort`, `socks5Authentication`, `socksUsername`, `socksPassword`.
- **Tunnel tuning** — balancing/duplication/compression, MTU range + tests, worker counts, channel/queue sizing, retry curve, watchdog, traffic warmup.
- **Local DNS** — `localDnsEnabled`, `localDnsPort`.
- **Diagnostics** — `logLevel`.

Runtime (non-persisted) state:

- `ConnectionStatus` ∈ `DISCONNECTED | CONNECTING | CONNECTED`
- `ConnectionProgressState` — startup probing telemetry
- `ResolverRuntimeState` — active/standby/valid resolver lists
- `ConnectionStats` — bytes + speeds + connected apps
- `WhiteDnsRuntimeState` — service-side lifecycle (starting/ready/stopped/failed)

---

## 7. Where Things Live (Quick Map)

**Android (`/WhiteDNS`):**
- `app/src/main/java/shop/whitedns/client/MainActivity.kt` — entry point, hosts Compose tree.
- `ui/WhiteDnsScreen.kt` — every Composable on screen (~4.6k LOC; tab content, dialogs, banners, advanced panel).
- `ui/WhiteDnsViewModel.kt` — orchestrates services, parses logs, owns `uiState`.
- `ui/WhiteDnsTheme.kt` — palette, typography, theme.
- `model/` — settings, profile + validation models, `stormdns://` link encoding.
- `proxy/` — `WhiteDnsProxyService` and HTTP bridge.
- `vpn/` — `WhiteDnsVpnService` + `tun2proxy` integration.
- `storm/` — StormDNS config rendering and process management.
- `runtime/` — line parsers and runtime state store.
- `jniLibs/` — packaged StormDNS / tun2proxy native libraries.

**Desktop (`/whitedns-desktop`):**
- `src/main/index.ts` — Electron entry; window setup; registers IPC handlers.
- `src/main/ConnectionManager.ts` — orchestrates start/stop sequence.
- `src/main/core/` — `StormDnsProcessManager`, `HttpProxyBridge`, `TrafficWarmup`, `RuntimeStateStore`, `SettingsStore`, `StormDnsConfigRenderer`.
- `src/main/parsers/` — progress / resolver / traffic line parsers.
- `src/main/ipc/handlers.ts` — renderer ↔ main bridge.
- `src/renderer/App.tsx` — three-tab shell.
- `src/renderer/screens/{Connect,Profiles,Logs}Screen.tsx` — tab content.
- `src/renderer/store/useAppStore.ts` — Zustand store + IPC listener wiring.
- `src/shared/{models.ts,profileLinks.ts}` — shared types and link codec.

---

## 8. Design Principles in Practice

The product's design intent shows up consistently across both clients:

1. **The binary is the source of truth.** The UI parses what the binary says; it never simulates progress or stats. If StormDNS is silent, the UI stays empty.
2. **Profile-first.** Nothing happens without a connection profile and a resolver profile. The Connect screen pushes the user to Profiles when either is missing rather than silently failing.
3. **Observable defaults.** Defaults are tuned to "just work" (`127.0.0.1:10886`, SOCKS5, INFO logging, traffic warmup on), but every parameter is reachable from the Advanced panel for diagnostic work.
4. **Two-mode parity.** Proxy and VPN share state, parsers, and most UI; mode-specific affordances (split tunnel, VPN consent, performance warning) appear inline only when relevant.
5. **Platform-honest design.** Android leans into a polished branded dark UI with motion; desktop is a quieter utility shell. Both serve the same control plane.
