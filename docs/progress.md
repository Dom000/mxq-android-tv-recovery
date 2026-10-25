# Durable investigation checkpoint

Updated 2026-10-24, Africa/Lagos. **Safe diagnostics/research completed for this pass. Remain on current firmware. No verified upgrade and no demonstrated recovery path.**

## Scope and user steering

The original task is to investigate a real MXQ Pro 4K RK3229 box for a stable lightweight Android TV OS, with SSV6158 Wi-Fi, remote, HDMI and accelerated video working. The enclosure cannot be opened. User later asked to check open-source TV OS options online; Armbian and LibreELEC were compared. The user then explicitly reaffirmed that it **must be an Android TV OS that installs and runs Android apps**, so Linux-only OS options, generic tablet firmware and launcher-only conversions are excluded as final solutions. Confirmed equipment: a **USB flash drive** and a **microSD slot on the box**. The user has **no microSD card**. Reader, data cable, reset access and USB boot support remain unconfirmed. User has not authorized a backup, reboot, mode change, flash, install, service change or bootloader alteration. The later single Brave launch-and-observe/Home test was explicitly approved and completed; see the latest checkpoint below.

## Completed

- Created the requested local workspace, all eight requested documents, six macOS-compatible shell scripts, shared read-only Python collector, offline verifier and extra open-source comparison/source register.
- Connected TV was already online; target `get-state` returned device. No normal reconnect or LAN scan needed. All device commands explicitly used `-s 192.168.1.4:5555`; never targeted emulator-5554.
- Confirmed serial, MBOX/rk322x profile, DT rockchip,rk3229 and root uid=0.
- Collected filtered build/CPU/RAM/kernel, Wi-Fi SDIO/module binding, Bluetooth configuration, GPU/HDMI/media-codec assets, input/keylayout, boot/recovery metadata and mounts.
- Resolved every named symlink, recorded kernel absolute start/size and reconciled /proc partitions/mounts/cmdline. Preserved spelling baseparamer.
- Collected all 2,904 device-tree binary properties using in-root base64 and strict host decoding; archive property paths match independent find inventory. Known strings decoded separately.
- Checked Mac storage (401 GiB free), complete eMMC size and backup feasibility. Prepared per-partition destinations/lengths, integrity/consistency requirements and a concrete approval-gated boot extraction proposal.
- Researched Android candidates, including creator TechLife ATV9/10 posts and an uploader listing specifically naming RK3229Q-221P-V1.3/SV6158. No target match, actual upgraded API or trusted compatible binary established.
- Checked current LibreELEC and Armbian source configurations and downloadable release metadata/checksums. Recorded immutable source commits and public links. No OS binary downloaded.
- Inspected SSV6158 public SDIO source: ID 5653:2060 exactly matches this TV. Not shown integrated/working in selected OS builds.
- Prepared independent-recovery requirements and qualitative no-go assessment. No approval-gated operation executed.

## Most important findings

1. API 25, 7.1.2 fingerprint and 3.10.104 kernel contradict release property 11.1. Current software strongly indicates Android 7.1.2; display/security-patch strings are not proof of upgrades.
2. Usable RAM ~998 MiB; eMMC user area 7,834,959,872 bytes (7.297 GiB), fourteen partitions plus an unnamed 8 MiB prefix.
3. Wi-Fi is live ssv6158 / SDIO 5653:2060 / SSV6XXX_SDIO. Bluetooth DT disabled and no HCI device exposed; physical absence unverified.
4. Exact physical PCB and DDR initialization requirements remain unknown. Generic DT has no root model identifying a board revision.
5. Actual sysfs partition starts are 0x2000 sectors above command-line offsets. Restore tool addressing must be established independently.
6. Root binary stdout is not trustworthy unencoded: first tar invalid; corrected in-root base64 transfer preserved all properties. Future backups need encoded transport, exact size and source/host SHA-256.
7. Current LibreELEC and Armbian checked configs expose SSV6051 support, not demonstrated SSV6158 integration. Public matching-ID source is a porting lead, not ready-to-use firmware verification.
8. LibreELEC is a Kodi/Linux TV system; Armbian is Linux desktop/server. Neither runs Android APKs. Stock USB flash boot has not been established.
9. No backup exists, rkdeveloptool is absent, no matched recovery loader obtained, no independent recovery path demonstrated. Do not flash.

