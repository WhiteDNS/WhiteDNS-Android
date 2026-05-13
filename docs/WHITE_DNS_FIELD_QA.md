# WhiteDNS QA Test Plan

This document defines repeatable quality-assurance coverage for WhiteDNS. It is
written as an executable test plan: every section describes what to prepare, what
to do, and what result must be observed.

The plan focuses on areas that are most likely to affect real users: profile
setup, resolver imports, proxy/VPN runtime behavior, diagnostics, sensitive data
handling, traffic warmup, background recovery, UI resilience, and release safety.

## Scope

This plan covers the Android WhiteDNS client and its integration boundary with the
bundled StormDNS executable.

WhiteDNS must treat StormDNS as a black-box upstream engine. QA should verify
integration through public process boundaries only:

- Executable startup and exit codes.
- CLI flags and version/capability output.
- Generated TOML and resolver files.
- Files written under the app working directory.
- Standard output, standard error, and runtime telemetry.
- Proxy and VPN behavior visible from the Android app.

QA must not rely on StormDNS private Go packages, internal source layout, or
implementation details.

The plan also covers user-facing education inside the app. WhiteDNS handles
settings that directly affect reliability, speed, battery, traffic cost, privacy,
and LAN exposure. A release is not complete if those settings exist but users
cannot understand the effect of changing them.

## Test Environments

Run the core suite on at least these environments before release:

| Environment | Required Coverage |
| --- | --- |
| Fresh install on a current Android release | First-run setup, defaults, permissions, connection, diagnostics. |
| Upgrade install from the previous public release | Settings migration, encrypted secrets, profiles, resolver lists. |
| Older supported Android release | Foreground service, VPN permission, clipboard behavior, file sharing. |
| Battery saver enabled | Warmup backoff, background recovery, foreground notification behavior. |
| Wi-Fi network | Proxy mode, VPN mode, network-change recovery. |
| Mobile data network | Proxy mode, VPN mode, resolver behavior, MTU presets. |
| Restricted/background app state | Disconnect recovery, notification state, runtime state persistence. |
| Large installed-app list | Split-tunnel app picker responsiveness and search. |
| Large text/font scale | Layout resilience, user guide readability, warnings. |
| Companion proxy client | Proxy address copy, local routing, port mismatch handling. |

When possible, repeat proxy and VPN connection tests with more than one mobile
operator or network type because DNS tunneling behavior can vary sharply by
network.

## Test Data

Prepare these test assets before running the suite:

- One known-good server profile.
- One intentionally invalid server profile with a malformed domain.
- One server profile with an invalid or missing encryption key.
- One profile containing a single domain.
- One profile containing multiple domains.
- One legacy profile link that uses a single `server.domain` field.
- One current profile link that uses multiple domains.
- A small valid resolver file.
- A resolver file with duplicates.
- A resolver file with invalid rows.
- A resolver file with private, loopback, or link-local addresses.
- A resolver file with explicit port suffixes.
- A resolver file near the configured import size/count limit.
- A resolver file beyond the configured import size/count limit.
- Long profile names, resolver names, and domain values for UI wrapping tests.

Do not use production secrets in QA artifacts. Test credentials must be disposable.

## Entry Criteria

Begin a full QA pass only when:

- The app builds locally or in CI.
- Required native binaries are present for the tested ABI.
- The tested branch has no known merge conflicts.
- Release notes or change summary are available for upgrade/regression focus.
- Test credentials and resolver fixtures are prepared.

## Exit Criteria

A release candidate is acceptable only when:

- P0 test cases pass.
- P1 failures are either fixed or explicitly accepted with documented impact.
- Diagnostics exports are verified redacted by default.
- Profile export/import remains functional.
- No known flow stores new plaintext secrets outside approved encrypted storage.
- No critical crash, connection-state corruption, or LAN proxy exposure issue is
  open.

## Severity Definitions

| Severity | Meaning |
| --- | --- |
| P0 | Blocks release. Security leak, data loss, crash loop, broken connection, broken import/export, unsafe LAN exposure. |
| P1 | Must fix before stable release unless explicitly accepted. Major UX break, misleading state, background disconnect, excessive traffic. |
| P2 | Should fix soon. Confusing copy, missing guidance, slow UI, incomplete edge-case validation. |
| P3 | Nice to improve. Cosmetic issue or documentation polish. |

## Core Smoke Tests

