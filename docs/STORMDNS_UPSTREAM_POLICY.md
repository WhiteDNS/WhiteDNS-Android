# StormDNS Upstream Policy

WhiteDNS treats `third_party/StormDNS` as a black-box upstream engine.

The Android app must integrate with StormDNS only through stable runtime boundaries:

- The packaged StormDNS executable.
- CLI flags passed to that executable.
- Generated TOML configuration files.
- Generated resolver files.
- stdout/stderr telemetry emitted by the process.
- Process exit codes.
- Version and capability detection from the executable.
- Files written by StormDNS under the WhiteDNS-owned working directory.

## Disallowed Coupling

WhiteDNS app features must not depend on:

- StormDNS internal Go packages.
- Private StormDNS source layout.
- Unexported Go symbols or package structure.
- Parsing upstream Go source files for app behavior.
- Editing upstream code to satisfy Android app feature needs.
- Runtime assumptions that require knowing StormDNS internals beyond documented executable behavior.

Build tooling may know where the upstream client command lives so it can compile the executable. App runtime code must not.

## Allowed Upstream Changes

Changes under `third_party/StormDNS` are allowed only when they are intentional upstream maintenance, such as:

- Updating the pinned submodule commit.
- Syncing a reviewed upstream fix.
- Refreshing native binaries from a documented upstream commit.

Any pull request that changes `third_party/StormDNS` or `.gitmodules` must include the label:

`allow-stormdns-upstream`

Without that label, CI fails before review to prevent accidental local edits to the upstream engine or its submodule metadata.

## Review Checklist

For any pull request touching StormDNS integration:

- Confirm app code interacts through the executable boundary only.
- Confirm generated TOML and resolver files are owned by WhiteDNS and live under app-controlled runtime directories.
- Confirm telemetry parsing is tolerant of unknown or changed output.
- Confirm optional behavior is gated by executable version/capability detection when needed.
- Confirm no Android app code imports, parses, or depends on StormDNS Go internals.
- Confirm `third_party/StormDNS` and `.gitmodules` changes are either absent or intentionally labeled and explained.