## Diagnostic locations and caveats

- `diagnostics/20261024T001014+0100/`: authoritative hardware/partition text and command journal. Its **device-tree.tar and extracted tree are invalid**, replaced below. Some optional file probes print expected absence errors; shell status alone does not establish hardware presence.
- `diagnostics/20261024T001107+0100/`: authoritative full DT binary archive, raw properties, inventory/index and hashes; all 2,904 properties matched.
- `diagnostics/20261024T002211+0100/`: successful live wrapper connection/identity/root recheck.
- Any directory with `OFFLINE-FAKE-ADB-TEST.txt` contains a fake-target safety test, **not TV evidence**.
- All diagnostic/backup directories are Git-ignored. No private dumps, account stores, MACs/SSIDs or eMMC serial/CID were intentionally collected. Raw DT may contain identifiers and stays private.
- Main diagnostic snapshots have SHA256SUMS files. These validate local diagnostic bytes, not firmware restore readiness.

## Commands executed

Exact collector command arguments are in each private `commands.jsonl`. The diagnostic shell script and fixed command maps in `scripts/diagnose.py` are reviewable. Entry points executed:

```text
adb -s 192.168.1.4:5555 get-state
adb -s 192.168.1.4:5555 get-serialno
adb -s 192.168.1.4:5555 shell [fixed read-only getprop/uname/DT/su-id probes]
adb -s 192.168.1.4:5555 shell [root ls/readlink/cat of named aliases, proc and sysfs metadata]
python3 scripts/diagnose.py all
python3 scripts/diagnose.py device-tree
./scripts/inspect-root.sh
```

Additional root metadata probes checked tar/base64/xxd/od tools, DT file counts/listings, SHA command availability/BusyBox applets, pipefail support and `blockdev --getsize64 /dev/block/mmcblk0`. Only ioctl capacity metadata was read by blockdev; no block contents, firmware headers, RPMB, bulk partition hashes or dumps were read. Host commands included df/sw_vers/uname, file writes, Python syntax/safety checks, Bash syntax checks, Git ignore/diff checks, web search/open and TLS-verified curl of public source/API/checksum text. No downloaded source was executed.

## Validation completed

- All six shell wrappers pass `bash -n`; Python collector/verifier compile.
- Live root wrapper reverified correct TV identity and uid=0.
- Offline verifier accepted intact binary + matching source hash; rejected truncation, same-length corruption, empty manifests and parent traversal.
- Fake ADB returned emulator/wrong identity: collector refused before any root operation, with every ADB invocation explicitly targeting the fixed TV serial.
- Full DT archive inventory exactly matched independent live file listing.
- Git ignore checks confirmed backups and diagnostics excluded. No existing app source files modified. Whitespace/content checks completed on generated text.

## Remaining work and next recommended action

**Do not proceed to firmware flashing.** No operation is waiting for assumed approval.

For Android: obtain a legitimate candidate artifact and published hash with actual API/board/DDR/SSV6158 evidence. Inspect it offline, compare DT/kernel/remote/partition packing and provenance. The gated DCBOX source can only be pursued through the user's own authorized access, not bypassing login/payment.

For OS selection: focus on native Android firmware only. Armbian/LibreELEC fail the mandatory app requirement and should not be proposed for installation. Their source research can still inform recovery and driver compatibility. Do not suggest an unverified Android container or emulator as a solution on this box.

For preservation: the backup plan proposes a 12 MiB boot read to establish safe transfer/throughput, but requires explicit approval with exact command/effect/risk/rollback. Complete consistent offline imaging requires a separately approved external boot with internal storage unmounted. Establish how original firmware can be restored after Android fails before attempting any upgrade.

Open questions: accessible external reset/OTG path and acquisition/borrowing of a microSD card with host-writing access; exact PCB/DDR; matching loader; actual candidate API/provenance; SSV6158 modern-kernel integration; target remote/HDMI/audio/decode tests; streaming/DRM eligibility. Do not repeat diagnostics already saved unless the device or candidate changes.

