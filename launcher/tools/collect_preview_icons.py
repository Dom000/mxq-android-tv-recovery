#!/usr/bin/env python3
"""Read installed APK metadata/icon ZIP entries via fixed-target ADB; never write to TV.

Only byte ranges for ZIP directories, AndroidManifest.xml, resources.arsc and the
selected icon cross ADB. No full APK pull, package launch, profile or screenshot.
"""
import base64
import hashlib
import importlib.util
import io
import json
from pathlib import Path
import re
import shlex
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location("tv", ROOT.parent / "scripts/diagnose.py")
tv = importlib.util.module_from_spec(spec)
spec.loader.exec_module(tv)
AAPT = Path.home() / "Library/Android/sdk/build-tools/35.0.0/aapt"
PACKAGES = {
    "YouTube": "com.google.android.youtube.tv", "Brave": "com.brave.browser",
    "KD Player": "org.com.kdplayer", "Netflix": "com.netflix.mediaclient",
    "Play Store": "com.android.vending", "Aptoide TV": "cm.aptoidetv.pt",
}


class RemoteApk(io.RawIOBase):
    def __init__(self, path):
        self.path = shlex.quote(path)
        code, data, _ = tv.remote("stat -c %s " + self.path)
        if code: raise RuntimeError("Cannot stat APK")
        self.size = int(data.strip()); self.offset = 0; self.transferred = 0

    def seekable(self): return True
    def readable(self): return True
    def tell(self): return self.offset
    def seek(self, offset, whence=io.SEEK_SET):
        self.offset = offset if whence == io.SEEK_SET else self.offset + offset if whence == io.SEEK_CUR else self.size + offset
        if self.offset < 0: raise ValueError("negative APK offset")
        return self.offset

    def read(self, size=-1):
        size = min(self.size - self.offset, size if size >= 0 else self.size - self.offset)
        if size <= 0: return b""
        if size > 16 * 1024 * 1024 or self.transferred + size > 48 * 1024 * 1024:
            raise RuntimeError("Metadata read exceeds bounded diagnostic budget")
        # Read-only regular APK file; deliberately no dd 'of', block device or TV write.
        block = 4096; start = self.offset // block; prefix = self.offset % block
        blocks = (prefix + size + block - 1) // block
        command = f"dd if={self.path} bs={block} skip={start} count={blocks} 2>/dev/null | base64"
        code, data, _ = tv.remote(command)
        if code: raise RuntimeError("APK range read failed")
        decoded = base64.b64decode(b"".join(data.split()), validate=True)[prefix:prefix + size]
        if len(decoded) != size: raise RuntimeError("Truncated APK range")
        self.offset += size; self.transferred += size
        return decoded


def dump(*args):
    return subprocess.check_output([str(AAPT), "dump", *map(str, args)], stderr=subprocess.PIPE).decode(errors="replace")


def icon_entry(apk):
    manifest = dump("xmltree", apk, "AndroidManifest.xml")
    application = manifest.split("E: application", 1)[1]
    icon = re.search(r"A: [^\n]*:icon[^\n]*=@(0x[0-9a-f]+)", application).group(1)
    resources = dump("--values", "resources", apk)
    entries = {}; config = ""; current = None
    for line in resources.splitlines():
        match = re.match(r"\s+config (.*):$", line)
        if match: config = match.group(1); current = None; continue
        match = re.match(r"\s+resource (0x[0-9a-f]+) .* t=(0x[0-9a-f]+) d=(0x[0-9a-f]+)", line)
        if match:
            current = match.group(1)
            if match.group(2) == "0x01": entries.setdefault(current, []).append((config, match.group(3), True))
            continue
        match = re.search(r'\(string(?:8|16)\) "([^"\n]+)"', line)
        if match and current: entries.setdefault(current, []).append((config, match.group(1), False))

    def rank(entry):
        config, _, _ = entry
        version = re.search(r"(?:^|-)v(\d+)", config)
        api = int(version.group(1)) if version else 0
        density = 0 if re.search(r"(?:^|-)mdpi(?:-|$)", config) else 1 if not config else 2
        return (api > 25, density, api)

    def resolve(key, seen):
        if key in seen: return None
        for config, value, alias in sorted(entries.get(key, []), key=rank):
            version = re.search(r"(?:^|-)v(\d+)", config)
            if version and int(version.group(1)) > 25: continue
            if alias:
                result = resolve(value, seen | {key})
                if result: return result
            elif value.startswith("res/") and not value.endswith(".xml"): return value
        return None
    result = resolve(icon, set())
    if not result: raise RuntimeError("No API-25 raster icon found")
    return result


def main():
    tv.guard()
    destination = ROOT / "preview/icons"; destination.mkdir(exist_ok=True)
    previous = json.loads((destination / "catalog.json").read_text()) if (destination / "catalog.json").exists() else []
    cache = {entry["package"]: entry for entry in previous}
    records = []
    for label, package in PACKAGES.items():
        old = cache.get(package)
        if old and (destination / old["file"]).exists() and hashlib.sha256((destination / old["file"]).read_bytes()).hexdigest() == old["sha256"]:
            records.append(old); print("Using collected icon: " + label, flush=True); continue
        print("Inspecting " + label, flush=True)
        code, data, _ = tv.remote("pm path " + package)
        paths = [line.removeprefix("package:") for line in data.decode().splitlines() if line.startswith("package:")]
        if code or not paths: raise RuntimeError("Package path unavailable: " + package)
        path = next((p for p in paths if p.endswith("/base.apk")), paths[0])
        cached = ROOT.parent / "diagnostics/publisher-apk-inspection/BraveMonoarm-1.60.127.apk"
        source = zipfile.ZipFile(cached) if package == "com.brave.browser" else zipfile.ZipFile(RemoteApk(path))
        with source:
            metadata = tv.OUT / (package + "-metadata.apk")
            with zipfile.ZipFile(metadata, "w") as local:
                for name in ["AndroidManifest.xml", "resources.arsc"]:
                    local.writestr(name, source.read(name))
            entry = icon_entry(metadata)
            image = source.read(entry)
        extension = "png" if image.startswith(b"\x89PNG") else "webp" if image.startswith(b"RIFF") else "jpg"
        name = package + "." + extension
        (destination / name).write_bytes(image)
        records.append(dict(label=label, package=package, file=name, resource=entry,
            sha256=hashlib.sha256(image).hexdigest(), bytes=len(image)))
        (destination / "catalog.json").write_text(json.dumps(records, indent=2) + "\n")
        print(f"Saved {label}: {entry}, {len(image)} bytes", flush=True)
    (destination / "catalog.json").write_text(json.dumps(records, indent=2) + "\n")
    (ROOT / "preview/app-icons.js").write_text("// Extracted read-only from installed APK resources.\nwindow.APP_ICONS = "
        + json.dumps({r["label"]: "icons/" + r["file"] for r in records}, indent=2) + ";\n")
    print("Evidence: " + str(tv.OUT))


if __name__ == "__main__": main()
