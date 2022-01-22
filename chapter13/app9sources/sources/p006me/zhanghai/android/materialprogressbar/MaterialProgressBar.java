package p006me.zhanghai.android.materialprogressbar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.p003v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;
import p006me.zhanghai.android.materialprogressbar.internal.DrawableCompat;

/* renamed from: me.zhanghai.android.materialprogressbar.MaterialProgressBar */
/* loaded from: classes.dex */
public class MaterialProgressBar extends ProgressBar {
    public static final int DETERMINATE_CIRCULAR_PROGRESS_STYLE_DYNAMIC = 1;
    public static final int DETERMINATE_CIRCULAR_PROGRESS_STYLE_NORMAL = 0;
    public static final int PROGRESS_STYLE_CIRCULAR = 0;
    public static final int PROGRESS_STYLE_HORIZONTAL = 1;
    private static final String TAG = "MaterialProgressBar";
    private int mProgressStyle;
    private boolean mSuperInitialized = true;
    private TintInfo mProgressTintInfo = new TintInfo();

    public MaterialProgressBar(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public MaterialProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet, 0, 0);
    }

    public MaterialProgressBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(attributeSet, i, 0);
    }

    @TargetApi(21)
    public MaterialProgressBar(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init(attributeSet, i, i2);
    }

    private void init(AttributeSet attributeSet, int i, int i2) {
        Context context = getContext();
        TintTypedArray obtainStyledAttributes = TintTypedArray.obtainStyledAttributes(context, attributeSet, C1055R.styleable.MaterialProgressBar, i, i2);
        this.mProgressStyle = obtainStyledAttributes.getInt(C1055R.styleable.MaterialProgressBar_mpb_progressStyle, 0);
        boolean z = obtainStyledAttributes.getBoolean(C1055R.styleable.MaterialProgressBar_mpb_setBothDrawables, false);
        boolean z2 = obtainStyledAttributes.getBoolean(C1055R.styleable.MaterialProgressBar_mpb_useIntrinsicPadding, true);
        boolean z3 = obtainStyledAttributes.getBoolean(C1055R.styleable.MaterialProgressBar_mpb_showProgressBackground, this.mProgressStyle == 1);
        int i3 = obtainStyledAttributes.getInt(C1055R.styleable.MaterialProgressBar_mpb_determinateCircularProgressStyle, 0);
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_progressTint)) {
            this.mProgressTintInfo.mProgressTint = obtainStyledAttributes.getColorStateList(C1055R.styleable.MaterialProgressBar_mpb_progressTint);
            this.mProgressTintInfo.mHasProgressTint = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_progressTintMode)) {
            this.mProgressTintInfo.mProgressTintMode = DrawableCompat.parseTintMode(obtainStyledAttributes.getInt(C1055R.styleable.MaterialProgressBar_mpb_progressTintMode, -1), null);
            this.mProgressTintInfo.mHasProgressTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_secondaryProgressTint)) {
            this.mProgressTintInfo.mSecondaryProgressTint = obtainStyledAttributes.getColorStateList(C1055R.styleable.MaterialProgressBar_mpb_secondaryProgressTint);
            this.mProgressTintInfo.mHasSecondaryProgressTint = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_secondaryProgressTintMode)) {
            this.mProgressTintInfo.mSecondaryProgressTintMode = DrawableCompat.parseTintMode(obtainStyledAttributes.getInt(C1055R.styleable.MaterialProgressBar_mpb_secondaryProgressTintMode, -1), null);
            this.mProgressTintInfo.mHasSecondaryProgressTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_progressBackgroundTint)) {
            this.mProgressTintInfo.mProgressBackgroundTint = obtainStyledAttributes.getColorStateList(C1055R.styleable.MaterialProgressBar_mpb_progressBackgroundTint);
            this.mProgressTintInfo.mHasProgressBackgroundTint = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_progressBackgroundTintMode)) {
            this.mProgressTintInfo.mProgressBackgroundTintMode = DrawableCompat.parseTintMode(obtainStyledAttributes.getInt(C1055R.styleable.MaterialProgressBar_mpb_progressBackgroundTintMode, -1), null);
            this.mProgressTintInfo.mHasProgressBackgroundTintMode = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_indeterminateTint)) {
            this.mProgressTintInfo.mIndeterminateTint = obtainStyledAttributes.getColorStateList(C1055R.styleable.MaterialProgressBar_mpb_indeterminateTint);
            this.mProgressTintInfo.mHasIndeterminateTint = true;
        }
        if (obtainStyledAttributes.hasValue(C1055R.styleable.MaterialProgressBar_mpb_indeterminateTintMode)) {
            this.mProgressTintInfo.mIndeterminateTintMode = DrawableCompat.parseTintMode(obtainStyledAttributes.getInt(C1055R.styleable.MaterialProgressBar_mpb_indeterminateTintMode, -1), null);
            this.mProgressTintInfo.mHasIndeterminateTintMode = true;
        }
        obtainStyledAttributes.recycle();
        switch (this.mProgressStyle) {
            case 0:
                if ((isIndeterminate() || z) && !isInEditMode()) {
                    setIndeterminateDrawable(new IndeterminateCircularProgressDrawable(context));
                }
                if (!isIndeterminate() || z) {
                    setProgressDrawable(new CircularProgressDrawable(i3, context));
                    break;
                }
                break;
            case 1:
                if ((isIndeterminate() || z) && !isInEditMode()) {
                    setIndeterminateDrawable(new IndeterminateHorizontalProgressDrawable(context));
                }
                if (!isIndeterminate() || z) {
                    setProgressDrawable(new HorizontalProgressDrawable(context));
                    break;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown progress style: " + this.mProgressStyle);
        }
        setUseIntrinsicPadding(z2);
        setShowProgressBackground(z3);
    }

    @Override // android.widget.ProgressBar
    public synchronized void setIndeterminate(boolean z) {
        super.setIndeterminate(z);
        if (this.mSuperInitialized && !(getCurrentDrawable() instanceof MaterialProgressDrawable)) {
            Log.w(TAG, "Current drawable is not a MaterialProgressDrawable, you may want to set app:mpb_setBothDrawables");
        }
    }

    @Override // android.widget.ProgressBar, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        fixCanvasScalingWhenHardwareAccelerated();
    }

    private void fixCanvasScalingWhenHardwareAccelerated() {
        if (Build.VERSION.SDK_INT < 18 && isHardwareAccelerated() && getLayerType() != 1) {
            setLayerType(1, null);
        }
    }

    public int getProgressStyle() {
        return this.mProgressStyle;
    }

    @Override // android.widget.ProgressBar
    public Drawable getCurrentDrawable() {
        return isIndeterminate() ? getIndeterminateDrawable() : getProgressDrawable();
    }

    public boolean getUseIntrinsicPadding() {
        Drawable currentDrawable = getCurrentDrawable();
        if (currentDrawable instanceof IntrinsicPaddingDrawable) {
            return ((IntrinsicPaddingDrawable) currentDrawable).getUseIntrinsicPadding();
        }
        throw new IllegalStateException("Drawable does not implement IntrinsicPaddingDrawable");
    }

    public void setUseIntrinsicPadding(boolean z) {
        Drawable currentDrawable = getCurrentDrawable();
        if (currentDrawable instanceof IntrinsicPaddingDrawable) {
            ((IntrinsicPaddingDrawable) currentDrawable).setUseIntrinsicPadding(z);
        }
        Drawable indeterminateDrawable = getIndeterminateDrawable();
        if (indeterminateDrawable instanceof IntrinsicPaddingDrawable) {
            ((IntrinsicPaddingDrawable) indeterminateDrawable).setUseIntrinsicPadding(z);
        }
    }

    public boolean getShowProgressBackground() {
        Drawable currentDrawable = getCurrentDrawable();
        if (currentDrawable instanceof ShowBackgroundDrawable) {
            return ((ShowBackgroundDrawable) currentDrawable).getShowBackground();
        }
        return false;
    }

    public void setShowProgressBackground(boolean z) {
        Drawable currentDrawable = getCurrentDrawable();
        if (currentDrawable instanceof ShowBackgroundDrawable) {
            ((ShowBackgroundDrawable) currentDrawable).setShowBackground(z);
        }
        Drawable indeterminateDrawable = getIndeterminateDrawable();
        if (indeterminateDrawable instanceof ShowBackgroundDrawable) {
            ((ShowBackgroundDrawable) indeterminateDrawable).setShowBackground(z);
        }
    }

    @Override // android.widget.ProgressBar
    public void setProgressDrawable(Drawable drawable) {
        super.setProgressDrawable(drawable);
        if (this.mProgressTintInfo != null) {
            applyProgressTints();
        }
    }

    @Override // android.widget.ProgressBar
    public void setIndeterminateDrawable(Drawable drawable) {
        super.setIndeterminateDrawable(drawable);
        if (this.mProgressTintInfo != null) {
            applyIndeterminateTint();
        }
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public ColorStateList getProgressTintList() {
        return this.mProgressTintInfo.mProgressTint;
    }

    @Override // android.widget.ProgressBar
    public void setProgressTintList(@Nullable ColorStateList colorStateList) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mProgressTint = colorStateList;
        tintInfo.mHasProgressTint = true;
        applyPrimaryProgressTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public PorterDuff.Mode getProgressTintMode() {
        return this.mProgressTintInfo.mProgressTintMode;
    }

    @Override // android.widget.ProgressBar
    public void setProgressTintMode(@Nullable PorterDuff.Mode mode) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mProgressTintMode = mode;
        tintInfo.mHasProgressTintMode = true;
        applyPrimaryProgressTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public ColorStateList getSecondaryProgressTintList() {
        return this.mProgressTintInfo.mSecondaryProgressTint;
    }

    @Override // android.widget.ProgressBar
    public void setSecondaryProgressTintList(@Nullable ColorStateList colorStateList) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mSecondaryProgressTint = colorStateList;
        tintInfo.mHasSecondaryProgressTint = true;
        applySecondaryProgressTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public PorterDuff.Mode getSecondaryProgressTintMode() {
        return this.mProgressTintInfo.mSecondaryProgressTintMode;
    }

    @Override // android.widget.ProgressBar
    public void setSecondaryProgressTintMode(@Nullable PorterDuff.Mode mode) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mSecondaryProgressTintMode = mode;
        tintInfo.mHasSecondaryProgressTintMode = true;
        applySecondaryProgressTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public ColorStateList getProgressBackgroundTintList() {
        return this.mProgressTintInfo.mProgressBackgroundTint;
    }

    @Override // android.widget.ProgressBar
    public void setProgressBackgroundTintList(@Nullable ColorStateList colorStateList) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mProgressBackgroundTint = colorStateList;
        tintInfo.mHasProgressBackgroundTint = true;
        applyProgressBackgroundTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public PorterDuff.Mode getProgressBackgroundTintMode() {
        return this.mProgressTintInfo.mProgressBackgroundTintMode;
    }

    @Override // android.widget.ProgressBar
    public void setProgressBackgroundTintMode(@Nullable PorterDuff.Mode mode) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mProgressBackgroundTintMode = mode;
        tintInfo.mHasProgressBackgroundTintMode = true;
        applyProgressBackgroundTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public ColorStateList getIndeterminateTintList() {
        return this.mProgressTintInfo.mIndeterminateTint;
    }

    @Override // android.widget.ProgressBar
    public void setIndeterminateTintList(@Nullable ColorStateList colorStateList) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mIndeterminateTint = colorStateList;
        tintInfo.mHasIndeterminateTint = true;
        applyIndeterminateTint();
    }

    @Override // android.widget.ProgressBar
    @Nullable
    public PorterDuff.Mode getIndeterminateTintMode() {
        return this.mProgressTintInfo.mIndeterminateTintMode;
    }

    @Override // android.widget.ProgressBar
    public void setIndeterminateTintMode(@Nullable PorterDuff.Mode mode) {
        TintInfo tintInfo = this.mProgressTintInfo;
        tintInfo.mIndeterminateTintMode = mode;
        tintInfo.mHasIndeterminateTintMode = true;
        applyIndeterminateTint();
    }

    private void applyProgressTints() {
        if (getProgressDrawable() != null) {
            applyPrimaryProgressTint();
            applyProgressBackgroundTint();
            applySecondaryProgressTint();
        }
    }

    private void applyPrimaryProgressTint() {
        Drawable tintTargetFromProgressDrawable;
        if (getProgressDrawable() != null) {
            if ((this.mProgressTintInfo.mHasProgressTint || this.mProgressTintInfo.mHasProgressTintMode) && (tintTargetFromProgressDrawable = getTintTargetFromProgressDrawable(16908301, true)) != null) {
                applyTintForDrawable(tintTargetFromProgressDrawable, this.mProgressTintInfo.mProgressTint, this.mProgressTintInfo.mHasProgressTint, this.mProgressTintInfo.mProgressTintMode, this.mProgressTintInfo.mHasProgressTintMode);
            }
        }
    }

    private void applySecondaryProgressTint() {
        Drawable tintTargetFromProgressDrawable;
        if (getProgressDrawable() != null) {
            if ((this.mProgressTintInfo.mHasSecondaryProgressTint || this.mProgressTintInfo.mHasSecondaryProgressTintMode) && (tintTargetFromProgressDrawable = getTintTargetFromProgressDrawable(16908303, false)) != null) {
                applyTintForDrawable(tintTargetFromProgressDrawable, this.mProgressTintInfo.mSecondaryProgressTint, this.mProgressTintInfo.mHasSecondaryProgressTint, this.mProgressTintInfo.mSecondaryProgressTintMode, this.mProgressTintInfo.mHasSecondaryProgressTintMode);
            }
        }
    }

    private void applyProgressBackgroundTint() {
        Drawable tintTargetFromProgressDrawable;
        if (getProgressDrawable() != null) {
            if ((this.mProgressTintInfo.mHasProgressBackgroundTint || this.mProgressTintInfo.mHasProgressBackgroundTintMode) && (tintTargetFromProgressDrawable = getTintTargetFromProgressDrawable(16908288, false)) != null) {
                applyTintForDrawable(tintTargetFromProgressDrawable, this.mProgressTintInfo.mProgressBackgroundTint, this.mProgressTintInfo.mHasProgressBackgroundTint, this.mProgressTintInfo.mProgressBackgroundTintMode, this.mProgressTintInfo.mHasProgressBackgroundTintMode);
            }
        }
    }

    private Drawable getTintTargetFromProgressDrawable(int i, boolean z) {
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable == null) {
            return null;
        }
        progressDrawable.mutate();
        Drawable findDrawableByLayerId = progressDrawable instanceof LayerDrawable ? ((LayerDrawable) progressDrawable).findDrawableByLayerId(i) : null;
        return (findDrawableByLayerId != null || !z) ? findDrawableByLayerId : progressDrawable;
    }

    private void applyIndeterminateTint() {
        Drawable indeterminateDrawable = getIndeterminateDrawable();
        if (indeterminateDrawable != null) {
            if (this.mProgressTintInfo.mHasIndeterminateTint || this.mProgressTintInfo.mHasIndeterminateTintMode) {
                indeterminateDrawable.mutate();
                applyTintForDrawable(indeterminateDrawable, this.mProgressTintInfo.mIndeterminateTint, this.mProgressTintInfo.mHasIndeterminateTint, this.mProgressTintInfo.mIndeterminateTintMode, this.mProgressTintInfo.mHasIndeterminateTintMode);
            }
        }
    }

    @SuppressLint({"NewApi"})
    private void applyTintForDrawable(Drawable drawable, ColorStateList colorStateList, boolean z, PorterDuff.Mode mode, boolean z2) {
        if (z || z2) {
            if (z) {
                if (drawable instanceof TintableDrawable) {
                    ((TintableDrawable) drawable).setTintList(colorStateList);
                } else {
                    Log.w(TAG, "Drawable did not implement TintableDrawable, it won't be tinted below Lollipop");
                    if (Build.VERSION.SDK_INT >= 21) {
                        drawable.setTintList(colorStateList);
                    }
                }
            }
            if (z2) {
                if (drawable instanceof TintableDrawable) {
                    ((TintableDrawable) drawable).setTintMode(mode);
                } else {
                    Log.w(TAG, "Drawable did not implement TintableDrawable, it won't be tinted below Lollipop");
                    if (Build.VERSION.SDK_INT >= 21) {
                        drawable.setTintMode(mode);
                    }
                }
            }
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: me.zhanghai.android.materialprogressbar.MaterialProgressBar$TintInfo */
    /* loaded from: classes.dex */
    public static class TintInfo {
        public boolean mHasIndeterminateTint;
        public boolean mHasIndeterminateTintMode;
        public boolean mHasProgressBackgroundTint;
        public boolean mHasProgressBackgroundTintMode;
        public boolean mHasProgressTint;
        public boolean mHasProgressTintMode;
        public boolean mHasSecondaryProgressTint;
        public boolean mHasSecondaryProgressTintMode;
        public ColorStateList mIndeterminateTint;
        public PorterDuff.Mode mIndeterminateTintMode;
        public ColorStateList mProgressBackgroundTint;
        public PorterDuff.Mode mProgressBackgroundTintMode;
        public ColorStateList mProgressTint;
        public PorterDuff.Mode mProgressTintMode;
        public ColorStateList mSecondaryProgressTint;
        public PorterDuff.Mode mSecondaryProgressTintMode;

        private TintInfo() {
        }
    }
}
