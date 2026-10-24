# MXQ firmware, recovery and compatibility research

Research for one confirmed RK3229 / ARMv7 MXQ Pro 4K box with SSV6158 Wi-Fi and
Android 7.1.2 / API 25. These are findings and recovery plans, not a flashable
firmware release. The current recommendation is to retain the existing firmware.

| Document | Purpose |
|---|---|
| [Hardware profile](hardware-profile.md) | Confirmed hardware, Android version and remaining unknowns. |
| [Partition map](partition-map.md) | Named partitions, geometry and addressing caveats. |
| [Firmware candidates](firmware-candidates.md) | Android firmware leads and evidence gaps. |
| [Compatibility matrix](compatibility-matrix.md) | Hardware and software support assessment. |
| [Open-source TV options](open-source-tv-options.md) | Armbian/LibreELEC comparison and Android-app limitations. |
| [Backup plan](backup-plan.md) | Proposed extraction, consistency and integrity requirements. |
| [Recovery plan](recovery-plan.md) | Recovery prerequisites and unverified recovery routes. |
| [Risk assessment](risk-assessment.md) | Why an unverified firmware flash was not attempted. |
| [Performance and app compatibility](performance-and-app-compatibility.md) | Performance findings, Android/ABI limits and Brave investigation. |
| [Brave repair record](brave-arm32-repair-plan.md) | ARM32 package replacement and startup test. |
| [Music app installation](music-app-installation.md) | Spotify/Audiomack signatures, installation and limits. |
| [Dynamic wallpapers](dynamic-wallpapers.md) | TVMaze API choice, integration and verification. |
| [Recent apps](recent-apps.md) | Optional system task-switcher shortcut, dismissal and Force stop. |
| [Bluetooth audio](bluetooth-support.md) | Existing driver/assets, unidentified controller and external hardware limits. |
| [Progress history](progress.md) | Investigation checkpoints and later results that supersede earlier ones. |

Public source references are in [research/sources.md](../research/sources.md).
Raw diagnostics, device-tree archives, APK downloads and backups are private local
artifacts and are not part of the repository. References to diagnostic timestamps
identify investigation evidence, not files available for download here.

For the native launcher, use [Evening Home](../launcher/README.md).