Final scope clarification: actual Android TV framework/TV interface and remote navigation are required. AOSP/LineageOS searches found no verifiable target release; the generated HK1 TWRP tree is recovery-only. The LineageOS official device-list fetch failed (403), so do not turn that search limit into an absolute claim that no build exists. No new device operation followed these clarifications.

Equipment update — 2026-10-24: microSD slot confirmed; no card available. Card-based recovery testing is pending suitable media and host-writing access. Safe firmware/source research may continue. No state-changing action or purchase authorized by this update.

## Performance/app-closure investigation — 2026-10-24

User asked about speeding up existing firmware and upgrading Android for newer apps, reporting Brave closes. Read-only snapshot saved in diagnostics/20261024T002948+0100; launcher resolution in 20261024T003029+0100. Brave 1.60.127, minSdk 24, targetSdk 34, armeabi-v7a. Device API 25 meets this installed minimum/ABI; no matching recent Brave crash record and no running Brave process. Current Brave officially requires Android 10+, while the installed version is an unsupported historical Android-7 hotfix. Do not recommend reinstalling the hotfix already present.

CPU/load low at snapshot, /data ~3.25 GiB free, UI already 720p. No current evidence establishes the closure's cause or proves a memory bottleneck. Added reusable read-only performance mode/wrapper and docs/performance-and-app-compatibility.md with observations, sources and exact proposed app-launch/Home commands. Original approval boundary still applies: no launch, setting change, package modification or data clearing executed. Next useful step is one approved observed Brave launch to capture filtered failure evidence. Current OS optimization cannot supply missing APIs; firmware upgrade recovery/compatibility requirements remain unmet.

## Approved Brave launch checkpoint — 2026-10-24

User replied “yh go ahead” to the exact single-launch/read-only observation/Home proposal. Executed the approved am start command once against 192.168.1.4:5555 after identity guard, observed for ~14 seconds after activity startup, and executed KEYCODE_HOME in a finally block. Home focus independently confirmed. No settings/packages/data reset/force-stop/reboot/firmware changes were made. Browser startup may have written its ordinary application data.

Confirmed startup exception: null Resources.getStringArray in AwResource.getConfigKeySystemUuidMapping, then JNI FATAL and SIGTRAP in main/GPU processes. EGL_BAD_DISPLAY/EGL_BAD_ATTRIBUTE also logged. Brave disappeared within seconds. This is evidence of an actual startup crash, not proof of insufficient memory or a reason to indiscriminately debloat. Do not label GPU/DRM/provider as the proven root cause.

Configured WebView provider is com.android.chrome (installed 101.0.4951.61); separate Google WebView 55.0.2883.91 is listed disabled with enabled=3. webviewupdate service exists but dumpsys returns empty, so service-selected provider remains unconfirmed. No package/provider repair performed. No APK or browser-profile dump taken. No second launch authorized.

Evidence directories: 20261024T003331+0100 (approved launch, process samples, signatures, Home command), 003414 (filtered stack details), 003437/003514 (provider metadata, Home confirmation), 003543 (selected DRM library string markers; none matched). Host date Africa/Lagos 2026-10-24; device log timestamps show October 23 23:33 and are preserved as device-clock values. Raw live logs were not persisted, and no buffers cleared. See performance-and-app-compatibility.md for immediate failure and uncertainty distinctions. Next safe work is APK/provider compatibility/provenance investigation before proposing a justified reversible repair. The original backup/flashing/recovery boundary remains unchanged.

## Brave APK variant diagnosis — 2026-10-24

Latest “go ahead” authorized read-only APK/provider inspection, not package installation. Device APK size 281343991 and source SHA-256 exactly match official Bravearm64Universal.apk 1.60.127 / versionCode 426012722. APK installer name is com.uptodown, but byte identity matches publisher artifact. The ARM32 library in the Universal 64_32 variant is WebView-only (no ChromeMainDelegate marker); the ARM64 library has the browser implementation. This fits the observed AwResource startup crash on ARM32. Earlier primary-ABI-match reasoning was insufficient and has been corrected.

