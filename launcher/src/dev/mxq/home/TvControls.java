package dev.mxq.home;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.PointerIcon;
import android.view.View;
import android.widget.Button;

/** Shared action geometry and focus treatment for Home and the remote task panel. */
final class TvControls {
    static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
    static Button button(Context context, String label, Runnable click) {
        return button(context, label, click, false);
    }
    static Button button(Context context, String label, Runnable click, boolean quiet) {
        Button button = new Button(context) {
            @Override public void setEnabled(boolean enabled) { super.setEnabled(enabled); setAlpha(enabled ? 1f : .45f); }
        };
        button.setText(label); button.setAllCaps(false);
        button.setTextSize(17); button.setTextColor(Tokens.TEXT);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setMinHeight(0); button.setMinimumHeight(0); button.setMinWidth(dp(context, 90));
        button.setPadding(dp(context, 22), 0, dp(context, 22), 0); button.setStateListAnimator(null);
        button.setSingleLine(true); button.setEllipsize(TextUtils.TruncateAt.END); button.setMaxWidth(dp(context, 410));
        button.setId(View.generateViewId()); button.setFocusable(true);
        button.setPointerIcon(PointerIcon.getSystemIcon(context, PointerIcon.TYPE_HAND));
        button.setBackground(background(context, false, false, quiet));
        button.setOnFocusChangeListener((view, focused) -> view.setBackground(background(context, focused, false, quiet)));
        button.setOnClickListener(view -> click.run()); return button;
    }
    static GradientDrawable surface(Context context, boolean focused, boolean selected) {
        GradientDrawable drawable = new GradientDrawable(); drawable.setCornerRadius(dp(context, Tokens.RADIUS));
        drawable.setColor(focused ? 0xff344f63 : selected ? 0xff2a4052 : Tokens.SURFACE);
        drawable.setStroke(dp(context, focused ? 3 : 1), focused ? Tokens.FOCUS : selected ? 0xff6e8799 : 0xff35495b);
        return drawable;
    }
    static StateListDrawable background(Context context, boolean focused, boolean selected) {
        return background(context, focused, selected, false);
    }
    private static StateListDrawable background(Context context, boolean focused, boolean selected, boolean quiet) {
        StateListDrawable states = new StateListDrawable();
        GradientDrawable pressed = surface(context, focused, selected); pressed.setColor(0xff466278);
        states.addState(new int[] {android.R.attr.state_pressed}, pressed);
        GradientDrawable hovered = surface(context, focused, selected); hovered.setColor(0xff2a4052);
        states.addState(new int[] {android.R.attr.state_hovered}, hovered);
        GradientDrawable rest = surface(context, focused, selected);
        if (quiet && !focused && !selected) { rest.setColor(0x00000000); rest.setStroke(0, 0); }
        states.addState(new int[] {}, rest); return states;
    }
}