### QA-CORE-001 Fresh Install Defaults

Priority: P0

Steps:

1. Install WhiteDNS on a device with no existing app data.
2. Launch the app.
3. Inspect the default profile, resolver, proxy, VPN, warmup, and diagnostics
   settings.

Expected result:

- No real credentials are prefilled.
- Required fields are clearly empty or marked as not configured.
- Default traffic warmup is low-risk.
- Backup and cleartext network defaults are not exposed as user-facing risks.
- The app does not crash or request unrelated permissions.

### QA-CORE-002 Upgrade From Previous Release

Priority: P0

Steps:

1. Install the previous public release.
2. Create a profile with server credentials, resolver settings, proxy settings,
   and VPN settings.
3. Upgrade to the candidate build without clearing app data.
4. Launch the app and inspect the migrated data.
5. Restart the app and inspect the data again.

Expected result:

- Existing profiles remain available.
- Secrets remain usable after migration.
- Secrets are stored through the approved encrypted path after migration.
- Non-sensitive settings remain readable.
- No duplicate default profiles or resolver profiles are created.

### QA-CORE-003 Start, Stop, Start Again

Priority: P0

Steps:

1. Configure a valid profile.
2. Start a connection.
3. Stop the connection while startup is still in progress.
4. Immediately attempt to start again.
5. Repeat for proxy mode and VPN mode.

Expected result:

- The app enters a disconnecting state while ports, routes, and processes close.
- Reconnect is disabled until the previous runtime is stopped or timed out.
- Stale events from the previous attempt do not overwrite the new runtime state.
- No old StormDNS process remains running.

### QA-CORE-004 First-Run Starter Walkthrough

Priority: P1

Steps:

1. Install WhiteDNS with no saved connection profile and no resolver profile.
2. Open the main connect screen.
3. Read the starter walkthrough.
4. Tap the actions to add a connection profile and resolver profile.
5. Return to the connect screen after each step.

Expected result:

- The walkthrough explains the minimum setup path in order.
- It distinguishes server profile, resolver profile, runtime mode, and readiness.
- Actions take the user to the correct profile creation flow.
- The walkthrough disappears or becomes non-blocking once setup is complete.
- The text remains readable at large font sizes.

### QA-CORE-005 In-App Guide Tab

Priority: P1

Steps:

1. Open the bottom navigation guide tab.
2. Scroll through every guide section.
3. Rotate the device or change display size if practical.
4. Return to connect, profiles, and logs tabs.

Expected result:

- The guide is reachable without an active connection.
- The guide explains what each major parameter affects.
- Sections cover connection basics, proxy mode, VPN mode, traffic/battery, status
  metrics, import/export, diagnostics, troubleshooting, and QA.
- Navigation state remains stable after leaving and returning to the guide.
- The guide does not expose secrets or refer to private support material.

## Profile Import, Export, And QR

### QA-PROFILE-001 Full Profile Export Is Functional

Priority: P0

Steps:

1. Create a valid profile with a real test server key.
2. Open the profile export flow.
3. Inspect the visible export text.
4. Generate the QR preview.
5. Import the exported value on a clean install or second device.

Expected result:

- The visible profile export value is the real importable value.
- The QR code encodes the same real importable value.
- A clear warning explains that the export includes connection secrets.
- The imported profile preserves all supported fields.
- Diagnostics and logs still redact the same profile link.

### QA-PROFILE-002 Redacted Diagnostics Do Not Break Export

Priority: P0

Steps:

1. Create a profile with server key, SOCKS credentials, and multiple domains.
2. Generate diagnostics.
3. Copy the default diagnostics output.
4. Export the profile link separately.

Expected result:

- Default diagnostics are redacted.
- Server keys, SOCKS passwords, profile links, TOML secrets, runtime paths, and
  generated file paths are not exposed in diagnostics.
- Profile export remains full and importable.
- The product clearly separates "share diagnostics" from "share profile".

### QA-PROFILE-003 Legacy Single-Domain Import

Priority: P1

Steps:

1. Import a legacy profile link containing one server domain.
2. Save the imported profile.
3. Generate StormDNS launch config.

Expected result:

- Import succeeds.
- The domain appears as the selected profile domain.
- Generated config contains the expected single-domain value.
- No extra blank domain is added.

### QA-PROFILE-004 Multi-Domain Import And Export

Priority: P1

Steps:

