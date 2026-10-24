package dev.mxq.home;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/** Optional, user-enabled bridge to Android's own task switcher. No event subscription. */
public final class RecentsShortcut extends AccessibilityService {
    enum Result { OPENED, NEEDS_SETUP, UNAVAILABLE }
    private static volatile RecentsShortcut connected;

    static Result showRecents() {
        RecentsShortcut service = connected;
        if (service == null) return Result.NEEDS_SETUP;
        try {
            return service.performGlobalAction(GLOBAL_ACTION_RECENTS) ? Result.OPENED : Result.UNAVAILABLE;
        } catch (SecurityException | IllegalStateException unavailable) {
            return Result.UNAVAILABLE;
        }
    }

    @Override protected void onServiceConnected() { connected = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* No screen/event data used. */ }
    @Override public void onInterrupt() { }
    @Override public boolean onUnbind(Intent intent) {
        if (connected == this) connected = null;
        return super.onUnbind(intent);
    }
    @Override public void onDestroy() {
        if (connected == this) connected = null;
        super.onDestroy();
    }
}
