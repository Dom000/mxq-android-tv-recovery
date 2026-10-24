# Open-source TV operating systems checked

2026-10-24. **User requirement confirmed: the final OS must install and run Android apps. Armbian and LibreELEC are therefore excluded as final solutions.** LibreELEC is a Kodi/Linux media appliance; Armbian is general-purpose Linux. Neither supplies the Android framework. Hardware compatibility on this box is also unverified. Research only: no OS image download, boot-media write, reboot, install or driver load has occurred.

| Requirement | LibreELEC RK322x | Armbian rk322x-box |
|---|---|---|
| Purpose | Kodi media appliance, remote-oriented TV interface | Debian/Ubuntu-family Linux, minimal/server or desktop |
| RK322x evidence | Maintainer device target, generic and multiple MXQ-family DTs | Current upstream rk322x-box.tvb target |
| 1 GiB suitability | More relevant for single-task Kodi; RAM/video test required | Minimal image is the lighter research choice; desktop/browser use likely constrained |
| Android apps / Play Store | No Android framework/GMS | No Android framework/GMS |
| Built-in SSV6158 | No integration demonstrated in checked device source/config | No integration demonstrated in checked current kernel config |
| GPU/video | Lima/Hantro/HDMI configuration exists | Lima/Hantro/HDMI configuration exists; application integration matters |
| Remote / audio | Exact keymap and HDMI audio require testing | Remote/media session requires configuration/testing |
| Bluetooth | Stock DT disables it; physical hardware unknown | Same |
| External trial | Maintainer documents microSD boot; this board untested | Community documents SD boot; this board untested |
| USB flash drive alone | Stock bootloader USB startup unproven | Same |
| Recovery | Must be demonstrated independently | Same |
| Decision | Excluded: does not meet Android-app requirement | Excluded: does not meet Android-app requirement |

## LibreELEC artifacts and inspected source

The [maintainer download directory](https://ilmich.libreelec.tv/libreelec-12/) lists the [March 11, 2026 image](https://ilmich.libreelec.tv/libreelec-12/LibreELEC-RK322X.arm-12.2-nightly-20260311-1b9970a-rk322x.img.gz) and [SHA-256 sidecar](https://ilmich.libreelec.tv/libreelec-12/LibreELEC-RK322X.arm-12.2-nightly-20260311-1b9970a-rk322x.img.gz.sha256). Published compressed-file digest:

```text
6d0a24f734387f409cfc0c5df44193f2af791a8bf740d4f978bd6e29602f2d29
```

The checksum text was retrieved; the 177 MiB-listed OS image was not. This digest has not been compared with a local binary and is not an authenticity signature.

Inspected [device options at commit c6116632](https://github.com/rk-unofficial-cfw/LibreELEC.tv/blob/c6116632c435822fe4bc46fa35a48b03f43385d5/projects/Rockchip/devices/RK322X/options) target Cortex-A7/hard-float and Lima, with multiple board DTs. The [kernel config](https://github.com/rk-unofficial-cfw/LibreELEC.tv/blob/c6116632c435822fe4bc46fa35a48b03f43385d5/projects/Rockchip/devices/RK322X/linux/default/linux.arm.conf) enables SSV6051, Hantro, HDMI/CEC and MMC support. No SSV6158 option was found. The complete repository tree (4,867 entries, not truncated) names an SSV6051 patch but no SSV6158 path. These observations concern source configuration; they are **not a binary module inventory or proof no unlabelled support exists**.

The [maintainer's policy](https://forum.libreelec.tv/thread/29006-unofficial-le12-rk3228-rk3229-box-libreelec-builds/) is SD boot; internal eMMC installation is not recommended/supported. This box's DDR initialization and GPIO match are still unknown. Do not select an MXQ-named DT solely from the case label.

## Armbian artifacts and inspected source

The [community release 26.11.0-trunk.95](https://github.com/armbian/community/releases/tag/26.11.0-trunk.95), published October 14, 2026, exposes a [Trixie minimal RK322x image](https://github.com/armbian/community/releases/download/26.11.0-trunk.95/Armbian_community_26.11.0-trunk.95_Rk322x-box_trixie_current_6.18.56_minimal.img.xz), **331,491,888 bytes compressed**, with GitHub asset digest:

```text
01eed4961bd4c72f6489c864c836cf9fc5552316c8645c8c1668dcd6d5a04ef1
```

An XFCE variant is also listed. Metadata came from the project's GitHub releases API; no OS image was downloaded. These are community/trunk builds, not a board-vendor stability guarantee. The release kernel label is 6.18.56; actual image bootability/modules remain uninspected.

The [board target](https://github.com/armbian/build/blob/fac57a02f1c6ff2221a3f3099a1094dcdf41be26/config/boards/rk322x-box.tvb) describes 1/2 GiB DDR2/DDR3 eMMC/NAND configurations but has no named maintainer. Generic support does not prove this board's DDR compatibility. The [kernel config](https://github.com/armbian/build/blob/fac57a02f1c6ff2221a3f3099a1094dcdf41be26/config/kernel/linux-rockchip-current.config) includes SSV6051, Lima, Hantro and HDMI; no SSV6158 option was found. The [family configuration](https://github.com/armbian/build/blob/fac57a02f1c6ff2221a3f3099a1094dcdf41be26/config/sources/families/rockchip.conf) uses RK322x DDR/TEE blobs: an open-source OS can still depend on proprietary boot components. This source snapshot is October 23, later than the release, so it cannot establish the exact October 14 image contents.

The [October 2024 maintainer discussion](https://forum.armbian.com/topic/34923-csc-armbian-for-rk322x-tv-box-boards/page/91/) reports nonworking SSV6158 on comparable hardware. Treat this as historical compatibility evidence, not a current assertion that source is unavailable.

## SSV6158 driver lead

The [public SDIO driver source](https://github.com/wltechblog/wifi-ssv6158/blob/a2afa9d80568895edd8875841137103801b51859/hwif/sdio/sdio.c) defines vendor **0x5653**, device **0x2060**, and registers that pair in its SDIO ID table. This exactly matches the TV's live hardware ID. The inspected repository commit is `a2afa9d80568895edd8875841137103801b51859`, dated November 19, 2025.

It still needs target-kernel compilation/porting, matching firmware/config, power/reset GPIO integration, calibration handling and runtime testing. An SDIO ID can match related chips; it does not prove radio initialization. No integration into the selected Armbian/LibreELEC builds was established. The [separate RK322x ssv6x5x source](https://github.com/paolosabatino/ssv6x5x) describes Linux 4.4 support, which does not establish support on newer kernels. No driver was built or loaded.

## USB flash limitation

The available USB flash drive can hold media or an OS image, but stock CNV6 USB-OS boot support is unproven. Community examples sometimes rely on an already-modified bootloader or SD chainloader. A [maintainer's older RK322x report](https://forum.libreelec.tv/user-post-list/24278-knaerzche/?pageNo=5) documents legacy-chain USB limitations on a different board. Do not transplant its erase/bootloader-change procedure.

No further Linux-OS trial is recommended for the current goal. Linux recovery tools remain relevant to preservation/restoration, and public Wi-Fi source may inform future Android driver work, but neither constitutes a compatible Android OS. Continue only with actual Android firmware candidates; internal flashing still requires independent recovery and verified backups. No Android-container/emulation workaround on these Linux images has been validated for this box.
