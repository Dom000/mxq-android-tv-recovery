# Verification — 2026-10-24

Current package: **Evening Home 0.3.0 / versionCode3**, installed with a signed
in-place update on the guarded MBOX. Home resolution, resumed activity and focused
window still select dev.mxq.home/.HomeEntry. Native screenshot confirms the new
classic layout, real app icons, focus border, online TVMaze wallpaper, Wi-Fi indication and
absence of welcome/footer copy. Music controls are hidden when no audio is active.

## Host checks passed

- JDK17 compilation, D8 minAPI25, SDK35 resource packaging, alignment and v2/v3
  signature verification. minSdk25 / targetSdk28; only ACCESS_NETWORK_STATE and INTERNET for the requested artwork feed.
  Packaged HomeEntry remains disabled; device override is separate.
- APK: 1,074,182 bytes, SHA-256 `747298faae4139b420329902b3e945eb8d4c5d2f7d400efaa29a773f052d9353`.
- Signing certificate unchanged:
  `c9e9d22d527578e42fe577b8b706d8747eb24ec7daeb6b48328dc4d092e076a6`.
- Four Java ordering/persistence cases; online URL isolation/dimension/freshness/
  cooldown/clock-rollback/forced-refresh cases; manifest/API/permission gate; eight shared
  palette tokens; four provenance image hashes, five exact TVMaze seed IDs/URLs and exact APK asset inclusion;
  six real installed-app icon hashes.
- Strict premium project audit: zero findings. DESIGN.md lint: zero errors/warnings.
  These are static/web checks, not native runtime verification.
- Playwright/Chromium130: clean Home, real icons, Home/Apps, navigation, favorites
  Save/Cancel/empty, dialog focus, wheel scrolling, Wi-Fi simulation, wallpaper
  manual/15-second auto/pause, 16:9 scaling at 1920/960/640. Simulated music bar
  visibility, pause/resume labels, next feedback, hide/focus restoration and stable
  app-row position. All 15 visible Home controls, including music, reachable with
  arrows. Provider fixture checks cover online artwork, poster/untrusted-URL rejection,
  retained artwork on failure and bundled fallback. No JavaScript errors.
  Full results: preview/verification.json.

```bash
bash build.sh
bash tests/check.sh
PLAYWRIGHT_MODULE=/Users/user/.npm/_npx/360550e4913b8759/node_modules/playwright \
CHROMIUM_PATH='/Users/user/Library/Caches/ms-playwright/chromium-1140/chrome-mac/Chromium.app/Contents/MacOS/Chromium' \
node preview/verify.cjs
```

## Native0.3 online update evidence

Private diagnostics/20261024T193304+0100: guard/preflight, same-key install-r
Success, Home key, versionCode3 and enabled/default HomeEntry. The normal INTERNET
permission was added for the owner-selected TVMaze source.

At193417+0100, the native Home screenshot showed downloaded Batman artwork;
at193539+0100 the filtered own-tag log reported TVMaze cache refreshed:8 images.
API/CDN HTTPS fetching and native display were observed. No separate server/key,
network setting change or certificate-verification workaround was used.

## Historical0.2 update evidence

Private diagnostics/20261024T191114+0100: fixed-target identity/API/ABI and v0.1
preflight, exact host hash verification, adb install -r Success, Android Home key,
versionCode2 and HomeEntry enabled/default confirmation. No root or preference
reset was needed for the update.

Private diagnostics/20261024T191145+0100: resumed/focused HomeEntry, native screen,
PID5893 and one PSS snapshot15,278KiB (~14.9MiB). No matching launcher crash
signature in the sampled buffer. This snapshot is not peak RAM, a complete GPU
memory estimate or sustained performance evidence.

## Initial installation and default activation history

v0.1.0 was installed/opened at the owner's request (183639+0100). Its host SHA was
6ec87800a3fe6b69fcdedb11e02104ccbb86aa41f388f0a76c879b0f642ad9ff.
Initial rendering was observed at183755+0100. Owner confirmed D-pad navigation.
At184306+0100 ordinary shell pm enable was refused with SecurityException. The
same authorized HomeEntry enable succeeded using existing root at184511+0100;
cmd package set-home-activity then selected Evening Home. An Android Home-key test
opened it. The stock system launcher remains installed/enabled. Root was limited
to that component override; no firmware/system-file writes or reboot occurred.

Initial preview PID3147 remained present in eight samples over25.87sec while the
owner could use other apps. This was not continuous foreground testing. The TV
lacks non-root sha256sum; a device-side hash attempt failed. Do not confuse host
artifact verification with an installed-device byte hash.