1. Create a profile with multiple domains.
2. Save it.
3. Export it.
4. Import it into a clean profile slot.
5. Generate StormDNS launch config.

Expected result:

- Every domain survives save, export, import, and config generation.
- The UI summary is readable and does not overflow.
- Invalid domains are reported individually.
- Legacy consumers can still read the first domain when applicable.

### QA-PROFILE-005 Oversized Or Malformed Profile Link

Priority: P0

Steps:

1. Import an oversized profile link.
2. Import a link with malformed encoding.
3. Import a link with excessively long field values.
4. Import a link with invalid domain, port, worker, timeout, and MTU values.

Expected result:

- Invalid imports are rejected with clear errors.
- Existing saved profiles are not changed after a failed import.
- No crash or UI freeze occurs.
- No partial secrets are written to runtime launch storage.

### QA-PROFILE-006 Profile Export Warning Copy

Priority: P1

Steps:

1. Open profile export for a profile with a real test secret.
2. Read the warning shown above the export value.
3. Copy the profile value.
4. Share the profile value if the flow is available.

Expected result:

- The warning clearly states that the export contains connection secrets.
- The warning does not imply that the visible export value is redacted.
- Copy/share actions remain intentional.
- The QR preview remains consistent with the visible export value.

## Settings Validation

### QA-VALID-001 Fatal Validation Blocks Connection

Priority: P0

Steps:

1. Configure an invalid domain.
2. Configure an invalid server key.
3. Configure an out-of-range proxy port.
4. Configure invalid worker, queue, timeout, and MTU values.
5. Attempt to connect after each invalid setting.

Expected result:

- Connection is blocked for fatal validation errors.
- The app identifies the field that must be fixed.
- No foreground service is started for invalid settings.
- No runtime launch request is written for invalid settings.

### QA-VALID-002 Risky Expert Settings Produce Warnings

Priority: P1

Steps:

1. Configure high worker counts.
2. Configure aggressive warmup.
3. Configure a LAN-reachable proxy listener.
4. Configure experimental VPN behavior if available.

Expected result:

- Risky but allowed settings show warnings before save or connect.
- Warnings are specific and actionable.
- The app does not silently downgrade user-selected expert values unless the
  value is invalid.

## Resolver Import And Quality

### QA-RESOLVER-001 Small Valid Resolver Import

Priority: P0

Steps:

1. Import a small valid resolver file.
2. Review the import preview.
3. Save the resolver profile.
4. Connect using the resolver profile.

Expected result:

- Preview shows total and accepted count.
- Save succeeds.
- Connection uses the selected resolver profile.
- Diagnostics report resolver count without exposing file paths unnecessarily.

### QA-RESOLVER-002 Duplicate-Heavy Resolver Import

Priority: P1

Steps:

1. Import a resolver file with many duplicate rows.
2. Review the import preview.
3. Save the resolver profile.

Expected result:

- Duplicate rows are counted.
- Saved resolver list is deduplicated.
- The user can see the final accepted count before saving.
- The app remains responsive.

### QA-RESOLVER-003 Invalid And Private Resolver Rows

Priority: P0

Steps:

1. Import a resolver file with invalid rows.
2. Import a resolver file with private, loopback, or link-local addresses.
3. Import a resolver file with explicit port suffixes.

Expected result:

- Invalid rows are rejected or clearly excluded.
- Private/local addresses are blocked or require an explicit expert decision.
- Port-suffixed entries are handled according to the documented resolver format.
- Import preview explains what was accepted, changed, or rejected.

### QA-RESOLVER-004 Oversized Resolver Import

Priority: P0

Steps:

1. Import a resolver file near the configured limit.
2. Import a resolver file beyond the configured limit.
3. Repeat while the app is connected.

Expected result:

- Near-limit import completes without UI freeze.
- Over-limit import is blocked before excessive memory use.
- Failed import does not alter the active resolver profile.
- Editing an inactive resolver profile while connected does not disrupt the
  active runtime.

### QA-RESOLVER-005 Resolver Guidance Copy

Priority: P2

Steps:

1. Open resolver profile creation.
2. Open the in-app guide section that explains resolvers.
3. Import or paste a duplicate-heavy resolver list.
4. Import or paste a list containing private/local resolvers.

Expected result:

- Resolver guidance explains that larger lists are not automatically better.
- Duplicate, invalid, and private/local resolver risks are described in user-facing
  language.
