# Evening Home behavior

Visual decisions: [DESIGN.md](DESIGN.md). This native project has no UI ownership
relationship with the containing Next.js website.

## Canonical UI Map

| Capability | Canonical owner | Source of truth | Allowed variants | Verification |
|---|---|---|---|---|
| Navigation | MainActivity.navigate and onBackPressed | This contract | Home / Apps | Desktop preview checks; owner confirmed D-pad operation |
| App Launch | MainActivity.launch and open | Android PackageManager | TV entry preferred / standard entry fallback | APK build; native app launch test pending |
| Favorites | MainActivity.editFavorites and AppOrder | SharedPreferences pins | Native multi-choice dialog / hold OK | Host ordering tests; browser dialog checks; native dialog pending |
| Toast | MainActivity.open and native Toast | This contract | Launch error / favorite result | Desktop status check; native test pending |
| App settings | MainActivity.appSettings + Android Settings | PackageManager and Android app details | App picker / system settings fallback | APK build; physical Force stop handoff pending |
| Recent apps | RecentsShortcut + Android SystemUI | Android task switcher | User-enabled / setup / unavailable | Installed 0.4.0; native header observed; setup/handoff/dismissal pending |
| Music controls | MusicControls + MainActivity.buildMusicBar | This contract | Hidden / playing / retained for resume | Browser simulation; real player test pending |
| Wallpaper | WallpaperView, OnlineWallpapers and OnlineWallpaperPolicy | This contract | TVMaze cached / bundled / paused | Native fetch8/display; host policy and browser provider tests |

## Navigation and focus

Start Home focused on its first available favorite; use Home navigation if none exists. Both navigation tabs, Settings and every
tile are reachable by D-pad. Native spatial focus search controls movement. A 3dp
border identifies focus, without resizing the control. All Apps restores its
scroll position; the last app/package focus is restored after returning from an
app and after enumeration. Removed focus targets fall back to the current screen's
entry action. Back from Apps goes Home. Back from the app-only preview exits;
when launched as Home, Back stays Home. No Home key interception. All actions
also accept ordinary pointer clicks and
show hover/hand-cursor feedback. The favorites rail accepts horizontal trackpad
scrolling and maps vertical mouse-wheel events to horizontal movement. The Apps
grid uses standard Android vertical scrolling. Physical remote/trackpad behavior
remains pending on the physical device.

## App list and failures

Enumerate installed enabled exported MAIN activities from LEANBACK_LAUNCHER first,
then LAUNCHER; deduplicate by package and locale-sort the labels. Exclude this
launcher. Load on resume on a single worker, not the rendering thread. Display
loading, empty and failure separately. Retry repeats enumeration. If an app has
gone away or requires unavailable permissions, show an error toast, refresh, and
leave launcher navigation usable. The launcher cannot prove that a launched app
is stable or compatible.

## Favorites

First use suggests installed YouTube, Brave, KD Player, Netflix, Play Store and
Aptoide TV. Save persists package names only. Retain existing ordering, append
newly selected entries, filter unavailable packages on read. Intentionally empty
favorites remain empty. Uninstalled packages aren't erased from saved preferences
merely by enumeration; they can reappear if reinstalled. Cancel discards dialog
changes and returns focus to the trigger. Hold OK adds/removes one favorite and
shows feedback. Favorites are app preferences, with no account/cloud synchronization.

## Permissions and installation

ACCESS_NETWORK_STATE reads Wi-Fi connection status; INTERNET fetches the requested
TVMaze artwork. The optional RecentsShortcut accessibility service is enabled only
by the owner through Android settings. It subscribes to zero events, cannot retrieve
window content, and invokes only GLOBAL_ACTION_RECENTS when selected. No location,
root, remote configuration, playback service or system-package changes. Launching another app is initiated by the user's action.
HomeEntry is shipped disabled. Preview installation and opening require the
device owner's approval under their original instructions. Enabling HomeEntry
and setting a default require a subsequent explicit opt-in. Keep the original
com.droidlogic.mboxlauncher/.Launcher installed and available.

## Wallpaper slideshow

Movie art is fetched on the host from official publisher pages and bundled in the
APK as a fallback. OnlineWallpapers adds TVMaze backgrounds from the official
public API with no key or server; see the online-source contract below. Images dwell
15 seconds then crossfade for 900ms. Only two decoded frames are referenced, each
capped at 1600×900 with RGB565 requested. Decoding runs on a separate worker. Timers
and fades stop after pause/detach/background/destroy. System animation scale zero
freezes automatic movement; Next scene still swaps instantly. Settings can pause
rotation. The motion preference and current scene index persist. Background changes
never capture or move focus. Missing/corrupt assets retain the previous image or
the vector fallback. Settings identifies the current artwork; no wallpaper title overlays Home.
No movie shelves. See WALLPAPERS.md for source credits.

## Online source and cache

