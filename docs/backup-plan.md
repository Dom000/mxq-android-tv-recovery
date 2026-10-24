# Backup plan — prepared, not executed

Date: 2026-10-24. **No backup exists.** The user explicitly requires approval before bulk partition reads/full imaging. Nothing in this plan authorizes execution. All extraction destinations are on the Mac; do not stage images in the TV's limited `/data` partition.

## Capacity and consistency

The Mac's current APFS volume reports **401 GiB available** (`df -h .`). The eMMC user area is **7,834,959,872 bytes (7.297 GiB)**. One raw full image plus one working duplicate needs about 14.6 GiB, plus firmware downloads and headroom; reserve at least 25 GiB and recheck immediately before approval/extraction. APFS supports files larger than 4 GiB. Verify the filesystem of any USB destination; FAT32 cannot hold a full raw image in one file.

A full user-area dump includes all named partitions and the first 8 MiB of unnamed boot metadata. It does not include OTP/eFuses or authenticated RPMB state. Separate eMMC boot0/boot1 devices are not exposed. Do not claim this is a complete hardware clone.

Android is live, and `/data`, `/cache`, `/metadata` are mounted read-write. A live full image is a salvage capture, **not an application-consistent restore image**. Source hashes can change while reading live filesystems. Reliable system-data recovery requires an approved external/offline boot with the internal filesystems unmounted (or equivalent proven quiescence); no freeze, unmount, remount or reboot is authorized now. Read-only `/system` and unmounted boot partitions are better candidates for stable source-hash comparison.

## Proposed extraction inventory

All operations need root on the TV, write only to the Mac, and remain approval-gated. Every image needs an exact-byte check and SHA-256. Rows describe source partitions rather than asserting the contents are valid bootable images.

| Source | Expected bytes | Destination under backups/session/ | Priority |
|---|---:|---|---|
| mmcblk0, full user area | 7,834,959,872 | emmc-user-area.img | P0 for complete layout preservation; offline preferred |
| mmcblk0 first 8 MiB (optional duplicate of full-image region) | 8,388,608 | emmc-prefix-8MiB.img | P0, reserved ID/parameter/loader region |
| mmcblk0p1 (uboot) | 4,194,304 | uboot.img | P0 |
| mmcblk0p2 (trust) | 8,388,608 | trust.img | P0 |
| mmcblk0p3 (misc) | 4,194,304 | misc.img | P0 |
| mmcblk0p4 (baseparamer) | 1,048,576 | baseparamer.img | P0 |
| mmcblk0p5 (resource) | 15,728,640 | resource.img | P0 |
| mmcblk0p6 (kernel) | 12,582,912 | kernel.img | P0 |
| mmcblk0p7 (boot) | 12,582,912 | boot.img | P0 |
| mmcblk0p8 (recovery) | 33,554,432 | recovery.img | P0 |
| mmcblk0p9 (backup) | 67,108,864 | backup.img | P0 |
| mmcblk0p10 (cache) | 134,217,728 | cache.img | P2 |
| mmcblk0p11 (metadata) | 16,777,216 | metadata.img | P1 |
| mmcblk0p12 (kpanic) | 4,194,304 | kpanic.img | P2 |
| mmcblk0p13 (system) | 2,147,483,648 | system.img | P0 |
| mmcblk0p14 (userdata) | 5,364,514,816 | userdata.img | P1 |

Optional individual images can be carved **on the Mac** from a verified full raw image using the recorded absolute start sectors and sizes, without rereading the TV. This avoids duplicating device reads and preserves the original image unchanged. If taking an initial smaller preservation set, include the unnamed prefix, boot chain, DT/resources, recovery, kernel, system/Wi-Fi assets and board parameters; it is not a substitute for a consistent complete backup.

## Timing and integrity for every row

