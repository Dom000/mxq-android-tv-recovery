# Compatibility matrix

As of 2026-10-24: **zero verified-compatible replacements**. A complete Android TV OS with native Android app installation/execution and TV/remote navigation is mandatory. Linux-only systems and launcher-only conversions fail that requirement; ordinary stock Android candidates remain research leads until their Android TV framework is verified. SoC support alone cannot establish board, DDR initialization or recovery compatibility.

Legend: **C** = confirmed on this unit; **R** = author/tester report on other hardware; **?** = insufficient evidence; **N/A** = not an Android system. A reported match is not a verified match.

| Requirement | Current firmware | TechLife ATV9 | TechLife ATV10 | DCBOX SV6158 | Stock 8.1 | LibreELEC | Armbian |
|---|---|---|---|---|---|---|---|
| RK3229 | C software identity | R | R | R | R | R | R |
| Exact PCB revision | Running here, revision unknown | ? | ? | Named other PCB, match ? | ? | ? | ? |
| DDR init / geometry | Running here | ? | ? | ? | ? | ? | ? |
| eMMC and parameter offsets | C | ? | ? | ? | ? | ? | ? |
| Bootloader / trust | Running here, binaries unknown | ? | ? | ? | ? | ? | ? |
| SSV6158 | C live bound module | ? (6051P advertised) | ? (RK912/915a advertised) | R uploader claim | ? | ? | Historical unsupported; current image ? |
| HDMI | C configured 1080p60 | Other-board bugs reported | R | ? | ? | R | R |
| Remote input | C input configuration, buttons untested | Mixed R | R | ? | ? | IR supported generally, mapping ? | Mapping ? |
| Hardware decode | C vendor stack, playback untested | ? | ? | ? | ? | R FullHD | R, application-stack dependent |
| Android TV framework | Box build; certified ATV unverified | Claimed, uninspected | Claimed, uninspected | ? | ? | N/A, Kodi TV UI | N/A, desktop/server |
| Better Android app API | API 25 | 28 if genuine, unverified | 29 if genuine, unverified | ? | 27 if genuine, unverified | N/A | N/A |
| Proven recovery after failed flash | No | No | No | No | No | No | No |
| Overall | Known running baseline | Insufficient evidence | Insufficient evidence | Potentially compatible lead only | Insufficient evidence | Excluded: cannot run Android apps | Excluded: cannot run Android apps |

“Potentially compatible” means there is relevant hardware evidence worth investigating; it does not authorize installation. No recommendation assumes Ethernet or a USB Wi-Fi adapter is an acceptable substitute for the user's working built-in Wi-Fi requirement.

Known incompatible classes: Amlogic S905/S905W MXQ images and RK3318/RK3328 images target different SoCs and must be rejected regardless of product name. RK3128 SSV6158 images found in research are a different SoC family and are not candidates for this RK3229 profile.

The most realistic proven OS/API remains the installed Android 7.1.2/API 25. Android 8.1/9/10 builds are documented for portions of the RK322x ecosystem; no one of those versions is proven upgradeable on this unit. Android 11+ support was not established. Linux can modernize a Kodi/server use case, but cannot satisfy Android application compatibility by itself.
