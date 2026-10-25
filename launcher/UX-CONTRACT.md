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
| Recent apps | RecentsPanel, RemoteRecentsService and RecentsTree; RecentsShortcut for system view | Recognized Android SystemUI task controls | Remote panel / system view / setup / unsupported | Installed0.5.8; native D-pad/confirmation passed; owner confirmed Close |
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
window content, and invokes only GLOBAL_ACTION_RECENTS when selected. The separate
RemoteRecentsService requires explicit owner enablement for screen-content access;
it reads SystemUI Recents controls only while the requested panel is active. Only SystemUI window-state notifications while idle and control/scroll change
notifications while requested; their contents are ignored. No global key filter,
gestures, task history storage or upload. No location,
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

The owner requested a replacement after finding the native Close/Clear all controls
unreachable by D-pad, then said to continue with the optional helper. Recents uses
RemoteRecentsService if the owner enabled it. Otherwise a native setup dialog
explains screen-content access and offers Accessibility settings, Use system view
and Cancel. Choosing system view persists that preference; Settings always offers
Remote Recent apps setup and Open system Recent apps. Installation never enables
either service through ADB, root or secure-settings writes.

The original RecentsShortcut remains unchanged: zero events and no window-content
capability. The new service is protected by BIND_ACCESSIBILITY_SERVICE, with
canRetrieveWindowContent plus REPORT_VIEW_IDS/RETRIEVE_INTERACTIVE_WINDOWS/INCLUDE_NOT_IMPORTANT_VIEWS; no
notifications from other packages, event-content inspection, key interception,
gestures or touch exploration. Android7 needs a nonzero SystemUI window-state
event mask to refresh stale window/root references; content/scroll notifications
are enabled only while the panel is requested. A global
Recents action opens SystemUI, then bounded retries await a recognized tree.

Only inspect the top application window, reject a focused system popup or an
application other than SystemUI, then require known Recents markers. RecentsTree
limits traversal to512 nodes/24 levels; oversized/unknown trees authorize no
action. Task text is read only from known title fields; no thumbnail, body text,
content description, account data or screenshots. Names/nodes stay in memory
while the panel is open and are released on exit/unbind/destroy. No network, task-data files/preferences or task-text logging in the service.
Its platform dump emits state labels, counts, focus action names and public
control IDs for diagnostics.

RecentsPanel is a focusable TYPE_ACCESSIBILITY_OVERLAY using shared TvControls, a16dp outer inset and
NOT_TOUCH_MODAL so this vendor keeps the underlying application exposed.
Rows contain task name, Open and Close. These are actual exposed task controls,
not launch history or a list of running processes. More tasks can be paged through
Android's scroll action. Refresh re-reads controls. Missing actions are disabled
or explain unsupported layout; System view remains an escape.

Re-read/revalidate a task's node and label before Open/Close. Never send guessed
coordinates or root commands. Do not optimistically remove rows; refresh after
Android handles the action. Refusal/stale controls retain a usable view. Clear
all confirms the scope and possible unsaved-state loss, with Cancel focused.
Prefer Android's real Clear all action; otherwise dismiss exposed cards one at a
time, bounded to32 requests/20s, with Stop, generation cancellation, refusal and
no-progress checks. Interrupted/partial operations never claim all tasks closed.
No action is sent merely by opening Home or Recents.

Read on one worker; poll window validity at600ms only while the panel is active; stop and discard nodes
when another app opens. Timers/retries cancel on exit, interrupt, unbind or destroy.
Done/Back returns Home, System view reveals the native cards, and confirmation
Cancel/Back restores the Clear all trigger. Individual Close restores a surviving
row's safe Open action. The footer is outside the task scroller.

The view removes tasks, not arbitrary background services. Settings → App settings
opens Android's app details page for explicit Force stop, preserving app data.

Browser recents.html uses explicit sample tasks; verify-recents.cjs checks D-pad
reachability, Close, confirmation/cancel, refusal, paging, loading, unsupported and
empty states at16:9 widths. It cannot verify actual task removal. Physical test
results and remaining limits belong in VERIFICATION.md. UIAutomator can suppress
accessibility services, so active-panel checks use the service's metadata-only
dump and real key events rather than UIAutomator.

Native0.5.8 key tests and owner-confirmed Close passed. Do not inject touch taps
or swipes on this MBOX: its NavBarEventListener can crash the system process.
The assistant uses key events and metadata-only diagnostics for native checks.

## Classic task icons — version0.6.0

The owner requested original app icons and a classic treatment. InstalledTaskIcons
and TaskIconIndex own decorative artwork lookup; task identity and all actions
remain owned by SystemUI nodes. Query installed application and TV/regular launcher
labels on the reader worker, then match exact normalized aliases only. Duplicate
aliases from one package are fine; different packages with the same name, unknown
titles and document titles fall back to a neutral icon. This is a display-label
match, not a claim that SystemUI exposed the task's package. No fuzzy matching.

Load original PackageManager application drawables and preserve aspect/shape.
Publish an immutable bitmap map for the UI; no package or image lookup on the
UI thread. Limit cached artwork to24 icons sampled to32–96px; clear the in-memory
index/cache on exit. Icon failure must keep Open/Close usable. Nothing about
tasks or installed apps is sent over the network or persisted. Icons are not
focusable or separately announced; labels/actions provide accessible names.

Classic rows are flat96dp bands with48dp icons,20dp icon/name spacing and1dp
muted dividers. Shared TvControls adds a quiet variant, preserving Home's existing
variant. Remove the introductory paragraph; retain stable inline error/status
space and all existing footer recovery actions. D-pad and confirmation behavior
are unchanged. Native0.6.0 verified two original icons and one neutral fallback
in three rows, plus Close/Clear all/Cancel focus and Back restoration.