- Import validation and guide wording agree with each other.

## Connection Health And Troubleshooting

### QA-HEALTH-001 Server Health Check Success

Priority: P1

Steps:

1. Configure a known-good server profile.
2. Run the server health check.
3. Run resolver health check.
4. Run local route/proxy check.

Expected result:

- Each check reports its own pass/fail state.
- A successful final route check is required before the app reports the runtime
  as ready for user traffic.
- The user can distinguish server health, resolver health, and local route health.

### QA-HEALTH-002 Connected But No Traffic

Priority: P0

Steps:

1. Configure a profile that starts StormDNS but fails outbound traffic.
2. Start proxy mode.
3. Observe status, logs, and diagnostics.
4. Repeat in VPN mode.

Expected result:

- The app does not report a misleading ready state.
- Failed outbound SOCKS or route probes are surfaced as a failure or needs-attention
  state.
- Diagnostics include the failing layer without exposing secrets.
- The user receives a concrete next step.

### QA-HEALTH-003 Startup Progress Timeout

Priority: P1

Steps:

1. Configure a profile that causes startup to stall.
2. Start the connection.
3. Wait for the startup timeout.
4. Stop the connection.

Expected result:

- Progress does not remain indefinitely stuck.
- Timeout state explains which layer did not become ready.
- Stop closes process, files, ports, and VPN resources.
- Reconnect becomes available only after cleanup.

### QA-HEALTH-004 Troubleshooting Guide Accuracy

Priority: P1

Steps:

1. Create a server failure.
2. Create a resolver failure.
3. Create a local proxy route failure.
4. Create a VPN split-tunnel exclusion failure.
5. Compare the guide's troubleshooting order with the observed diagnostics.

Expected result:

- The guide recommends checking server, resolvers, local route, and network in a
  sensible order.
- Diagnostics and UI labels use the same concepts as the guide.
- The guide does not recommend changing unrelated advanced settings first.

## Proxy Mode

### QA-PROXY-001 Loopback Proxy Without Authentication

Priority: P0

Steps:

1. Configure proxy mode with loopback listen address.
2. Disable SOCKS authentication.
3. Connect.
4. Use a local client to send traffic through the proxy.

Expected result:

- Loopback-only listener starts.
- Local traffic works.
- Diagnostics report listener address and port in redacted/safe form.
- No LAN exposure warning is required for loopback-only use.

### QA-PROXY-002 LAN Proxy Requires Complete Authentication

Priority: P0

Steps:

1. Configure a LAN-reachable listen address.
2. Disable SOCKS authentication and attempt to connect.
3. Enable SOCKS authentication with blank username.
4. Enable SOCKS authentication with blank password.
5. Enable SOCKS authentication with nonblank username and password.

Expected result:

- LAN listener is blocked without authentication.
- LAN listener is blocked with blank username or blank password.
- LAN listener starts only with complete credentials.
- UI warning is visible before the risky configuration is used.

### QA-PROXY-003 HTTP Proxy Bridge Limits

Priority: P0

Steps:

1. Start proxy mode.
2. Open more client connections than the configured limit.
3. Send oversized headers.
4. Send an excessively long target host.
5. Leave tunnels idle beyond the configured idle timeout.

Expected result:

- Excess clients are rejected or queued according to limits.
- Oversized headers and host values are rejected.
- Idle tunnels are closed.
- Executor threads are released after stop or bind failure.
- Bridge statistics appear in diagnostics.

### QA-PROXY-004 Bind Failure Cleanup

Priority: P0

Steps:

1. Occupy the configured proxy port with another process.
2. Start proxy mode.
3. Stop and retry with a free port.

Expected result:

- Bind failure is reported cleanly.
- No client or tunnel executor threads leak after failed start.
- Retrying with a free port succeeds.

## VPN Mode

### QA-VPN-001 Basic VPN Connection

Priority: P0

Steps:

1. Grant VPN permission.
2. Start VPN mode.
3. Open a browser and another app that uses the network.
4. Stop VPN mode.

Expected result:

- VPN starts with a foreground notification.
- Traffic routes according to selected mode and split-tunnel settings.
- Stop removes the VPN interface and foreground notification.
- Runtime state changes to stopped.

### QA-VPN-002 IPv6 Strategy

Priority: P1

Steps:

