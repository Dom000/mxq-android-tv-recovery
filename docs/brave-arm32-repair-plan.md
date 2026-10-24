# Brave ARM32 variant repair — installed and startup tested

2026-10-24 Africa/Lagos. **The user approved and the ARM32 replacement is installed. The previous immediate startup crash did not recur during a 30-second observation; browsing is still untested.** No provider or firmware change occurred. The original proposal below is retained for command/risk/rollback context; the execution result at the end is authoritative.

## Findings

The installed `com.brave.browser` APK has SHA-256 `0bd8a2968d612b3126e9c07c5a604ceb5ec82670f8a6d334c3729a1875f4575a`, exactly matching the publisher's `Bravearm64Universal.apk` for version 1.60.127. It is 281,343,991 bytes and versionCode 426012722. Installer metadata names com.uptodown; the exact publisher hash match establishes the APK bytes, independently of that installer label.

The TV has only armeabi-v7a/armeabi ABIs. The installed Universal variant is identified as MonochromePublic6432 by the crash stack. Its ARM64 `libmonochrome_64.so` contains both browser and WebView implementation markers. Its 32-bit `libmonochrome_64.so` contains AwMainDelegate/WebView markers and **no ChromeMainDelegate marker**, with only 2 chrome/browser path markers, compared with 860 in the full ARM32 browser library. The official `BraveMonoarm.apk` contains the full 32-bit combined implementation and targets armeabi-v7a.

[Chromium's native-library packaging documentation](https://chromium.googlesource.com/chromium/src/+show/132.0.6804.0/docs/android_native_libraries.md) explains that the 64_32 variant supplies a 64-bit browser plus an additional 32-bit **WebView-only** library. That distinction means an APK can have 32-bit native content without being the appropriate 32-bit browser. This corrects the earlier overly broad interpretation that matching the package manager's primary ABI excluded a variant mismatch.

This is **strong evidence of the underlying variant problem**, consistent with launching into uninitialized AwResource state. Runtime success of the replacement has not been established, and separate graphics errors may remain. It is not evidence of a damaged OS, malware or a need to flash firmware.

[Brave's Android-7 recovery instructions](https://community.brave.app/t/android-7-crash-on-launch-issue-steps-to-resolve-with-security-warning/621712) identify BraveMonoarm.apk for ARM32. Both prepared artifacts were downloaded over verified HTTPS directly from the [publisher release](https://github.com/brave/brave-browser/releases/tag/v1.60.127), compared with its checksum sidecars, and verified offline with the installed Android SDK apksigner.

| Check | Prepared ARM32 replacement | Prepared original variant for rollback |
|---|---|---|
| Filename on Mac | diagnostics/publisher-apk-inspection/BraveMonoarm-1.60.127.apk | diagnostics/publisher-apk-inspection/Bravearm64Universal-1.60.127.apk |
| Bytes | 164,302,719 | 281,343,991 |
| Version name | 1.60.127 | 1.60.127 |
| Version code | 426012720 | 426012722 |
| Minimum SDK | 24 | 24 |
| Target SDK | 34 | 34 |
| Browser architecture | ARM32 | ARM64, plus separate 32-bit WebView library |
| SHA-256 | 2df0721c9f28e421584cd879bec1426e1b49a573e1c742e1423e098b9a6c199e | 0bd8a2968d612b3126e9c07c5a604ceb5ec82670f8a6d334c3729a1875f4575a |
| APK signature | v2/v3 verified | v2/v3 verified |

Both signer-certificate SHA-256 digests are identical:

```text
9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac
```

The original local APK also exactly matches the installed APK's device-side SHA-256. This provides a verified application-package rollback artifact, **not a browser-data backup or complete firmware backup**. Raw APKs remain in the ignored diagnostics directory; compact metadata is in research/brave-apk-variant-assessment.json.

## WebView provider assessment

Offline inspection of the existing framework-res.apk's `res/xml/config_webview_packages.xml` permits Chrome Stable and Google WebView, with the latter designated as fallback. Brave is not an allowed system provider in this configuration. The configured Chrome provider and disabled fallback WebView are therefore not, by themselves, proof of a broken provider setup. Do not change the provider, enable the old WebView, or replace system packages as part of this repair.

## Approved operation and preflight requirements

Working directory: `/Users/user/Desktop/projects/personal/Qcleint`. Recheck the TV identity, current installed version/hash, prepared artifact hashes/signatures, available storage, and absence of a running Brave process immediately before installation. If the device/package has changed or Brave is running, stop and reassess; no force-stop or uninstall is automatically authorized.

```bash
adb -s 192.168.1.4:5555 install -r -d mxq-android-tv-recovery/diagnostics/publisher-apk-inspection/BraveMonoarm-1.60.127.apk
```

`-r` requests replacement while retaining existing app data. `-d` is required because the ARM32 variant has a lower numeric version code, despite the same visible 1.60.127 version. The box reports ro.debuggable=1 / userdebug. Android 7.1.2's [package-manager source](https://android.googlesource.com/platform/frameworks/base/+/android-7.1.2_r39/services/core/java/com/android/server/pm/PackageManagerService.java) permits a requested downgrade on a debuggable platform; this is evidence of feasibility, not a guarantee that this vendor installer behaves identically. If Android rejects the replacement, stop; do not uninstall, clear data, alter signatures, disable verification or modify the system to force it.

Expected effects: stage/replace the user-installed Brave package and regenerate relevant compiled app code; write ordinary app/package data on internal storage. No firmware partition, bootloader, provider setting or unrelated package is intentionally modified. APK installation changes device state; the user provided that approval for this operation.

## Validation scope to approve with the repair

After successful install, verify package name, versionCode 426012720, versionName 1.60.127 and ARM32 ABI. Then launch once and observe filtered crash/process records using the previously reviewed method:

```bash
adb -s 192.168.1.4:5555 shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -n com.brave.browser/com.google.android.apps.chrome.Main
adb -s 192.168.1.4:5555 shell input keyevent KEYCODE_HOME
```

Do not press onboarding controls, browse to websites, sign in, change settings, clear logs/data, force-stop or reboot. Startup may restore tabs/network activity and write its normal profile state. Return to Home in a finally/cleanup step and verify focus. A successful startup would support the variant diagnosis; it does not establish current-browser security, stability under load, or full TV-remote compatibility.

## Risks and rollback

Same package name, signer and visible version reduce replacement risk, and `-r` requests data retention. They do not guarantee a bit-for-bit unchanged profile or a bug-free migration. Browser profile data has not been backed up. The candidate is still an unsupported legacy Brave/Chromium build; this repair does not turn it into current supported Brave or upgrade Android.

The exact original APK is already downloaded and verified. If a rollback is separately approved, restore the original variant without uninstalling:

```bash
adb -s 192.168.1.4:5555 install -r mxq-android-tv-recovery/diagnostics/publisher-apk-inspection/Bravearm64Universal-1.60.127.apk
```

This restores the original application binary/version code and likely the original crash behavior. It cannot undo profile writes made during install/startup. Do not promise full data rollback. Stop for review on installer errors rather than expanding the approved scope.

## Recommendation

The approved ARM32 package replacement and one observed launch/Home test have completed. The immediate startup failure did not recur. Keep the change while checking actual browsing after user onboarding; do not expand into provider/system changes automatically. Modern Android TV firmware/app compatibility remains a separate unresolved objective.

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
