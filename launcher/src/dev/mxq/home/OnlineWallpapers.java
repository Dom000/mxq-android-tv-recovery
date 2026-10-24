package dev.mxq.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** A small, bounded TVMaze cache. No server, API key, account or background service. */
final class OnlineWallpapers {
    interface Listener { void changed(List<Entry> entries); }
    static final class Entry {
        final String title, url, source, file;
        Entry(String name, String image, String page, String filename) {
            title = name; url = image; source = page; file = filename;
        }
    }
    private final Context context;
    private final SharedPreferences preferences;
    private final File directory;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile HttpURLConnection connection;
    private volatile boolean active, closed;
    private volatile int generation;
    private boolean busy;
    private Future<?> task;
    private volatile String status = "TVMaze · not refreshed yet";

    OnlineWallpapers(Context owner, Listener callback) {
        context = owner.getApplicationContext(); listener = callback;
        preferences = context.getSharedPreferences("evening-home", Context.MODE_PRIVATE);
        directory = new File(context.getFilesDir(), "online-wallpapers");
    }

    boolean enabled() { return preferences.getBoolean("wallpaper-online", true); }
    String status() { return enabled() ? status : "Bundled movie wallpapers"; }
    void toggle() {
        preferences.edit().putBoolean("wallpaper-online", !enabled()).apply();
        cancel(); if (enabled() && active) refresh(false);
    }
    void setActive(boolean value) {
        active = value;
        if (value) refresh(false); else cancel();
    }
    private void cancel() {
        generation++;
        if (task != null) task.cancel(true);
        HttpURLConnection pending = connection; if (pending != null) pending.disconnect();
        busy = false;
    }
    private void check(int token) throws IOException {
        if (closed || !active || token != generation || Thread.currentThread().isInterrupted())
            throw new IOException("Cancelled");
    }

    void refresh(boolean force) {
        if (busy || closed || !active || !enabled()) return;
        busy = true; final int token = generation;
        task = worker.submit(() -> {
            List<Entry> cached = new ArrayList<>();
            try {
                if (!directory.isDirectory() && !directory.mkdirs()) throw new IOException("Cache unavailable");
                check(token);
                try { cached = readCache(); } catch (Exception corrupt) { /* Rebuild public cache. */ }
                prune(cached);
                publish(cached, token);
                long now = System.currentTimeMillis();
                if (!OnlineWallpaperPolicy.due(now, cached.isEmpty() ? 0 : preferences.getLong("online-last-success", 0),
                    preferences.getLong("online-last-attempt", 0), force)) {
                    status = "TVMaze · " + cached.size() + " saved backgrounds"; return;
                }
                preferences.edit().putLong("online-last-attempt", now).apply();
                status = "TVMaze · updating backgrounds…";
                List<List<Entry>> groups = candidates(token);
                List<Entry> fresh = new ArrayList<>();
                // Round-robin keeps action and animation represented in the small cache.
                for (int round = 0; round < 8 && fresh.size() < OnlineWallpaperPolicy.MAX_IMAGES; round++) {
                    for (List<Entry> group : groups) {
                        if (fresh.size() == OnlineWallpaperPolicy.MAX_IMAGES) break;
                        if (round >= group.size()) continue;
                        check(token); Entry entry = group.get(round);
                        try { fetchImage(entry, token); fresh.add(entry); }
                        catch (IOException unavailable) { check(token); }
                    }
                }
                check(token);
                if (fresh.isEmpty()) throw new IOException("No images available");
                writeCache(fresh); prune(fresh);
                preferences.edit().putLong("online-last-success", System.currentTimeMillis()).apply();
                status = "TVMaze · " + fresh.size() + " saved backgrounds";
                Log.i("EveningWallpaper", "TVMaze cache refreshed: " + fresh.size() + " images");
                publish(fresh, token);
            } catch (Exception unavailable) {
                if (active && token == generation && !closed) {
                    status = "TVMaze · update unavailable, " + cached.size() + " saved";
                    Log.i("EveningWallpaper", "TVMaze update unavailable: " + unavailable.getClass().getSimpleName());
                    prune(cached);
                }
            } finally {
                handler.post(() -> { if (token == generation) busy = false; });
            }
        });
    }

    private void publish(List<Entry> entries, int token) {
        if (entries.isEmpty()) return;
        List<Entry> snapshot = new ArrayList<>(entries);
        handler.post(() -> { if (!closed && active && enabled() && token == generation) listener.changed(snapshot); });
    }

