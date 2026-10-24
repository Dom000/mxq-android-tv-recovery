# Live partition map

Collected 2026-10-24 Africa/Lagos. Sources: `diagnostics/20261024T001014+0100/partition-links.txt`, `partitions.txt`, `block-geometry.txt`, `mounts.txt`, `cmdline.txt`. Root used only for metadata reads. **No partition contents have been dumped.**

The alias directory is `/dev/block/platform/30020000.rksdmmc/by-name/`. Names below preserve the firmware spelling, including `baseparamer`.

Sizes and start LBAs are from kernel sysfs; sectors are 512 bytes. `/proc/partitions` KiB counts agree with these sizes. Mounts are resolved using the actual aliases, not guessed partition numbers.

| Name | Block device | Start LBA (absolute) | Bytes | MiB | Mount | Filesystem | Criticality / role | Priority |
|---|---|---:|---:|---:|---|---|---|---|
| uboot | mmcblk0p1 | 16384 | 4,194,304 | 4 | — | Unknown / raw container | Boot chain | P0 |
| trust | mmcblk0p2 | 24576 | 8,388,608 | 8 | — | Unknown / raw container | Boot chain / trusted OS | P0 |
| misc | mmcblk0p3 | 40960 | 4,194,304 | 4 | — | Unknown / raw container | Boot/recovery selection | P0 |
| baseparamer | mmcblk0p4 | 49152 | 1,048,576 | 1 | — | Unknown / raw container | Display/board parameters | P0 |
| resource | mmcblk0p5 | 51200 | 15,728,640 | 15 | — | Unknown / raw container | DT / boot resources (inferred) | P0 |
| kernel | mmcblk0p6 | 81920 | 12,582,912 | 12 | — | Unknown / raw container | Kernel | P0 |
| boot | mmcblk0p7 | 106496 | 12,582,912 | 12 | — | Unknown / raw container | Android boot | P0 |
| recovery | mmcblk0p8 | 131072 | 33,554,432 | 32 | — | Unknown / raw container | Recovery | P0 |
| backup | mmcblk0p9 | 196608 | 67,108,864 | 64 | — | Unknown / raw container | Vendor backup role unverified | P0 |
| cache | mmcblk0p10 | 327680 | 134,217,728 | 128 | /cache | ext4 | Runtime / data | P2 |
| metadata | mmcblk0p11 | 589824 | 16,777,216 | 16 | /metadata | ext4 | Runtime / data | P1 |
| kpanic | mmcblk0p12 | 622592 | 4,194,304 | 4 | — | Unknown / raw container | Runtime / data | P2 |
| system | mmcblk0p13 | 630784 | 2,147,483,648 | 2048 | /system | ext4 | Android / drivers | P0 |
| userdata | mmcblk0p14 | 4825088 | 5,364,514,816 | 5116 | /data | ext4 | Runtime / data | P1 |

P0: preserve before experimentation; P1: data/runtime state, with offline consistency requirements; P2: lower restore priority. Criticality is conservative engineering assessment based on names and the running boot chain; unmounted container formats have not been inspected.

## Whole device and unnamed areas

`/dev/block/mmcblk0` is **15,302,656 sectors = 7,834,959,872 bytes ≈ 7.297 GiB**. `blockdev --getsize64` independently agrees. The first named partition begins at LBA 16,384, leaving **8,388,608 bytes (8 MiB)** outside all named partitions. This region may contain Rockchip ID blocks, DDR/miniloader data, parameter records and redundant copies; exact contents are unverified. Do not mistake a set of named-partition backups for a complete bootloader backup.

The named partitions are contiguous from LBA 16,384 to the device end. The userdata partition ends exactly at LBA 15,302,656. A full user-area image includes the unnamed first 8 MiB and all 14 partitions. No `mmcblk0boot0` / `boot1` nodes were exposed. `mmcblk0rpmb` exists with a 2 MiB reported node size; authenticated RPMB is not an ordinary restorable partition and is excluded from the proposed raw user-area image. Do not attempt to read/program its keys or change eMMC configuration.

## Rockchip packaging and offset discrepancy

The running command line contains `mtdparts=rk29xxnand:...` despite actual `storagemedia=emmc`. This is strong evidence of Rockchip legacy parameter-based partition handling rather than proof of physical NAND. `uboot`, `trust`, separate `kernel`/`resource`, and `loader.timestamp=2021-07-30_11:45:27` fit that boot chain. No original `update.img`, parameter file or MiniLoader has been obtained; packaging is not verified from an image header.

**Every observed named-partition start is 0x2000 sectors (4 MiB) greater than its command-line offset.** Example: command-line `uboot @ 0x2000`, actual start `0x4000`; command-line `system @ 0x98000`, actual `0x9a000`; command-line userdata `0x498000`, actual `0x49a000`. This is consistent with a reserved Rockchip base offset, but the restore tool's address semantics must be established independently. Never use command-line offsets as absolute restore addresses or blindly add/subtract offsets in a flash tool.

`system` is mounted ext4 read-only; cache, metadata and userdata are ext4 read-write. The 2 GiB system partition contains a roughly 716 MiB mounted filesystem; filesystem usage is not the partition-image size. Recovery is a 32 MiB block partition; its image validity and ability to boot remain unknown. There is no exposed partition named `parameter` or `loader`.
