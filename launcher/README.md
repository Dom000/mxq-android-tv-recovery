# Evening Home for the MXQ TV box

A native Android launcher with an offline fallback built for the confirmed RK3229 / ARM32 box
running Android 7.1.2 / API 25. This changes the home interface, not Android's version
or application compatibility. This directory is the standalone launcher project.

![Current native MBOX Home with TVMaze artwork](preview/evening-online-native.png)

The desktop preview uses actual icons extracted read-only from six installed
favorite apps. The APK uses Android’s live app labels/icons and discovers both
TV and regular Android launch entries. Wi-Fi status in the browser is simulated;
the native APK reads connection changes from Android.

- Fresh TVMaze show/cartoon backgrounds, daily online refresh and an offline cache;
  no account or API key. Source/refresh controls stay in Settings.
- Four bundled real movie wallpapers with 15-second automatic changes, gentle fades,
  Next scene and a persistent pause option. Readable app rail and six-column grid.
- Clean Home: wallpaper and favorites, without a welcome headline or instruction footer.
- Compact play/pause and Next controls appear after audio is detected; playback stays
  in the music app. No notification access or track metadata. Player support varies.
- D-pad focus, OK to open, hold OK to pin, visible Edit alternative.
- Saved favorites, system-format clock, device settings and original Home shortcut.
- Live Wi-Fi connected/disconnected icon, with a shortcut to Wi-Fi settings.
- D-pad, mouse/remote-trackpad clicks and scrolling.
- Only network-status and Internet permissions; no location, notification access,
  root, ads or background service. TVMaze receives fixed show-artwork requests.
- **Evening Home is now this MBOX’s default Home**, enabled at the owner’s request.
  The stock launcher remains installed for rollback. Fresh APK installs still ship
  HomeEntry disabled until explicitly activated.

## Review locally

Open [preview/index.html](preview/index.html) in a browser. Arrow keys move focus,
Enter activates and Escape returns Home. Apps are illustrative; no Android app
can run in this browser preview. Favorite edits last only for that preview session.

Wallpapers were fetched online from official studio sites: Fast X, Avengers:
Endgame, Spider-Man: Across the Spider-Verse and Kung Fu Panda 4. They are bundled
for offline use, not live movie recommendations or playback links. See
[source credits](WALLPAPERS.md) and [the asset manifest](assets/wallpapers/catalog.json).
Generated artwork is not included. Reduced-motion mode freezes automatic changes;
Next scene remains usable. Native slideshow preferences persist in app storage. Online mode adds fresh
landscape artwork from TVMaze for Arcane, Avatar: The Last Airbender, Batman:
The Animated Series, The Mandalorian and Star Wars: The Clone Wars. The actual
MBOX fetched8 images and displayed a Batman background. See
[online-source research and limits](../docs/dynamic-wallpapers.md).

## Build

Requires Python 3, JDK 17 and the official Android SDK's Android-35 platform and build-tools
35.0.0. No Gradle, Maven dependencies, npm install or downloaded build script is
required for the APK.

```bash
# From the repository root:
cd launcher
bash build.sh
bash tests/check.sh
```

The script syncs the asset catalogue to the browser preview, then packages the APK.
It defaults to this Mac's SDK and JDK paths; override JAVA_HOME,
ANDROID_SDK_ROOT, MXQ_BUILD_TOOLS or MXQ_COMPILE_SDK for another host. Uses the SDK's
core-lambda-stubs for compilation and D8 for API-25-compatible lambda desugaring.
Output: `build/EveningHome-preview.apk`, SHA-256 sidecar, package metadata and
signature verification. `.keys/preview.jks` is a local preview key: preserve it
privately for upgrades. It and all build output are ignored by git. No installer
or ADB command is included in the build script.

Optional browser checks require an installed Playwright module and its Chromium:

```bash
node preview/verify.cjs
```

Alternatively set PLAYWRIGHT_MODULE to a local module path and CHROMIUM_PATH to a
compatible installed Chromium binary. Playwright is optional and separate from
the native Android build. [Verification](VERIFICATION.md) distinguishes host checks from pending
native device testing. The source is original, uses only Android platform APIs,
and includes no firmware code or bundled publisher app assets.

## Version 0.3.0 installed and selected as Home

**Version 0.3.0 was installed and opened on the reference MBOX on 2026-10-24.** The native screen displays
movie wallpaper, installed-app icons and Wi-Fi connected status. Stock MBOX Home
remained the default during initial preview. The owner confirmed D-pad operation
and then requested Evening Home as default. Its HomeEntry is now enabled, Home
resolution selects it, and the Android Home-key test opened it. Physical trackpad,
Wi-Fi change handling and broader native validation remain pending.

See [the handoff](READY.md), [installation/rollback record](INSTALL.md) and
[verification](VERIFICATION.md). The original system launcher was not removed or disabled.

Design/behavior ownership: [DESIGN.md](DESIGN.md), [UX-CONTRACT.md](UX-CONTRACT.md).
Primary Android references: [TV D-pad navigation](https://developer.android.com/training/tv/get-started/navigation)
and [HOME/LAUNCHER intent categories](https://developer.android.com/reference/android/content/Intent).

## Firmware investigation

The companion [research project](../docs/README.md) records hardware identification,
Android/ABI findings, firmware candidates and recovery prerequisites. The launcher
can be built and reviewed without connecting any TV box.

## Public repository contents

Source, build scripts, tests, artwork provenance and review screenshots are tracked.
Downloaded third-party APKs, local build/delivery files, raw device diagnostics,
backups and private signing keys are excluded. Links under build/ in local handoff
records refer to generated files; build the APK locally rather than expecting those
binaries in a fresh clone. Historical diagnostic timestamps identify private
investigation evidence.