Downloaded official BraveMonoarm.apk (164302719 bytes, versionCode 426012720) and exact original Universal APK to ignored diagnostics/publisher-apk-inspection. Both match publisher checksum sidecars; apksigner verifies v2/v3 and identical certificate SHA-256 9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac. Original artifact matches device-side SHA-256. No app profile copied and no firmware/userdata dump. Source metadata in research/brave-release-1.60.127.json and research/brave-apk-variant-assessment.json.

Pulled static system framework-res.apk (no user data) for offline aapt inspection. Allowlist supports Chrome Stable and fallback Google WebView, not Brave; current provider setting/fallback state is not independently a reason to change it. Device ro.debuggable=1 userdebug; AOSP7 source allows requested package version downgrade on debuggable builds. Candidate same visible version but lower versionCode needs install -r -d, with stop-on-refusal and no uninstall.

Concrete package repair/test/rollback plan prepared in docs/brave-arm32-repair-plan.md. It is a strong, narrowly justified next test, not yet a proven successful repair. No APK installation/provider change/second launch executed. Next step is explicit approval for the exact ARM32 replacement and one observed launch/Home. Original APK is a binary rollback artifact, not browser-data backup. Android 10+ requirement for current Brave remains unresolved by this legacy-build repair.

## ARM32 replacement authorized — 2026-10-24

User explicitly approved the proposed official ARM32 install plus one observed launch/Home test (“yh go ahead”). Preflight rechecked TV identity, no running Brave process, old versionCode 426012722, installed original hash, both local artifact hashes/signatures and storage. Initial streamed adb install -r -d reached a 180-second host timeout with no success/rejection result. Post-timeout inspection confirmed the original package remained, no active install sessions and no running Brave process. No uninstall or launch was performed.

Retried the same approved package replacement with adb install --no-streaming -r -d and a longer transfer deadline. This changes only ADB transport/staging, not package, flags or intended installation scope. Read-only progress check confirmed the staged file /data/local/tmp/BraveMonoarm-1.60.127.apk was growing (35,323,904 bytes at first check). Installation outcome and the approved single launch are still pending in this checkpoint; consult later entries before resuming anything. Do not treat the timed-out attempt as Android rejection or successful installation.

## ARM32 repair completed — latest checkpoint, 2026-10-24

**Current state: official BraveMonoarm.apk 1.60.127 / versionCode 426012720 is installed.** Authorized single post-repair launch/Home test completed. The prior immediate AwResource/JNI/SIGTRAP startup crash did not recur across 24 samples/30.13 seconds; main PID 7553 remained throughout, with renderer0/GPU processes at the end. Actual browsing remains untested because the app is at welcome/onboarding, whose consent/settings controls were not pressed.

The first streamed installer hit a 180-second host timeout; inspection found no active session or package change. Equivalent non-streaming retry with the same -r -d flags succeeded: uploaded 164302719 bytes at ~1.0 MB/s, Android returned Success. Device-installed APK hash exactly matches the prepared official ARM32 artifact. Preflight verified original hash, signatures and no running browser. Staged temporary APK was automatically removed by ADB. No force-stop, uninstall, app-data clearing, provider switch, firmware write or reboot. User-data retention requested with -r; no browser-profile backup or bitwise state comparison exists.

Post-repair logs show secondary isolated-renderer starts/exits and old GMS/EGL warnings, so do not claim complete browser stability or diagnose all app closures as solved. Prior immediate main-process crash is resolved in the observed startup test. Current Brave still requires a newer OS than Android7. Home returned in finally, then independently confirmed via resumed/focused activity and window focus. Do not relaunch/install again automatically: approved test is complete.

Install evidence: diagnostics/20261024T004956+0100; validation: 005404; secondary process inspection: 005513; final Home/package/staging status: 005552. Earlier attempt/post-timeout diagnostics preserved. Exact original Universal APK remains locally verified for binary rollback, which is not a profile restore and has not been executed. Updated brave-arm32-repair-plan.md and performance-and-app-compatibility.md distinguish earlier proposals from final outcome. Next useful validation is actual browsing after the user makes their own onboarding choices, with separate scope for any agent-driven interactions beyond the completed test.


## Evening Home launcher — latest checkpoint, 2026-10-24

