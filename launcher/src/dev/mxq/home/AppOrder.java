package dev.mxq.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/** Pure ordering logic shared by initial pins, saved pins and package refresh. */
final class AppOrder {
    static final String[] SEEDS = {
        "com.google.android.youtube.tv", "com.brave.browser", "org.com.kdplayer",
        "com.netflix.mediaclient", "com.android.vending", "cm.aptoidetv.pt"
    };

    static List<String> availablePins(Collection<String> saved, Collection<String> installed) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String name : saved) if (installed.contains(name)) result.add(name);
        return new ArrayList<>(result);
    }

    private AppOrder() { }
}
