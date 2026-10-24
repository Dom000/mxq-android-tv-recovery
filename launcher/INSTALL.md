# Installation and default-Home record

Completed 2026-10-24 on the fixed TV target 192.168.1.4:5555 after verifying its
RK3229 identity, API25, storage, reviewed APK hash/signature and existing Home.
The owner approved the preview, confirmed native D-pad operation, then explicitly
requested Evening Home as default for the remote Home button.

## Current state

- Installed dev.mxq.home v0.3.0 / versionCode3.
- Evening Home’s HomeEntry is enabled through a per-user component override.
- Default Home resolves to dev.mxq.home/.HomeEntry.
- One Android KEYCODE_HOME test opened Evening Home.
- Original com.droidlogic.mboxlauncher/.Launcher remains installed and enabled.
- No firmware write, stock-app removal/disable, network change or reboot.

The APK itself still ships HomeEntry disabled for fresh installations. Do not
repeat installation or activation merely because these commands are recorded.

## Completed installation

From launcher/:

```bash
adb -s 192.168.1.4:5555 install build/EveningHome-preview.apk
adb -s 192.168.1.4:5555 shell am start -W -n dev.mxq.home/.MainActivity
```

Android returned Success; initial launch Status: ok, TotalTime 796ms. Native icons,
movie wallpaper and Wi-Fi connected label rendered. Actual app-tile launch,
favorite dialogs, physical trackpad, Wi-Fi changes and sustained performance
remain untested by the assistant. The owner confirmed D-pad navigation.

## Completed 0.2.0 update

Owner requested the clean/classic layout and compact music controls. After fixed
identity/API/ABI and current-version/default checks, the same signing certificate
was verified and the following update succeeded:

```bash
adb -s 192.168.1.4:5555 install -r build/EveningHome-preview.apk
adb -s 192.168.1.4:5555 shell input keyevent KEYCODE_HOME
```

VersionCode2, enabled HomeEntry, default resolution and native Home rendering were
confirmed. Preferences and preferred Home survived; no root operation was used.
Music playback testing remains pending. See VERIFICATION.md for the exact hash.

## Completed 0.3.0 online-background update

The owner chose TVMaze without an API key. Same-key install-r succeeded after
fixed-target/API/current-version checks; HomeEntry/default remained selected.
The only added normal permission is INTERNET for API/CDN artwork. Native logs
confirmed8 cached backgrounds, and a Home screenshot showed downloaded Batman
art. No server, API key or extra Android app was installed for wallpapers.
Evidence:193304+0100 install,193417+0100 screen,193539+0100 source status.

## Completed default selection

This firmware rejected ordinary shell pm enable with SecurityException. The same
component enable succeeded via the existing root binary; root was used only for
the requested component setting, never to modify system files or partitions.

```bash
adb -s 192.168.1.4:5555 exec-out "su -c 'pm enable --user 0 dev.mxq.home/.HomeEntry'"
adb -s 192.168.1.4:5555 shell cmd package set-home-activity --user 0 dev.mxq.home/.HomeEntry
adb -s 192.168.1.4:5555 shell input keyevent KEYCODE_HOME
```

Package Manager reported Success, Home resolution selected Evening Home and the
Home-key test resumed it. Physical remote Home is expected to follow Android’s
Home action; the owner can confirm the physical button. Reboot persistence has
not been tested; no reboot is part of this work.

## Return to original Home — prepared, not executed

If requested, verify device identity first, restore the stock preferred Home,
withdraw Evening Home’s handler and press Home:

```bash
adb -s 192.168.1.4:5555 shell cmd package set-home-activity --user 0 com.droidlogic.mboxlauncher/.Launcher
adb -s 192.168.1.4:5555 exec-out "su -c 'pm disable-user --user 0 dev.mxq.home/.HomeEntry'"
adb -s 192.168.1.4:5555 shell input keyevent KEYCODE_HOME
```

Keep the original system launcher installed. Uninstalling Evening Home is a
separate optional operation and removes its own preferences:

```bash
adb -s 192.168.1.4:5555 uninstall dev.mxq.home
```

The owner’s original state-change approval rule still applies to future rollback
or removal. No rollback is authorized merely by this record.
ACCESS_NETWORK_STATE reads Wi-Fi connection state; INTERNET fetches requested
TVMaze artwork. No
location, SSID or credential query. Changing Home does not upgrade Android or
change application compatibility.