User separately requested a sleeker launcher UI, clarified that wallpaper should
cycle through action/superhero/cartoon scenes (not movie shelves), requested real
movie artwork fetched online, original app icons, Wi-Fi connection indication and
remote trackpad support. This does not replace the OS-upgrade objective or change
Android compatibility. No system launcher source was available; implemented a
separate native Java/platform-Views launcher under launcher/, leaving the unrelated
Next.js site untouched.

Built dev.mxq.home v0.1.0, minSdk25, targetSdk28. Current APK is
launcher/build/EveningHome-preview.apk, 1,065,909 bytes, SHA-256
6ec87800a3fe6b69fcdedb11e02104ccbb86aa41f388f0a76c879b0f642ad9ff.
SDK packaging/alignment and APK v2/v3 signature verification pass. HomeEntry alias
is disabled in the compiled manifest: installing a preview cannot register a new
Home handler. Original com.droidlogic.mboxlauncher/.Launcher remains installed and
default; no new APK was installed or launched. Exact install/open proposal, risks,
effect and rollback are in launcher/INSTALL.md and still require explicit approval.
Do not treat earlier Brave-repair approvals as permission for launcher operations.

Implemented Home/Apps, installed TV/standard app discovery, saved favorites,
D-pad focus/Back, pointer click/hover/hand cursor, trackpad horizontal scroll and
vertical mouse-wheel fallback. Header includes live Android Wi-Fi connected or
disconnected glyph/text, updating on connectivity broadcasts and linking to system
Wi-Fi settings. Only ACCESS_NETWORK_STATE normal permission is requested; no
Internet/location/SSID/password access. Wi-Fi link status does not prove Internet
access. Browser connection states are explicitly simulated, not live TV telemetry.

Fetched four actual publisher images from Universal (Fast X), Disney/Marvel
(Avengers: Endgame), Sony (Across the Spider-Verse) and DreamWorks (Kung Fu Panda 4).
Original image bytes/credits/source URLs/hashes are recorded in
launcher/assets/wallpapers/catalog.json and launcher/WALLPAPERS.md. Native slideshow
has 15-second dwell, 900ms crossfade, Next scene and saved pause/resume. RGB565 frame
decode is capped at 1600x900 off the UI thread, with only two active frame references.
Timers/fades stop in background/detach; system animation scale zero freezes auto
rotation/fades. No memory/performance improvement has been measured. Generated
wallpaper alternatives were superseded and are not packaged.

Read-only ADB work reverified TV identity/display/launcher metadata and inspected
public installed APK resources, always fixed serial 192.168.1.4:5555. Extracted six
favorite app icons without full APK pulls or app-data reads. Selective ZIP ranges
used read-only dd with if=regular-APK, skip/count and stdout (never of= or a block
device), base64 transport, bounded metadata budgets and host-only ZIP/resource
parsing. Brave icon reused the previously verified byte-identical publisher APK.
Native code uses Android PackageManager icons; preview now uses the exact extracted
raster resources. Resource hashes are in launcher/preview/icons/catalog.json.
Private diagnostics include 005947 launcher metadata, 012530/012607 icon-tool
inspection and 013329 final icon collection; intermediate failed range/metadata
reads changed no device state. An incidental non-Home screenshot was deleted
immediately; no Home key, app launch or further screenshot followed.

Passed meaningful host ordering/persistence tests, manifest/permission checks,
source/native palette matching, exact packaged asset hashes and six icon hashes.
Browser checks pass: all 14 Home controls reachable with arrows, mouse-wheel rail,
Home/Apps, favorites empty/Cancel/Save, modal focus restoration, real icons, Wi-Fi
connected/disconnected simulation, manual/automatic wallpaper change, pause and
16:9 widths 1920/960/640, with no JS errors. Strict premium audit and DESIGN.md lint
pass without warnings/errors. See launcher/VERIFICATION.md and preview/verification.json.
Native D-pad/physical trackpad, actual app launch, Wi-Fi updates/settings, overscan,
font scaling, decoder/lifecycle behavior and heap/CPU remain untested. No emulator
commands, firmware/settings changes, reboot, app clearing or new APK installation.

