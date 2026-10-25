package dev.mxq.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reader-worker-owned installed artwork; UI only reads the published bitmap map. */
final class InstalledTaskIcons {
    private static final int LIMIT = 24;
    private final Context context;
    private TaskIconIndex index;
    private final LinkedHashMap<String, Bitmap> cache = new LinkedHashMap<String, Bitmap>(LIMIT, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Bitmap> entry) { return size() > LIMIT; }
    };
    private volatile Map<String, Bitmap> published = Collections.emptyMap();
    InstalledTaskIcons(Context owner) { context = owner; }
    Bitmap get(String label) { return published.get(label); }
    void clear() { index = null; cache.clear(); published = Collections.emptyMap(); }
    void prepare(List<RecentsTree.Card> cards) {
        if (cards.isEmpty()) { published = Collections.emptyMap(); return; }
        PackageManager pm = context.getPackageManager();
        if (index == null) {
            index = new TaskIconIndex();
            try {
                List<ApplicationInfo> applications = pm.getInstalledApplications(0);
                // Never treat a partial installed-label index as an unambiguous match.
                if (applications.size() > 512) { published = Collections.emptyMap(); return; }
                for (ApplicationInfo app : applications) index.add(app.loadLabel(pm).toString(), app.packageName);
                for (String category : new String[] {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
                    for (ResolveInfo entry : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category), 0))
                        if (entry.activityInfo != null) index.add(entry.loadLabel(pm).toString(), entry.activityInfo.packageName);
                }
            } catch (RuntimeException unavailable) { index = new TaskIconIndex(); }
        }
        Map<String, Bitmap> ready = new HashMap<>(); Set<String> requested = new HashSet<>();
        for (RecentsTree.Card card : cards) {
            String pkg = index.uniquePackage(card.label);
            if (pkg == null) continue;
            if (!requested.contains(pkg) && requested.size() >= LIMIT) continue;
            requested.add(pkg);
            Bitmap bitmap = cache.get(pkg);
            if (bitmap == null) {
                try {
                    Drawable drawable = pm.getApplicationIcon(pkg);
                    int size = Math.max(32, Math.min(96, TvControls.dp(context, 48)));
                    bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    int width = Math.max(1, drawable.getIntrinsicWidth()), height = Math.max(1, drawable.getIntrinsicHeight());
                    float scale = Math.min((float)size / width, (float)size / height);
                    int w = Math.round(width * scale), h = Math.round(height * scale);
                    drawable.setBounds((size - w) / 2, (size - h) / 2, (size + w) / 2, (size + h) / 2);
                    drawable.draw(canvas); cache.put(pkg, bitmap);
                } catch (PackageManager.NameNotFoundException | RuntimeException unavailable) { bitmap = null; }
            }
            if (bitmap != null) ready.put(card.label, bitmap);
        }
        published = Collections.unmodifiableMap(ready);
    }
}
