package dev.mxq.home;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;

/** Basic transport for Android's current media receiver; no notification access. */
final class MusicControls {
    private final AudioManager audio;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable changed;
    private boolean resumed, seen, hidden, playing;
    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (!resumed) return;
            refresh(); handler.postDelayed(this, 1000);
        }
    };

    MusicControls(Context context, Runnable listener) {
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        changed = listener;
    }

    boolean visible() { return seen && !hidden; }
    boolean playing() { return playing; }

    void setActive(boolean active) {
        resumed = active; handler.removeCallbacks(poll);
        if (active) { hidden = false; poll.run(); }
    }

    private void refresh() {
        try { playing = audio != null && audio.isMusicActive(); }
        catch (RuntimeException unavailable) { playing = false; }
        if (playing) seen = true;
        changed.run();
    }

    void hide() { hidden = true; changed.run(); }

    boolean send(int code) {
        if (audio == null) return false;
        try {
            long now = SystemClock.uptimeMillis();
            audio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0));
            audio.dispatchMediaKeyEvent(new KeyEvent(now, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, code, 0));
            // Read actual audio activity rather than assuming the player accepted the key.
            if (resumed) { handler.removeCallbacks(poll); handler.postDelayed(poll, 300); }
            return true;
        } catch (RuntimeException unavailable) { return false; }
    }
}