Next step: ask approval for the two exact install/open commands in INSTALL.md,
then preview alongside the stock Home. Enabling HomeEntry/default selection is a
separate later approval. Do not install, activate Home or run rollback implicitly.


## Local launcher finalization — MBOX disconnected, 2026-10-24

Owner asked to finish locally and explicitly reported the MBOX disconnected.
No ADB connection attempt, device query, install, launch or Home change ran in
this finalization. Installation and all native-device tests remain deferred.

Reverified current APK signature (v2/v3), alignment, compiled disabled HomeEntry,
network-status-only permission, native input timestamps, host tests, exact bundled
artwork/icon hashes and strict project audit. Existing desktop interaction results
remain applicable; no native runtime verification is implied. APK remains unchanged:
1,065,909 bytes; SHA-256
6ec87800a3fe6b69fcdedb11e02104ccbb86aa41f388f0a76c879b0f642ad9ff.

Prepared launcher/build/delivery/EveningHome-0.1.0-preview.apk and matching checksum,
README, public signature evidence, credits/catalogue and verification metadata.
Seven-file handoff ZIP: launcher/build/EveningHome-0.1.0-preview.zip (1,043,902 bytes).
ZIP integrity and packaged APK hash verified. No signing keys, diagnostics, app
profiles or credentials included. launcher/READY.md links the final artifacts;
README/INSTALL/VERIFICATION record the disconnected state and test limitations.

Local implementation and handoff are complete. When the box is available, follow
the existing identity check and authorized INSTALL.md plan. Physical remote/trackpad,
Wi-Fi updates/settings, actual app launch, native rendering and performance still
require device testing. Do not automatically connect/install/activate Home on a
future turn solely because this package exists.


## Launcher installed and selected as default — latest checkpoint, 2026-10-24

Owner reported MBOX powered on after acknowledging preview installation/testing.
Reconnected only fixed serial 192.168.1.4:5555; RK3229 guard passed, API25, ~3.7GiB
available /data, package absent and stock Home confirmed. Both host APK copies
matched reviewed SHA-256/signature. adb install returned Success, then one
am start -W dev.mxq.home/.MainActivity returned Status: ok / TotalTime796ms.
Native screenshot confirmed actual installed app icons, movie wallpaper, focus
indicator and Wi-Fi connected text. Initial PID3147 present across eight samples
in25.87sec, no matching launcher crash signatures in sampled buffer. One startup
PSS snapshot15559KiB is not a peak/complete graphics-memory estimate. Owner used
other apps during observation, so do not call it continuous foreground testing.
A later non-root sha256sum attempt was unavailable; no device APK hash result was
obtained. Host artifact hashes/signature were verified before successful install.

Owner explicitly confirmed D-pad works and requested this launcher as default,
including the remote Home button. Actual device help confirmed set-home-activity.
Ordinary shell pm enable --user0 was rejected with SecurityException and changed
nothing. The exact component enable was retried using existing su access and
succeeded. Root was limited to the authorized component override; no partition
or system-file write. Shell cmd package set-home-activity --user0
 dev.mxq.home/.HomeEntry returned Success. Sent KEYCODE_HOME once, then verified
Home resolution, resumed activity and focused window all dev.mxq.home/.HomeEntry.
Final processPID4341; component activation may restart the app. Do not label that
PID change a crash. Original system MboxLaunchers.apk remains installed/enabled.
No app-tile launch, Wi-Fi toggle, old launcher disable/removal or reboot by agent.

Evidence: 183517 preflight;183639 installation/launch;183755 native screen/memory;
183923 bounded observation;184139 help/before-state;184306 shell component refusal;
184511 successful root component enable, shell default selection/Home test;
184721 settled default/resumed/focused-window confirmation. All are private
20261024T...+0100 diagnostic folders. Device clock differed from host; do not
change it without instruction. No emulator commands executed.

Current default is Evening Home; this supersedes earlier stock-default/pending
installation checkpoints. Native APK bytes remain unchanged SHA-256
6ec87800a3fe6b69fcdedb11e02104ccbb86aa41f388f0a76c879b0f642ad9ff.
Packaged manifest still defaults HomeEntry=false; per-user device override=true.
README/READY/INSTALL/VERIFICATION and handoff ZIP metadata updated to distinguish
these states. Recorded rollback restores stock preferred Home with cmd package
set-home-activity, then withdraws Evening Home's alias via root pm disable-user,
then presses Home. Rollback is prepared only, not executed or implicitly approved.

