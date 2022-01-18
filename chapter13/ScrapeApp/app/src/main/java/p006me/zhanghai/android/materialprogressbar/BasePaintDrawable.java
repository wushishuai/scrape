package p006me.zhanghai.android.materialprogressbar;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.p000v4.view.ViewCompat;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: me.zhanghai.android.materialprogressbar.BasePaintDrawable */
/* loaded from: classes.dex */
public abstract class BasePaintDrawable extends BaseDrawable {
    private Paint mPaint;

    protected abstract void onDraw(Canvas canvas, int i, int i2, Paint paint);

    protected abstract void onPreparePaint(Paint paint);

    @Override // p006me.zhanghai.android.materialprogressbar.BaseDrawable
    protected final void onDraw(Canvas canvas, int width, int height) {
        if (this.mPaint == null) {
            this.mPaint = new Paint();
            this.mPaint.setAntiAlias(true);
            this.mPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
            onPreparePaint(this.mPaint);
        }
        this.mPaint.setAlpha(this.mAlpha);
        this.mPaint.setColorFilter(getColorFilterForDrawing());
        onDraw(canvas, width, height, this.mPaint);
    }
}