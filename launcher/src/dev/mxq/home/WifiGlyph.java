package dev.mxq.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** Connection indicator paired with visible text; never conveys state by color alone. */
final class WifiGlyph extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arc = new RectF();
    private boolean connected;
    WifiGlyph(Context context) { super(context); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    void setConnected(boolean value) { connected = value; invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight(), x = w / 2, y = h * .78f;
        paint.setColor(connected ? Tokens.FOCUS : Tokens.MUTED);
        paint.setStrokeWidth(w * .07f); paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        for (float radius : new float[] {w * .25f, w * .46f, w * .67f}) {
            arc.set(x - radius, y - radius, x + radius, y + radius);
            canvas.drawArc(arc, 224, 92, false, paint);
        }
        if (!connected) canvas.drawLine(w * .18f, h * .15f, w * .85f, h * .86f, paint);
        paint.setStyle(Paint.Style.FILL); canvas.drawCircle(x, y, w * .055f, paint);
    }
}
