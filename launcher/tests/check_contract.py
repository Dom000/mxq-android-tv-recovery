"""Verify the state-changing Home capability stays disabled in the shipped APK source."""
from pathlib import Path
import hashlib
import json
import re
import xml.etree.ElementTree as ET
import zipfile

root = Path(__file__).resolve().parents[1]
manifest = ET.parse(root / "AndroidManifest.xml").getroot()
a = "{http://schemas.android.com/apk/res/android}"
permissions = {entry.get(a + "name") for entry in manifest.findall("uses-permission")}
assert permissions == {"android.permission.ACCESS_NETWORK_STATE", "android.permission.INTERNET"}, "only connection status and requested online wallpapers"
assert manifest.find("uses-sdk").get(a + "minSdkVersion") == "25"
alias = manifest.find("application/activity-alias")
assert alias.get(a + "enabled") == "false", "Home entry must require separate opt-in"
assert alias.get(a + "name") == ".HomeEntry"
assert manifest.find("application/activity").get(a + "exported") == "true"
native = (root / "src/dev/mxq/home/Tokens.java").read_text()
preview = (root / "preview/index.html").read_text()
for key in ["BACKGROUND", "SURFACE", "TEXT", "MUTED", "FOCUS", "SUN", "RIDGE", "FOREGROUND"]:
    color = re.search(rf"{key} = 0xff([0-9a-f]{{6}})", native).group(1)
    assert re.search(rf"--{key.lower()}:\s*#{color}\b", preview, re.I), f"preview token drift: {key}"
print("Manifest gate, API floor, network/Internet-only permissions and eight preview color tokens passed")
catalog = json.loads((root / "assets/wallpapers/catalog.json").read_text())
assert len(catalog["wallpapers"]) == 4
for entry in catalog["wallpapers"]:
    image = root / "assets/wallpapers" / entry["file"]
    assert hashlib.sha256(image.read_bytes()).hexdigest() == entry["sha256"], entry["file"]
    assert entry["sourcePage"].startswith("https://")
print("Four bundled publisher images match their provenance hashes")
with zipfile.ZipFile(root / "build/EveningHome-preview.apk") as apk:
    assert "classes.dex" in apk.namelist()
    assert json.loads(apk.read("assets/wallpapers/catalog.json")) == catalog
    shows = json.loads((root / "assets/wallpapers/tvmaze-shows.json").read_text())
    assert len(shows) == 5 and len({row["id"] for row in shows}) == 5
    assert all(row["id"] > 0 and row["sourcePage"].startswith("https://www.tvmaze.com/shows/") for row in shows)
    assert json.loads(apk.read("assets/wallpapers/tvmaze-shows.json")) == shows
    for entry in catalog["wallpapers"]:
        data = apk.read("assets/wallpapers/" + entry["file"])
        assert hashlib.sha256(data).hexdigest() == entry["sha256"], "APK contains a stale asset"
print("Built APK includes DEX, the current catalogue and four exact publisher images")
icons = json.loads((root / "preview/icons/catalog.json").read_text())
assert len(icons) == 6
for entry in icons:
    assert hashlib.sha256((root / "preview/icons" / entry["file"]).read_bytes()).hexdigest() == entry["sha256"]
print("Six exact installed-app icon resources match their extraction hashes")