## Limits

Real music playback, play/pause and Next handling are **not yet verified** against
both requested players. Spotify TV1.81.55 and Audiomack6.59.1 are installed/opened.
Audiomack has a standard Android entry rather than Leanback. See
../docs/music-app-installation.md for signatures, installed metadata and the
ADB timeout/offline recovery record. MusicControls uses ordinary
AudioManager media-key dispatch, no metadata/notification access or new permission.
The browser music bar is a simulation. The launcher cannot ensure background
playback or skip support in another app.

Physical trackpad, remote Home button, Wi-Fi transitions/settings, app-tile launch,
native favorite dialogs, font scaling, overscan, sustained decoding/performance
and reboot persistence remain untested by the assistant. Previous D-pad operation
was owner-confirmed; the new revision's full native focus traversal remains to be
checked. No emulator was used. No firmware, accounts, network settings, original
launcher removal or other apps' data changes.

The Pinterest inspiration board returned HTTP403; its exact pins were not viewed.
The design follows the user's explicit classic/simple direction. Browser images
are illustrations; the private native screenshot is separate. Icon resources were
previously read selectively from public installed APK resources, without app-data
reads or full APK pulls. Their source hashes remain in preview/icons/catalog.json.

## Music app installation completed

Spotify TV1.81.55/1815500 (minAPI21, ARMv7, dedicated Leanback entry) installed
and first-launch Status:ok at192146+0100. Audiomack6.59.1/28914 (minAPI21, ARMv7)
was confirmed installed after a transport-finalization error and fixed-target host
reconnect. First launch at195246+0100 reached AuthenticationActivity, Status:ok,
TotalTime11191ms. Its manifest has no dedicated Leanback entry; launcher Apps
will use the standard Android launch activity. May require pointer/trackpad.

All downloaded APK signatures verified before installation. Audiomack's four
splits match its official website assetlinks certificate; Spotify TV matches
independent known TV signer metadata. No sign-in, playback, broad runtime
permission grant, account UI capture, store-service update or extra installer app.
Real media transport behavior remains pending. Details/hashes/source limitations
are in ../docs/music-app-installation.md. Do not infer service/playback success
from package installation or a successful initial Activity launch.

## Version 0.4.0 — installed; task-switcher setup pending

The Recents shortcut and App settings destination build for minAPI25. The signed
APK is 1078352 bytes, SHA-256
`c559b16c8214e7aa03a9fcf3f85c1e6098076e27aaca2e1e44a50f9ff335feda`. The same local signer is retained
(SHA-256 `c9e9d22d527578e42fe577b8b706d8747eb24ec7daeb6b48328dc4d092e076a6`),
with v2/v3 signature and alignment verification. No signing key is published.

Host checks pass, including the unchanged Home opt-in gate, exact two normal
permissions and the accessibility service's protected binding, zero event
subscription and disabled window-content capability. All18 browser checks pass,
including the Recents illustration, Escape/Done focus restoration, header fit and
all16 visible Home controls reachable by arrows. Browser pictures remain
illustrations. Strict UI audit and DESIGN.md lint pass with no errors/warnings.

The initial fixed-target address refused connections on2026-10-24. The owner
subsequently supplied192.168.1.3; only that address was connected, and RK3229
identity/API25/ARMv7 and version0.3.0/default preflight passed.

Private diagnostics/20261024T202930+0100 records the exact host hash/signer
verification and adb install-r Success. Readback confirmed versionCode4,
versionName0.4.0, HomeEntry enabled/default and the original launcher still
installed. KEYCODE_HOME resumed dev.mxq.home/.HomeEntry.

At203007+0100 the own-launcher hierarchy contained Recents at[464,34][570,82];
a tap was sent. The following foreground guard failed during a transient ADB
transport loss; no screenshot or further click was attempted. A host-only
disconnect/reconnect restored the same device, with identity,0.4.0/default and
HomeEntry resumed confirmed again at203056+0100.

The next hierarchy did not independently establish the setup dialog. At203149
window-focus metadata identified Android Settings/SubSettings, so screenshot
capture and further UI commands were stopped. No Settings contents or private
task thumbnails were stored, no accessibility permission was enabled through
ADB, and no apps were dismissed/force-stopped. Do not infer service setup or
SystemUI success from a sent tap.

The owner must enable Evening Home — Recents in Accessibility settings, return
Home and select Recents again. Actual task-switcher handoff, D-pad controls,
selected-task dismissal and App settings/Force stop handoff remain pending.
Do not close unrelated tasks or photograph private thumbnails for verification.
