package com.lcodecore.tkrefreshlayout.header.bezierlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

/* loaded from: classes.dex */
public class RippleView extends View {
    private OnRippleEndListener listener;
    private Paint mPaint;

    /* renamed from: r */
    private int f79r;

    /* renamed from: va */
    ValueAnimator f80va;

    /* loaded from: classes.dex */
    public interface OnRippleEndListener {
        void onRippleEnd();
    }

    public int getR() {
        return this.f79r;
    }

    public void setR(int i) {
        this.f79r = i;
    }

    public RippleView(Context context) {
        this(context, null, 0);
    }

    public RippleView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RippleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(-1);
        this.mPaint.setStyle(Paint.Style.FILL);
    }

    public void setRippleColor(@ColorInt int i) {
        Paint paint = this.mPaint;
        if (paint != null) {
            paint.setColor(i);
        }
    }

    public void startReveal() {
        setVisibility(0);
        if (this.f80va == null) {
            int sqrt = (int) Math.sqrt(Math.pow((double) getHeight(), 2.0d) + Math.pow((double) getWidth(), 2.0d));
            this.f80va = ValueAnimator.ofInt(0, sqrt / 2);
            this.f80va.setDuration((long) sqrt);
            this.f80va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.header.bezierlayout.RippleView.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    RippleView.this.f79r = ((Integer) valueAnimator.getAnimatedValue()).intValue() * 2;
                    RippleView.this.invalidate();
                }
            });
            this.f80va.addListener(new AnimatorListenerAdapter() { // from class: com.lcodecore.tkrefreshlayout.header.bezierlayout.RippleView.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (RippleView.this.listener != null) {
                        RippleView.this.listener.onRippleEnd();
                    }
                }
            });
        }
        this.f80va.start();
    }

    public void stopAnim() {
        ValueAnimator valueAnimator = this.f80va;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.f80va.cancel();
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2), (float) this.f79r, this.mPaint);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ValueAnimator valueAnimator = this.f80va;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public void setRippleEndListener(OnRippleEndListener onRippleEndListener) {
        this.listener = onRippleEndListener;
    }
}