Measured bulk throughput is not yet available because no bulk read is authorized. Time is `expected_bytes / measured_decoded_bytes_per_second`, plus source hashing and verification. As planning examples only, at a sustained **1–5 MiB/s decoded** the 7.297 GiB full capture takes roughly **25–125 minutes**, a 32 MiB recovery capture 6–32 seconds, and the 2 GiB system partition 7–34 minutes. These are illustrative ranges, not device performance measurements. Base64 expands wire traffic by about one third and adds CPU cost; a separate device SHA-256 pass adds another complete read. Start with the approved 12 MiB boot capture to measure practical throughput.

SHA-256 is not directly in PATH, but the existing **BusyBox advertises the sha256sum applet**; verify it against an innocuous metadata file before any partition-hash operation. No tools are installed on the TV. For each stable source, record source SHA-256 before/after extraction and compare both to the host SHA-256. If any differ, mark the capture invalid or inconsistent and investigate; do not merely regenerate a host checksum and call it verified.

Future transfer must encode bytes *inside* the root command. This su/ADB transport altered binary LF bytes in the first DT attempt; plain `adb exec-out su ... cat block > file` is unsafe here. For each read: record source path, resolved alias, geometry, start/end timestamps, exit status, exact expected size, host SHA-256, source digest and consistency status. Save to a new `.partial` filename, fail on interruption/size/hash mismatch, then rename only after all checks pass. Avoid overwriting previous captures. Full live writable-area hash equality cannot establish application consistency even if it happens to match.

## Exact first backup proposal (not run)

This is a concrete **12 MiB read of boot only**, to establish transport/throughput. Recheck identity and the resolved alias first. A later execution must create a fresh private session directory and refuse existing destinations.

```bash
# Requires explicit user approval before any line reading a block device.
TV="192.168.1.4:5555"
set -o pipefail
adb -s "$TV" exec-out "su -c 'busybox sha256sum /dev/block/mmcblk0p7'"
adb -s "$TV" exec-out "su -c 'set -o pipefail; cat /dev/block/mmcblk0p7 | base64'" \
  | base64 -D > backups/session/boot.img.partial
stat -f %z backups/session/boot.img.partial   # Must equal 12582912.
shasum -a 256 backups/session/boot.img.partial
adb -s "$TV" exec-out "su -c 'busybox sha256sum /dev/block/mmcblk0p7'"
```

Expected effect: root reads and CPU/network/disk load; a private image is saved only on the Mac. No device writes, reboot or remount. Risks: a disconnect/incomplete file, a changing source, extra load during use, and sensitive firmware material saved locally. Rollback: no device rollback required; retain the original OS and reject/remove only the failed local partial. This command sequence is a proposal, not an approved/run backup. Automatic size/digest gating and no-clobber file creation must wrap any actual run; do not rely on the displayed `stat` comment as enforcement.

After approval and successful capture, save a local manifest next to the images:

```json
{"images":[{"filename":"boot.img","expected_bytes":12582912,"sha256":"HOST_SHA256_64_HEX","source_sha256":"MATCHING_SOURCE_SHA256_64_HEX"}]}
```

Then `scripts/verify-backup.sh /absolute/path/to/manifest.json` checks local existence, safe relative paths, exact size, SHA-256 and source digest if present. It rejects an empty manifest, missing file, truncation or corruption. This verifies file integrity only, not successful restore.

## Restore readiness

**Not ready.** The Mac has no rkdeveloptool installed, no verified USB Loader/MaskROM route, no board-matched loader, and no demonstrated independent external boot. USB flash availability does not establish any of those. See [recovery-plan.md](recovery-plan.md). Restoring a full image would require an approved Rockchip USB or external Linux environment that sees the correct internal eMMC, with independently established address semantics and capacity. Restoring each named image also requires exact source geometry and the matching boot chain; recovery cannot depend on ADB surviving a bad flash.

Never format/erase the device to make it boot external media. Never restore using guessed offsets or write a compressed archive / Rockchip update container as if it were a raw eMMC image. No restore command is executable here until recovery entry and the image format have been verified and the user approves the exact operation.
