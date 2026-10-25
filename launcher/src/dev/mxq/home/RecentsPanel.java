package dev.mxq.home;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Focusable accessibility overlay; ordinary D-pad buttons, no global key filter. */
final class RecentsPanel {
    interface Actions {
        void open(int index); void close(int index); void clear(); void page(boolean forward);
        void refresh(); void system(); void home(); void cancelClear();
    }
    private final Context context;
    private final WindowManager windows;
    private final Actions actions;
    private final InstalledTaskIcons icons;
    private final FrameLayout stage;
    private final List<Button> buttons = new ArrayList<>();
    private RecentsTree.Result last;
    private String message = "";
    private int restore;
    private boolean attached, confirming, clearing;
    private Button clearAction;
    private TextView statusText;
    private int originalIcons, fallbackIcons;

    RecentsPanel(Context service, InstalledTaskIcons artwork, Actions callbacks) {
        context = new ContextThemeWrapper(service, android.R.style.Theme_Material_NoActionBar);
        windows = (WindowManager)service.getSystemService(Context.WINDOW_SERVICE); actions = callbacks;
        icons = artwork;
        stage = new FrameLayout(context) {
            @Override public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (confirming) cancelConfirmation();
                        else if (clearing) actions.cancelClear(); else actions.home();
                    }
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        stage.setBackgroundColor(Tokens.BACKGROUND);
        stage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    boolean show() {
        try {
            // Leave a small application region visible. This vendor's window
            // observer drops the underlying task window for a full-screen modal
            // overlay even though accessibility overlays should preserve it.
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                context.getResources().getDisplayMetrics().widthPixels - dp(32),
                context.getResources().getDisplayMetrics().heightPixels - dp(32),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.OPAQUE);
            params.gravity = Gravity.CENTER; params.dimAmount = .85f;
            params.setTitle("Evening Home remote Recents");
            windows.addView(stage, params); attached = true; return true;
        } catch (RuntimeException refused) { return false; }
    }
    void hide() {
        if (attached) { try { windows.removeViewImmediate(stage); } catch (RuntimeException ignored) { } }
        attached = false; stage.removeAllViews(); buttons.clear(); last = null; message = "";
    }
    String diagnostics() {
        View focused = stage.findFocus();
        String role = focused instanceof Button ? ((Button)focused).getText().toString() : "none";
        if (!java.util.Arrays.asList("Open", "Close", "Clear all", "Cancel", "Earlier apps", "Later apps",
            "Refresh", "System view", "Done", "Stop").contains(role)) role = "none";
        return "REMOTE_PANEL attached=" + attached + " windowFocus=" + stage.hasWindowFocus()
            + " confirming=" + confirming + " clearing=" + clearing + " focus=" + role
            + " rows=" + (last == null ? 0 : last.cards.size())
            + " icons=" + originalIcons + " fallbackIcons=" + fallbackIcons;
    }
    boolean canRefresh() { return !confirming && !clearing; }
    int focusedRow() {
        int index = buttons.indexOf(stage.findFocus());
        return last != null && index >= 1 && index <= last.cards.size() * 2 ? (index - 1) / 2 : 0;
    }
    void render(RecentsTree.Result data, String status, int focus) {
        last = data; message = status; restore = focus; confirming = false; clearing = false;
        originalIcons = 0; fallbackIcons = 0;
        LinearLayout page = page();
        LinearLayout header = row(); header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text("Recent apps", 32, Tokens.TEXT), new LinearLayout.LayoutParams(0, dp(64), 1));
        Button clear = button("Clear all", this::confirm);
        clearAction = clear;
        clear.setEnabled(!data.cards.isEmpty() || data.clear != null);
        header.addView(clear, size(132, 52)); page.addView(header);
        page.addView(new View(context), new LinearLayout.LayoutParams(-1, dp(16)));

        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(true);
        LinearLayout list = new LinearLayout(context); list.setOrientation(LinearLayout.VERTICAL);
        List<Button> opens = new ArrayList<>(), closes = new ArrayList<>();
        for (int i = 0; i < data.cards.size(); i++) {
            final int index = i; RecentsTree.Card card = data.cards.get(i);
            LinearLayout line = row(); line.setGravity(Gravity.CENTER_VERTICAL);
            line.setPadding(dp(16), dp(12), dp(12), dp(12));
            ImageView icon = new ImageView(context); icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            icon.setFocusable(false); icon.setClickable(false);
            Bitmap bitmap = icons.get(card.label);
            if (bitmap != null) { icon.setImageBitmap(bitmap); originalIcons++; }
            else { icon.setImageDrawable(new AppGlyph()); fallbackIcons++; }
            LinearLayout.LayoutParams image = new LinearLayout.LayoutParams(dp(48), dp(48));
            image.rightMargin = dp(20); line.addView(icon, image);
            TextView label = text(card.label, 20, Tokens.TEXT); label.setMaxLines(2); label.setEllipsize(TextUtils.TruncateAt.END);
            label.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            line.addView(label, new LinearLayout.LayoutParams(0, -1, 1));
            Button open = button("Open", () -> actions.open(index)); open.setContentDescription("Open " + card.label);
            open.setEnabled(card.open != null); line.addView(open, size(112, 52)); opens.add(open);
            Button close = button("Close", () -> actions.close(index)); close.setContentDescription("Close " + card.label + " task");
            line.addView(close, size(112, 52)); closes.add(close);
            open.setNextFocusRightId(close.getId()); close.setNextFocusLeftId(open.getId());
            open.setNextFocusLeftId(open.getId()); close.setNextFocusRightId(close.getId());
            list.addView(line, new LinearLayout.LayoutParams(-1, dp(96)));
            View divider = new View(context); divider.setBackgroundColor((Tokens.MUTED & 0xffffff) | 0x33000000);
            list.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        }
        if (data.cards.isEmpty()) list.addView(text(data.hasTitles
            ? "This firmware doesn't expose task controls. Choose System view."
            : "No recent apps", 22, Tokens.MUTED), new LinearLayout.LayoutParams(-1, dp(150)));
        scroll.addView(list); page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView feedback = text(status, 17, Tokens.MUTED); feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        statusText = feedback;
        feedback.setMaxLines(2); page.addView(feedback, new LinearLayout.LayoutParams(-1, dp(48)));
        LinearLayout footer = row();
        Button earlier = button("Earlier apps", () -> actions.page(false));
        Button later = button("Later apps", () -> actions.page(true));
        earlier.setEnabled(data.stack != null && !data.cards.isEmpty()); later.setEnabled(earlier.isEnabled());
        footer.addView(earlier, size(160, 52)); footer.addView(later, size(160, 52));
        Button refresh = button("Refresh", actions::refresh); footer.addView(refresh, size(120, 52));
        footer.addView(new View(context), new LinearLayout.LayoutParams(0, 1, 1));
        Button system = button("System view", actions::system); footer.addView(system, size(156, 52));
        Button done = button("Done", actions::home); footer.addView(done, size(100, 52)); page.addView(footer);
        for (int i = 0; i < opens.size(); i++) {
            opens.get(i).setNextFocusUpId(i == 0 ? clear.getId() : opens.get(i - 1).getId());
            closes.get(i).setNextFocusUpId(i == 0 ? clear.getId() : closes.get(i - 1).getId());
            opens.get(i).setNextFocusDownId(i + 1 < opens.size() ? opens.get(i + 1).getId() : refresh.getId());
            closes.get(i).setNextFocusDownId(i + 1 < closes.size() ? closes.get(i + 1).getId() : done.getId());
        }
        if (!opens.isEmpty()) {
            clear.setNextFocusDownId(opens.get(0).getId());
            for (Button footerButton : new Button[] {earlier, later, refresh, system, done})
                footerButton.setNextFocusUpId(opens.get(opens.size() - 1).getId());
        }
        View initial = opens.isEmpty() ? done : opens.get(Math.max(0, Math.min(focus, opens.size() - 1)));
        if (!initial.isEnabled() && !closes.isEmpty()) initial = closes.get(Math.max(0, Math.min(focus, closes.size() - 1)));
        initial.requestFocus();
    }
    private void confirm() {
        confirming = true; LinearLayout page = page(); page.setGravity(Gravity.CENTER);
        page.addView(text("Clear all recent apps?", 28, Tokens.TEXT));
        TextView description = text("Removes the tasks in Android's Recents list. Unsaved app state may be lost.\nMusic and other background services may continue.", 19, Tokens.MUTED);
        description.setPadding(0, dp(20), 0, dp(28)); page.addView(description);
        LinearLayout controls = row();
        Button cancel = button("Cancel", this::cancelConfirmation); controls.addView(cancel, size(120, 52));
        controls.addView(button("Clear all", actions::clear), size(132, 52)); page.addView(controls); cancel.requestFocus();
    }
    private void cancelConfirmation() {
        render(last, message, restore);
        if (clearAction != null && clearAction.isEnabled()) clearAction.requestFocus();
    }
    void pending(String status) {
        clearing = false; confirming = false;
        for (Button button : buttons) button.setEnabled(false);
        if (statusText != null) statusText.setText(status);
        stage.announceForAccessibility(status);
    }
    void clearing() {
        confirming = false; clearing = true; LinearLayout page = page(); page.setGravity(Gravity.CENTER);
        page.addView(text("Clearing recent tasks…", 28, Tokens.TEXT));
        TextView label = text("You can stop before the next task is closed.", 19, Tokens.MUTED);
        statusText = label;
        label.setPadding(0, dp(20), 0, dp(28)); page.addView(label);
        Button stop = button("Stop", actions::cancelClear); page.addView(stop, size(120, 52)); stop.requestFocus();
    }
    private LinearLayout page() {
        stage.removeAllViews(); buttons.clear();
        LinearLayout page = new LinearLayout(context); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(Tokens.SAFE_X), dp(Tokens.SAFE_Y), dp(Tokens.SAFE_X), dp(Tokens.SAFE_Y));
        stage.addView(page, new FrameLayout.LayoutParams(-1, -1)); return page;
    }
    private Button button(String label, Runnable click) {
        Button button = TvControls.button(context, label, click, true); button.setFocusableInTouchMode(true);
        buttons.add(button); return button;
    }
    private TextView text(String value, float size, int color) {
        TextView text = new TextView(context); text.setText(value); text.setTextSize(size); text.setTextColor(color);
        text.setGravity(Gravity.CENTER_VERTICAL); text.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL)); return text;
    }
    private LinearLayout row() { LinearLayout row = new LinearLayout(context); row.setOrientation(LinearLayout.HORIZONTAL); return row; }
    private LinearLayout.LayoutParams size(int width, int height) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(width), dp(height)); lp.rightMargin = dp(12); return lp;
    }
    private int dp(float value) { return TvControls.dp(context, value); }
    private static final class AppGlyph extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        AppGlyph() { paint.setColor(Tokens.MUTED); paint.setAlpha(150); }
        @Override public void draw(Canvas canvas) {
            float unit = getBounds().width() / 8f, x = getBounds().left + unit, y = getBounds().top + unit;
            for (int row = 0; row < 2; row++) for (int column = 0; column < 2; column++) {
                float left = x + column * unit * 3.5f, top = y + row * unit * 3.5f;
                canvas.drawRoundRect(left, top, left + unit * 2.5f, top + unit * 2.5f, unit * .5f, unit * .5f, paint);
            }
        }
        @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
        @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
