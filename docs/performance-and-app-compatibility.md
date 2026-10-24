# Speed improvements and Android application compatibility

2026-10-24, Africa/Lagos. User now authorizes investigation of current-firmware performance as well as an Android TV OS upgrade. The reported symptom is apps closing, including Brave. Original requirements for approval before state-changing commands remain in effect. **The separately approved ARM32 Brave package replacement is installed and startup-tested. No system setting, unrelated package or service was changed; no browser data was deliberately cleared. Both the initial diagnostic launch and the approved post-repair launch/Home test are complete. Normal installation/startup may write app data.**

## Live read-only findings

Sources: private `diagnostics/20261024T002948+0100/` snapshot and `20261024T003029+0100/brave-launcher-resolution.txt`. Device identity was rechecked before both passes. Commands are recorded in their command journals. The reusable `scripts/collect-performance.sh` takes the same type of snapshot without root, app launches or configuration changes. It only saves selected crash signatures, never a raw browser crash log or browsing history.

| Observation | Finding | Meaning / limit |
|---|---|---|
| Android | API 25, 7.1.2 fingerprint; display release 11.1 | Framework capability is not established by the displayed release string |
| Brave installed | com.brave.browser, version 1.60.127, versionCode 426012722 | Installed identity/version from package manager; publisher signature not inspected |
| Brave requirements | minSdk 24, targetSdk 34 | API 25 meets this installed app's declared minimum; targetSdk 34 does not mean it requires Android 14 |
| Brave ABI | armeabi-v7a | Matches the 32-bit ARM device; basic ABI mismatch not indicated |
| Brave running | No Brave process in the snapshot | App wasn't running; not evidence of why it previously closed |
| Existing crash buffer | No selected Brave signature in last 600 crash-buffer lines | No causal diagnosis yet; low-memory process kills may not create Java crash records |
| CPU/load | Load average 0.18 / 0.32 / 0.40 | Low at observation, not a startup/playback stress test |
| RAM | 997.97 MiB total; 94.54 MiB raw free; 578.77 MiB page cache; no swap | Raw free alone does not establish an out-of-memory problem; page cache is not fully equivalent to available app memory |
| Storage | /data 33% used, 3,412,032 KiB available (~3.25 GiB) | This snapshot does not indicate full userdata storage |
| UI resolution | wm reports 1280x720, density 160 | UI already 720p; earlier framebuffer output was 1080p60, which is a separate layer/output setting |
| Animations | window 0.6, transition 0.0, animator 1.0 | Some animation reduction already present; further changes affect perceived motion, not Android APIs |
| Background PSS | Launcher ~39.5 MiB; GMS persistent ~37.3 MiB; media-center processes ~22.8 MiB combined | Candidates for investigation only; no service disabled. Small test apps each ~3.6 MiB are not proof of a major bottleneck |
| Thermal | Standard thermal_zone entries absent | Temperature/throttling not established; do not infer cool operation |

## Brave: current support versus the installed legacy build