Physical trackpad, physical remote Home confirmation, Wi-Fi transition/settings,
app-tile launch, sustained performance and reboot persistence remain pending.
Owner-confirmed D-pad and verified Android Home action are completed checks.


## Classic Home 0.2.0 and music controls — latest checkpoint, 2026-10-24

Owner requested a clean, non-verbose Home, then classic/simple inspiration from
https://www.pinterest.com/pocketshopapp/smart-tv-ui/. The board returned403; no
exact pins were viewed. Removed welcome headline, promotional copy, wallpaper
caption and instruction/footer watermark. Quiet underlined tabs, 10dp flatter
controls, continuous shading, real icons and a spacious wallpaper area. Compact
Music bar appears after observed audio activity, retains play/pause/Next while
paused, can be hidden, and sends ordinary media keys. No track metadata or new
permissions; playback remains the player's responsibility. Polling stops when
the activity pauses. Browser music behavior is explicitly simulated.

Built/signed dev.mxq.home0.2.0/versionCode2; SHA256 48435eaef1b35a0d2611cf02d53a0656f6f2bc9b34686c8981fb57b78a4b28f5,
1065909 bytes. Same certificate, API25, only ACCESS_NETWORK_STATE, packaged
HomeEntry=false. Host ordering/contract/assets checks, strict audit, design lint
and updated browser tests pass. All15 Home controls including simulated music
reachable by arrows. Browser Home/Apps, favorites, Wi-Fi, real artwork rotation,
pause, pointer wheel and responsive frame tests pass without JS errors.

Fixed-target guarded update authorized by these feature requests: install-r
returned Success, Home key and package/default queries confirmed version2 and
HomeEntry enabled/default. Evidence191114+0100. At191145+0100 native screenshot
confirmed classic layout, icons and Wi-Fi; Home resumed/focused, PID5893, single
PSS15278KiB snapshot, no matching sampled crash signatures. No live player test,
performance claim or reboot. Previous default/user preferences retained. No root
needed for this update. Handoff/README/INSTALL/VERIFICATION updated to0.2.0.

Spotify/Audiomack request remains pending. Public metadata/certificate research
performed, but downloads returned403/404 or needed a human CAPTCHA/installer.
Old Play client opened a market route; UI dump timed out and later foreground
was Home. No Install button/action observed and no failure cause established.
Neither requested package was installed. No modified/unchecked APK, account login
or unasked store-service update. See docs/music-app-installation.md. Real music
transport testing, physical trackpad/remoteHome, Wi-Fi transitions and sustained
native performance remain pending. These results supersede earlier current-APK
hash/version/layout checkpoints, while preserving their historical evidence.


## Online TVMaze backgrounds0.3.0 — latest checkpoint

Owner asked about z9m/androidtvbackgroundWebGui or an API, then explicitly chose
TVMaze without an API key. Inspected repository README/TMDB.py and official
TMDB/TVMaze docs. The linked project is a separate web editor/server, not the
Android app; no .env/token read, code execution or server deployment. Implemented
original native OnlineWallpapers provider and OnlineWallpaperPolicy. Curated
landscape action/animation galleries,8 compressed images<=3MiB each, daily refresh,
30min failure cooldown, forced refresh in Settings, HTTPS/CDN allowlist, dimensions,
atomic provenance cache/pruning and lifecycle cancellation. Two-frame sampled
renderer retained; source/status/credits in Settings/About. Home remains clean.
Only new normal permission INTERNET. No API key/account, extra service or root.

Built dev.mxq.home0.3.0/code3, 1074182 bytes, SHA256 747298faae4139b420329902b3e945eb8d4c5d2f7d400efaa29a773f052d9353,
same certificate. Host safety/freshness/clock/ordering/contract/asset tests pass.
Browser source fixture tests cover online image, unsafe/poster rejection, failure
retention and bundled fallback; existing remote/pointer/slideshow tests pass.
Strict audit/design lint pass. Native same-key update succeeded at193304+0100,
HomeEntry/default retained. At193417+0100, actual Home displayed downloaded Batman
art. At193539+0100, filtered own-tag status confirmed TVMaze fetched8 images.
No Wi-Fi change, live airplane/offline test, firmware write or reboot. Source
research/limits: docs/dynamic-wallpapers.md. Updated versioned0.3.0 handoff.

