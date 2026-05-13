# WhiteDNS Documentation

This directory contains public documentation for WhiteDNS users, contributors, QA
testers, and release reviewers.

## User Documentation

- [User Guide](WHITE_DNS_USER_GUIDE.md): explains profiles, resolvers, proxy mode,
  VPN mode, warmup, advanced tuning, metrics, export, and diagnostics.
- [Troubleshooting Guide](WHITE_DNS_TROUBLESHOOTING.md): gives a step-by-step path
  for connected-but-no-traffic, startup stalls, high upload, background disconnects,
  resolver errors, multi-domain issues, and UI visibility problems.

## QA And Release Documentation

- [QA Test Plan](WHITE_DNS_FIELD_QA.md): executable release and regression test
  plan for Android behavior, security, storage, diagnostics, proxy, VPN, UI,
  StormDNS boundary checks, and release sign-off.
- [Foreground Service Types](ANDROID_FOREGROUND_SERVICE_TYPES.md): Android
  foreground-service type notes.

## Persian Documentation

- [مستندات فارسی](fa/README.md)
- [راهنمای کاربر فارسی](fa/WHITE_DNS_USER_GUIDE.md)
- [راهنمای عیب‌یابی فارسی](fa/WHITE_DNS_TROUBLESHOOTING.md)
- [برنامه QA فارسی](fa/WHITE_DNS_QA.md)

## Documentation Rules

- Do not include real server keys, SOCKS passwords, profile links, QR payloads,
  raw logs, private resolver lists, or user-identifying support data.
- Explain settings by user impact: reliability, speed, traffic cost, battery,
  privacy, and exposure risk.
- Keep troubleshooting layered: server, resolvers, local route, then network.
- Keep StormDNS integration documentation limited to the public executable
  boundary.
