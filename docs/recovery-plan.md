# Recovery feasibility and proposed installation gate

2026-10-24. **Recovery is not demonstrated, so no firmware installation is proposed for execution.** The enclosure must remain closed. Do not use shorted flash pins or erase storage to force MaskROM.

| Path | Public evidence | This unit / equipment | Current status |
|---|---|---|---|
| Existing Android recovery | A named 32 MiB recovery partition exists | Boot image unread; screen/ADB/sideload behavior unknown | Unverified; partition existence is not a recovery test |
| Rockchip Loader via USB OTG | [Vendor guide](https://ugoos.com/firmware-upgrade-guide-for-rockchip-devices) describes external button + OTG entry for its products | USB OTG controller exists in DT; exposed physical port/button and suitable data cable unknown | Potential route; not demonstrated |
| MaskROM | [rkdeveloptool source](https://github.com/rockchip-linux/rkdeveloptool) supports Rockusb mode operations | Accessible hardware entry, board-specific DDR loader and USB enumeration unknown | Not a usable guaranteed path yet |
| MicroSD external boot / Multitool | [Maintainer's Multitool source](https://github.com/paolosabatino/multitool) and [RK322x guide](https://forum.armbian.com/topic/34923-csc-armbian-for-rk322x-tv-box-boards/) | microSD slot confirmed by user; no microSD card available; reader unconfirmed; DDR/trust bootability untested | Card/host-writing access needed for trial; requires approval to boot |
| USB flash external boot | Some RK322x bootloaders can chain to USB | User has a USB flash drive; **stock bootloader USB-OS support not established** | Do not assume inserting a flashed stick will work |
| ADB network root | Working now | Depends on booting the current Android and Wi-Fi/network | Diagnostic/backup route only; cannot be relied on after a failed flash |

## macOS, Linux and Windows

[Radxa's macOS documentation](https://docs.radxa.com/en/rock4/rock4ab-se/low-level-dev/maskrom/macos) demonstrates rkdeveloptool on macOS; it is evidence of host tooling, not instructions specific to this PCB. This Mac is x86_64, macOS 26.6. `rkdeveloptool` is not currently installed, and no package installation was performed. Rockchip's [source usage](https://github.com/rockchip-linux/rkdeveloptool/blob/master/main.cpp) includes device listing, flash info, sector reads and writes. Only the connected Rockchip device should be physically present during future USB operations; do not assume this tool has ADB-like serial targeting.

Linux is a practical alternative host for Rockusb tooling and offline inspection. Windows vendor FactoryTool/AndroidTool plus Rockchip drivers are commonly referenced by Android ROM authors. Neither host choice solves an inaccessible reset/OTG path or an incompatible DDR loader. There is no evidence that Windows is mandatory for all recovery, nor that a Mac alone can already restore this exact box.

Required equipment depends on the confirmed path: a matching USB data cable for the identified OTG port, correct stable box power, accessible documented reset control, or a spare microSD + reader with enough space. A USB flash drive is storage, not a host-to-device data cable or a substitute for a microSD slot. Do not blindly connect USB-A host ports together or guess power wiring from another board's photo.

## Safe order of work after separate approvals

1. Obtain externally visible port/reset information without disassembly. Identify an actual matching board/bootloader recovery procedure from evidence.
2. Review and prepare trusted host tools locally; inspect source/build instructions. Retrieve a legitimate loader only if its DDR/SoC match can be established, and verify provenance/checksum. A RAM loader transfer changes device state and still needs approval.
3. Present the exact approved mode-entry command or physical sequence, expected USB identity, risks and return-to-Android procedure. A candidate `adb -s 192.168.1.4:5555 reboot loader` is **not run and not assumed supported**. No reboot in this phase is authorized.
4. After approval, demonstrate Loader enumeration and harmless metadata reads, and return to Android. A software-triggered Loader test alone does not prove hardware recovery after loader corruption; an independent button/external-media route must also be established before risking that boot chain.
5. With approval, demonstrate a trusted external environment boots while leaving internal eMMC unmodified. Verify internal device identity from its capacity/geometry; Linux numbering can differ from Android. Do not run install/erase menu options. Check that the environment does not automatically write internal storage before booting it.
6. With separate backup approval, take and verify offline full user-area and necessary recovery images. Keep two verified copies and metadata. Establish a restore path independent of Android before changing any boot components.
7. Only if a ROM becomes verified for PCB/DDR/eMMC/Wi-Fi/remote/HDMI/decoder, produce its exact installation procedure from its actual packaging. Include the tool version, hashes, partitions/offset semantics, backup manifest and rollback steps. Obtain explicit approval for that procedure.

## Conditional rollback

If a proven external SD trial leaves internal storage untouched, removal of that external medium and an approved normal reboot should return to Android, **provided the trial truly made no internal writes**. This has not been tested here.

After an internal flash failure, a usable rollback would be: enter independently demonstrated Loader/MaskROM or external recovery; use the matching loader if needed; verify eMMC capacity and addressing; restore the verified original raw backup/boot chain using the established method; read back and compare preserved ranges; then approve reboot and validate hardware. Exact write commands cannot responsibly be specified while loader/address semantics and the recovery entry path are unknown. Named partition images alone may omit the critical initial 8 MiB.

MaskROM requiring pin shorting or internal test pads is unavailable under the user's enclosure restriction. If that is the only reliable fallback, **do not proceed with an internal OS upgrade**. A backup without an accessible restore path does not change that decision.

## Latest equipment update — 2026-10-24

The user confirms a microSD slot but has no microSD card. A card-based recovery test cannot currently be performed. A USB flash drive remains available, but stock USB boot is unverified. Continue offline Android TV firmware and USB recovery research; do not assume this update authorizes purchases, media writes, backups or reboot. Reset access, OTG port/cable and host card-writing access remain unconfirmed.