1. Test IPv6 block mode.
2. Test IPv6 bypass-with-warning mode.
3. Test experimental IPv6 route mode if available.

Expected result:

- Each strategy behaves according to its label.
- Warnings are visible for bypass or experimental behavior.
- Diagnostics report the selected strategy.
- IPv6 behavior does not silently contradict the UI setting.

### QA-VPN-003 MTU Presets

Priority: P1

Steps:

1. Select MTU 1280, connect, and run traffic.
2. Select MTU 1400, connect, and run traffic.
3. Select MTU 1500, connect, and run traffic.
4. Select custom MTU below and above allowed ranges.

Expected result:

- Preset MTUs are applied.
- Invalid custom MTUs are blocked by validation.
- Diagnostics show the active MTU.

### QA-VPN-004 Network Change Recovery

Priority: P0

Steps:

1. Start VPN mode on Wi-Fi.
2. Switch to mobile data.
3. Toggle airplane mode off and on.
4. Lock and unlock the device.
5. Stop the connection during a recovery attempt.

Expected result:

- Network-change callbacks are debounced.
- Recovery uses the same runtime session.
- Recovery stops after configured attempt/window limits.
- Stale callbacks cannot restart a stopped session.
- The UI and notification agree on current state.

### QA-VPN-005 Always-On Guidance

Priority: P2

Steps:

1. Enable Always-on VPN for WhiteDNS if supported by the device.
2. Enable block-without-VPN if supported.
3. Start and stop WhiteDNS.
4. Reboot the device if practical.

Expected result:

- The app provides clear guidance for Always-on behavior.
- Block-without-VPN implications are explained.
- Runtime state after reboot is not misleading.

## Traffic Warmup And Battery

### QA-WARMUP-001 Warmup Modes

Priority: P1

Steps:

1. Test Off.
2. Test Startup-only.
3. Test Balanced.
4. Test Aggressive.
5. Test Custom with valid values.

Expected result:

- Off sends no deliberate warmup traffic.
- Startup-only stops after the startup window.
- Balanced skips keepalive when recent real traffic exists.
- Aggressive is clearly marked as higher traffic.
- Custom values are applied only after validation.

### QA-WARMUP-002 Battery Saver Backoff

Priority: P1

Steps:

1. Enable battery saver.
2. Start a connection with Balanced or Aggressive warmup.
3. Background the app.
4. Observe warmup behavior and diagnostics.

Expected result:

- Warmup backs off under battery saver or background constraints.
- Diagnostics show warmup mode and recent activity without secrets.
- The app remains connected unless the platform stops it.

### QA-WARMUP-003 Warmup Explanation Matches Behavior

Priority: P1

Steps:

1. Read the in-app guide section for traffic and battery.
2. Test Off, Startup-only, Balanced, Aggressive, and Custom warmup modes.
3. Observe upload/download counters and diagnostics.

Expected result:

- Guide wording matches actual behavior.
- Startup-only is clearly described as low-cost.
- Aggressive is clearly described as higher traffic and battery cost.
- Users can understand why upload may increase during warmup or keepalive.

## Advanced Parameter Guidance

### QA-GUIDE-001 Tunnel Shape Guidance

Priority: P1

Steps:

1. Open the in-app guide.
2. Read the tunnel-shape section.
3. Compare the guide text with the advanced settings editor.
4. Change upload/download size ranges, duplication, compression, DNS fragments,
   and base encoding.

Expected result:

- Every major tunnel-shape setting has a plain-language explanation.
- The guide explains the tradeoff between compatibility, throughput, CPU, traffic,
  and resolver load.
- The advanced editor and guide use consistent names.
- Invalid values are blocked by validation before connection.

### QA-GUIDE-002 Reliability Control Guidance

Priority: P1

Steps:

1. Open the in-app guide.
2. Read the reliability-controls section.
3. Compare the guide text with resolver retry, timeout, worker, channel, queue,
   and retry pacing settings.
4. Configure low, normal, and high values where validation allows.

Expected result:

- The guide explains when increasing a value can help and when it can hurt.
- Timeout settings are described in terms of fast failure vs slow-network patience.
- Worker and queue settings are described in terms of throughput vs CPU, memory,
  battery, and delayed failure visibility.
- Retry pacing is described in terms of recovery speed vs repeated traffic on
  failing networks.

### QA-GUIDE-003 Runtime Housekeeping Guidance

Priority: P2

Steps:

