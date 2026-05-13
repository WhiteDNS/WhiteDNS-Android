# WhiteDNS User Guide

This guide explains WhiteDNS from a user-facing point of view: what each major
setting means, how it affects connection quality, and what to check when a session
looks connected but traffic does not work.

## Who This Guide Is For

Use this guide if you are setting up WhiteDNS for the first time, importing a
profile, changing resolver lists, choosing between Proxy and VPN mode, or trying
to understand why a session can look connected while traffic still does not load.

You do not need to understand StormDNS internals to use WhiteDNS. Think in layers:
server, resolvers, local route, and network.

Related documents:

- [Troubleshooting Guide](WHITE_DNS_TROUBLESHOOTING.md)
- [QA Test Plan](WHITE_DNS_FIELD_QA.md)
- [Persian User Guide](fa/WHITE_DNS_USER_GUIDE.md)

## Quick Start

Use this order for a clean setup:

1. Create a connection profile.
   - Enter the server domain or domains.
   - Enter the encryption key.
   - Use a clear name so you can identify the profile later.

2. Create a resolver profile.
   - Start with a small, clean resolver list.
   - Avoid huge lists until you know the server and route are healthy.
   - Remove duplicates, invalid rows, private addresses, loopback addresses, and
     resolvers that do not work on your network.

3. Choose a runtime mode.
   - Use Proxy mode when another app can connect to a local proxy address.
   - Use VPN mode when Android app traffic should route through WhiteDNS directly.

4. Connect and read the status.
   - StormDNS startup is only one layer.
   - Resolver activity is another layer.
   - The local route check confirms whether user traffic can actually leave through
     the proxy or VPN path.

Keep one rule while testing: change only one layer at a time. If you change the
server, resolver list, MTU, warmup, and proxy app together, you will not know which
change fixed or broke the session.

## Glossary

| Term | Meaning |
| --- | --- |
| Connection profile | Saved server route and encryption settings. |
| Resolver profile | Saved DNS resolver list used to carry tunnel packets. |
| Proxy mode | Starts a local proxy address for another app to use. |
| VPN mode | Uses Android VPN routing so selected apps can use WhiteDNS directly. |
| Route check | Verifies that user traffic can actually leave through the proxy or VPN path. |
| Warmup | Optional startup or keepalive traffic used to prepare the tunnel. |
| Diagnostics | Redacted support output for troubleshooting. |

## Connection Profile

### Domain(s)

The domain field tells WhiteDNS which public server route StormDNS should use.
Multiple domains can help with fallback, but each domain must be valid and must
belong to the same intended setup.

Accepted practical formats include:

```text
example.com
backup.example.com
```

or:

```text
example.com, backup.example.com
```

User impact:

- A wrong domain can cause startup failure or a session that appears active but
  does not carry useful traffic.
- Multiple domains can improve resilience only when each domain is valid.
- Invalid domains should be fixed before connection.

### Encryption Key

The encryption key is a shared secret between the client and server. If it is
wrong, the tunnel cannot carry useful traffic even if the app is able to start the
runtime process.

User impact:

- The key must match the server.
- The key must not appear in logs, screenshots, diagnostics, or public messages.
- Profile export links and QR codes contain this secret.

### Encryption Method

The encryption method must match the server configuration.

User impact:

- A mismatched method can cause failed or useless sessions.
- Changing it randomly is unlikely to fix a resolver or network problem.

## Resolver Profile

Resolvers carry the DNS tunnel packets. Resolver quality is often more important
than resolver count.

Large lists can contain:

- Duplicate rows.
- Dead or slow resolvers.
- Private, loopback, or link-local addresses that do not make sense on the user's
  network.
- Resolver addresses that work only on one operator or region.
- Invalid lines copied from mixed sources.

User impact:

- Startup can become slower.
- Upload can increase due to retries and probing.
- The app can look connected while traffic does not work.
- Logs become harder to understand.

Recommended approach:

- Start with a smaller trusted list.
- Save resolver profiles by network or source when testing.
- Change one thing at a time: server, resolver list, local route, or network.

## Proxy Mode

Proxy mode starts a local SOCKS proxy and, when enabled, an HTTP proxy bridge.

### Listen IP

The listen IP controls who can reach the proxy.

- Loopback, such as `127.0.0.1`, keeps the proxy on this device.
- A LAN-reachable address can expose the proxy to other devices on the network.

User impact:

- Loopback is safest for single-device use.
- LAN-reachable listeners must require a nonblank SOCKS username and password.
- The companion app must use the exact address shown by WhiteDNS.

### SOCKS Port

The SOCKS port is the local port used by SOCKS-capable companion apps.

User impact:

- If another app already owns the port, proxy startup should fail cleanly.
- A wrong port in the companion app produces "connected but no traffic" symptoms.

### HTTP Proxy Bridge

The HTTP bridge lets HTTP-proxy clients reach the SOCKS tunnel.

User impact:

- It improves compatibility with some apps.
- It adds resource use and must enforce limits for clients, headers, hosts, and
  idle tunnels.
- Users must not confuse the HTTP bridge port with the SOCKS port.

