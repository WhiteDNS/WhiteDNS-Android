#!/usr/bin/env bash
set -euo pipefail

base_ref="${1:-origin/main}"
head_ref="${2:-HEAD}"

if ! git rev-parse --verify "${base_ref}" >/dev/null 2>&1; then
  echo "Base ref '${base_ref}' is not available." >&2
  exit 2
fi

if ! git rev-parse --verify "${head_ref}" >/dev/null 2>&1; then
  echo "Head ref '${head_ref}' is not available." >&2
  exit 2
fi

changed_files="$(
  git diff --name-only "${base_ref}...${head_ref}" -- 'third_party/StormDNS' '.gitmodules'
)"

if [[ -z "${changed_files}" ]]; then
  echo "No StormDNS upstream changes detected."
  exit 0
fi

echo "StormDNS upstream changes detected:" >&2
echo "${changed_files}" >&2
echo >&2
echo "WhiteDNS treats third_party/StormDNS as a black-box upstream engine." >&2
echo "Add the 'allow-stormdns-upstream' pull request label only when these changes are intentional upstream maintenance." >&2
exit 1