Spotify TV1.81.55 is installed and first launch returned Status:ok (192146+0100).
Downloaded mirror page labelled1.134.2, but actual APK metadata is1.81.55/code1815500;
record actual version. API21, ARMv7 present, Leanback launch activity. Signature
verifies and cert matches known Spotify TV cert fd450f5f94b9dba0ae7f6b19e0b512e041ea7fc2cc291b71737b319ba7f4b3d4.
APK SHA25646b12520b7dbda118d286fddfaf8d4d1cba553162f536f3e12ac9618cfee2b76.
No sign-in or playback by agent. Filtered UI dump showed no allowlisted action
labels; no raw UI, account or device-pairing code saved. Audiomack6.59.1 ARMv7
bundle download is still running. Earlier4.12.5 APK actually verifies against
publisher assetlinks certificate despite store metadata warning, but it lacks
Leanback and was not installed. Prefer the TV-enabled6.x build now downloading.


## Both music apps installed — latest checkpoint

Completed the existing Spotify/Audiomack installation request. Spotify TV1.81.55
was already installed/opened at192146+0100. Audiomack6.59.1/code28914 ARMv7 bundle
finished downloading (87,233,274 bytes; archive SHA25690274926e0baf86c8f998dacd834386c0c2fe18e02c2059c17c889e891a5a650).
Inspected/extracted only4 bounded APK entries; CRC passes. Base + ARMv7 + English
+ xxhdpi splits all verify under publisher cert3562a57fd9e67dc1ef9f3c35a2c5448069a1b4534b91a8c8f9fd8342fcc9fcd4,
anchored in official audiomack.com assetlinks. Actual minAPI21/target34. No dedicated
Leanback launcher entry; optional TV/touch features do not prove a TV interface.
User was informed of standard Android UI/pointer limitation before installation.

Initial install-multiple exceeded120sec (194555). Immediate PM check absent;
retry with420sec budget reported ADB offline at finalization (194831). Refreshed
only host transport for fixed serial via disconnect/connect; identity passed
195225. At195246, preflight confirmed correct Audiomack installed, so proposed
non-streaming copy/install was not run. PM confirms code28914, version6.59.1,
ARMv7; standard launcher resolves and Leanback does not. First am start returned
Status:ok, AuthenticationActivity, TotalTime11191ms. No sign-in, permission grant,
purchase, playback or user-account UI capture. Both requested packages are now
installed/opened; service/playback/transport testing remains pending. Evening
Home0.3/default and TVMaze8-image cache remain in place. No Wi-Fi setting change,
root install, firmware write or reboot. Updated docs/music-app-installation.md
with exact results and per-split hashes. Earlier pending statements are historical.

## 2026-10-25 — remote-friendly Recents complete

The owner requested a simpler replacement for the native X/Clear all controls.
Evening Home0.5.8 now has a separately enabled Accessibility-backed task panel
with Open/Close, confirmed Clear all, paging and System view. No root or firmware
modification. Current owner-confirmed endpoint is192.168.1.4:5555; identity guard
passed. Native key focus/confirmation/cancel tests passed, and the owner confirmed
actual Close. See launcher/VERIFICATION.md and docs/recent-apps.md for the exact
artifact, privacy boundary and vendor compatibility work.

Injected pointer tests hit a vendor NavBarEventListener NullPointerException and
framework restarts. They were stopped; further automation uses D-pad keys only.
No reboot command/system-file edit or app-data clearing was issued.

## 2026-10-25 — classic Recents and original icons

Version0.6.0 installed with the same signer and retained default Home. Added
PackageManager task icons through exact installed-label aliases, neutral fallback
for unknown/ambiguous names, flat dividers, quiet buttons and less copy. Native
icon counts and D-pad/confirmation checks passed. No new permission, task-data
upload, root command or pointer injection. See launcher/VERIFICATION.md.
