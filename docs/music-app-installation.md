# Spotify / Audiomack installation — latest status

**Both apps are installed and their initial launches succeeded.**

Owner authorized both apps. Target is fixed192.168.1.4:5555, RK3229 ARM32,
Android7.1.2/API25. No account login, purchase, store-service update or firmware
change is included. Do not repeat permission questions for these same installs.

## Spotify TV — installed

Installed com.spotify.tv.android1.81.55/code1815500. The actual APK is older than
the mirror page's1.134.2 label; record the inspected package version. minAPI21,
target33, universal native libraries including ARMv7; dedicated Leanback entry
com.spotify.tv.android/.SpotifyTVActivity. Device primaryCpuAbi=armeabi-v7a.
Normal adb install returned Success and first am start-W returned Status:ok,
TotalTime3737ms. A later check found its process present/resumed. No account or
playback test was performed; service compatibility is not proved by installation.

Source page: https://spotify-tv.en.divxland.org/download/
Used its normal public download-button POST/link flow; no CAPTCHA bypass or
modified APK. Host package/signature inspection preceded the guarded install.
APK SHA25646b12520b7dbda118d286fddfaf8d4d1cba553162f536f3e12ac9618cfee2b76,
9,465,371 bytes. apksigner verification passes including v2; signing certificate
SHA256fd450f5f94b9dba0ae7f6b19e0b512e041ea7fc2cc291b71737b319ba7f4b3d4,
SHA1aa88e76ce8b79a5961320079a7e69b9d6dd9a613 and MD5718118d245a3baa5cefda0e7ddf90035.
Matches independent Spotify TV certificate metadata. Spotify's public website
assetlinks file did not identify the TV package; do not claim it did.

Official package/source reference:
https://play.google.com/store/apps/details?id=com.spotify.tv.android
https://connect.spotify.com/devices/android-tv

Private evidence192146+0100: guard/preflight/install/package/Leanback resolution/
first launch.192220+0100: process/resumed check and bounded UI action filtering.
No allowlisted UI labels observed; no raw UI/account/pairing-code data retained.

## Audiomack — installed and opened

Downloaded the complete6.59.1 ARMv7 bundle (87,233,274 bytes). Archive SHA256
90274926e0baf86c8f998dacd834386c0c2fe18e02c2059c17c889e891a5a650; CRC check passed.
Actual base package com.audiomack6.59.1/code28914, minAPI21, target34. Includes
base, ARMv7, English and xxhdpi resource splits. Android scales the resource
configuration for this160dpi screen. Each APK's cryptographic signature verifies;
all share publisher certificate3562a57fd9e67dc1ef9f3c35a2c5448069a1b4534b91a8c8f9fd8342fcc9fcd4,
anchored in https://audiomack.com/.well-known/assetlinks.json.

The manifest marks touch optional/Leanback supported, but has **no dedicated
LEANBACK_LAUNCHER activity**. Its entry is the standard Android SplashActivity.
The owner was informed before installation: official Android build, visible in
Evening Home's Apps, potentially needing pointer/trackpad. Do not call this a
verified dedicated TV interface.

The initial guarded install-multiple exceeded120sec; its immediate package
check showed the app absent. The retry subsequently reported an ADB offline
finalization error. Refreshed only the host transport for fixed192.168.1.4:5555
(disconnect/connect), then reverified RK3229 identity. After reconnection,
PackageManager confirmed6.59.1/code28914, primaryCpuAbi=armeabi-v7a. Therefore the
Android-side install had completed despite the host-side finalization error.
A proposed file-copy retry was skipped because preflight found the correct app
already installed. Do not claim that a non-streaming install ran or that either
failed client command returned Success.

First launch returned Status:ok and reached
com.audiomack/.ui.authentication.AuthenticationActivity, TotalTime11191ms.
Standard LAUNCHER resolution succeeds; LEANBACK_LAUNCHER reports No activity found,
matching the inspected manifest. No sign-in, payment, playback or runtime
permission grant by the agent. Live music-control testing remains pending.
Evidence:194555+0100 initial timeout;194831+0100 retry/finalization failure;
195225+0100 fixed-target host reconnect/identity;195246+0100 installed-package
confirmation and first launch. Later package/process/Home checks are recorded
with final diagnostics. No Wi-Fi setting or reboot was used to reconnect.

| APK | Bytes | SHA256 |
|---|---:|---|
| com.audiomack-6.59.1.apk | 83051207 | `07a6ea08f79b8e596a27b7f40f86a7a269f567e2583ab2e531815b9decc221a5` |
| config.armeabi_v7a.apk | 3081039 | `892771303bc5def234171463cac74a161013251e1622ed32fe87bda255f3ad50` |
| config.en.apk | 172441 | `b5ef59e611b41fcd2eaae98921fff63faa590c92e604287895cd1b6b324358f3` |
| config.xxhdpi.apk | 928021 | `454105652cd27d8c5d2cc97b03b28f6c4976d2cc3b73ba9a70dae52d56803d73` |

A previously downloaded4.12.5 ARMv7 APK also verifies against that publisher cert,
despite a store metadata warning. It is older and lacks a TV entry; not installed.
Official package: https://play.google.com/store/apps/details?id=com.audiomack
Public download/version-list page:
https://audiomack-download-new-music.apk.watch/6.34.10
The selected6.59.1 ARMv7 bundle route:
https://apk.watch/download?split=217982/3163645/audiomack-download-new-music
No third-party installer, modified APK, account login or runtime permission grant.

## Earlier download blockers

Uptodown required its installer/CAPTCHA; other attempted hosts returned403/404.
The old Google Play client resolved the market URL, but a UI dump timed out and
later foreground was Home. No Install action or cause established. These earlier
failures are historical; Spotify's normal public download now succeeded.
All host artifacts/metadata are ignored under diagnostics/music-apk-inspection.

Evening Home0.3.0 has compact current-player transport controls. Real background
playback/pause/Next testing awaits the owner's signed-in/playing music app.