1. Open the in-app guide.
2. Read the runtime-housekeeping section.
3. Compare guide wording with ping watchdog, idle poll, busy retry, log controls,
   and retain-limit settings.

Expected result:

- Watchdog behavior is described as detection of stuck sessions, not as a speed
  booster.
- Polling controls explain responsiveness and battery tradeoffs.
- Log controls do not imply that secrets may be safely shared without redaction.
- Retain settings are described as bounded diagnostic history.

## Runtime Files And Storage

### QA-STORAGE-001 Runtime Launch Requests

Priority: P0

Steps:

1. Start a connection.
2. Confirm a per-session runtime launch request is created under no-backup storage.
3. Stop the service.
4. Start several new sessions.
5. Trigger stale request cleanup.

Expected result:

- Launch request intents carry only a session identifier.
- Full launch parameters are stored under no-backup runtime storage.
- Request files are deleted after service stop or cleanup.
- Stale request files do not accumulate indefinitely.
- Request contents are encrypted or contain no plaintext secrets according to the
  current storage design.

### QA-STORAGE-002 StormDNS Launch File Lifetime

Priority: P0

Steps:

1. Start proxy mode or VPN mode.
2. Confirm generated TOML and resolver files exist while StormDNS is running.
3. Stop the connection.
4. Confirm generated launch files are removed.
5. Create stale `.wd-*` files and start a new connection.

Expected result:

- Launch files remain alive for the process lifetime.
- Launch files are removed after process exit or service stop.
- Stale `.wd-*` files are pruned by age.
- Fresh or unrelated files are not deleted.

### QA-STORAGE-003 Encrypted Secrets

Priority: P0

Steps:

1. Save server keys and SOCKS credentials.
2. Restart the app.
3. Reboot the device if practical.
4. Inspect app-created preferences and runtime files through developer tooling.

Expected result:

- Secrets remain usable by the app.
- Secrets are not stored in plaintext SharedPreferences.
- Non-sensitive settings remain separate from encrypted secret material.
- Backup and data extraction rules exclude sensitive files.

## Diagnostics, Logs, Clipboard, And Sharing

### QA-DIAG-001 Redacted Diagnostics

Priority: P0

Steps:

1. Generate diagnostics while disconnected.
2. Generate diagnostics while connecting.
3. Generate diagnostics while connected.
4. Generate diagnostics after a failure.

Expected result:

- Diagnostics include enough state to support troubleshooting.
- Server keys, SOCKS credentials, profile links, TOML secrets, runtime paths, raw
  resolver paths, logs, and launch request paths are redacted.
- The default copy/share action uses redacted output.
- Full sensitive export requires explicit confirmation where available.

### QA-DIAG-002 Clipboard Safety

Priority: P0

Steps:

1. Copy a non-sensitive proxy address.
2. Copy a sensitive profile link.
3. Copy a SOCKS password if the UI allows it.
4. Test on Android 13 or newer.

Expected result:

- Non-sensitive values copy normally.
- Sensitive values require explicit user intent.
- Sensitive clipboard content is marked sensitive on Android 13+.
- Clipboard success messages do not expose secrets.

### QA-DIAG-003 Share Cache Cleanup

Priority: P1

Steps:

1. Share diagnostics or export files repeatedly.
2. Restart the app.
3. Trigger share/export cleanup.

Expected result:

- Old cache export files are removed.
- New shared files are scoped through the configured provider.
- Shared filenames do not expose secrets.

### QA-DIAG-004 Log UI Cost

Priority: P1

Steps:

1. Start a noisy connection.
2. Leave logs open for several minutes.
3. Navigate away and back.
4. Stop the connection.

Expected result:

- Logs are batched and capped.
- Long lines are truncated safely.
- UI remains responsive.
- Stats polling stops when the UI no longer observes it.

### QA-DIAG-005 Support Screenshot Readability

Priority: P2

Steps:

1. Generate a failure that shows a diagnostic or status summary.
2. Take a screenshot on a phone-sized display.
3. Compress or share the screenshot through a messaging app if practical.
4. Inspect readability.

Expected result:

- The primary status, failing layer, and next action remain readable.
- No secret is visible in the screenshot by default.
- Users do not need to screenshot long raw logs for common support cases.

## Split Tunnel And App Picker

### QA-SPLIT-001 Large App List

Priority: P1

Steps:

