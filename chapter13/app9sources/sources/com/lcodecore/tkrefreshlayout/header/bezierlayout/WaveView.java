package com.lcodecore.tkrefreshlayout.header.bezierlayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

/* loaded from: classes.dex */
public class WaveView extends View {
    private int headHeight;
    Paint paint;
    Path path;
    private int waveHeight;

    public WaveView(Context context) {
        this(context, null, 0);
    }

    public WaveView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WaveView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        this.path = new Path();
        this.paint = new Paint();
        this.paint.setColor(-14736346);
        this.paint.setAntiAlias(true);
    }

    public int getHeadHeight() {
        return this.headHeight;
    }

    public void setHeadHeight(int i) {
        this.headHeight = i;
    }

    public int getWaveHeight() {
        return this.waveHeight;
    }

    public void setWaveHeight(int i) {
        this.waveHeight = i;
    }

    public void setWaveColor(@ColorInt int i) {
        Paint paint = this.paint;
        if (paint != null) {
            paint.setColor(i);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.path.reset();
        this.path.lineTo(0.0f, (float) this.headHeight);
        this.path.quadTo((float) (getMeasuredWidth() / 2), (float) (this.headHeight + this.waveHeight), (float) getMeasuredWidth(), (float) this.headHeight);
        this.path.lineTo((float) getMeasuredWidth(), 0.0f);
        canvas.drawPath(this.path, this.paint);
    }

    static Bitmap drawableToBitmap(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        return createBitmap;
    }
}
