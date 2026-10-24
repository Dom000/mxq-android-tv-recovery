# Public source register

Checked 2026-10-24 Africa/Lagos. All entries are read-only research; no downloaded script was executed. Public API/source text was retrieved with TLS verification using macOS curl after this Python installation reported a missing CA trust chain. TLS verification was never disabled. Large temporary API/source copies were replaced with compact evidence and selected configuration excerpts.

- [TechLife ATV9 author posts, March/April 2023](https://4pda.to/forum/index.php?showtopic=935891&st=2520): version/Wi-Fi/install claims; Google Drive endpoints not verified because direct forum fetch returns 403.
- [TechLife ATV10 author post, August 2023](https://4pda.to/forum/index.php?showtopic=1064395&st=20): RK912/RK915a variants and older-board limitations. Direct fetch 403; indexed author content available.
- [Tanix first-hand boot/remote reports](https://4pda.to/forum/index.php?showtopic=847054&st=560): variant-dependent outcomes.
- [HK1 first-hand speed/remote reports](https://4pda.to/forum/index.php?showtopic=935891&st=2540): mixed results, no target-board verification.
- [DCBOX SV6158 uploader file page](https://eletronicabr.com/en/files/file/56011-firmware-dcbox_rk3229q-221p-v1-board/) and [uploader announcement](https://eletronicabr.com/forums/topic/421336-firmware-placa-dcbox_rk3229q-221p-v1/): source listing, Android version absent, authenticated download required.
- [LibreELEC RK322x maintainer thread](https://forum.libreelec.tv/thread/29006-unofficial-le12-rk3228-rk3229-box-libreelec-builds/): scope and external-boot policy.
- [LibreELEC download directory](https://ilmich.libreelec.tv/libreelec-12/): public artifacts and checksums.
- [LibreELEC source snapshot](https://github.com/rk-unofficial-cfw/LibreELEC.tv/tree/c6116632c435822fe4bc46fa35a48b03f43385d5/projects/Rockchip/devices/RK322X): target source/configuration; not binary runtime verification.
- [Armbian community release](https://github.com/armbian/community/releases/tag/26.11.0-trunk.95) and [release API](https://api.github.com/repos/armbian/community/releases?per_page=1): actual asset names/sizes/digests captured in open-source-evidence.json.
- [Armbian board target](https://github.com/armbian/build/blob/fac57a02f1c6ff2221a3f3099a1094dcdf41be26/config/boards/rk322x-box.tvb): current source snapshot, not a target-specific compatibility promise.
- [RK322x maintainer discussion of SSV6158, October 2024](https://forum.armbian.com/topic/34923-csc-armbian-for-rk322x-tv-box-boards/page/91/): historical driver/eMMC problems; direct page sometimes 502, indexed posts accessible.
- [SSV6158 SDIO driver source](https://github.com/wltechblog/wifi-ssv6158/blob/a2afa9d80568895edd8875841137103801b51859/hwif/sdio/sdio.c): exact SDIO-ID lead. Header permits use/copy with its notice; only small identification evidence is retained here.
- [RK322x ssv6x5x README](https://github.com/paolosabatino/ssv6x5x): Linux 4.4 scope, no validated current-kernel integration.
- [Rockchip rkdeveloptool](https://github.com/rockchip-linux/rkdeveloptool) and [command usage source](https://github.com/rockchip-linux/rkdeveloptool/blob/master/main.cpp): authoritative tool commands; no tool installation or execution.
- [Radxa macOS host documentation](https://docs.radxa.com/en/rock4/rock4ab-se/low-level-dev/maskrom/macos): macOS tool capability, not this PCB's procedure.
- [Ugoos Rockchip recovery guide](https://ugoos.com/firmware-upgrade-guide-for-rockchip-devices): host/OTG/reset concepts, vendor-specific hardware cautions.
- [Original Multitool source](https://github.com/paolosabatino/multitool): external maintenance project; not boot-tested here.

XDA searches were attempted for RK3229, Android TV, SSV6158 and ESP8089. No accessible author release verifying this target was found; direct XDA search access failed. Search-result marketing pages, generic SoC download catalogs and second-hand firmware mirrors were not used as compatibility proof. Searches also found RK3128 SSV6158 ROMs; their SoC mismatch excludes them.

The research is not exhaustive proof that no compatible firmware exists. It establishes that none in the checked evidence meets the user's verification/recovery requirements.

Additional Android-TV-only search: [generated HK1 Mini TWRP tree](https://github.com/twrpdtgen/android_device_rockchip_rk3229) is recovery source, not an OS release. [Official LineageOS device list](https://wiki.lineageos.org/devices/) returned 403. No compatible AOSP/LineageOS Android TV artifact was established.
