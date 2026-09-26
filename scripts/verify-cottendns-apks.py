"""Reject APKs containing stale CottenDNS executables or missing engines."""
import hashlib
from pathlib import Path
import subprocess
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
pin = subprocess.check_output(
    ["git", "-C", str(root / "third_party/CottenDns"), "rev-parse", "HEAD"], text=True
).strip()
abis = {"arm64-v8a", "armeabi-v7a", "x86", "x86_64"}
expected = {}
for abi in sorted(abis):
    binary = root / "app/src/main/jniLibs" / abi / "libcottendns_client.so"
    metadata = subprocess.check_output(["go", "version", "-m", str(binary)], text=True)
    # Go omits linker flags from build info with -trimpath; VCS records the source pin.
    if "vcs.revision=" + pin not in metadata:
        raise SystemExit(f"{abi}: core build does not identify pinned commit {pin}")
    expected[abi] = hashlib.sha256(binary.read_bytes()).digest()

apks = sorted((root / sys.argv[1]).glob("*.apk"))
if not apks:
    raise SystemExit("No APKs found")
seen = set()
for apk in apks:
    with zipfile.ZipFile(apk) as archive:
        names = set(archive.namelist())
        packaged = {abi for abi in abis if f"lib/{abi}/libcottendns_client.so" in names}
        if not packaged:
            raise SystemExit(f"{apk.name}: CottenDNS core missing")
        if "universal" in apk.name and packaged != abis:
            raise SystemExit(f"{apk.name}: universal APK lacks an ABI")
        for abi in sorted(packaged):
            if f"lib/{abi}/libstormdns_client.so" not in names:
                raise SystemExit(f"{apk.name}: StormDNS core missing for {abi}")
            raw = archive.read(f"lib/{abi}/libcottendns_client.so")
            if hashlib.sha256(raw).digest() != expected[abi]:
                raise SystemExit(f"{apk.name}: stale or modified CottenDNS core for {abi}")
            seen.add(abi)
        print(f"{apk.name}: verified {', '.join(sorted(packaged))} at {pin}")
if seen != abis:
    raise SystemExit(f"APK outputs missing ABIs: {sorted(abis - seen)}")
