# Recent apps on Evening Home

Pressing Home leaves an app's task available for returning later. Version 0.4.0
adds a **Recents** button to Home and Apps that opens Android's own task switcher.
This is the actual system task list; the launcher does not invent a list from
apps you opened through it.

## One-time setup

1. Select **Recents → Accessibility settings**.
2. Enable **Evening Home — Recents** in Android's Accessibility settings.
3. Return Home and select **Recents** again.
4. Select a task to return to it, or use Android's dismiss control to remove it.

The exact card layout, D-pad controls and availability of Clear all depend on
the firmware. They have not yet been verified on this MBOX. The launcher does not
add its own Clear all. A remote with an app-switch key may open the same screen
without enabling this shortcut.

## Actually stopping an app

A recent task is not proof that an app is still running. Removing a card does not
guarantee that a music player or another separate service stops.

For the YouTube example: press Home, select Recents and dismiss YouTube's task.
If you also want to stop its background work, use **Settings → App settings →
YouTube → Force stop**, then confirm Android's prompt if shown. This can interrupt
playback/downloads. Saved app data remains; do not select Clear data.

App settings lists installed apps with launchable activities, not running
processes. Android owns the Force stop control and can restrict it for some apps.
No apps are automatically stopped when Home opens, when installing the launcher,
or during testing.

## Permission and implementation

Android 7 restricts ordinary launchers from listing/removing other apps' tasks.
The AOSP 7.1.2 SystemUI Recents activities are not exported. Resolving an activity
through the shell does not establish that a normal application may launch it.

The optional `RecentsShortcut` service uses the documented accessibility global
action `GLOBAL_ACTION_RECENTS`. Android requires the owner to enable the service
in its settings. Installation does not enable it. Disable it in the same settings
at any time. It subscribes to zero accessibility events, cannot retrieve window
content, and does not request gestures, key filtering or touch exploration.
Only Android may bind to the service through `BIND_ACCESSIBILITY_SERVICE`.
No root commands, screen contents, thumbnails, app history or usage-access data
are read or retained by the launcher. Nothing about tasks is sent to TVMaze.

If setup is incomplete or Android refuses the action, a native dialog offers
Accessibility settings, App settings and Cancel. The launcher does not enable
permissions through ADB or bypass SystemUI's restrictions.

## Verification

Version 0.4.0 builds and passes host packaging/privacy checks. Browser checks cover
the button, an explicitly illustrative handoff dialog, focus restoration and header
fit. The preview cannot show or close real Android tasks.

The previously verified MBOX installation remains 0.3.0. Its fixed ADB endpoint
refused connections during this change on 2026-10-24. Installation, accessibility
setup, actual SystemUI handoff and task dismissal remain pending; no arbitrary
apps were closed for verification.

Sources: [Android Recents screen](https://developer.android.com/guide/components/activities/recents),
[AccessibilityService global actions](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#GLOBAL_ACTION_RECENTS),
[AOSP 7.1.2 SystemUI manifest](https://android.googlesource.com/platform/frameworks/base/+/android-7.1.2_r39/packages/SystemUI/AndroidManifest.xml).
