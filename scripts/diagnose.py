#!/usr/bin/env python3
"""Bounded, read-only ADB diagnostics. No block contents or account data read."""
import datetime
import base64
import hashlib
import io
import json
import os
from pathlib import Path, PurePosixPath
import re
import shlex
import subprocess
import sys
import tarfile
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parents[1]
TV = "192.168.1.3:5555"  # Owner-confirmed address; identity checked. No emulator fallback.
STAMP = datetime.datetime.now(ZoneInfo("Africa/Lagos")).strftime("%Y%m%dT%H%M%S%z")
OUT = ROOT / "diagnostics" / STAMP
os.umask(0o077)
OUT.mkdir(parents=True, exist_ok=True)

def call(args, timeout=30):
    with (OUT / "commands.jsonl").open("a") as log:
        log.write(json.dumps({"time": datetime.datetime.now().isoformat(), "argv": args}) + "\n")
    try:
        p = subprocess.run(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=timeout)
        return p.returncode, p.stdout, p.stderr
    except subprocess.TimeoutExpired as e:
        return 124, e.stdout or b"", (e.stderr or b"") + b"\nTIMEOUT\n"

def adb(*args):
    return call(["adb", "-s", TV, *args])

def remote(command, root=False):
    return adb("exec-out", "su -c " + shlex.quote(command) if root else command)

def save(name, result):
    code, data, error = result
    (OUT / name).write_bytes(data)
    (OUT / (name + ".status.json")).write_text(json.dumps({"exit_code": code, "stderr": error.decode(errors="replace")}, indent=2) + "\n")
    print(f"{name}: exit={code}, bytes={len(data)}")
    return data

def guard():
    state = adb("get-state")
    if state[0] or state[1].strip() != b"device":
        # Normal connect is explicitly authorized; no network scanning.
        save("connection.txt", call(["adb", "connect", TV], timeout=15))
        state = adb("get-state")
    if state[0] or state[1].strip() != b"device":
        raise SystemExit("TV unavailable. Confirm its address; no other target tried.")
    serial = adb("get-serialno")
    identity = remote("getprop ro.board.platform; getprop ro.product.device; cat /proc/device-tree/compatible")
    save("identity.txt", identity)
    if serial[0] or serial[1].strip() != TV.encode() or identity[0] or b"rk322x" not in identity[1] or b"rockchip,rk3229" not in identity[1]:
        raise SystemExit("Identity mismatch; refusing further diagnostics.")

def root_check():
    result = remote("id", root=True)
    save("root.txt", result)
    if result[0] or b"uid=0(root)" not in result[1]:
        raise SystemExit("Root unavailable. No alternate privilege operation attempted.")

HARDWARE = {
    "build-properties.txt": "getprop | grep -E '^\\[(ro\\.build\\.|ro\\.product\\.(model|brand|name|device|board|manufacturer|cpu\\.)|ro\\.board\\.|ro\\.hardware|ro\\.bootloader|ro\\.opengles|ro\\.zygote|ro\\.config\\.low_ram|wifi\\.interface)' | grep -viE 'serial|macaddr|ssid|password|token'",
    "cpu-memory-kernel.txt": "cat /proc/cpuinfo | grep -vi serial; cat /proc/meminfo; uname -a; cat /proc/version; cat /proc/swaps; df -k /data /system /cache",
    "wireless.txt": "cat /proc/modules; for d in /sys/bus/sdio/devices/*; do echo NODE:$d; for p in vendor device class modalias; do echo PROPERTY:$p; cat $d/$p; done; ls -l $d/driver; done; ls -l /system/lib/modules; ls /system/etc/firmware | grep -iE 'ssv|wifi|bt|bluetooth'; ls -l /sys/class/bluetooth; ls -l /sys/bus/usb/drivers/btusb",
    "graphics-media.txt": "dumpsys SurfaceFlinger | grep -iE 'GLES:|GL_VERSION|GL_VENDOR|GL_RENDERER'; for p in name mode modes screen_info; do echo FB0:$p; cat /sys/class/graphics/fb0/$p; done; ls -l /dev/*vpu* /dev/*rkv* /dev/*mali* /dev/*video*; cat /proc/asound/cards; cat /system/etc/media_codecs.xml; cat /system/etc/media_codecs_performance.xml",
    "input-config.txt": "cat /proc/bus/input/devices | grep -vE '^[PU]:|^S: Sysfs='; ls -l /system/usr/keylayout; ls -l /sys/class/rc",
    "boot-recovery-config.txt": "getprop ro.bootloader; getprop ro.bootmode; getprop ro.boot.verifiedbootstate; ls -l /fstab* /system/etc/recovery* /system/bin/*recovery* /sbin/recovery /sys/firmware/fdt /sys/firmware/devicetree/base; for f in /fstab.rk30board /fstab.rk30sdk; do if [ -f $f ]; then echo FILE:$f; cat $f; fi; done; ls -l /sys/class/android_usb/android0; cat /sys/class/android_usb/android0/functions",
}

def hardware():
    for name, command in HARDWARE.items():
        save(name, remote(command, root=True))