### SOCKS5 Authentication

SOCKS5 authentication protects the proxy with a username and password.

User impact:

- Loopback-only proxy use can be safe without authentication because only the same
  device can reach it.
- LAN-reachable proxy listeners must require a nonblank username and password.
- Do not share proxy passwords in screenshots, logs, diagnostics, or public
  messages.

## VPN Mode

VPN mode uses Android `VpnService` to route selected app traffic through WhiteDNS.
It is convenient, but it depends on Android VPN permission, foreground service
behavior, battery rules, split tunnel settings, MTU, IPv6 behavior, and network
changes.

### Split Tunnel

Split tunnel decides which apps use the VPN path.

User impact:

- If an app is excluded, it will not use WhiteDNS even when WhiteDNS is connected.
- Editing inactive profiles is safe while another profile is connected.
- Active runtime changes should require reconnect or a clear apply action.

### IPv6 Strategy

IPv6 behavior can affect leaks and compatibility.

- Block is safest when leak prevention matters.
- Bypass can improve compatibility but means IPv6 traffic does not use the tunnel.
- Experimental route should be treated as network-specific.

### VPN MTU

MTU controls packet size in the VPN path.

User impact:

- `1280` favors compatibility.
- `1400` is a practical balance.
- `1500` can be faster on clean networks but may fail on filtered or fragile paths.
- Custom values should be used only with a clear reason.

## Traffic Warmup

Warmup and keepalive can make startup smoother, but they can also create visible
upload and battery cost.

### Off

No deliberate warmup traffic.

Best for:

- Lowest background traffic.
- Lowest battery use.

### Startup-only

Sends startup probes and then stops.

Best for:

- Low-cost default behavior.
- Users who do not want ongoing keepalive traffic.

### Balanced

Keeps the tunnel warm when needed and should skip keepalive after recent real
traffic.

Best for:

- Networks that are somewhat unstable.
- Users who want a balance between reliability and traffic cost.

### Aggressive

Uses more warmup/keepalive behavior.

Best for:

- Difficult networks.
- Short tests where the user accepts extra upload, resolver load, and battery use.

## Advanced Tuning

### Packet Size And MTU Ranges

Min/max upload and download values shape packet size.

User impact:

- Smaller values usually improve compatibility.
- Larger values can improve throughput on clean paths.
- Values outside the supported range should be blocked by validation.

### Duplication

Duplication repeats packets to tolerate loss.

User impact:

- Can help unstable resolvers.
- Increases upload and resolver load.

### Compression

Compression reduces payload size when data is compressible.

User impact:

- Can reduce traffic for some workloads.
- Costs CPU and battery.
- Helps less when app traffic is already compressed.

### DNS Fragments

Fragments split data into DNS-sized pieces.

User impact:

- More fragments can improve compatibility with size limits.
- Too many fragments increase request count, upload, and startup time.

### Workers, Queues, Retries, And Timeouts

These settings control concurrency and patience.

User impact:

- More workers can improve throughput on strong devices but cost CPU and battery.
- Larger queues can smooth bursts but increase memory use and delay failures.
- More retries can help packet loss but increase traffic on broken paths.
- Shorter timeouts fail faster; longer timeouts tolerate slow networks.

## Status And Metrics

### Startup Progress

Shows which startup layer is active. It should eventually become ready, fail, or
time out with a clear message.

### Resolver State

Shows whether resolvers are active and valid. Bad resolver lists can mimic server
failure.

### Route Check

Confirms that user traffic can actually leave through the local proxy or VPN path.
If this fails, the app should not claim the session is ready.

### Upload And Download

Upload can include DNS requests, warmup, keepalive, retries, and real user traffic.
High upload with little or no download usually means you should check:

- Server health.
- Resolver quality.
- Route check.
- Warmup mode.
- Duplication and fragment settings.

## Diagnostics And Logs

Diagnostics and logs are for troubleshooting. They should help explain what is
happening without exposing credentials.

Safe diagnostics should redact:

- Server encryption keys.
- SOCKS usernames and passwords when sensitive.
- Profile links and QR payloads.
- TOML secrets.
- Runtime launch paths.
- Generated resolver/config paths.
- Raw log lines that contain sensitive values.

Logs can be useful, but long raw logs are not the best support format. Prefer a
redacted diagnostics summary when asking for help.

## Export And QR

Profile export and diagnostics serve different purposes.

Profile export:

- Must remain the real importable value.
- Includes connection secrets.
- Should show a clear warning before copy, share, or QR display.
- Should be shared only with trusted people or trusted devices.

QR export:

- Encodes the same real profile value.
- Is just as sensitive as the text export.
- Should not be posted publicly.

## Recommended Troubleshooting Order

When traffic does not work, check one layer at a time:

1. Server: domain, key, capacity, and validity.
2. Resolvers: small clean list, no duplicates, no private/local rows.
3. Local route: proxy address/port or VPN split tunnel.
4. Network: Wi-Fi vs mobile data, operator behavior, IPv6, MTU, battery rules.

Avoid changing many advanced settings at once. If several settings change together,
it becomes impossible to know which change helped or hurt.
