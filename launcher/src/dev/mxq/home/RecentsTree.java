package dev.mxq.home;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

/** Bounded adapter for API-25 SystemUI controls, never a process/history inventory. */
final class RecentsTree {
    static final String SYSTEM_UI = "com.android.systemui";
    static final int MAX_NODES = 512, MAX_DEPTH = 24;
    interface Node {
        String pkg(); String id(); String text();
        Object key();
        boolean clickable(); boolean enabled(); boolean scrollable();
        int children(); Node child(int index); Node parent();
    }
    static final class Card {
        final String label;
        final Node open, close;
        Card(String name, Node launch, Node dismiss) { label = name; open = launch; close = dismiss; }
    }
    static final class Result {
        final List<Card> cards = new ArrayList<>();
        Node stack, clear;
        boolean recognized, truncated, hasTitles;
    }
    static boolean id(Node node, String value) {
        return node != null && SYSTEM_UI.equals(node.pkg())
            && (SYSTEM_UI + ":id/" + value).equals(node.id());
    }
    static Result scan(Node root) {
        Result result = new Result();
        if (root == null || !SYSTEM_UI.equals(root.pkg())) return result;
        List<Node> nodes = new ArrayList<>();
        Map<Node, Node> parents = new IdentityHashMap<>();
        Map<Node, int[]> spans = new IdentityHashMap<>();
        visit(root, null, 0, nodes, result, parents, spans);
        if (result.truncated) return result; // Partial trees never authorize actions.
        for (Node node : nodes) {
            if (id(node, "title")) result.hasTitles = true;
            if (id(node, "recents_view") || id(node, "recents_root")) result.recognized = true;
            if (id(node, "task_stack_view")) { result.stack = node; result.recognized = true; }
        }
        if (!result.recognized) return result;
        if (result.stack == null) for (Node node : nodes) {
            if (node.scrollable()) { result.stack = node; break; }
        }
        for (Node node : nodes) {
            if (id(node, "dismiss_task") && node.clickable() && node.enabled()) {
                Node ancestor = parents.get(node), title = null, launch = null;
                // Accessibility may omit a decorative header container. Find the
                // smallest ancestors containing exactly ONE title and dismiss button.
                for (int depth = 0; ancestor != null && depth < 4; depth++, ancestor = parents.get(ancestor)) {
                    int[] span = spans.get(ancestor);
                    if (span == null) break;
                    int titles = 0, dismissals = 0; Node candidate = null;
                    for (int i = span[0]; i < span[1]; i++) {
                        Node item = nodes.get(i);
                        if (id(item, "title")) { titles++; candidate = item; }
                        if (id(item, "dismiss_task")) dismissals++;
                    }
                    if (titles > 1 || dismissals > 1) break;
                    if (titles == 1 && dismissals == 1) {
                        title = candidate;
                        if (ancestor.clickable() && ancestor.enabled() && !id(ancestor, "task_view_bar")) {
                            launch = ancestor; break;
                        }
                    }
                }
                if (title != null) result.cards.add(new Card(label(title.text()), launch, node));
            }
            // API-25 uses the generic id/button for the stack's Clear all action.
            // Require BOTH the recognized Recents tree and its exact English action label.
            if ((id(node, "button") || id(node, "dismiss_all_task_views"))
                && node.clickable() && node.enabled()
                && "Clear all".equalsIgnoreCase(label(node.text()))) result.clear = node;
        }
        return result;
    }
    private static void visit(Node node, Node parent, int depth, List<Node> nodes, Result result,
        Map<Node, Node> parents, Map<Node, int[]> spans) {
        if (node == null || !SYSTEM_UI.equals(node.pkg())) return;
        if (depth > MAX_DEPTH || nodes.size() >= MAX_NODES) { result.truncated = true; return; }
        int start = nodes.size(); nodes.add(node); parents.put(node, parent);
        int count = Math.min(node.children(), MAX_NODES + 1);
        for (int i = 0; i < count && !result.truncated; i++)
            visit(node.child(i), node, depth + 1, nodes, result, parents, spans);
        spans.put(node, new int[] {start, nodes.size()});
    }
    static String label(String value) {
        if (value == null) return "App";
        String clean = value.replaceAll("[\\p{Cntrl}\\u202a-\\u202e\\u2066-\\u2069]", " ").trim();
        return clean.isEmpty() ? "App" : clean.substring(0, Math.min(80, clean.length()));
    }
    static boolean sameTasks(Result left, Result right) {
        if (left.cards.size() != right.cards.size() || (left.clear == null) != (right.clear == null)
            || (left.stack == null) != (right.stack == null) || left.hasTitles != right.hasTitles) return false;
        for (int i = 0; i < left.cards.size(); i++) {
            Card a = left.cards.get(i), b = right.cards.get(i);
            if (!a.label.equals(b.label) || !a.close.key().equals(b.close.key())
                || (a.open == null) != (b.open == null)) return false;
        }
        return true;
    }
}
