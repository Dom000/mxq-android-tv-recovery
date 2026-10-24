package dev.mxq.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

/** One static vector scene; no wallpaper service, bitmap backdrop or animation loop. */
final class Landscape extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path ridge = new Path();
    private LinearGradient sky;
    private LinearGradient shade;

    Landscape(Context context) { super(context); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        sky = new LinearGradient(0, 0, w, h * .8f,
            new int[] {Tokens.BACKGROUND, 0xff294550, 0xff537478}, null, Shader.TileMode.CLAMP);
        shade = new LinearGradient(0, 0, w * .72f, 0,
            new int[] {0xff101c29, 0xf0101c29, 0x00101c29}, null, Shader.TileMode.CLAMP);
    }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        paint.setShader(sky); canvas.drawRect(0, 0, w, h, paint); paint.setShader(null);
        paint.setColor(Tokens.SUN); canvas.drawCircle(w * .815f, h * .27f, h * .105f, paint);
        mountain(canvas, Tokens.RIDGE, new float[] {
            0,.64f, .28f,.58f, .42f,.65f, .65f,.26f, .82f,.51f, .91f,.4f, 1,.54f });
        mountain(canvas, Tokens.FOREGROUND, new float[] {
            0,.81f, .4f,.7f, .6f,.59f, .75f,.71f, .9f,.55f, 1,.66f });
        mountain(canvas, 0xff162d3a, new float[] {
            0,.9f, .45f,.83f, .68f,.78f, .88f,.87f, 1,.79f });
        paint.setShader(shade); canvas.drawRect(0, 0, w, h, paint); paint.setShader(null);
        paint.setColor(0xcc101c29); canvas.drawRect(0, h * .67f, w, h, paint);
    }

    private void mountain(Canvas canvas, int color, float[] points) {
        ridge.reset(); ridge.moveTo(0, getHeight());
        for (int i = 0; i < points.length; i += 2)
            ridge.lineTo(points[i] * getWidth(), points[i + 1] * getHeight());
        ridge.lineTo(getWidth(), getHeight()); ridge.close();
        paint.setColor(color); canvas.drawPath(ridge, paint);
    }
}
