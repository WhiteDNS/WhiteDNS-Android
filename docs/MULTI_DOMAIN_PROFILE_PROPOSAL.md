# Multiple Domains in One Connection Profile

This is a proposal for supporting two server domains inside one WhiteDNS connection profile.

## Maintainer Question

Should a single profile be allowed to carry a primary and secondary StormDNS domain when both domains use the same encryption key and encryption method?

Today users can create two connection profiles, but that duplicates the same resolver profile, connection mode, and advanced settings choice. For users who receive a primary and backup domain for the same WhiteDNS server, that feels like two profiles for one real connection.

The important boundary is the encryption model:

- If both domains share one encryption key and method, WhiteDNS can probably support this at the Android app layer by rendering both domains into the existing `DOMAINS = [...]` TOML field.
- If each domain can have a different encryption key or method, this should not be squeezed into the current profile shape. That would need an upstream-supported multi-endpoint format, because the current generated client TOML has one `ENCRYPTION_KEY` and one `DATA_ENCRYPTION_METHOD`.

## Current App Shape

WhiteDNS currently treats the selected server as one domain:

- `ConnectionProfile` stores `customServerDomain`, `customServerEncryptionKey`, and `customServerEncryptionMethod`.
- `StormDnsServerProfile` stores one `domain`, one `encryptionKey`, and one `encryptionMethod`.
- `StormDnsConfigRenderer` renders `DOMAINS = ["server.example.com"]`, so the TOML field is already an array but only receives one item.
- profile links export/import one `server.domain`.
- runtime launch request storage, the QS tile, proxy service, VPN service, scan service, and profile editor all follow that single-domain shape.

## Recommended Implementation

Keep this as a small "up to two domains, one key" feature first.

Suggested data model:

```kotlin
data class ConnectionProfile(
    ...
    val customServerDomain: String = "",
    val customServerSecondaryDomain: String = "",
    val customServerEncryptionKey: String = "",
    val customServerEncryptionMethod: Int = 1,
    ...
)
```

Add one helper instead of spreading normalization rules through the app:

```kotlin
fun ConnectionProfile.normalizedServerDomains(): List<String> {
    return listOf(customServerDomain, customServerSecondaryDomain)
        .map { it.trim().trimEnd('.') }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
        .take(2)
}
```

Then update `StormDnsServerProfile` to carry the normalized domain list while keeping the first domain as the display/compatibility fallback:

```kotlin
data class StormDnsServerProfile(
    val id: String,
    val label: String,
    val domain: String,
    val encryptionKey: String,
    val encryptionMethod: Int,
    val domains: List<String> = listOf(domain),
)
```

`StormDnsConfigRenderer` can render:

```toml
DOMAINS = ["primary.example.com", "backup.example.com"]
DATA_ENCRYPTION_METHOD = 1
ENCRYPTION_KEY = "shared-key"
```

This keeps the change inside WhiteDNS' existing StormDNS executable boundary and does not require editing `third_party/StormDNS`.

## UI Guide

In the connection profile editor:

- keep the existing "Server domain" field as the primary domain;
- add an optional "Backup domain" field below it;
- show profile summaries as `primary.example.com +1` when both are set;
- keep connect/scan disabled until at least the primary domain and encryption key are present;
- reject a secondary domain that normalizes to the same value as the primary domain.

This avoids turning the profile editor into a server-pool manager while still covering the two-domain user request.

## Import and Export

For profile links, keep old links working and make new links degrade cleanly:

- continue reading existing payloads with `server.domain`;
- accept an optional `server.domains` array;
- when exporting a one-domain profile, keep the current payload shape;
- when exporting a two-domain profile, include both `server.domain` for older clients and `server.domains` for newer clients.

Example:

```json
{
  "schema": "whitedns.profile",
  "version": 1,
  "profile": {
    "name": "Main",
    "server": {
      "domain": "primary.example.com",
      "domains": ["primary.example.com", "backup.example.com"],
      "encryption_key": "shared-key",
      "encryption_method": 1
    }
  }
}
```

Older WhiteDNS builds will ignore `server.domains` and import the primary domain. Newer builds can preserve both.

## Tests to Add

- settings store round-trips the secondary domain;
- profile normalization deduplicates primary/secondary domains case-insensitively;
- client and scan TOML render one or two domains correctly;
- profile link import accepts old `domain` payloads and new `domains` payloads;
- export all profiles preserves two-domain profiles;
- QS tile, proxy, VPN, and scan launch requests carry both domains to the renderer.

## Interim User Guide

Until this lands, users who have two domains should create two connection profiles with the same encryption key and resolver profile:

1. create/import the first profile for the primary domain;
2. duplicate the settings manually into a second profile for the backup domain;
3. switch profiles when one domain is unavailable.

That is more awkward than a single profile with two domains, but it avoids mixing domains with different encryption settings before the intended server model is confirmed.
