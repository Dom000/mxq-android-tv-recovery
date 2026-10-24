package dev.mxq.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Dependency-free, remote-first launcher for the confirmed API-25 TV. */
public final class MainActivity extends Activity {
    private static final ComponentName STOCK = new ComponentName(
        "com.droidlogic.mboxlauncher", "com.droidlogic.mboxlauncher.Launcher");
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final List<App> apps = new ArrayList<>();
    private final List<String> pins = new ArrayList<>();
    private final Map<String, View> targets = new LinkedHashMap<>();
    private SharedPreferences preferences;
    private LinearLayout page;
    private TextView clock;
    private TextView wifiLabel;
    private WifiGlyph wifiGlyph;
    private View wifiControl;
    private ScrollView appScroll;
    private HorizontalScrollView pinScroll;
    private String screen = "home", focusKey = "home-entry";
    private String loadError;
    private boolean loading = true, refreshing, clockRegistered;
    private int scrollY, scrollX;
    private AlertDialog activeDialog;
    private WallpaperView wallpaper;
    private MusicControls music;
    private View musicBar;
    private Button musicToggle;
    private final BroadcastReceiver ticker = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { updateClock(); updateWifi(); }
    };

    private static final class App {
        final String name, pkg;
        final ComponentName component;
        final Bitmap icon;
        App(String label, String packageName, ComponentName activity, Bitmap image) {
            name = label; pkg = packageName; component = activity; icon = image;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = getSharedPreferences("evening-home", MODE_PRIVATE);
        music = new MusicControls(this, this::updateMusic);
        if (state != null) {
            screen = state.getString("screen", "home");
            focusKey = state.getString("focus", "home-entry");
            scrollY = state.getInt("scrollY"); scrollX = state.getInt("scrollX");
        }
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED); filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(ticker, filter); clockRegistered = true;
        wallpaper.setActive(true);
        music.setActive(true);
        refreshApps();
    }

    @Override protected void onPause() {
        wallpaper.setActive(false);
        music.setActive(false);
        if (clockRegistered) { unregisterReceiver(ticker); clockRegistered = false; }
        rememberScroll();
        super.onPause();
    }

    @Override protected void onDestroy() {
        if (wallpaper != null) wallpaper.close();
        if (music != null) music.setActive(false);
        if (activeDialog != null) activeDialog.dismiss();
        loader.shutdownNow();
        super.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        rememberScroll();
        state.putString("screen", screen); state.putString("focus", focusKey);
        state.putInt("scrollY", scrollY); state.putInt("scrollX", scrollX);
        super.onSaveInstanceState(state);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent);
        if (intent.hasCategory(Intent.CATEGORY_HOME)) navigate("home");
    }

    @Override public void onBackPressed() {
        if (!screen.equals("home")) navigate("home");
        else if (getIntent().hasCategory(Intent.CATEGORY_HOME)) {
            View primary = homeFocus();
            if (primary != null) primary.requestFocus();
        } else super.onBackPressed();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { editFavorites(); return true; }
        return super.onKeyDown(keyCode, event);
    }

    private void rememberScroll() {
        if (appScroll != null) scrollY = appScroll.getScrollY();
        if (pinScroll != null) scrollX = pinScroll.getScrollX();
    }

    private void navigate(String next) {
        rememberScroll(); screen = next; focusKey = next.equals("home") ? "home-entry" : "tab:apps";
        render();
    }

    private void refreshApps() {
        if (refreshing) return;
        refreshing = true;
        loader.execute(() -> {
            List<App> result = new ArrayList<>();
            String error = null;
            try {
                PackageManager pm = getPackageManager();
                Map<String, ResolveInfo> entries = new LinkedHashMap<>();
                // Prefer the TV entry point when a package exposes both kinds.
                for (String category : new String[] {
                    Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER }) {
                    Intent query = new Intent(Intent.ACTION_MAIN).addCategory(category);
                    for (ResolveInfo info : pm.queryIntentActivities(query, 0)) {
                        String pkg = info.activityInfo.packageName;
                        if (!pkg.equals(getPackageName()) && info.activityInfo.exported
                            && info.activityInfo.enabled && info.activityInfo.applicationInfo.enabled
                            && !entries.containsKey(pkg)) entries.put(pkg, info);
                    }
                }
                for (ResolveInfo info : entries.values()) {
                    Bitmap bitmap = null;
                    try {
                        Drawable icon = info.loadIcon(pm);
                        bitmap = Bitmap.createBitmap(dp(56), dp(56), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        icon.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight()); icon.draw(canvas);
                    } catch (RuntimeException ignored) { /* A monogram remains usable. */ }
                    result.add(new App(info.loadLabel(pm).toString(), info.activityInfo.packageName,
                        new ComponentName(info.activityInfo.packageName, info.activityInfo.name), bitmap));
                }
                Collator collator = Collator.getInstance();
                Collections.sort(result, (left, right) -> collator.compare(left.name, right.name));
            } catch (RuntimeException failure) { error = "Your apps couldn't be loaded. Try again."; }
            final String message = error;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (loading && screen.equals("home") && focusKey.equals("tab:home")) focusKey = "home-entry";
                rememberScroll(); refreshing = false; loading = false; loadError = message;
                apps.clear(); apps.addAll(result); readPins(); render();
            });
        });
    }

    private void readPins() {
        List<String> installed = new ArrayList<>();
        for (App app : apps) installed.add(app.pkg);
        List<String> saved = preferences.contains("pins")
            ? Arrays.asList(preferences.getString("pins", "").split(","))
            : Arrays.asList(AppOrder.SEEDS);
        pins.clear(); pins.addAll(AppOrder.availablePins(saved, installed));
    }

    private void render() {
        final String restoreFocus = focusKey;
        targets.clear(); appScroll = null; pinScroll = null;
        musicBar = null; musicToggle = null;
        FrameLayout stage = new FrameLayout(this);
        stage.addView(new Landscape(this), new FrameLayout.LayoutParams(-1, -1));
        if (wallpaper == null) {
            wallpaper = new WallpaperView(this);
        }
        if (wallpaper.getParent() != null) ((ViewGroup)wallpaper.getParent()).removeView(wallpaper);
        stage.addView(wallpaper, new FrameLayout.LayoutParams(-1, -1));
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(Tokens.SAFE_X), dp(Tokens.SAFE_Y), dp(Tokens.SAFE_X), dp(Tokens.SAFE_Y));
        stage.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(stage);
        header();
        if (screen.equals("home")) home(); else allApps();
        page.post(() -> {
            if (appScroll != null) appScroll.scrollTo(0, scrollY);
            if (pinScroll != null) pinScroll.scrollTo(scrollX, 0);
            View target = targets.get(restoreFocus);
            if (target == null || target.getVisibility() != View.VISIBLE
                || (restoreFocus.startsWith("music:") && !music.visible()))
                target = screen.equals("home") ? homeFocus() : targets.get("tab:apps");
            if (target != null) target.requestFocus();
        });
    }

    private void header() {
        LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("◒  evening", 23, Tokens.TEXT);
        brand.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        bar.addView(brand, new LinearLayout.LayoutParams(dp(212), dp(48)));
        bar.addView(action("Home", "tab:home", () -> navigate("home"), screen.equals("home")));
        bar.addView(action("Apps", "tab:apps", () -> navigate("apps"), screen.equals("apps")));
        bar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        LinearLayout wireless = row(); wireless.setGravity(Gravity.CENTER_VERTICAL);
        wireless.setPadding(dp(14), 0, dp(14), 0);
        wifiGlyph = new WifiGlyph(this); wireless.addView(wifiGlyph, new LinearLayout.LayoutParams(dp(26), dp(26)));
        wifiLabel = text("Wi-Fi", 16, Tokens.TEXT); wifiLabel.setPadding(dp(10), 0, 0, 0);
        wireless.addView(wifiLabel); wifiControl = wireless;
        wire(wireless, "wifi", false, () -> open(new Intent(Settings.ACTION_WIFI_SETTINGS), "Wi-Fi settings aren't available."));
        LinearLayout.LayoutParams wifiParams = new LinearLayout.LayoutParams(-2, dp(48));
        wifiParams.setMargins(0, 0, dp(12), 0); bar.addView(wireless, wifiParams);
        bar.addView(action("Settings", "settings", this::settings, false));
        clock = text("", 20, Tokens.TEXT); clock.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        bar.addView(clock, new LinearLayout.LayoutParams(dp(132), dp(48)));
        updateClock(); updateWifi(); page.addView(bar, new LinearLayout.LayoutParams(-1, dp(60)));
    }

    private void home() {
        LinearLayout hero = new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.BOTTOM);
        buildMusicBar(hero);
        page.addView(hero, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout section = row(); section.setGravity(Gravity.CENTER_VERTICAL);
        section.addView(text("Favorites", 22, Tokens.TEXT), new LinearLayout.LayoutParams(0, dp(48), 1));
        section.addView(action("Next scene", "next-scene", () -> wallpaper.next(true), false));
        section.addView(action("Edit", "edit", this::editFavorites, false));
        targets.get("edit").setContentDescription("Edit favorites");
        page.addView(section);
        pinScroll = new HorizontalScrollView(this);
        pinScroll.setClipToPadding(false); pinScroll.setHorizontalScrollBarEnabled(false);
        pinScroll.setFillViewport(true); pinScroll.setPadding(dp(5), dp(6), dp(5), dp(6));
        pinScroll.setOnGenericMotionListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_SCROLL) return false;
            float delta = event.getAxisValue(MotionEvent.AXIS_HSCROLL);
            if (delta == 0) delta = -event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (delta == 0) return false;
            pinScroll.scrollBy(Math.round(delta * dp(64)), 0); return true;
        });
        LinearLayout rail = row(); rail.setGravity(Gravity.CENTER_VERTICAL);
        if (loading || loadError != null || pins.isEmpty()) {
            emptyState(rail, loading ? "Finding your apps…" : loadError != null ? loadError
                : "No favorites. Choose Edit to add apps.");
        } else for (String pkg : pins) {
            App app = find(pkg); if (app == null) continue;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(168), dp(142));
            lp.setMargins(0, 0, dp(16), 0); rail.addView(tile(app), lp);
        }
        pinScroll.addView(rail, new ViewGroup.LayoutParams(-2, -1));
        page.addView(pinScroll, new LinearLayout.LayoutParams(-1, dp(162)));
    }

    private View homeFocus() {
        if (!pins.isEmpty() && targets.containsKey("app:" + pins.get(0)))
            return targets.get("app:" + pins.get(0));
        return targets.get("tab:home");
    }

    private void buildMusicBar(LinearLayout hero) {
        LinearLayout bar = row(); bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(16), 0, 0, 0); bar.setBackground(surface(false, false));
        TextView label = text("♫  Music", 17, Tokens.TEXT);
        label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        bar.addView(label, new LinearLayout.LayoutParams(dp(116), dp(56)));
        musicToggle = action("Ⅱ", "music:toggle", () -> mediaKey(music.playing()
            ? KeyEvent.KEYCODE_MEDIA_PAUSE : KeyEvent.KEYCODE_MEDIA_PLAY), false);
        bar.addView(musicToggle);
        Button next = action("▸▸", "music:next", () -> mediaKey(KeyEvent.KEYCODE_MEDIA_NEXT), false);
        next.setContentDescription("Next track"); bar.addView(next);
        Button hide = action("×", "music:hide", () -> {
            View entry = homeFocus(); if (entry != null) entry.requestFocus(); music.hide();
        }, false);
        hide.setContentDescription("Hide music controls"); bar.addView(hide);
        musicBar = bar;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(64));
        lp.setMargins(0, 0, 0, dp(12)); hero.addView(bar, lp); updateMusic();
    }

    private void mediaKey(int code) {
        if (!music.send(code)) Toast.makeText(this, "Music controls aren't available.", Toast.LENGTH_SHORT).show();
    }

    private void updateMusic() {
        if (musicBar == null || musicToggle == null) return;
        musicBar.setVisibility(music.visible() ? View.VISIBLE : View.INVISIBLE);
        musicToggle.setText(music.playing() ? "Ⅱ" : "▶");
        musicToggle.setContentDescription(music.playing() ? "Pause music" : "Play music");
    }

    private void allApps() {
        LinearLayout heading = row(); heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setBackgroundColor(0xb8101c29);
        heading.addView(text("All apps", 32, Tokens.TEXT), new LinearLayout.LayoutParams(0, dp(64), 1));
        heading.addView(text(loading ? "Loading…" : apps.size() + " apps", 16, Tokens.MUTED));
        page.addView(heading);
        appScroll = new ScrollView(this); appScroll.setFillViewport(true);
        appScroll.setClipToPadding(false); appScroll.setPadding(dp(5), dp(6), dp(5), dp(6));
        LinearLayout grid = new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL);
        if (loading || loadError != null || apps.isEmpty()) {
            emptyState(grid, loading ? "Finding your apps…" : loadError != null ? loadError
                : "No apps found. Install an app using your usual app store, then refresh.");
        } else {
            int columns = getResources().getConfiguration().screenWidthDp < 1000 ? 4 : 6;
            for (int start = 0; start < apps.size(); start += columns) {
                LinearLayout line = row(); line.setBaselineAligned(false);
                for (int column = 0; column < columns; column++) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(142), 1);
                    lp.setMargins(dp(5), dp(5), dp(5), dp(10));
                    line.addView(start + column < apps.size() ? tile(apps.get(start + column)) : new View(this), lp);
                }
                grid.addView(line);
            }
        }
        appScroll.addView(grid); page.addView(appScroll, new LinearLayout.LayoutParams(-1, 0, 1));
    }

    private void emptyState(LinearLayout container, String message) {
        TextView label = text(message, 18, Tokens.MUTED); label.setPadding(dp(12), dp(10), dp(18), dp(10));
        label.setMaxWidth(dp(750)); container.addView(label);
        if (!loading) container.addView(action(loadError == null ? "Refresh apps" : "Try again", "retry", this::refreshApps, false));
    }

    private View tile(App app) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER); card.setPadding(dp(12), dp(16), dp(12), dp(12));
        if (app.icon != null) {
            ImageView icon = new ImageView(this); icon.setImageBitmap(app.icon);
            icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            card.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));
        } else card.addView(text(app.name.isEmpty() ? "?" : app.name.substring(0, 1), 32, Tokens.TEXT));
        TextView label = text(app.name, 17, Tokens.TEXT); label.setGravity(Gravity.CENTER);
        label.setMaxLines(2); label.setEllipsize(TextUtils.TruncateAt.END);
        label.setPadding(0, dp(12), 0, 0); card.addView(label, new LinearLayout.LayoutParams(-1, -2));
        card.setContentDescription(app.name + (pins.contains(app.pkg) ? ", favorite" : "") + ". Open app");
        wire(card, "app:" + app.pkg, false, () -> launch(app));
        card.setOnLongClickListener(view -> { togglePin(app); return true; });
        return card;
    }

    private Button action(String label, String key, Runnable click, boolean selected) {
        Button button = new Button(this); button.setText(label); button.setAllCaps(false);
        button.setTextSize(17); button.setTextColor(Tokens.TEXT);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setMinHeight(0); button.setMinimumHeight(0); button.setMinWidth(dp(90));
        button.setPadding(dp(22), 0, dp(22), 0); button.setStateListAnimator(null);
        button.setSingleLine(true); button.setEllipsize(TextUtils.TruncateAt.END);
        button.setMaxWidth(dp(410));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(48));
        lp.setMargins(0, dp(3), dp(12), dp(3)); button.setLayoutParams(lp);
        wire(button, key, selected, click); return button;
    }

    private void wire(View view, String key, boolean selected, Runnable click) {
        view.setId(View.generateViewId()); view.setFocusable(true); view.setClickable(true);
        view.setPointerIcon(PointerIcon.getSystemIcon(this, PointerIcon.TYPE_HAND));
        boolean quiet = key.startsWith("tab:") || key.equals("wifi") || key.equals("settings")
            || key.equals("edit") || key.equals("next-scene");
        view.setBackground(quiet ? quietBackground(false, selected) : controlBackground(false, selected));
        view.setOnFocusChangeListener((focusedView, focused) -> {
            focusedView.setBackground(quiet ? quietBackground(focused, selected) : controlBackground(focused, selected));
            if (focused) focusKey = key;
        });
        view.setOnClickListener(v -> click.run()); targets.put(key, view);
    }

    private StateListDrawable quietBackground(boolean focused, boolean selected) {
        GradientDrawable base = surface(focused, false);
        if (!focused) { base.setColor(0x00000000); base.setStroke(0, 0); }
        GradientDrawable underline = new GradientDrawable();
        underline.setColor(selected ? Tokens.FOCUS : 0x00000000);
        LayerDrawable rest = new LayerDrawable(new Drawable[] {base, underline});
        rest.setLayerHeight(1, dp(2)); rest.setLayerGravity(1, Gravity.BOTTOM);
        rest.setLayerInset(1, dp(20), 0, dp(20), dp(2));
        StateListDrawable states = new StateListDrawable();
        GradientDrawable pressed = surface(focused, false); pressed.setColor(0xff466278);
        states.addState(new int[] {android.R.attr.state_pressed}, pressed);
        states.addState(new int[] {android.R.attr.state_hovered}, surface(focused, selected));
        states.addState(new int[] {}, rest); return states;
    }

    private GradientDrawable surface(boolean focused, boolean selected) {
        GradientDrawable drawable = new GradientDrawable(); drawable.setCornerRadius(dp(Tokens.RADIUS));
        drawable.setColor(focused ? 0xff344f63 : selected ? 0xff2a4052 : Tokens.SURFACE);
        drawable.setStroke(dp(focused ? 3 : 1), focused ? Tokens.FOCUS : selected ? 0xff6e8799 : 0xff35495b);
        return drawable;
    }

    private StateListDrawable controlBackground(boolean focused, boolean selected) {
        StateListDrawable states = new StateListDrawable();
        GradientDrawable pressed = surface(focused, selected); pressed.setColor(0xff466278);
        states.addState(new int[] {android.R.attr.state_pressed}, pressed);
        GradientDrawable hovered = surface(focused, selected); hovered.setColor(0xff2a4052);
        states.addState(new int[] {android.R.attr.state_hovered}, hovered);
        states.addState(new int[] {}, surface(focused, selected)); return states;
    }

    private void launch(App app) {
        Intent intent = new Intent(Intent.ACTION_MAIN).setComponent(app.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        open(intent, "This app couldn't open. Check it in device settings.");
    }

    private boolean open(Intent intent, String failure) {
        try { startActivity(intent); return true; }
        catch (ActivityNotFoundException | SecurityException exception) {
            Toast.makeText(this, failure, Toast.LENGTH_LONG).show(); refreshApps(); return false;
        }
    }

    private void settings() {
        activeDialog = new AlertDialog.Builder(this).setTitle("Settings")
            .setItems(new String[] {"Device settings", "Edit favorites", "Open original Home", "About Evening Home",
                wallpaper.motionEnabled() ? "Pause wallpaper slideshow" : "Resume wallpaper slideshow",
                "Wallpaper: " + wallpaper.title(),
                wallpaper.onlineEnabled() ? "Use bundled movie wallpapers" : "Use online TVMaze wallpapers",
                "Refresh online backgrounds", "Background source & status"}, (dialog, index) -> {
                if (index == 0) open(new Intent(Settings.ACTION_SETTINGS), "Device settings aren't available.");
                if (index == 1) editFavorites();
                if (index == 2) open(new Intent(Intent.ACTION_MAIN).setComponent(STOCK)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), "The original Home app isn't available.");
                if (index == 3) {
                    activeDialog = new AlertDialog.Builder(this).setTitle("Evening Home")
                        .setMessage("Version 0.3.0\nA quieter home for your TV.\n\nMusic controls use Android's current media player. Playback and Next support depend on that app.\n\nOnline background data: TVMaze (CC BY-SA). Image rights remain with their owners.\nBundled artwork: Universal Pictures, Marvel Studios, Sony Pictures and DreamWorks Animation.")
                        .setNeutralButton("TVMaze", (about, which) -> open(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.tvmaze.com/")), "A browser isn't available."))
                        .setPositiveButton("Done", null).create(); activeDialog.show();
                }
                if (index == 4) {
                    wallpaper.toggleMotion();
                    Toast.makeText(this, wallpaper.motionEnabled() ? "Slideshow resumed" : "Slideshow paused", Toast.LENGTH_SHORT).show();
                }
                if (index == 5) wallpaper.next(true);
                if (index == 6) wallpaper.toggleOnline();
                if (index == 7) {
                    if (wallpaper.onlineEnabled()) { wallpaper.refreshOnline();
                        Toast.makeText(this, "Checking TVMaze for backgrounds", Toast.LENGTH_SHORT).show();
                    } else Toast.makeText(this, "Enable online TVMaze wallpapers first", Toast.LENGTH_SHORT).show();
                }
                if (index == 8) {
                    activeDialog = new AlertDialog.Builder(this).setTitle("Backgrounds")
                        .setMessage(wallpaper.onlineStatus() + "\n\n" + wallpaper.title()
                            + "\n\nTVMaze supplies show and cartoon artwork. Backgrounds refresh daily and are saved for offline use.\n\nData: TVMaze, CC BY-SA. Image rights remain with their owners.")
                        .setNeutralButton("TVMaze", (source, which) -> open(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.tvmaze.com/")), "A browser isn't available."))
                        .setPositiveButton("Done", null).create(); activeDialog.show();
                }
            }).setNegativeButton("Cancel", null).create(); activeDialog.show();
    }

    private void editFavorites() {
        if (loading || apps.isEmpty() || loadError != null) {
            Toast.makeText(this, "Your apps need to load first.", Toast.LENGTH_SHORT).show(); return;
        }
        String[] labels = new String[apps.size()]; boolean[] checked = new boolean[apps.size()];
        List<App> snapshot = new ArrayList<>(apps);
        for (int i = 0; i < snapshot.size(); i++) {
            labels[i] = snapshot.get(i).name; checked[i] = pins.contains(snapshot.get(i).pkg);
        }
        activeDialog = new AlertDialog.Builder(this).setTitle("Edit favorites")
            .setMultiChoiceItems(labels, checked, (dialog, index, value) -> checked[index] = value)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save favorites", (dialog, which) -> {
                // Preserve existing order; new selections follow installed-app order.
                List<String> next = new ArrayList<>();
                for (String pkg : pins) for (int i = 0; i < snapshot.size(); i++)
                    if (checked[i] && snapshot.get(i).pkg.equals(pkg)) next.add(pkg);
                for (int i = 0; i < snapshot.size(); i++)
                    if (checked[i] && !next.contains(snapshot.get(i).pkg)) next.add(snapshot.get(i).pkg);
                pins.clear(); pins.addAll(next); savePins(); rememberScroll(); render();
                Toast.makeText(this, "Favorites saved", Toast.LENGTH_SHORT).show();
            }).create(); activeDialog.show();
    }

    private void togglePin(App app) {
        boolean removed = pins.remove(app.pkg); if (!removed) pins.add(app.pkg);
        savePins(); rememberScroll(); render();
        Toast.makeText(this, removed ? "Removed from favorites" : "Added to favorites", Toast.LENGTH_SHORT).show();
    }

    private void savePins() { preferences.edit().putString("pins", TextUtils.join(",", pins)).apply(); }

    private App find(String pkg) { for (App app : apps) if (app.pkg.equals(pkg)) return app; return null; }

    private void updateClock() {
        if (clock != null) clock.setText(DateFormat.getTimeFormat(this).format(new Date()));
    }

    private void updateWifi() {
        if (wifiLabel == null || wifiGlyph == null) return;
        String label = "Wi-Fi status unavailable"; boolean connected = false;
        try {
            ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo wifi = manager == null ? null : manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            connected = wifi != null && wifi.isConnected();
            label = connected ? "Wi-Fi connected" : "Wi-Fi disconnected";
        } catch (SecurityException ignored) { /* A restricted platform must not crash Home. */ }
        wifiGlyph.setConnected(connected); wifiLabel.setText(label);
        wifiControl.setContentDescription(label + ". Open Wi-Fi settings");
    }

    private TextView text(String value, float size, int color) {
        TextView text = new TextView(this); text.setText(value); text.setTextSize(size);
        text.setTextColor(color); text.setGravity(Gravity.CENTER_VERTICAL);
        text.setFontFeatureSettings("kern"); return text;
    }

    private LinearLayout row() { LinearLayout line = new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL); return line; }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