The owner chose TVMaze on2026-10-24. Fresh online metadata can change the
background catalogue without rebuilding the APK. Select up to8 shuffled landscape
backdrops across five curated action/cartoon galleries. Do not invent movie
recommendations or claim TVMaze supplies a movie library. Attribution remains in
Settings/About; Home stays clean. User can switch to bundled art, pause slideshow,
refresh now or view current source/status. Browser starts bundled to permit an
offline review; native update starts online as requested.

Use only generated https://api.tvmaze.com/shows/{id}/images routes and validated
https://static.tvmaze.com/uploads/images/... image URLs. Reject redirects, user
info, nonstandard ports, queries/fragments, other hosts, traversal, portraits,
banners, small images and >20MP images. Download JSON<=1MiB and each image<=3MiB.
Decode bounds before accepting a file; render with sampled RGB565<=1600×900.
Limit the cache to8 compressed images plus temporary replacement files. Persist
source image/page URLs and TVMaze data licensing in an atomic JSON catalogue;
prune unreferenced files. Cache failure rebuilds public data. Preserve current
bitmap and last valid cache if fetching fails. Never blank Home for a network error.

Refresh at most daily on resume, with30min failure cooldown and an explicit
Refresh override. Cancel worker requests/disconnect on pause, reject stale
callbacks by generation; shutdown on destroy. No polling/background service.
Cached artwork and bundled fallback remain usable offline; native flight-mode
verification is not claimed. Browser network tests are deterministic fixtures;
actual native fetch/display was independently observed.

## Connection status

Read ConnectivityManager TYPE_WIFI while resumed, on initial layout and on
CONNECTIVITY_ACTION broadcasts. Pair the Wi-Fi glyph with visible connected or
disconnected text and a full accessible name; disconnected includes a slash.
Unknown/restricted status is explicit. This means Wi-Fi link status, not proof
of Internet reachability. ACCESS_NETWORK_STATE is a normal permission; there is
no location or ACCESS_WIFI_STATE permission, SSID or credential query.
The separate INTERNET permission is only used for requested TVMaze backgrounds.
Selecting the indicator opens platform Wi-Fi settings. Never toggle Wi-Fi
automatically. Browser status is explicitly simulated with a review-only toggle.

## Music transport

The compact bar appears after AudioManager.isMusicActive detects audio. It stays
available when audio pauses so Play can resume the current media receiver. Hide
removes it and restores favorite/Home focus. Returning Home resets that hide
choice. Activity recreation/process death resets session visibility; no listening
history is persisted. Poll once per second while resumed, cancel on pause/destroy.
Send KEYCODE_MEDIA_PLAY/PAUSE or MEDIA_NEXT as paired DOWN/UP events using
AudioManager.dispatchMediaKeyEvent; observe actual audio activity afterward.
A dispatch without an exception is not proof that the receiving app handled it.
Show a toast on dispatch failure. Do not claim a track title, player identity or
successful skip. Audio activity may also come from a video app. The launcher cannot
make a player support background playback; this is the player's responsibility.
No notification access, new permissions or playback service. Desktop music state
is an explicitly labelled simulation, separate from actual device verification.

API reference: [Android AudioManager](https://developer.android.com/reference/android/media/AudioManager).

## Verification limits

Host checks cover APK packaging/signature, preference ordering logic and browser
review interactions. Browser screenshots are an illustration of the native layout,
not screenshots from Android. Native D-pad traversal, dialog focus restoration,
real installed-app launch, scroll restoration, font scaling, HDMI overscan, and
CPU/RAM use require a separately approved device test. There is no emulator test.

## Recent apps

Recents is a quiet header action shared by Home and Apps. It opens Android's own
task switcher through an optional AccessibilityService global action. Android owns
the task list, thumbnails, resume and dismissal controls. No custom launch-history
list is presented as running processes. AOSP API25 Recents activities are not
exported; do not attempt privileged activity launches, hidden APIs or key injection.

First selection shows a native explanatory dialog with Accessibility settings,
App settings and Cancel. The owner must enable Evening Home — Recents in Android
settings, return Home and select Recents again. Never enable it through ADB/root or
automatically open Recents on returning from setup. The service is protected by
BIND_ACCESSIBILITY_SERVICE and requests no accessibility events, window content,
key filtering, touch exploration or gestures. No task/screen data is collected,
stored or sent to TVMaze. Disabling/unbinding clears the in-process bridge. A
false/refused global action shows recovery choices; returning restores trigger focus.

Settings → Recent apps help explains dismissal and setup. Settings → App settings
shows installed launchable apps, opens the chosen app's Android details page, and
leaves Force stop/confirmation to Android. This list is never called a running-app
list. Force stop may interrupt playback/downloads; don't clear app data or stop
apps automatically. Recent tasks do not prove active processes; dismissing a task
may leave separate services running. No custom Clear all or force-stop claim.

The browser illustrates the Recents button and explanatory handoff dialog only; it
never simulates actual task removal. Version0.4.0 was installed after the owner
supplied the changed IP; HomeEntry/default and the native Recents header action
were verified. Accessibility setup, native handoff, dismissal, D-pad navigation
inside SystemUI and app-details availability still require physical validation.