1. Test on a device with many installed apps.
2. Open split-tunnel app selection.
3. Search for an app.
4. Toggle several apps.
5. Rotate the device or background/resume.

Expected result:

- Package scanning runs off the main thread.
- The list is lazy and responsive.
- Search filters quickly.
- Selection state survives recomposition and resume.

### QA-SPLIT-002 Runtime Split-Tunnel Behavior

Priority: P1

Steps:

1. Allow one app and exclude another.
2. Start VPN mode.
3. Test traffic from both apps.
4. Change inactive profile split-tunnel settings while connected.

Expected result:

- Active runtime uses the selected profile's split-tunnel settings.
- Inactive profile edits do not affect the active session.
- Changes to active runtime settings require reconnect or explicit apply behavior.

## UI, Accessibility, And Localization

### QA-UI-001 Repeated Navigation Visibility

Priority: P0

Steps:

1. Navigate repeatedly between main, profiles, logs, diagnostics, advanced settings,
   and dialogs.
2. Switch theme if available.
3. Background and resume the app.
4. Repeat while connected.

Expected result:

- Text remains visible.
- Labels do not disappear after navigation.
- UI state is coherent after resume.
- Runtime updates do not corrupt Compose state.

### QA-UI-002 Text Scaling And Wrapping

Priority: P1

Steps:

1. Set Android font scale to normal.
2. Set Android font scale to large.
3. Set Android font scale to the largest supported value.
4. Inspect settings, profiles, resolver import, logs, diagnostics, and dialogs.

Expected result:

- Text does not overlap.
- Buttons remain tappable.
- Long domain/profile/resolver names wrap cleanly.
- Critical warnings remain readable.

### QA-UI-003 Accessibility Basics

Priority: P1

Steps:

1. Use TalkBack or equivalent screen reader.
2. Navigate major controls.
3. Open dialogs and menus.
4. Use switch, checkbox, slider, and segmented controls.

Expected result:

- Controls have meaningful labels.
- Focus order is logical.
- Touch targets are large enough.
- Error messages are announced or reachable.
- Reduced-motion/device animation settings are respected where relevant.

### QA-UI-004 Persian And RTL Readiness

Priority: P2

Steps:

1. Switch device language to Persian if supported.
2. Inspect mixed Persian/English values such as domains, ports, and links.
3. Inspect warnings, validation errors, and diagnostics summaries.

Expected result:

- Text direction is readable.
- Domains, ports, and profile links remain selectable/copyable.
- Labels do not become ambiguous in mixed-direction content.

### QA-UI-005 Guide Readability

Priority: P1

Steps:

1. Open the guide tab.
2. Test normal, large, and maximum font scale.
3. Inspect every guide section.
4. Use a screen reader to navigate the guide.

Expected result:

- Guide text wraps without clipping or overlap.
- Section order is logical.
- Screen reader focus follows visual order.
- Parameter names are understandable without external documentation.

### QA-UI-006 Navigation Tab Capacity

Priority: P1

Steps:

1. Inspect the bottom navigation on narrow and wide phones.
2. Switch rapidly between Profiles, Connect, Logs, and Guide.
3. Test maximum font scale.

Expected result:

- Tab labels fit or truncate cleanly.
- Icons and labels remain tappable.
- Selected state is visually clear.
- Switching tabs does not reset unsaved profile edits unless the flow explicitly
  closes them.

## Release And Build Safety

### QA-RELEASE-001 Release Package Shape

Priority: P1

Steps:

1. Inspect generated release artifacts.
2. Confirm ABI-specific packages are produced as intended.
3. Confirm universal release APK is disabled unless intentionally enabled.

Expected result:

- Release output matches documented packaging policy.
- Debug/internal QA artifacts are not confused with public release artifacts.
- Native binaries exist for supported ABIs.

### QA-RELEASE-002 Native Binary Provenance

Priority: P0

Steps:

1. Build or obtain packaged native binaries.
2. Compare packaged StormDNS binary hashes with documented/pinned source.
3. Compare packaged tun2proxy binary hash with documented version/hash.

Expected result:

- Packaged binaries match expected hashes.
- Hashes are recorded in release notes or provenance documentation.
- Any mismatch blocks release until explained.

### QA-RELEASE-003 Dependency And CI Hygiene

Priority: P1

Steps:

1. Run unit tests in CI.
2. Run lint in CI.
3. Assemble debug in CI.
4. Run native binary smoke tests in CI.
5. Run the StormDNS boundary guard in CI.

