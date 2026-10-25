package dev.mxq.home;

import java.util.ArrayList;
import java.util.List;

/** Synthetic UI trees: no personal app state or real task dismissal. */
public final class RecentsTreeTest {
    static final class N implements RecentsTree.Node {
        String pkg = RecentsTree.SYSTEM_UI, id = "", value = "";
        boolean click, enabled = true, scroll;
        int textReads;
        N parent; List<N> children = new ArrayList<>();
        N(String name) { id = pkg + ":id/" + name; }
        N add(N node) { node.parent = this; children.add(node); return this; }
        public String pkg() { return pkg; } public String id() { return id; }
        public Object key() { return this; }
        public String text() { textReads++; return value; }
        public boolean clickable() { return click; } public boolean enabled() { return enabled; }
        public boolean scrollable() { return scroll; }
        public int children() { return children.size(); } public N child(int i) { return children.get(i); }
        public N parent() { return null; } // Vendor parent lookup can be unavailable.
    }
    static N card(String label) {
        N card = new N("task_view"); card.click = true;
        N title = new N("title"); title.value = label;
        N close = new N("dismiss_task"); close.click = true;
        return card.add(new N("task_view_bar").add(title).add(close));
    }
    static void check(boolean condition) { if (!condition) throw new AssertionError(); }
    public static void main(String[] args) {
        N body = new N("thumbnail_private_contents"); body.value = "NEVER READ THIS";
        N one = card("YouTube"), two = card("Music"); one.add(body);
        N root = new N("recents_view").add(new N("task_stack_view").add(one).add(two));
        N clear = new N("button"); clear.click = true; clear.value = "CLEAR ALL"; root.add(clear);
        RecentsTree.Result result = RecentsTree.scan(root);
        check(result.recognized && !result.truncated && result.cards.size() == 2 && result.clear == clear);
        check(result.cards.get(0).label.equals("YouTube") && result.cards.get(0).open == one);
        check(body.textReads == 0);
        check(RecentsTree.sameTasks(result, RecentsTree.scan(root)));
        check(!RecentsTree.sameTasks(result, RecentsTree.scan(new N("recents_view"))));
        check(!RecentsTree.sameTasks(RecentsTree.scan(new N("recents_view").add(card("Same app"))),
            RecentsTree.scan(new N("recents_view").add(card("Same app")))));
        N flat = new N("task_view"); flat.click = true;
        N flatTitle = new N("title"); flatTitle.value = "Flat header";
        N flatClose = new N("dismiss_task"); flatClose.click = true;
        flat.add(flatTitle).add(flatClose);
        N unnamedStack = new N(""); unnamedStack.scroll = true; unnamedStack.add(flat);
        result = RecentsTree.scan(new N("recents_view").add(unnamedStack));
        check(result.cards.size() == 1 && result.cards.get(0).open == flat && result.stack == unnamedStack);
        N other = card("Secret"); other.pkg = "com.example.other";
        check(!RecentsTree.scan(other).recognized);
        N notification = new N("notification_panel").add(card("Fake task"));
        check(RecentsTree.scan(notification).cards.isEmpty());
        N falseClear = new N("button"); falseClear.click = true; falseClear.value = "Install update";
        check(RecentsTree.scan(new N("recents_view").add(falseClear)).clear == null);
        N disabled = card("Blocked"); disabled.children.get(0).children.get(1).enabled = false;
        result = RecentsTree.scan(new N("recents_view").add(disabled));
        check(result.cards.isEmpty() && result.hasTitles);
        N huge = new N("recents_view"); for (int i = 0; i < 600; i++) huge.add(new N("filler"));
        result = RecentsTree.scan(huge); check(result.truncated && result.cards.isEmpty() && result.clear == null);
        N deep = new N("recents_view"), cursor = deep;
        for (int i = 0; i < 30; i++) { N child = new N("nested"); cursor.add(child); cursor = child; }
        check(RecentsTree.scan(deep).truncated);
        check(RecentsTree.label("\u202e App\nName\u2066").equals("App Name"));
        check(RecentsTree.label(new String(new char[100]).replace('\0', 'x')).length() == 80);
        System.out.println("Recents: real-control selection, no body reads, package/layout isolation, disabled actions, tree bounds and safe labels passed");
    }
}
