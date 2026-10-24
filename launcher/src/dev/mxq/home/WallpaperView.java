package dev.mxq.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Cached online/bundled stills. At most two decoded frames; no playback service. */
final class WallpaperView extends View {
    static final long INTERVAL_MS = 15000;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect crop = new Rect();
    private final RectF destination = new RectF();
    private final ExecutorService decoder = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SharedPreferences preferences;
    private final List<String> titles = new ArrayList<>(), files = new ArrayList<>();
    private final List<String> bundledTitles = new ArrayList<>(), bundledFiles = new ArrayList<>();
    private final OnlineWallpapers online;
    private List<String> pendingTitles, pendingFiles;
    private String currentTitle = "Dusk", incomingTitle;
    private Bitmap current, incoming;
    private LinearGradient scrim, verticalScrim;
    private int index, incomingIndex;
    private float blend;
    private boolean active, attached, closed, decoding;
    private ValueAnimator animator;
    private Runnable listener;
    private final Runnable advance = () -> next(true);

    WallpaperView(Context context) {
        super(context); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        preferences = context.getSharedPreferences("evening-home", Context.MODE_PRIVATE);
        online = new OnlineWallpapers(context, entries -> {
            List<String> names = new ArrayList<>(), paths = new ArrayList<>();
            for (OnlineWallpapers.Entry entry : entries) { names.add(entry.title); paths.add(onlinePath(entry)); }
            queueCatalog(names, paths);
        });
        decoder.execute(() -> {
            List<String> names = new ArrayList<>(), paths = new ArrayList<>();
            try (InputStream stream = getContext().getAssets().open("wallpapers/catalog.json")) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream(); byte[] chunk = new byte[4096]; int count;
                while ((count = stream.read(chunk)) != -1) bytes.write(chunk, 0, count);
                JSONArray entries = new JSONObject(bytes.toString("UTF-8")).getJSONArray("wallpapers");
                for (int i = 0; i < entries.length(); i++) {
                    JSONObject entry = entries.getJSONObject(i); String file = entry.getString("file");
                    if (file.matches("[a-z0-9-]+\\.(jpg|jpeg|png|webp)")) {
                        names.add(entry.getString("title")); paths.add("wallpapers/" + file);
                    }
                }
            } catch (Exception ignored) { /* Keep the built-in landscape if assets fail. */ }
            handler.post(() -> {
                if (closed) return;
                bundledTitles.addAll(names); bundledFiles.addAll(paths);
                if (files.isEmpty()) { titles.addAll(names); files.addAll(paths);
                    if (!files.isEmpty()) load(Math.floorMod(preferences.getInt("wallpaper", 0), files.size()), false);
                }
                else if (listener != null) listener.run();
            });
        });
    }

    void setListener(Runnable callback) { listener = callback; }
    String title() { return currentTitle; }
    private String onlinePath(OnlineWallpapers.Entry entry) { return online.path(entry); }
    boolean onlineEnabled() { return online.enabled(); }
    String onlineStatus() { return online.status(); }
    void refreshOnline() { online.refresh(true); }
    void toggleOnline() {
        online.toggle();
        if (!online.enabled()) queueCatalog(new ArrayList<>(bundledTitles), new ArrayList<>(bundledFiles));
    }
    private void queueCatalog(List<String> names, List<String> paths) {
        if (closed || paths.isEmpty()) return;
        if (files.equals(paths)) { pendingTitles = null; pendingFiles = null; return; }
        pendingTitles = names; pendingFiles = paths;
        if (current == null && !decoding) next(false); else schedule();
    }
    boolean motionEnabled() { return preferences.getBoolean("wallpaper-motion", true); }
    void toggleMotion() {
        preferences.edit().putBoolean("wallpaper-motion", !motionEnabled()).apply();
        if (!motionEnabled()) finishTransition(); schedule();
    }
    private boolean systemMotionEnabled() {
        try { return Settings.Global.getFloat(getContext().getContentResolver(),
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0; }
        catch (RuntimeException ignored) { return false; }
    }
    void setActive(boolean value) {
        active = value;
        online.setActive(value);
        if (!value) finishTransition();
        schedule();
    }
    void next(boolean animate) {
        if (closed || decoding) return;
        finishTransition();
        if (pendingFiles != null) {
            titles.clear(); titles.addAll(pendingTitles); files.clear(); files.addAll(pendingFiles);
            pendingTitles = null; pendingFiles = null; load(0, animate);
        } else if (files.size() > 1) load((index + 1) % files.size(), animate);
    }
    private void schedule() {
        handler.removeCallbacks(advance);
        if (active && attached && !closed && !decoding && incoming == null
            && (files.size() > 1 || pendingFiles != null) && motionEnabled() && systemMotionEnabled())
            handler.postDelayed(advance, INTERVAL_MS);
    }

    private void load(int next, boolean animate) {
        if (closed || decoding) return;
        finishTransition(); decoding = true; handler.removeCallbacks(advance);
        final String file = files.get(next);
        final String name = titles.get(next);
        decoder.execute(() -> {
            Bitmap decoded = null;
            try {
                BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
                try (InputStream stream = openFrame(file)) { BitmapFactory.decodeStream(stream, null, bounds); }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565; options.inSampleSize = 1;
                while (bounds.outWidth / options.inSampleSize > 1600 || bounds.outHeight / options.inSampleSize > 900)
                    options.inSampleSize *= 2;
                try (InputStream stream = openFrame(file)) { decoded = BitmapFactory.decodeStream(stream, null, options); }
            } catch (Exception ignored) { /* Bad/missing image leaves the previous frame. */ }
            final Bitmap image = decoded;
            handler.post(() -> {
                if (closed) return;
                decoding = false;
                if (image == null) { schedule(); return; }
                incoming = image; incomingIndex = next; incomingTitle = name; blend = 0;
                if (animate && active && current != null && motionEnabled() && systemMotionEnabled()) {
                    animator = ValueAnimator.ofFloat(0, 1); animator.setDuration(900);
                    animator.addUpdateListener(value -> { blend = (float)value.getAnimatedValue(); invalidate(); });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(Animator ignored) { promote(); }
                    }); animator.start();
                } else promote();
            });
        });
    }

    private InputStream openFrame(String file) throws java.io.IOException {
        return file.startsWith("/") ? new FileInputStream(file) : getContext().getAssets().open(file);
    }

    private void promote() {
        if (incoming != null) {
            current = incoming; incoming = null; index = incomingIndex; blend = 0;
            currentTitle = incomingTitle;
            preferences.edit().putInt("wallpaper", index).apply();
            if (listener != null) listener.run(); invalidate();
        }
        animator = null; schedule();
    }
    private void finishTransition() {
        if (animator != null) { ValueAnimator pending = animator; animator = null; pending.cancel(); }
        if (incoming != null) promote();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        destination.set(0, 0, w, h);
        scrim = new LinearGradient(0, 0, w, 0,
            new int[] {0x60101c29, 0x28101c29, 0x30101c29}, new float[] {0, .46f, 1}, Shader.TileMode.CLAMP);
        verticalScrim = new LinearGradient(0, 0, 0, h,
            new int[] {0xe0101c29, 0x00101c29, 0x00101c29, 0xf0101c29},
            new float[] {0, .22f, .50f, 1}, Shader.TileMode.CLAMP);
    }
    @Override protected void onDraw(Canvas canvas) {
        if (current == null) return;
        drawFrame(canvas, current, 255);
        if (incoming != null) drawFrame(canvas, incoming, Math.round(255 * blend));
        paint.setAlpha(255); paint.setShader(scrim); canvas.drawRect(destination, paint); paint.setShader(null);
        // Continuous shading protects navigation and app labels without hard bands.
        paint.setShader(verticalScrim); canvas.drawRect(destination, paint); paint.setShader(null);
    }
    private void drawFrame(Canvas canvas, Bitmap frame, int alpha) {
        float scale = Math.max((float)getWidth() / frame.getWidth(), (float)getHeight() / frame.getHeight());
        int width = Math.round(getWidth() / scale), height = Math.round(getHeight() / scale);
        int left = frame.getWidth() - width, top = (frame.getHeight() - height) / 2;
        crop.set(left, top, left + width, top + height); paint.setAlpha(alpha);
        canvas.drawBitmap(frame, crop, destination, paint);
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); attached = true; schedule(); }
    @Override protected void onDetachedFromWindow() {
        attached = false; finishTransition(); handler.removeCallbacks(advance); super.onDetachedFromWindow();
    }
    void close() {
        closed = true; active = false; handler.removeCallbacksAndMessages(null);
        online.close();
        finishTransition(); decoder.shutdownNow(); current = null; incoming = null;
    }
}
