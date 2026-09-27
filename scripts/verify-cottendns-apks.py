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
architectures = {"arm64-v8a": "arm64", "armeabi-v7a": "arm", "x86": "386", "x86_64": "amd64"}
abis = set(architectures)
expected = {}
for abi in sorted(abis):
    binary = root / "app/src/main/jniLibs" / abi / "libcottendns_client.so"
    metadata = subprocess.check_output(["go", "version", "-m", str(binary)], text=True)
    raw = binary.read_bytes()
    # -trimpath omits linker flags, and automatic VCS stamping can be absent.
    # The explicit -X BuildVersion string is null-terminated by the Go linker.
    if pin.encode("ascii") + b"\x00" not in raw:
        raise SystemExit(f"{abi}: explicit core version stamp does not match {pin}\n{metadata}")
    settings = set(metadata.splitlines())
    required = {"\tpath\tcottendns-go/cmd/client", "\tbuild\tGOOS=android",
                "\tbuild\tGOARCH=" + architectures[abi]}
    if not required.issubset(settings):
        raise SystemExit(f"{abi}: wrong native core target\n{metadata}")
    expected[abi] = hashlib.sha256(raw).digest()
    print(f"Native {abi}: verified version stamp and Android target at {pin}")

if sys.argv[1:] == ["--native-only"]:
    raise SystemExit(0)

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
