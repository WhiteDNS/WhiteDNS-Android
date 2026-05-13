# Android Foreground Service Types

WhiteDNS uses two foreground services and keeps their declared types narrow:

- `WhiteDnsProxyService` uses `dataSync` because proxy mode runs a user-initiated network transfer service without owning the Android VPN interface.
- `WhiteDnsVpnService` uses `systemExempted` because it extends `VpnService`, is protected by `android.permission.BIND_VPN_SERVICE`, and must keep the device VPN interface alive for Always-on VPN and Block connections without VPN behavior.

The VPN service should not be changed to `dataSync`, `connectedDevice`, or `specialUse` unless Android platform policy changes or release testing proves that the replacement works for configured VPN apps on the supported SDK range. If the target SDK or Play policy changes, re-check the Android foreground service type documentation before release.

References:

- Android foreground service types: https://developer.android.com/develop/background-work/services/fgs/service-types
- Android VPN apps and `VpnService`: https://developer.android.com/develop/connectivity/vpn
