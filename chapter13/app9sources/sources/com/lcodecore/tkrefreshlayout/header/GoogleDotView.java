package com.lcodecore.tkrefreshlayout.header;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.lcodecore.tkrefreshlayout.C0843R;
import com.lcodecore.tkrefreshlayout.IHeaderView;
import com.lcodecore.tkrefreshlayout.OnAnimEndListener;
import com.lcodecore.tkrefreshlayout.utils.DensityUtil;

/* loaded from: classes.dex */
public class GoogleDotView extends View implements IHeaderView {
    boolean animating;
    ValueAnimator animator1;
    ValueAnimator animator2;
    private int cir_x;
    float fraction1;
    float fraction2;
    private Paint mPath;
    private int num;

    /* renamed from: r */
    private float f76r;

    @Override // com.lcodecore.tkrefreshlayout.IHeaderView
    public View getView() {
        return this;
    }

    public void setCir_x(int i) {
        this.cir_x = i;
    }

    public GoogleDotView(Context context) {
        this(context, null, 0);
    }

    public GoogleDotView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public GoogleDotView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.num = 5;
        this.animating = false;
        init();
    }

    private void init() {
        this.f76r = (float) DensityUtil.dp2px(getContext(), 4.0f);
        this.mPath = new Paint();
        this.mPath.setAntiAlias(true);
        this.mPath.setColor(Color.rgb(114, 114, 114));
        this.animator1 = ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f, 0.8f);
        this.animator1.setDuration(800L);
        this.animator1.setInterpolator(new DecelerateInterpolator());
        this.animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.header.GoogleDotView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                GoogleDotView.this.fraction1 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                GoogleDotView.this.invalidate();
            }
        });
        this.animator1.setRepeatCount(-1);
        this.animator1.setRepeatMode(2);
        this.animator2 = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f, 1.2f);
        this.animator2.setDuration(800L);
        this.animator2.setInterpolator(new DecelerateInterpolator());
        this.animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.lcodecore.tkrefreshlayout.header.GoogleDotView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                GoogleDotView.this.fraction2 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        this.animator2.setRepeatCount(-1);
        this.animator2.setRepeatMode(2);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = (getMeasuredWidth() / this.num) - 10;
        for (int i = 0; i < this.num; i++) {
            if (this.animating) {
                switch (i) {
                    case 0:
                        this.mPath.setAlpha(105);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Yellow));
                        canvas.drawCircle((float) (((getMeasuredWidth() / 2) - (this.cir_x * 2)) - (((measuredWidth * 2) / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r * this.fraction2, this.mPath);
                        continue;
                    case 1:
                        this.mPath.setAlpha(145);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Green));
                        canvas.drawCircle((float) (((getMeasuredWidth() / 2) - (this.cir_x * 1)) - ((measuredWidth / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r * this.fraction2, this.mPath);
                        continue;
                    case 2:
                        this.mPath.setAlpha(255);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Blue));
                        canvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), this.f76r * this.fraction1, this.mPath);
                        continue;
                    case 3:
                        this.mPath.setAlpha(145);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Orange));
                        canvas.drawCircle((float) ((getMeasuredWidth() / 2) + (this.cir_x * 1) + ((measuredWidth / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r * this.fraction2, this.mPath);
                        continue;
                    case 4:
                        this.mPath.setAlpha(105);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Yellow));
                        canvas.drawCircle((float) ((getMeasuredWidth() / 2) + (this.cir_x * 2) + (((measuredWidth * 2) / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r * this.fraction2, this.mPath);
                        continue;
                }
            } else {
                switch (i) {
                    case 0:
                        this.mPath.setAlpha(105);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Yellow));
                        canvas.drawCircle((float) (((getMeasuredWidth() / 2) - (this.cir_x * 2)) - (((measuredWidth * 2) / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r, this.mPath);
                        continue;
                    case 1:
                        this.mPath.setAlpha(145);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Green));
                        canvas.drawCircle((float) (((getMeasuredWidth() / 2) - (this.cir_x * 1)) - ((measuredWidth / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r, this.mPath);
                        continue;
                    case 2:
                        this.mPath.setAlpha(255);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Blue));
                        canvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), this.f76r, this.mPath);
                        continue;
                    case 3:
                        this.mPath.setAlpha(145);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Orange));
                        canvas.drawCircle((float) ((getMeasuredWidth() / 2) + (this.cir_x * 1) + ((measuredWidth / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r, this.mPath);
                        continue;
                    case 4:
                        this.mPath.setAlpha(105);
                        this.mPath.setColor(getResources().getColor(C0843R.C0844color.Yellow));
                        canvas.drawCircle((float) ((getMeasuredWidth() / 2) + (this.cir_x * 2) + (((measuredWidth * 2) / 3) * 2)), (float) (getMeasuredHeight() / 2), this.f76r, this.mPath);
                        continue;
                }
            }
        }
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ValueAnimator valueAnimator = this.animator1;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimator2 = this.animator2;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.IHeaderView
    public void onPullingDown(float f, float f2, float f3) {
        float f4 = (f / 2.0f) + 1.0f;
        setScaleX(f4);
        setScaleY(f4);
        this.animating = false;
        if (this.animator1.isRunning()) {
            this.animator1.cancel();
            invalidate();
        }
        if (this.animator2.isRunning()) {
            this.animator2.cancel();
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.IHeaderView
    public void onPullReleasing(float f, float f2, float f3) {
        float f4 = (f / 2.0f) + 1.0f;
        setScaleX(f4);
        setScaleY(f4);
        if (f < 1.0f) {
            this.animating = false;
            if (this.animator1.isRunning()) {
                this.animator1.cancel();
                invalidate();
            }
            if (this.animator2.isRunning()) {
                this.animator2.cancel();
            }
        }
    }

    @Override // com.lcodecore.tkrefreshlayout.IHeaderView
    public void startAnim(float f, float f2) {
        this.animating = true;
        this.animator1.start();
        this.animator2.start();
    }

    @Override // com.lcodecore.tkrefreshlayout.IHeaderView
    public void onFinish(OnAnimEndListener onAnimEndListener) {
        onAnimEndListener.onAnimEnd();
    }

    @Override // com.lcodecore.tkrefreshlayout.IHeaderView
    public void reset() {
        this.animating = false;
        if (this.animator1.isRunning()) {
            this.animator1.cancel();
        }
        if (this.animator2.isRunning()) {
            this.animator2.cancel();
        }
        invalidate();
    }
}