def performance():
    """Read-only snapshot; never launches, clears, kills or modifies an app."""
    commands = {
        "performance-memory.txt": "cat /proc/meminfo; cat /proc/loadavg; cat /proc/swaps; df -k /data; dumpsys meminfo | head -90",
        "performance-settings.txt": "wm size; wm density; for p in window_animation_scale transition_animation_scale animator_duration_scale; do echo SETTING:$p; settings get global $p; done; getprop ro.config.low_ram; getprop dalvik.vm.heapsize; getprop dalvik.vm.heapgrowthlimit; for d in /sys/class/thermal/thermal_zone*; do echo THERMAL:$d; cat $d/type $d/temp; done",
        "brave-package.txt": "pm list packages brave; dumpsys package com.brave.browser | grep -E 'versionCode=|versionName=|minSdk=|targetSdk=|primaryCpuAbi=|secondaryCpuAbi=|codePath=|pkgFlags=|enabled=|Package \\[|Unable to find|not found'",
        "brave-process.txt": "ps | grep -E 'brave|USER'; dumpsys cpuinfo | head -35",
        "brave-launcher-resolution.txt": "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER com.brave.browser",
    }
    for name, command in commands.items():
        save(name, remote(command))
    code, data, error = remote("logcat -b crash -d -v brief -t 600")
    # Never persist the raw crash buffer, exception messages, URLs or tabs.
    selected = []
    active = False
    for line in data.decode(errors="replace").splitlines():
        if "FATAL EXCEPTION" in line:
            active = False
        if "Process:" in line:
            active = "com.brave.browser" in line
            if active:
                selected.append("Process: com.brave.browser")
        if active:
            for cls in re.findall(r"\b(?:java|android|org\.chromium|com\.brave)\.[A-Za-z0-9_.$]+(?:Exception|Error)\b", line):
                selected.append(cls)
        if "Cmdline: com.brave.browser" in line:
            selected.append("Native crash cmdline: com.brave.browser")
        if "Fatal signal" in line and "brave" in line:
            match = re.search(r"Fatal signal (\d+)", line)
            if match:
                selected.append("Brave fatal signal " + match.group(1))
    message = "\n".join(selected) if selected else "No matching Brave crash signatures found in the last 600 crash-buffer lines. This does not establish the cause."
    save("brave-crash-filtered.txt", (code, (message + "\n").encode(), error))

def partitions():
    commands = {
        "partition-links.txt": "ls -l /dev/block/platform/30020000.rksdmmc/by-name/; for p in /dev/block/platform/30020000.rksdmmc/by-name/*; do echo ALIAS:$p; readlink -f $p; done",
        "partitions.txt": "cat /proc/partitions",
        "mounts.txt": "cat /proc/mounts",
        "cmdline.txt": "cat /proc/cmdline",
        "block-geometry.txt": "for d in /sys/class/block/mmcblk0 /sys/class/block/mmcblk0p*; do echo BLOCK:$d; for p in start size queue/logical_block_size; do echo PROPERTY:$p; cat $d/$p; done; done; ls -l /dev/block/mmcblk0*; for p in name type manfid oemid date hwrev fwrev prv erase_size preferred_erase_size raw_rpmb_size_mult; do echo EMMC:$p; cat /sys/block/mmcblk0/device/$p; done",
    }
    for name, command in commands.items():
        save(name, remote(command, root=True))

def device_tree():
    # This su implementation expands LF to CRLF even under exec-out. Encode
    # inside the root command so no binary byte crosses that transport directly.
    result = remote("tar -cf - -C /proc/device-tree . | base64", root=True)
    encoded = save("device-tree.tar.base64", result)
    if result[0]:
        raise SystemExit("Device tree archive incomplete; inspect status file.")
    try:
        data = base64.b64decode(b"".join(encoded.split()), validate=True)
    except ValueError as error:
        raise SystemExit(f"Non-base64 diagnostic contamination: {error}")
    (OUT / "device-tree.tar").write_bytes(data)
    listing = save("device-tree-files.txt", remote("find /proc/device-tree/ -type f", root=True))
    expected = {line.removeprefix("/proc/device-tree/") for line in listing.decode().splitlines()}
    # Never extract symlinks, absolute paths, parent references, or special files.
    properties = []
    with tarfile.open(fileobj=io.BytesIO(data), mode="r:") as archive:
        for entry in archive:
            path = PurePosixPath(entry.name)
            if not entry.isfile() or path.is_absolute() or ".." in path.parts:
                continue
            if entry.size > 1024 * 1024 or len(properties) >= 10000:
                raise SystemExit("Unexpected device tree size; stopped.")
            raw = archive.extractfile(entry).read()
            dest = OUT / "device-tree" / str(path)
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(raw)
            record = {"path": str(path), "bytes": len(raw), "sha256": hashlib.sha256(raw).hexdigest(), "hex": raw.hex()}
            # These properties have defined DT string/string-list semantics.
            if path.name in {"compatible", "model", "name", "status", "device_type", "bootargs", "wifi_chip_type", "dr_mode"}:
                record["strings"] = raw.rstrip(b"\0").decode("ascii", errors="backslashreplace").split("\0")
            properties.append(record)
    (OUT / "device-tree-index.json").write_text(json.dumps(properties, indent=2) + "\n")
    if {p["path"] for p in properties} != expected:
        raise SystemExit("Device tree archive does not match file inventory; incomplete.")
    print(f"Preserved {len(properties)} binary properties; decoded only known strings.")

def main():
    mode = sys.argv[1] if len(sys.argv) == 2 else ""
    if mode not in {"connection", "root", "hardware", "performance", "partitions", "device-tree", "all"}:
        raise SystemExit("Usage: diagnose.py connection|root|hardware|performance|partitions|device-tree|all")
    guard()
    if mode not in {"connection", "performance"}:
        root_check()
    if mode == "performance":
        performance()
    if mode in {"hardware", "all"}:
        hardware()
    if mode in {"partitions", "all"}:
        partitions()
    if mode in {"device-tree", "all"}:
        device_tree()
    print(f"Saved diagnostics: {OUT}")

if __name__ == "__main__":
    main()
