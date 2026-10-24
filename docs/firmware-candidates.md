# Firmware candidates and evidence

Checked 2026-10-24. **No replacement is verified compatible with this unit. The target must be a complete Android TV OS with native Android app installation/execution and remote navigation.** Tablet Android with a TV launcher does not qualify without evidence of the actual Android TV framework. Published names, author claims and other users' reports are separated from inspected artifacts. No firmware image has been downloaded, unpacked or executed; availability, authenticity and build API levels remain unverified unless stated. No truncated search-result URL is presented as a download link.

## Android candidates

| ID / firmware | Android and release evidence | Hardware support evidence | Distribution / install / recovery evidence | Assessment |
|---|---|---|---|---|
| A — `RK3229_TechLife_ATV9Rom_SSV6051_20230313`, later `RK3229_ATV9_TECHLIFE_2.0_SMOOTHLESS` | Author posted ATV9 March 14, 2023; filename March 13. Later 2.0 post April 2, replacement link April 11. Android 9 claimed; API 28 expected if genuine, **not measured**. | Author names SSV6051P. RK3229-family use reported; our PCB, DDR, eMMC and SSV6158 unverified. | [Author's HK1 thread page](https://4pda.to/forum/index.php?showtopic=935891&st=2520): Google Drive links published, complete href not recoverable through current browser access (403); endpoint/download integrity unverified. Author specifies FactoryTool 1.66 Restore; this rewrites internal firmware. No tested rollback on our unit. | **Insufficient evidence**. Claimed ATV image, certification unknown. Do not assume it is a certified Android TV build or a verified OS/API upgrade. |
| B — `TECHLIFE_RK3229_ATV10_H20_X88_RK915a_root`, alternative `ATVRK3228A_HK1_LITE_RK912_10_root` | [Author post Aug 18, 2023](https://4pda.to/forum/index.php?showtopic=1064395&st=20) calls these ATV10 / Google TV; API 29 expected if genuine, **not measured**. | Author advertises RK3229/RK3228A and RK915a or RK912 Wi-Fi variants. Older PCB incompatibility/lag explicitly mentioned. No SSV6158 claim. | Download-homepage links in author post; actual binary endpoint and publisher checksum unverified. Other-user internal FactoryTool installs reported. No proven external trial or rollback for this board. | **Insufficient evidence**. No reason to prioritize a heavier build on 1 GiB RAM. |
| C — `TelaVip-DCBOX_RK3229Q-221P-V1.3-SV6158.img` | [Uploader's file listing](https://eletronicabr.com/en/files/file/56011-firmware-dcbox_rk3229q-221p-v1-board/), associated [uploader post dated Sept 19, 2026](https://eletronicabr.com/forums/topic/421336-firmware-placa-dcbox_rk3229q-221p-v1/). This is a publication date, not a firmware build date. **Android version/API unknown**. | Advertises RK3229Q-221P-V1.3 and SV6158. Closest Wi-Fi-label match found; our board remains unknown. RAM, eMMC and loader requirements unspecified. | Listed 2.03 GB image; sign-in/credit access required. No download performed, checksum absent in inspected page, no independent successful target match. Uploader names Rockchip Batch/FactoryTool. Recovery undocumented for this board. | **Potentially compatible hardware lead only**, low confidence. Interface described as TV-suitable; actual Android TV framework unverified. No evidence it upgrades Android. |
| D — HK1 Mini / MX4VR Android 8.1 images | [Tester post](https://4pda.to/forum/index.php?showtopic=1086849), including `rk3229_8.1_box_MX4VR_SSV6051p_SpecTek_flash-update_20180914` and related September/October 2018 builds. Android 8.1 claimed; API 27 expected, not measured. | SSV6051p / SSV6256 variants, not demonstrated SSV6158. Board and DDR requirements differ or are absent. | Published mirror links, downloads not validated. Rockchip internal flashing family; no demonstrated recovery for our unit. | **Insufficient evidence**. TV-box firmware is not automatically Android TV; stock/tablet-derived UI versus ATV framework unknown. |

### Per-candidate missing functional details

| Criterion | A: TechLife ATV9 | B: TechLife ATV10 | C: SV6158 DCBOX | D: stock 8.1 |
|---|---|---|---|---|
| RAM/eMMC requirement | Not specified; reports on HK1/Tanix do not establish 1/8 support | Not specified; author warns older boards may lag/fail | Not specified | Variant dependent; not established for ours |
| Exact supported PCB/DDR init | Unknown | H20/X88 or HK1 Lite labels; exact DDR match unknown | Names RK3229Q-221P-V1.3; ours unknown | MX4VR/HK1 family; ours unknown |
| SSV6158 | No evidence | No evidence | Uploader claim only | No evidence |
| Bluetooth | Unknown | Unknown | Unknown | Unknown |
| Remote | Mixed first-hand reports on Tanix; ours untested | One comparable installation reports working voice controls; ours untested | Unknown | Unknown |
| HDMI audio/video | Target untested; 2.0 green-display report on another board | Target untested | Unknown | Target untested |
| Hardware decoding | Target codecs/resolutions untested | Target codecs/resolutions untested | Unknown | Unknown |
| Play Store / GMS | Author advertises ATV store and Google account setup; current functionality unknown | Author says Play Store works; current functionality unknown | Unknown | Unknown |
| DRM / streaming apps | Widevine, certification and service support unknown | Same | Same | Same |
| Comparable successful installation | Tanix TX2/HK1 reports, with remote problems also reported | Other RK322x reports; no matching SSV6158+PCB | None found in inspected listing | Other-board tester reports only |
| Known limitations | 2.0 boot/green-screen issues; remote variation; reports ATV7 faster | Old-board lag/incompatibility; LED bug on another installation | No reviews in inspected page; Android version absent | Wi-Fi/board variants, provenance and API need verification |
| Confidence for this TV | Low | Low | Low; useful identification lead | Low |

First-hand [Tanix reports](https://4pda.to/forum/index.php?showtopic=847054&st=560) describe ATV9 booting but 2.0 hanging and remote failures. [HK1 reports](https://4pda.to/forum/index.php?showtopic=935891&st=2540) include a user reverting to ATV7 for speed/stability. These are warnings from other hardware, not predictions proven on this unit. A successful [ATV10 installation report](https://4pda.to/forum/index.php?showtopic=892127&st=1600) names working Wi-Fi but does not identify SSV6158.

## Linux systems — excluded by mandatory Android-app requirement

**These are not final OS candidates.** The user explicitly requires installing and running Android apps. Linux sources remain useful only for hardware/recovery research.

**LibreELEC RK322x**: [developer thread](https://forum.libreelec.tv/thread/29006-unofficial-le12-rk3228-rk3229-box-libreelec-builds/) and [download directory](https://ilmich.libreelec.tv/libreelec-12/). The directory exposes `LibreELEC-RK322X.arm-12.2-nightly-20260311-1b9970a-rk322x.img.gz` (177 MiB listed), dated March 11, 2026, and its `.sha256` sidecar. These are actual complete public artifact links; the image was not downloaded. This is Kodi/Linux, Android API/GMS not applicable. The maintainer specifies SD boot and does not support/recommend internal installation. SSV6158, our DDR and external USB boot remain unverified. See [open-source comparison](open-source-tv-options.md) for investigation details.

**Armbian rk322x-box**: [community releases](https://github.com/armbian/community/releases) and [RK322x maintainer thread](https://forum.armbian.com/topic/34923-csc-armbian-for-rk322x-tv-box-boards/). A Linux desktop/server OS, no Android framework or Play Store. Exact current artifact and source inspection are recorded separately. The 2024 maintainer report says SSV6158 was unsupported then; this is historical evidence, not proof no driver exists in 2026.

## Driver-source leads

[wltechblog/wifi-ssv6158](https://github.com/wltechblog/wifi-ssv6158) publishes a Linux SSV6158 driver. [paolosabatino/ssv6x5x](https://github.com/paolosabatino/ssv6x5x) describes RK322x Linux 4.4 integration. Neither README demonstrates a ready firmware image for this unit, and presence of source does not mean current kernel support or a working ARM driver build. The old Android `.ko` cannot simply be copied into a newer kernel: kernel ABI/configuration and firmware/platform hooks must match. No builds, module loads or patches were attempted.

## Research limits and next filter

XDA-targeted searches and direct XDA search access did not yield an accessible, verifiable SSV6158-compatible release. Several 4PDA pages return direct-fetch 403 while indexed author/tester excerpts remain searchable; download links are therefore not treated as verified. No approval to purchase/download gated content is inferred. Anonymous marketing lists and broad “MXQ/RK3229 compatible” claims were excluded.

Before a candidate advances: obtain its original complete binary and publisher checksum, inspect actual API/framework/kernel/DT and partition packaging offline, compare DDR/eMMC/GPIO/remote/Wi-Fi support with the saved profile, and demonstrate recovery independently. No candidate currently passes those steps.

## Additional AOSP / LineageOS check

Targeted searches did not establish a complete, maintained Android TV AOSP/LineageOS release for RK3229 plus SSV6158. The official LineageOS device page could not be fetched (403), so official support was not conclusively audited. A [generated HK1 Mini RK3229 device tree](https://github.com/twrpdtgen/android_device_rockchip_rk3229) found during research is for TWRP recovery; it is not a downloadable Android TV OS and is not added as a firmware candidate. Raspberry Pi or x86 Android TV builds target other hardware and are excluded.