[Brave's current requirements](https://support.brave.app/hc/en-us/articles/360021357112-What-are-the-system-requirements-to-install-Brave) specify **Android 10 or later** (checked 2026-10-24). Thus speeding up Android 7 does not make the current Brave release supported, and Android 8/9 alone would not meet the published current requirement.

Brave's [official Android-7 crash notice](https://community.brave.app/t/android-7-crash-on-launch-issue-steps-to-resolve-with-security-warning/621712) documents a 2025 ad-block parser crash in 1.60.125, with a narrow 1.60.127 fix. **The box already has version 1.60.127**, so reinstalling that historical fix is not a diagnosis or a justified next action. The [publisher's release page](https://github.com/brave/brave-browser/releases/tag/v1.60.127) identifies Chromium 119 and explicitly warns this legacy build is unsupported/vulnerable. No APK has been downloaded or installed, no downgrade/uninstall is proposed, and no browser data has been cleared.

The exact cause on this box remains unknown: possibilities include memory pressure under load, native/GPU failure, app state or another runtime problem. The installed minimum API and ABI match, so do not assert this particular closure is definitely a minimum-API failure. Crash timing and filtered logs during one observed launch are the next useful evidence.

## What current-firmware optimization can accomplish

Potentially improve menu responsiveness and resource use for apps that already support Android 7. It cannot add missing framework APIs, 64-bit execution support, hardware capabilities or app-service certification. [Android's manifest documentation](https://developer.android.com/guide/topics/manifest/uses-sdk-element) distinguishes minSdk from targetSdk; changing a version string or lowering an APK manifest minimum does not supply missing APIs.

[Android TV memory guidance](https://developer.android.com/training/tv/playback/memory) explains that memory pressure can cause lag and process kills, and distinguishes UI from video resolution. Our UI is already 720p, so blindly lowering display settings is not supported by this snapshot.

After a measured before/after baseline and separate approval, possible changes include reducing the remaining animations and evaluating individual optional background apps. Each requires recorded original values, exact commands and rollback. Do not disable GMS, TV services, the launcher, updater or vendor test apps indiscriminately. No evidence yet justifies clearing browser data, factory reset, a RAM booster, USB-backed swap, GPU flags, CPU overclocking or build-property spoofing.

## Approved diagnostic action — completed once

The installed launcher activity resolves to `com.brave.browser/com.google.android.apps.chrome.Main`. The user approved the ordinary startup command below with “yh go ahead”; it ran once:

```bash
adb -s 192.168.1.4:5555 shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.brave.browser/com.google.android.apps.chrome.Main
```

Expected effect: foreground the existing installed browser and observe whether it exits. It may restore tabs, make network requests and update its normal app profile. No root, install, force-stop, uninstall, cache/data clearing or reboot is involved. Record memory/process status and a narrow crash/system-log window without storing URLs, page titles, account data or raw logs. Do not clear existing log buffers.

Return-to-home command, also approved and executed:

```bash
adb -s 192.168.1.4:5555 shell input keyevent KEYCODE_HOME
```

Return to Home changes foreground navigation; it does not erase any normal browser writes or necessarily stop background execution. No automatic force-stop is proposed. If the app exhibits unwanted behavior, stop and ask before further changes. Explicit user approval covered only this launch/observation and return to Home. Both commands completed. No second launch, package change, browser setting, cache/data reset, force-stop, reboot or firmware write was authorized or executed. Further state-changing tests require their own concrete approval.

## Upgrade decision

An actual Android TV framework upgrade remains conditional on board/DDR/Wi-Fi/media compatibility, verified original backups and independent recovery. There is still no verified compatible upgrade for this unit. Android 10 is a necessary published OS minimum for current Brave, not proof that an RK3229 Android-10 ROM will boot, run quickly or provide a supported TV browser experience. Keep the TV bootable while investigating the specific crash first.

## Result of the approved launch — 2026-10-24

**Reproduced and returned to Home.** Host observation is dated 2026-10-24 Africa/Lagos. The TV log clock reports 10-23 23:33; these raw device timestamps differ from the host date/time and must not be silently treated as Lagos time.

- Android reported successful startup into `org.chromium.chrome.browser.firstrun.WelcomeOnboardingActivity`; activity launch TotalTime was 1,864 ms. That measures activity startup, not successful browser initialization or usable page load.
- Main PID 5445, renderer PID 5486 and privileged/GPU PID 5512 were observed. Main disappeared between observations ~3.8 and 5.0 seconds after launch-command completion; all Brave processes were absent by ~6.3 seconds. This is a short startup crash, not a sustained-load benchmark.
- Both main and GPU paths recorded `NullPointerException` while invoking `Resources.getStringArray(int)` on a null object in `org.chromium.android_webview.common.AwResource.getConfigKeySystemUuidMapping`.
- Chromium then logged `[FATAL:jni_android.cc(290)] Please include Java exception stack in crash report`; native tombstones recorded signal 5 / SIGTRAP in the main and CrGpuMain threads.
- The GPU path also logged EGL_BAD_DISPLAY / EGL_BAD_ATTRIBUTE. These are additional compatibility evidence; they do not establish that the GPU driver is the primary cause of the earlier resource exception.
- Existing Mali driver logging identifies r7p0-00rel1, vendor build August 18, 2017. Do not substitute a driver without verified hardware/kernel/userspace compatibility and separate approval.
- Post-observation memory snapshot contains no Brave process. The evidence establishes an exception/fatal-abort path; it does not prove every app closure has the same cause or completely exclude concurrent memory pressure.
- `KEYCODE_HOME` completed; window-manager focus independently confirmed the MBOX launcher. No onboarding controls were pressed and no webpage was deliberately opened.

### Provider and framework checks (read-only)

`settings get global webview_provider` returns `com.android.chrome`. Installed Chrome reports version 101.0.4951.61, minSdk 24, targetSdk 31, ARM-compatible. The separate `com.google.android.webview` package reports 55.0.2883.91, minSdk 21, targetSdk 25, `enabled=3`; `pm list packages -d webview` lists it, while the enabled-only list does not. The webviewupdate service exists, but its dumpsys output was empty, so its validated current provider was **not conclusively obtained** from the service. Configured provider and service-selected provider must not be conflated.

The resources/DRM-key-system startup path is the immediate failure location. [Chromium AwResource source](https://chromium.googlesource.com/chromium/src.git/+/2e7314ec42493e43b93327e0dc72446cff068732/android_webview/java/src/org/chromium/android_webview/AwResource.java) illustrates the Resources object and mapping accessor, and [Chromium's AwContentClient](https://chromium.googlesource.com/chromium/src/+/refs/heads/main/android_webview/common/aw_content_client.cc) connects it with MediaDrmBridge configuration. These source references are contextual; they are not a symbolicated audit of this installed Brave binary or proof that the vendor DRM library is defective.

No matching AwResource marker was emitted by targeted strings probes of selected existing DRM libraries. That negative observation does not exclude their involvement. No library/APK dump, profile backup, module install or DRM configuration change was made.

### Interpretation and next action

**Confirmed:** Brave has a reproducible Chromium startup exception/fatal abort on this installed system. General animation tuning or deleting small background apps is not an evidenced fix for this failure.

**Unverified:** why WebView key-system resources are uninitialized in this browser path; whether this is an APK/build issue, provider interaction, vendor-framework incompatibility, or another initialization defect. A WebView update or provider switch is not established as a cure. Do not clear browser data, disable Chrome, change DRM, force GPU flags or alter the OS version string based solely on this test.

Next safe work: compare the installed APK variant/signature and provider/framework compatibility with publisher information, and identify a narrowly justified reversible test. App/software artifact inspection remains distinct from bulk firmware/user-data backups; seek separate approval before any new state-changing test or large backup. The goal remains a functional Android TV OS with Android apps; this test does not establish a compatible newer firmware.

Private evidence: `diagnostics/20261024T003331+0100/` contains launch result, timed process samples and selected signatures; `003414` contains filtered stack details; `003437` contains provider/package metadata and Home confirmation; `003514` refines provider status; `003543` contains targeted library-marker probes. Raw live logs were held only in memory and not saved. Existing buffers were not cleared. These directories remain Git-ignored.

## Follow-up APK/provider inspection — 2026-10-24

**Strong variant diagnosis:** installed file exactly matches official Bravearm64Universal.apk. Although Android reports a 32-bit process/primary ABI, the 32-bit native library in this 64_32 package is WebView-only, not the full browser implementation. The correct official BraveMonoarm.apk has the full ARM32 browser library. Both publisher APK checksums and identical signing certificates were verified. This corrects the earlier provisional ABI-match assessment: presence of ARM32 libraries alone does not establish that this variant can run as an ARM32 browser.

Prepared exact original and replacement APKs locally, without installing them. The compiled framework provider allowlist identifies Chrome and Google WebView fallback, so there is no evidence requiring a provider change first. See [the concrete repair proposal](brave-arm32-repair-plan.md) for hashes, package metadata, lower version-code handling, install/test commands, risks and binary rollback. No browser-data backup exists. The latest authorization covers inspection only; replacement and another launch remain pending explicit approval.

## Execution result — approved ARM32 replacement completed, 2026-10-24

The user explicitly approved the package replacement and one observed launch/Home test. **ARM32 Brave 1.60.127 / versionCode 426012720 is now installed.** No OS firmware, bootloader, WebView provider or unrelated package was changed, and no uninstall/data-clearing command was used. Browser/package startup may have written ordinary app data; `-r` requested preservation, but no profile backup/comparison was made.

### Installation and verification

- Preflight verified TV identity, original installed package/version/hash, no running Brave process, both prepared APK hashes and matching verified signatures.
- Initial streamed install reached a 180-second host timeout with no success/rejection. The original package was still installed afterward and the active-install-session list was empty. No app launch followed the inconclusive attempt.
- The same approved replacement succeeded using `adb -s 192.168.1.4:5555 install --no-streaming -r -d .../BraveMonoarm-1.60.127.apk`. The extra flag changes upload method; package and replacement/downgrade flags were unchanged. Push transferred 164,302,719 bytes at about 1.0 MB/s; Android returned `Success`.
- Package manager now reports `/data/app/com.brave.browser-2`, versionCode 426012720, versionName 1.60.127, primary ABI armeabi-v7a.
- Device-side SHA-256 of the installed APK matches the publisher's ARM32 checksum: `2df0721c9f28e421584cd879bec1426e1b49a573e1c742e1423e098b9a6c199e`.
- A later read-only check confirms ADB removed the staged `/data/local/tmp/BraveMonoarm-1.60.127.apk`; no manual cleanup was needed.

### Single-launch result and limits

The approved launch reached WelcomeOnboardingActivity with TotalTime 1,205 ms. Main PID 7553 was present in every one of 24 process samples over **30.13 seconds**, together with persistent renderer PID 7586 and GPU PID 7621 at the end. The earlier null AwResource exception/JNI FATAL/SIGTRAP did not recur in the fresh observed log window. This supports the wrong-browser-variant diagnosis and resolves the previously reproduced immediate startup failure in this test. It is not a controlled speed benchmark or proof that every browser feature works.

Additional isolated renderer services started/exited during the interval. Follow-up read-only logs confirm that secondary process churn, plus old Google Play Services and EGL_BAD_MATCH warnings; no new matching main-process fatal crash was captured. These observations remain limitations, not grounds to disable sandboxing or replace graphics/Google packages automatically. Browser main-process PSS was about 91 MiB in the snapshot, not total browser/process-family memory.

No onboarding controls were pressed, no website deliberately loaded, and no sign-in or browsing test was performed. **Actual page rendering, remote usability, video playback and longer-session stability remain unverified.** The user should complete their own welcome/consent choices before a browsing test. This legacy Brave remains unsupported; installing the right architecture does not upgrade Android or satisfy current Brave's Android-10+ support requirement.

`KEYCODE_HOME` executed in the cleanup path. A final read-only check confirmed both resumed/focused activity and window focus on the MBOX launcher. The box was not rebooted. Original verified APK remains available for binary rollback; rollback has not been needed or authorized/executed. It would not undo browser-profile writes.

Evidence: install session `diagnostics/20261024T004956+0100/`; validation `diagnostics/20261024T005404+0100/`; filtered secondary-process details `005513`; final Home/package/staging confirmation `005552`. Earlier failed-install and post-timeout sessions are retained separately. Raw live log streams were not saved; filtered diagnostic records remain Git-ignored. Device logs use their own October 23 clock values, while the host investigation date is October 24 Africa/Lagos.