Expected result:

- CI blocks accidental edits to upstream StormDNS files unless explicitly allowed.
- Dependency verification and locking policies are honored.
- Native build and Android build failures are visible before release.

## Security And Hostile Input

### QA-SEC-001 Hostile Import Files

Priority: P0

Steps:

1. Attempt to import files with misleading extensions.
2. Attempt to import archives, scripts, HTML files, binary files, and random data.
3. Attempt to import huge files.

Expected result:

- The app imports only supported formats.
- Unsupported files are rejected safely.
- File contents are never executed.
- Error messages do not echo sensitive contents.

### QA-SEC-002 Backup And Data Extraction

Priority: P0

Steps:

1. Inspect Android manifest backup settings.
2. Inspect backup and data extraction rules.
3. Attempt platform backup/data extraction where practical.

Expected result:

- App backup is disabled or strict rules exclude sensitive data.
- Settings, runtime state, logs, generated configs, resolver files, diagnostics,
  cache exports, and launch requests are excluded.
- No secret-bearing file is eligible for backup.

### QA-SEC-003 Cleartext Network Policy

Priority: P0

Steps:

1. Inspect network security config.
2. Attempt cleartext HTTP traffic from app-controlled code paths where practical.
3. Confirm debug/local exceptions only exist if intentionally documented.

Expected result:

- Cleartext is denied by default.
- Any debug/local exception is scoped and documented.
- Release builds do not allow unintended cleartext traffic.

## StormDNS Boundary Tests

### QA-STORM-001 Config Contract Fixtures

Priority: P0

Steps:

1. Render TOML for a single-domain profile.
2. Render TOML for a multi-domain profile.
3. Render TOML for control characters and escaped values.
4. Render resolver files for valid and invalid resolver profiles.

Expected result:

- TOML escapes control characters correctly.
- Android runtime startup mode never renders an interactive `ask` mode.
- Generated config uses the documented StormDNS contract.
- Fixture tests catch accidental config shape changes.

### QA-STORM-002 Version And Capability Detection

Priority: P1

Steps:

1. Run StormDNS version detection.
2. Cache detected version/capabilities.
3. Restart the app.
4. Replace the binary with a different supported test binary if practical.

Expected result:

- Version/capability detection uses the executable boundary.
- Cache is stored under no-backup storage.
- Optional telemetry parsing is enabled only when capability exists.
- Missing or malformed version output fails gracefully.

### QA-STORM-003 Upstream Edit Guard

Priority: P0

Steps:

1. Open a branch with no StormDNS upstream changes.
2. Open a branch that modifies files under `third_party/StormDNS`.
3. Open a branch that modifies `.gitmodules`.
4. Apply the intentional upstream-change label if supported by the repository.

Expected result:

- Normal branches pass.
- Accidental upstream modifications fail CI.
- `.gitmodules` changes are included in the guard.
- The intentional label overrides only detected upstream-change failures, not
  script errors or missing refs.

## Regression Checklist

Run this checklist before every public release:

- Fresh install opens cleanly.
- Upgrade install preserves profiles and encrypted secrets.
- Profile export text and QR are real and importable.
- Redacted diagnostics do not expose secrets.
- Proxy mode connects on loopback.
- LAN proxy mode requires complete authentication.
- VPN mode connects and disconnects cleanly.
- Network-change recovery is bounded and same-session.
- Startup does not remain stuck forever.
- Connected-but-no-traffic is reported as a failure or needs-attention state.
- Resolver imports deduplicate, validate, and respect limits.
- Inactive profiles can be edited while connected.
- Active runtime profile cannot be mutated silently.
- Split-tunnel app picker remains responsive.
- Logs and stats do not overwhelm the UI.
- Text remains visible after repeated navigation.
- Large font scale does not break critical screens.
- Backup/data extraction excludes sensitive files.
- Cleartext network policy is denied by default.
- Native binary provenance is verified.
- StormDNS boundary guard passes.

## Release Sign-Off Template

Use this template for manual QA sign-off:

```text
Build:
Device(s):
Android version(s):
Network(s):
Mode(s) tested: Proxy / VPN
Upgrade tested from:
P0 result:
P1 result:
Known accepted risks:
Diagnostics redaction verified:
Profile export/import verified:
Native provenance verified:
Signer:
Date:
```