    private List<List<Entry>> candidates(int token) throws Exception {
        JSONArray shows;
        try (InputStream input = context.getAssets().open("wallpapers/tvmaze-shows.json")) {
            shows = new JSONArray(new String(readLocal(input, 65536), "UTF-8"));
        }
        List<List<Entry>> groups = new ArrayList<>();
        for (int i = 0; i < Math.min(shows.length(), 5); i++) {
            check(token); JSONObject show = shows.getJSONObject(i); int id = show.getInt("id");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try { download("https://api.tvmaze.com/shows/" + id + "/images", bytes, 1024 * 1024, token); }
            catch (IOException unavailable) { check(token); continue; }
            JSONArray images = new JSONArray(bytes.toString("UTF-8")); List<Entry> entries = new ArrayList<>();
            for (int n = 0; n < images.length(); n++) {
                JSONObject image = images.getJSONObject(n);
                if (!"background".equals(image.optString("type"))) continue;
                JSONObject resolutions = image.optJSONObject("resolutions");
                JSONObject original = resolutions == null ? null : resolutions.optJSONObject("original");
                if (original == null || !OnlineWallpaperPolicy.dimensions(original.optInt("width"), original.optInt("height"))) continue;
                String url = original.optString("url"); long imageId = image.optLong("id", -1);
                if (!OnlineWallpaperPolicy.imageUrl(url) || imageId < 0) continue;
                entries.add(new Entry(show.getString("title"), url, show.getString("sourcePage"), "tvmaze-" + imageId + ".jpg"));
            }
            Collections.shuffle(entries); if (!entries.isEmpty()) groups.add(entries);
        }
        return groups;
    }

    private void fetchImage(Entry entry, int token) throws IOException {
        File target = new File(directory, entry.file);
        if (validFile(target)) return;
        File partial = new File(directory, entry.file + ".part");
        try {
            try (OutputStream output = new FileOutputStream(partial)) {
                download(entry.url, output, OnlineWallpaperPolicy.MAX_IMAGE_BYTES, token);
            }
            check(token);
            if (!validFile(partial) || !partial.renameTo(target)) throw new IOException("Invalid image");
        } finally { partial.delete(); }
    }
    private boolean validFile(File file) {
        if (!file.isFile() || file.length() <= 0 || file.length() > OnlineWallpaperPolicy.MAX_IMAGE_BYTES) return false;
        BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        return OnlineWallpaperPolicy.dimensions(bounds.outWidth, bounds.outHeight);
    }

    private void download(String url, OutputStream output, int limit, int token) throws IOException {
        check(token);
        HttpURLConnection request = (HttpURLConnection)new URL(url).openConnection();
        connection = request;
        request.setInstanceFollowRedirects(false); request.setConnectTimeout(7000); request.setReadTimeout(10000);
        request.setRequestProperty("User-Agent", "EveningHome/0.3 (Android TV wallpaper cache)");
        request.setRequestProperty("Accept", url.startsWith("https://api.tvmaze.com/") ? "application/json" : "image/*");
        try {
            if (request.getResponseCode() != 200 || request.getContentLengthLong() > limit) throw new IOException("HTTP response unavailable");
            try (InputStream input = request.getInputStream()) {
                byte[] chunk = new byte[8192]; int count, total = 0;
                while ((count = input.read(chunk)) != -1) {
                    check(token); total += count;
                    if (total > limit) throw new IOException("Response too large");
                    output.write(chunk, 0, count);
                }
            }
        } finally { request.disconnect(); if (connection == request) connection = null; }
    }

    private List<Entry> readCache() throws Exception {
        File manifest = new File(directory, "catalog.json"); List<Entry> entries = new ArrayList<>();
        if (!manifest.isFile()) return entries;
        JSONArray rows;
        try (InputStream input = new FileInputStream(manifest)) { rows = new JSONArray(new String(readLocal(input, 65536), "UTF-8")); }
        for (int i = 0; i < Math.min(rows.length(), OnlineWallpaperPolicy.MAX_IMAGES); i++) {
            JSONObject row = rows.getJSONObject(i); String file = row.optString("file"), url = row.optString("imageUrl");
            if (!file.matches("tvmaze-[0-9]+\\.jpg") || !OnlineWallpaperPolicy.imageUrl(url) || !validFile(new File(directory, file))) continue;
            String title = row.optString("title", "TVMaze");
            entries.add(new Entry(title.substring(0, Math.min(title.length(), 80)), url, row.optString("sourcePage"), file));
        }
        return entries;
    }
    private void writeCache(List<Entry> entries) throws Exception {
        JSONArray rows = new JSONArray();
        for (Entry entry : entries) rows.put(new JSONObject().put("title", entry.title).put("file", entry.file)
            .put("imageUrl", entry.url).put("sourcePage", entry.source).put("provider", "TVMaze")
            .put("dataLicense", "CC BY-SA: https://www.tvmaze.com/api#licensing"));
        File partial = new File(directory, "catalog.json.part");
        try (FileOutputStream output = new FileOutputStream(partial)) {
            output.write(rows.toString().getBytes("UTF-8")); output.getFD().sync();
        }
        if (!partial.renameTo(new File(directory, "catalog.json"))) throw new IOException("Cache commit unavailable");
    }
    private void prune(List<Entry> keep) {
        Set<String> names = new HashSet<>(); names.add("catalog.json");
        for (Entry entry : keep) names.add(entry.file);
        File[] files = directory.listFiles(); if (files == null) return;
        for (File file : files) if (!names.contains(file.getName())) file.delete();
    }
    private byte[] readLocal(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] chunk = new byte[4096]; int count;
        while ((count = input.read(chunk)) != -1) {
            if (output.size() + count > limit) throw new IOException("Metadata too large");
            output.write(chunk, 0, count);
        }
        return output.toByteArray();
    }
    String path(Entry entry) { return new File(directory, entry.file).getAbsolutePath(); }
    void close() { closed = true; active = false; cancel(); worker.shutdownNow(); handler.removeCallbacksAndMessages(null); }
}
