package dev.mxq.home;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/** Separately enabled remote UI; SystemUI notifications only, no key filter/root/storage/network. */
public final class RemoteRecentsService extends AccessibilityService {
    private static volatile RemoteRecentsService connected;
    private final Handler handler = new Handler();
    private final ExecutorService reader = Executors.newSingleThreadExecutor();
    private RecentsPanel panel;
    private Snapshot shown;
    private volatile int generation;
    private volatile boolean active;
    private boolean busy, clearing;
    private long openDeadline;
    private long clearDeadline;
    private int clearRequests;
    private int clearGeneration;
    private Identity pendingIdentity;
    private volatile String probe = "idle", phase = "idle", failureClass = "none";
    private volatile int windowCount, cardCount;
    private AccessibilityNodeInfo trustedRoot;
    private boolean usedActiveRoot;
    private String controlIds = "none";

    @Override protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        // Platform diagnostic output contains fixed state labels/counts only, never task text.
        writer.println("REMOTE_RECENTS connected=" + (connected == this) + " active=" + active
            + " panel=" + (panel != null) + " phase=" + phase + " probe=" + probe
            + " windows=" + windowCount + " cards=" + cardCount + " activeRoot=" + usedActiveRoot + " error=" + failureClass);
        if (getServiceInfo() != null) writer.println("REMOTE_RECENTS flags=" + getServiceInfo().flags
            + " capabilities=" + getServiceInfo().getCapabilities());
        if (panel != null) writer.println(panel.diagnostics());
        writer.println("REMOTE_CONTROLS " + controlIds);
    }

    static boolean available() { return connected != null; }
    static void homeResumed() {
        RemoteRecentsService service = connected;
        if (service != null && service.active) service.stop(false);
    }
    static boolean open() {
        RemoteRecentsService service = connected;
        if (service == null) return false;
        service.begin(); return true;
    }
    @Override protected void onServiceConnected() { connected = this; watchChanges(false); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* Contents never inspected. */ }
    @Override public void onInterrupt() { stop(false); }
    @Override public boolean onUnbind(Intent intent) {
        if (connected == this) connected = null;
        stop(false); return super.onUnbind(intent);
    }
    @Override public void onDestroy() {
        if (connected == this) connected = null;
        stop(false); reader.shutdown(); super.onDestroy();
    }

    private void begin() {
        stop(false); active = true; phase = "opening"; openDeadline = SystemClock.uptimeMillis() + 10000;
        watchChanges(true);
        try {
            if (!performGlobalAction(GLOBAL_ACTION_RECENTS)) { fail(); return; }
            awaitTree(generation, 0);
        } catch (RuntimeException refused) { fail(); }
    }
    private void awaitTree(int token, int attempt) {
        handler.postDelayed(() -> {
            if (!active || token != generation) return;
            readAsync(snapshot -> {
            if (snapshot != null) {
                panel = new RecentsPanel(this, new RecentsPanel.Actions() {
                    public void open(int index) { act(index, false); }
                    public void close(int index) { act(index, true); }
                    public void clear() { startClear(); }
                    public void page(boolean forward) { pageTasks(forward); }
                    public void refresh() { if (!busy && !clearing) { panel.pending("Refreshing tasks…"); reload("", 0); } }
                    public void system() { stop(false); }
                    public void home() { stop(true); }
                    public void cancelClear() { RemoteRecentsService.this.cancelClear(); }
                });
                if (!panel.show()) { phase = "overlay-refused"; snapshot.close(); fail(); return; }
                phase = "panel-visible"; publish(snapshot, "", 0); watch(token); return;
            }
            if (attempt < 8 && SystemClock.uptimeMillis() < openDeadline) awaitTree(token, attempt + 1); else fail();
            });
        }, 160);
    }
    private void watch(int token) {
        handler.postDelayed(() -> {
            if (!active || token != generation) return;
            readAsync(current -> {
                if (current == null) { phase = "left-recents"; stop(false); return; }
                if (!busy && !clearing && panel != null && panel.canRefresh()
                    && shown != null && !RecentsTree.sameTasks(shown.tree, current.tree))
                    publish(current, "", panel.focusedRow());
                else current.close();
                watch(token);
            });
        }, 600);
    }
    private void publish(Snapshot snapshot, String message, int focus) {
        if (shown != null) shown.close(); shown = snapshot; busy = false;
        if (panel != null) panel.render(snapshot.tree, message, focus);
    }
    private void reload(String message, int focus) {
        if (!active || clearing) return;
        busy = true;
        readAsync(snapshot -> {
            if (snapshot == null) { stop(false); return; }
            publish(snapshot, message, focus);
        });
    }
    private void act(int index, boolean dismiss) {
        if (busy || clearing || shown == null || index < 0 || index >= shown.tree.cards.size()) return;
        RecentsTree.Card original = shown.tree.cards.get(index);
        busy = true; panel.pending(dismiss ? "Closing task…" : "Opening task…");
        readAsync(fresh -> {
        if (fresh == null) { stop(false); return; }
        RecentsTree.Card match = matching(fresh, original);
        boolean sent = match != null && click(dismiss ? match.close : match.open);
        fresh.close();
        if (!sent) { reload("That task changed or Android refused the action. Try again.", index); return; }
        if (!dismiss) { stop(false); return; }
        busy = true; panel.pending("Closing task…");
        int token = generation;
        handler.postDelayed(() -> { if (active && token == generation) reload("", index); }, 500);
        });
    }
    private RecentsTree.Card matching(Snapshot fresh, RecentsTree.Card previous) {
        ANode old = (ANode)previous.close;
        for (RecentsTree.Card card : fresh.tree.cards) {
            if (card.label.equals(previous.label) && ((ANode)card.close).info.equals(old.info)) return card;
        }
        return null;
    }
    private boolean click(RecentsTree.Node node) {
        if (!(node instanceof ANode)) return false;
        ANode target = (ANode)node;
        try {
            return target.info.refresh() && RecentsTree.SYSTEM_UI.equals(target.pkg())
                && target.enabled() && target.clickable()
                && target.info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } catch (RuntimeException refused) { return false; }
    }
    private void pageTasks(boolean forward) {
        if (busy || clearing) return;
        busy = true; panel.pending("Loading tasks…");
        readAsync(snapshot -> {
        if (snapshot == null) { stop(false); return; }
        boolean sent = false;
        try {
            // API25 scrolling uses the accessibility-focused task as its starting point.
            if (!snapshot.tree.cards.isEmpty()) {
                RecentsTree.Node card = snapshot.tree.cards.get(forward ? snapshot.tree.cards.size() - 1 : 0).open;
                if (card != null) ((ANode)card).info.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (snapshot.tree.stack != null) sent = ((ANode)snapshot.tree.stack).info.performAction(forward
                ? AccessibilityNodeInfo.ACTION_SCROLL_FORWARD : AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        } catch (RuntimeException ignored) { /* Keep navigation usable. */ }
        snapshot.close(); busy = true; panel.pending("Loading tasks…");
        final boolean moved = sent; int token = generation;
        handler.postDelayed(() -> { if (active && token == generation)
            reload(moved ? "" : "No further tasks exposed by Android in that direction.", 0); }, 450);
        });
    }
    private void startClear() {
        if (busy || clearing) return;
        clearing = true; clearRequests = 0; clearDeadline = SystemClock.uptimeMillis() + 20000;
        panel.clearing(); clearStep(++clearGeneration, null);
    }
    private void clearStep(int token, Identity previous) {
        if (!active || !clearing || token != clearGeneration) return;
        if (SystemClock.uptimeMillis() >= clearDeadline || clearRequests >= 32) {
            finishClear("Stopped at the safety limit. Some tasks may remain."); return;
        }
        readAsync(snapshot -> {
        if (!clearing || token != clearGeneration) { if (snapshot != null) snapshot.close(); return; }
        if (snapshot == null) { stop(false); return; }
        // Prefer Android's genuine bulk action when exposed. Never click a guessed coordinate.
        if (snapshot.tree.clear != null) {
            boolean sent = click(snapshot.tree.clear); snapshot.close();
            if (!sent) { finishClear("Android refused Clear all. No further action was sent."); return; }
            clearing = false; busy = true; panel.pending("Clear all requested…");
            handler.postDelayed(() -> { if (active && token == clearGeneration) reload("", 0); }, 700); return;
        }
        if (snapshot.tree.cards.isEmpty()) {
            boolean unsupported = snapshot.tree.hasTitles; snapshot.close();
            finishClear(unsupported ? "Some tasks don't expose a close control. Use System view." : "No recent apps."); return;
        }
        RecentsTree.Card first = snapshot.tree.cards.get(0);
        Identity identity = new Identity((ANode)first.close, first.label);
        if (previous != null && previous.same(identity)) {
            identity.close(); snapshot.close(); finishClear("Android didn't confirm removal. Some tasks may remain."); return;
        }
        boolean sent = click(first.close); snapshot.close();
        if (!sent) { identity.close(); finishClear("Android refused a close request. Some tasks may remain."); return; }
        clearRequests++;
        pendingIdentity = identity;
        handler.postDelayed(() -> {
            if (pendingIdentity != identity) return;
            pendingIdentity = null;
            try { clearStep(token, identity); } finally { identity.close(); }
        }, 500);
        });
    }
    private void finishClear(String message) { clearing = false; reload(message, 0); }
    private void cancelClear() {
        if (!clearing) return;
        clearing = false; clearGeneration++;
        if (pendingIdentity != null) { pendingIdentity.close(); pendingIdentity = null; }
        if (panel != null) panel.pending("Stopping…");
        reload("Stopped. Remaining tasks were left open.", 0);
    }
    private void fail() {
        stop(false);
        Toast.makeText(this, "Remote view couldn't read this Recents layout. Use System view in Home settings.", Toast.LENGTH_LONG).show();
    }
    private void stop(boolean home) {
        active = false; busy = false; clearing = false; generation++;
        clearGeneration++;
        if (pendingIdentity != null) { pendingIdentity.close(); pendingIdentity = null; }
        handler.removeCallbacksAndMessages(null);
        if (panel != null) { panel.hide(); panel = null; }
        if (shown != null) { shown.close(); shown = null; }
        reader.execute(() -> { if (trustedRoot != null) { trustedRoot.recycle(); trustedRoot = null; } });
        watchChanges(false);
        if (home) try { performGlobalAction(GLOBAL_ACTION_HOME); } catch (RuntimeException ignored) { }
    }

    private void watchChanges(boolean requested) {
        // Android7 needs window-change notifications to refresh its cached window
        // and root references. An empty event mask leaves stale roots after navigation;
        // content/scroll notifications refresh the framework cache only while used.
        try {
            AccessibilityServiceInfo info = getServiceInfo();
            if (info == null) return;
            // Package updates can retain an enabled service's old dynamic flags.
            // Apply the documented helper flags explicitly rather than copying
            // the server's previous configuration back over the new XML values.
            info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
                | AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
            info.packageNames = new String[] {RecentsTree.SYSTEM_UI};
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            if (requested) info.eventTypes |= AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED | AccessibilityEvent.TYPE_VIEW_SCROLLED;
            info.notificationTimeout = 200;
            setServiceInfo(info);
        } catch (RuntimeException disconnected) { /* A disabled service must remain harmless. */ }
    }

    private interface ReadResult { void accept(Snapshot snapshot); }
    private void readAsync(ReadResult callback) {
        int token = generation;
        reader.execute(() -> {
            if (!active || token != generation) return;
            Snapshot snapshot = read();
            handler.post(() -> {
                if (!active || token != generation) { if (snapshot != null) snapshot.close(); return; }
                callback.accept(snapshot);
            });
        });
    }

    /** Inspect only the top application window; other applications' content is never traversed. */
    private Snapshot read() {
        List<AccessibilityWindowInfo> windows = null;
        Snapshot snapshot = null;
        try {
            windows = getWindows(); windowCount = windows.size(); probe = "no-app-window";
            AccessibilityWindowInfo app = null;
            for (AccessibilityWindowInfo window : windows) {
                if (window.getType() == AccessibilityWindowInfo.TYPE_SYSTEM && window.isFocused()) {
                    probe = "system-window-focused"; return null;
                }
                if (window.getType() == AccessibilityWindowInfo.TYPE_APPLICATION
                    && (app == null || window.getLayer() > app.getLayer())) app = window;
            }
            if (app == null) return null;
            AccessibilityNodeInfo root = app.getRoot();
            if (root == null && trustedRoot != null && trustedRoot.getWindowId() == app.getId()) {
                root = AccessibilityNodeInfo.obtain(trustedRoot);
                if (!root.refresh()) { root.recycle(); root = null; }
            }
            // Some API25 vendor builds return null through AccessibilityWindowInfo
            // even for the focused application. Only before our overlay opens,
            // try the documented active-window API, then apply the same package
            // and Recents-layout checks. Never traverse other application roots.
            if (root == null && panel == null) {
                root = getRootInActiveWindow(); usedActiveRoot = root != null;
            }
            if (root == null) { probe = "no-root"; return null; }
            if (!RecentsTree.SYSTEM_UI.contentEquals(root.getPackageName() == null ? "" : root.getPackageName())) {
                probe = "other-app-window"; root.recycle(); return null;
            }
            snapshot = new Snapshot();
            snapshot.tree = RecentsTree.scan(snapshot.wrap(root));
            java.util.Set<String> ids = new java.util.LinkedHashSet<>();
            for (AccessibilityNodeInfo node : snapshot.owned) {
                String id = node.getViewIdResourceName();
                if (id != null && id.matches("com\\.android\\.systemui:id/[A-Za-z0-9_]{1,80}") && ids.size() < 24) ids.add(id);
            }
            controlIds = ids.toString();
            cardCount = snapshot.tree.cards.size();
            if (!snapshot.tree.recognized || snapshot.tree.truncated) {
                probe = snapshot.tree.truncated ? "tree-limit" : "unknown-layout";
                snapshot.close(); return null;
            }
            probe = "ready";
            if (trustedRoot != null) trustedRoot.recycle();
            trustedRoot = AccessibilityNodeInfo.obtain(root);
            return snapshot;
        } catch (RuntimeException unsupported) {
            probe = "exception"; failureClass = unsupported.getClass().getSimpleName();
            if (snapshot != null) snapshot.close(); return null;
        } finally {
            if (windows != null) for (AccessibilityWindowInfo window : windows) window.recycle();
        }
    }
    private static final class Identity {
        final int node, label;
        Identity(ANode value, String name) { node = value.info.hashCode(); label = name.hashCode(); }
        boolean same(Identity other) { return node == other.node && label == other.label; }
        void close() { /* Pure numeric fingerprint; no retained node or task text. */ }
    }
    private static final class Snapshot {
        final List<AccessibilityNodeInfo> owned = new ArrayList<>();
        RecentsTree.Result tree;
        ANode wrap(AccessibilityNodeInfo info) {
            if (info == null) return null;
            if (owned.size() >= 4096) { info.recycle(); throw new IllegalStateException("Tree too large"); }
            owned.add(info); return new ANode(this, info);
        }
        void close() { for (AccessibilityNodeInfo info : owned) info.recycle(); owned.clear(); }
    }
    private static final class ANode implements RecentsTree.Node {
        final Snapshot owner; final AccessibilityNodeInfo info;
        ANode(Snapshot snapshot, AccessibilityNodeInfo node) { owner = snapshot; info = node; }
        public String pkg() { return info.getPackageName() == null ? "" : info.getPackageName().toString(); }
        public String id() { return info.getViewIdResourceName(); }
        public Object key() { return info; }
        public String text() { return info.getText() == null ? "" : info.getText().toString(); }
        public boolean clickable() { return info.isClickable(); }
        public boolean enabled() { return info.isEnabled(); }
        public boolean scrollable() { return info.isScrollable(); }
        public int children() { return info.getChildCount(); }
        public ANode child(int index) { return owner.wrap(info.getChild(index)); }
        public ANode parent() { return owner.wrap(info.getParent()); }
    }
}
