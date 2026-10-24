#!/usr/bin/env python3
"""Offline size + SHA-256 verification against a previously recorded manifest."""
import hashlib
import json
from pathlib import Path
import re
import sys

def main():
    if len(sys.argv) != 2:
        raise SystemExit("Usage: verify-backup.sh /path/to/manifest.json")
    manifest = Path(sys.argv[1]).resolve()
    entries = json.loads(manifest.read_text()).get("images")
    if not isinstance(entries, list) or not entries:
        raise SystemExit("FAIL: manifest has no images; no backup has been verified.")
    seen = set()
    for item in entries:
        relative = Path(item["filename"])
        expected_size = item["expected_bytes"]
        expected_hash = item["sha256"]
        if relative.is_absolute() or ".." in relative.parts or relative in seen:
            raise SystemExit("FAIL: invalid or duplicate manifest filename")
        seen.add(relative)
        if type(expected_size) is not int or expected_size <= 0 or not re.fullmatch(r"[a-fA-F0-9]{64}", expected_hash):
            raise SystemExit("FAIL: invalid expected size or SHA-256")
        file = manifest.parent / relative
        if not file.resolve().is_relative_to(manifest.parent) or not file.is_file() or file.is_symlink():
            raise SystemExit(f"FAIL: missing or unsafe image {relative}")
        if file.stat().st_size != expected_size:
            raise SystemExit(f"FAIL: {relative} size differs from {expected_size} bytes")
        digest = hashlib.sha256()
        with file.open("rb") as stream:
            for chunk in iter(lambda: stream.read(4 * 1024 * 1024), b""):
                digest.update(chunk)
        if digest.hexdigest() != expected_hash.lower():
            raise SystemExit(f"FAIL: {relative} SHA-256 mismatch")
        source_hash = item.get("source_sha256")
        if source_hash and source_hash.lower() != digest.hexdigest():
            raise SystemExit(f"FAIL: {relative} differs from source SHA-256")
        print(f"PASS: {relative}: size + saved SHA-256" + (" + source SHA-256" if source_hash else " (no source digest supplied)"))
    print("File integrity only. This does not prove filesystem consistency or recoverability.")

if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, TypeError) as error:
        raise SystemExit(f"FAIL: {error}")
