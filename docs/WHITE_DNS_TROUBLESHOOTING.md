# WhiteDNS Troubleshooting Guide

Use this guide when WhiteDNS starts but traffic does not work, startup appears
stuck, upload looks unexpectedly high, or the app disconnects in the background.

If you are new to WhiteDNS, read the [User Guide](WHITE_DNS_USER_GUIDE.md) first.
This troubleshooting guide assumes you know which connection profile, resolver
profile, and runtime mode are active.

The most important rule is to change one layer at a time. Test server, resolvers,
local route, and network separately.

## Before You Start

Collect these safe details:

- Runtime mode: Proxy or VPN.
- Active connection profile name.
- Active resolver profile name and accepted resolver count.
- Whether the issue happens on Wi-Fi, mobile data, or both.
- Whether route verification passes.
- Redacted diagnostics output.

Do not share:

- Encryption keys.
- SOCKS passwords.
- Real profile links.
- Profile QR codes.
- Raw logs that may contain secrets or local runtime paths.

## Connected But Nothing Loads

This usually means one runtime layer started, but the final user-traffic path is
not healthy.

### 1. Check The Server

- Confirm the domain is correct.
- Confirm the encryption key matches the server.
- If the profile has multiple domains, test a single known-good domain.
- Confirm the server is not expired, exhausted, overloaded, or unreachable.

Expected result:

- A bad server must be fixed before resolver or MTU changes can help.

### 2. Check Resolvers

- Switch to a smaller known-good resolver list.
- Remove duplicates.
- Remove private, loopback, or link-local addresses unless you are intentionally
  testing a local resolver.
- Remove invalid rows and mixed-format lines.

Expected result:

- Startup becomes easier to interpret.
- Resolver errors decrease.

### 3. Check The Local Route

Proxy mode:

- Use the exact proxy address shown in WhiteDNS.
- Do not mix up SOCKS and HTTP bridge ports.
- If listening on a LAN-reachable address, enable SOCKS authentication with a
  nonblank username and password.

VPN mode:

- Confirm Android VPN permission is granted.
- Confirm the target app is included by split tunnel.
- Confirm notifications and foreground service behavior are allowed.

Expected result:

- If route verification fails, the session should be treated as not ready.

### 4. Check The Network

- Test Wi-Fi and mobile data separately.
- Try a safer MTU such as `1280`.
- Try blocking IPv6 if leak prevention and compatibility matter.
- Disable aggressive warmup while isolating the problem.

## Startup Gets Stuck

Common causes:

- Slow or broken resolvers.
- Unreachable server.
- Timeout values that wait too long.
- Too many retries.
- Large resolver lists.
- Network filtering.

Recommended path:

1. Use a smaller resolver list.
2. Set warmup to Startup-only.
3. Use conservative MTU values.
4. Test another network.
5. Generate redacted diagnostics if the same stage keeps failing.

Expected result:

- Startup should not stay stuck forever.
- The app should show the layer that failed or timed out.

## Upload Is High But Download Is Low

Upload can come from DNS tunnel requests, resolver probing, warmup, keepalive,
retries, duplication, fragments, and real user traffic.

Check these first:

1. Warmup mode.
   - Off sends no deliberate warmup traffic.
   - Startup-only is the low-cost default.
   - Aggressive is intentionally more expensive.

2. Resolver quality.
   - Broken resolvers cause retries.
   - Huge lists can increase probing and log noise.

3. Server health.
   - If the server cannot return useful traffic, upload may continue while download
     remains low.

4. Duplication and fragments.
   - Higher duplication and more fragments increase request count.

Recommended action:

- Use Startup-only warmup.
- Use a smaller clean resolver list.
- Keep duplication conservative.
- Treat Aggressive warmup as a short test mode, not a default.

## Disconnects In The Background

Common causes:

- Android battery restrictions.
- Notifications disabled for the foreground service.
- Network changes between Wi-Fi and mobile data.
- Device-specific background limits.
- VPN service being stopped by the platform.

Recommended path:

1. Disable battery optimization for WhiteDNS where appropriate.
2. Allow notifications for VPN foreground service use.
3. Test Wi-Fi and mobile data switching.
4. In VPN mode, review Always-on VPN and block-without-VPN settings.
5. Check whether the app recovers with the same runtime session.

Expected result:

- Recovery should be debounced and bounded.
- Stale network callbacks should not restart a stopped session.

## Resolver Errors

Symptoms:

- Slow startup.
- Repeated errors in logs.
- Low download.
- Connected state without usable traffic.

Recommended action:

- Deduplicate the resolver list.
- Remove invalid rows.
- Remove private and loopback addresses unless intentionally used.
- Test a smaller resolver profile first.
- Avoid changing server, MTU, and warmup at the same time.

## Multi-Domain Problems

Accepted practical formats:

```text
example.com
backup.example.com
```

or:

```text
example.com, backup.example.com
```

Recommended path:

1. Test each domain separately.
2. Remove extra quotes, brackets, and separators.
3. Confirm every domain belongs to the same intended server setup.
4. Export and import the profile to confirm all domains survive round trip.

## Text Or Labels Disappear

Recommended path:

1. Navigate repeatedly between Connect, Profiles, Logs, and Guide.
2. Increase Android font scale.
3. Background and resume the app.
4. Repeat while connected.

Expected result:

- Text stays visible.
- Long values wrap or truncate cleanly.
- Runtime updates do not corrupt UI state.

## What To Send For Support

Send:

- Redacted diagnostics.
- Runtime mode.
- Network type.
- Route verification result.
- Resolver count and whether the list was recently changed.
- App version.

Do not send:

- Encryption keys.
- SOCKS passwords.
- Real profile links.
- Profile QR codes.
- Unredacted raw logs.
