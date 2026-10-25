# Hardware profile

Live collection: 2026-10-24, Africa/Lagos. Initial details from the user's brief were checked against this unit. Confidence labels distinguish what software reports from physically verified hardware.

| Item | Finding | Confidence and evidence |
|---|---|---|
| Target | `192.168.1.4:5555`, MBOX / rk322x_box | Owner reconfirmed changed IP on2026-10-25; RK3229 identity rechecked. The intervening session used `.3`. |
| Root | `uid=0(root) gid=0(root) groups=0(root)` via `su -c id` | Confirmed |
| SoC identity | CPU hardware RK3229; DT `rockchip,rk3229`; rk322x platform | Confirmed software identity; physical chip marking/stepping unverified |
| CPU / ABI | Four Cortex-A7 cores (`0xc07`), ARMv7 / armeabi-v7a, 32-bit zygote | Confirmed `/proc/cpuinfo` and ABI properties |
| Board | rk30board / rk30sdk | Confirmed generic software identifiers; exact PCB revision **unknown** |
| Android | API 25 and 7.1.2 NHG47K fingerprint | Strong evidence of Android 7.1.2; no framework binary audit |
| Spoofed display | Release property 11.1; CNV6.20220831 display; fingerprint CNV6.20210810 | Confirmed inconsistency; 11.1 is not evidence of a newer OS |
| Security patch | Property says 2022-04-05 | Unverified patch content; do not trust it solely as a property |
| Kernel | Linux 3.10.104, ARMv7, build Aug 10 2021 | Confirmed live |
| RAM | MemTotal 1,021,920 KiB = 997.97 MiB; no swap | Confirmed current kernel view |
| RAM snapshot | MemFree 99.6 MiB, buffers 19.1 MiB, cached 577.1 MiB | Confirmed snapshot, not Android available RAM; cache figures cannot simply be treated as fully reclaimable |
| DRAM | DT `rockchip,rk322x-dram`, `dram_freq` bytes `280de800`; many DDR3 and LPDDR3 timing properties | Confirmed metadata; physical type, geometry, DDR init binary **unknown** |
| eMMC | MMC, model P1J95K; manufacturer code 0xfe, OEM 0x014e, date 12/2015 | Confirmed sysfs reports; do not infer manufacturer identity or physical PCB from these codes |
| Capacity | 7,834,959,872 bytes; 512-byte logical sectors | Confirmed sysfs, proc partition sizes and blockdev |
| Storage controller | 30020000.rksdmmc; DT 8-bit eMMC bus, configured max 50 MHz | Confirmed configuration; actual negotiated timing unverified |
| Userdata | Partition 5,364,514,816 bytes; filesystem 3,428,424 KiB available | Confirmed snapshot; actual partition size differs from df capacity |
| Wi-Fi | DT ssv6158; SDIO `5653:2060`; module ssv6158 loaded; bound `SSV6XXX_SDIO` | Confirmed converging evidence; no account/SSID collection |
| Wi-Fi assets | ssv6158.ko, ssv6051.ko, ssv6x5x.ko; firmware ssv6x5x-sw.bin and configs | Confirmed filenames only; file existence does not establish cross-ROM ABI compatibility |
| Bluetooth | DT node `disabled`, no HCI entries or advertised feature; Realtek `rtk_btusb` driver/device node and vendor software already present, no controller identified | Unavailable in current configuration; physical radio/variant unverified. See [Bluetooth audio](bluetooth-support.md). |
| GPU | SurfaceFlinger: ARM Mali-400 MP, OpenGL ES 2.0; DT arm,mali400 | Confirmed live; `ro.opengles.version=196609` is inconsistent with the live renderer and not used as a capability claim |
| HDMI | fb screen_info 1920×1080, 60 fps; command line hdmi.vic=16 | Confirmed configured output; no playback/audio listening test |
| Audio | RK_RK322x, RK-HDMI-I2S, RK-SPDIF-CARD exposed | Confirmed ALSA devices; audible output untested |
| Video acceleration | `/dev/rkvdec`, `/dev/vpu_service`, vcodec_service module, Rockchip codec XML | Strong evidence of vendor acceleration stack; codecs/resolutions not functionally benchmarked |
| Remote | PWM input `110b0030.pwm`, matching Android keylayout file | Strong evidence of IR path; button decoding and replacement-ROM support untested |
| Bootloader | ro.bootloader unknown; command-line loader timestamp 2021-07-30 11:45:27 | Confirmed metadata; binary/version/DDR loader compatibility unknown |
| Boot policy | SELinux-disabled cmdline and SecureBootCheckOk=0 string | Confirmed strings only; no secure-boot fuse interpretation or unlock attempted |
| Recovery | named mmcblk0p8, 32 MiB; current boot mode emmc | Confirmed partition metadata; recovery contents and boot function unverified |
| External recovery media | USB flash drive available; microSD slot present, but no microSD card available | Confirmed user report; reader, data cable, reset access and external bootability unverified |

## Binary device tree preservation

Authoritative collection: `diagnostics/20261024T001107+0100/`: 2,904 regular properties, each saved byte-for-byte with size, SHA-256 and hex in `device-tree-index.json`, and known string properties decoded separately. Inventory matches an independent `find /proc/device-tree/ -type f` listing. No root `model` property was found, and inspected identity strings did not reveal a PCB revision.

The earlier `20261024T001014+0100/device-tree.tar` is **invalid**: this root transport inserted CR bytes, leaving a parser able to read only 110 properties. Do not use its extracted tree. Recollection encoded the tar to base64 *inside* the root shell, then strictly decoded on the host. The valid tar is 3,185,664 bytes and aligned to 512-byte blocks.

Raw DT values remain binary. `compatible`, `status`, `name`, `wifi_chip_type`, bootargs and other defined strings are decoded. No arbitrary property is interpreted as text; numeric frequency/timing units and multi-cell bus-address formats are not guessed. These timing values do not identify the DDR init training required by a replacement loader.

## Remaining identification limits

A software profile cannot establish the physical board revision, RAM part markings or reset wiring when vendors reuse a generic DT. Existing drivers and GPIO bindings provide comparison evidence, not a flashing guarantee. Do not open the enclosure. Next useful data are a legitimate candidate's complete release metadata and a non-destructive external-recovery demonstration, both without assuming labels imply compatibility.
