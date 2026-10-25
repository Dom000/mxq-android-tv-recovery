package dev.mxq.home;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Exact installed-label aliases only; duplicate names never pick an arbitrary brand. */
final class TaskIconIndex {
    private final Map<String, Set<String>> packages = new HashMap<>();
    void add(String label, String pkg) {
        String key = normalize(label);
        if (key.isEmpty() || pkg == null || pkg.isEmpty()) return;
        Set<String> values = packages.get(key);
        if (values == null) { values = new HashSet<>(); packages.put(key, values); }
        values.add(pkg);
    }
    String uniquePackage(String label) {
        Set<String> values = packages.get(normalize(label));
        return values == null || values.size() != 1 ? null : values.iterator().next();
    }
    private static String normalize(String label) {
        if (label == null) return "";
        return Normalizer.normalize(label, Normalizer.Form.NFKC)
            .replaceAll("[\\p{Cntrl}\\u202a-\\u202e\\u2066-\\u2069]", " ")
            .trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
