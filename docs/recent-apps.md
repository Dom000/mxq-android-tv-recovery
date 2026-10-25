# Remote Recent apps

Evening Home0.6.0 replaces the small Android task-card controls with a simple
remote-friendly panel with original app icons and flat classic rows. It reads
Android's current Recents controls; it does not
invent a running-app list from launch history. The original system view remains
available. The owner requested this replacement because the native X and Clear
all could not be selected reliably with the D-pad.

## Setup and controls

1. Select **Recents → Accessibility settings**.
2. Enable **Evening Home — Remote Recents** (the separate entry with “Remote”).
3. Return Home and select **Recents** again.
4. Use Up/Down between rows, Left/Right between Open and Close, and OK to activate.

Clear all sits above the rows and asks for confirmation, initially focused on
Cancel. Back cancels that prompt and restores Clear all focus. Outside the prompt,
Back/Done returns Home. Refresh reloads Android's controls; Earlier/Later apps
pages the native stack where supported. System view reveals the original cards.
Settings also offers Remote Recent apps setup and Open system Recent apps.

The list updates after native tasks load or change. Installed icons are loaded
on the reader worker and matched through exact application/launcher labels.
Unknown or ambiguous names use a neutral glyph; these matches never change
task actions. The artwork cache is limited to24 icons32–96px and cleared on exit. No thumbnails, task-history
file, fake “running” counts or app shelves are added to Home. Unknown layouts and
refused/stale controls show recovery instead of sending guessed pointer actions.

## Task dismissal and background services

For the YouTube example: press Home, select Recents and choose Close beside
YouTube. This removes its task. A recent task does not prove the app is still
running, and dismissal may leave music or other services alive.

To stop those too, use **Settings → App settings → YouTube → Force stop** and
confirm Android's prompt if shown. This can interrupt playback/downloads but
preserves saved app data. Do not select Clear data for this purpose.

Clear all confirms possible unsaved-state loss. It uses Android's real bulk action
when available, otherwise closes exposed cards with a32-request/20-second limit,
Stop, cancellation and no-progress/refusal guards. Partial operations do not
claim everything was cleared. No task is dismissed merely by opening the panel.

## Permission and privacy

Android7 restricts ordinary launchers from managing other apps' tasks. The
optional RemoteRecentsService uses an owner-enabled Accessibility service and a
focusable TYPE_ACCESSIBILITY_OVERLAY. Android grants screen-content access for
this feature. Installation never enables it through ADB/root or secure settings.
The owner can disable it at any time. The old RecentsShortcut remains unchanged:
it only invokes the global Recents action, with zero events and no content access.

The new helper receives only SystemUI window-change notifications; content/scroll
notifications are added while requested to refresh Android's cache. Event contents
are ignored. Only a recognized SystemUI Recents hierarchy is traversed, bounded
to512 nodes/24 levels. Task-header labels and known control IDs remain in memory
while open; nothing is saved or uploaded. No screenshots, body/description text,
other apps' screens, root commands, global key filtering or gestures. Platform
diagnostics contain state/counts/focus roles and public control IDs, never names
or task contents. The service has no network or task-data storage path.

Screen reads run on one worker, with generation guards and bounded startup retries.
Callbacks are discarded and nodes released on exit/unbind/destroy; returning Home
cancels the panel. Reads never block the remote's UI thread. Nodes and labels are
revalidated before actions; rows are refreshed from Android rather than removed
optimistically. Unavailable actions and firmware limitations remain explicit.

## Reference MBOX compatibility and testing

The vendor's window API sometimes returns a null root; before displaying the
panel, the helper can use the documented active-window API with the same package/
layout checks. A verified root can be refreshed only for its matching window. The adapter avoids unreliable native parent lookups by building ancestry during
its bounded traversal. The firmware hides the underlying application from a
full-screen touch-modal overlay; the panel uses a16dp outer margin and a focusable non-modal
touch region so the native task controls remain exposed. Shared TvControls keeps
Home and the panel's button/focus style consistent.

**Do not use injected touch taps/swipes for automation on this firmware.** During
testing, injected touch events triggered a system-process NullPointerException in
`NavBarEventListener.onPointerEvent` (line83), restarting the Android framework.
No reboot, firmware edit or system-file write was issued. Testing switched to
D-pad key events. This is a firmware input-handler defect, not evidence of a
launcher permission failure. Physical mouse/trackpad coverage is not claimed.

The exact0.5.8 same-signer update installed successfully; default Home was retained.
Native key tests verified Open/Close focus, Clear all confirmation, Cancel focus,
Back cancellation and focus restoration with four task rows. The owner confirmed
that Close removes the chosen Settings task. The assistant did not execute actual
bulk clearing. Host tests cover tree/package isolation, nullable parents, bounds,
label sanitization, task-change detection and separate service/privacy settings.
Twelve browser checks use synthetic tasks; they are not native task-removal evidence.

Sources: [Android Recents](https://developer.android.com/guide/components/activities/recents),
[AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService),
[AccessibilityNodeInfo actions](https://developer.android.com/reference/android/view/accessibility/AccessibilityNodeInfo#performAction(int)),
[AOSP7.1.2 Recents activity](https://android.googlesource.com/platform/frameworks/base/+/android-7.1.2_r39/packages/SystemUI/src/com/android/systemui/recents/RecentsActivity.java).

The0.6.0 visual update was installed and verified separately: three live rows
showed two original installed icons and one neutral fallback. Native D-pad Close,
Clear all, confirmation Cancel and Back restoration passed again. No task was
closed during these cosmetic-update checks.
