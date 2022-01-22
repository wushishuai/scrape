package p006me.zhanghai.android.materialprogressbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.p000v4.graphics.ColorUtils;
import android.support.p000v4.view.ViewCompat;
import android.util.Log;
import p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable;
import p006me.zhanghai.android.materialprogressbar.ShowBackgroundDrawable;
import p006me.zhanghai.android.materialprogressbar.TintableDrawable;
import p006me.zhanghai.android.materialprogressbar.internal.ThemeUtils;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable */
/* loaded from: classes.dex */
public class BaseProgressLayerDrawable<ProgressDrawableType extends IntrinsicPaddingDrawable & ShowBackgroundDrawable & TintableDrawable, BackgroundDrawableType extends IntrinsicPaddingDrawable & ShowBackgroundDrawable & TintableDrawable> extends LayerDrawable implements IntrinsicPaddingDrawable, MaterialProgressDrawable, ShowBackgroundDrawable, TintableDrawable {
    private float mBackgroundAlpha;
    private BackgroundDrawableType mBackgroundDrawable = (BackgroundDrawableType) ((IntrinsicPaddingDrawable) getDrawable(0));
    private ProgressDrawableType mSecondaryProgressDrawable = (ProgressDrawableType) ((IntrinsicPaddingDrawable) getDrawable(1));
    private ProgressDrawableType mProgressDrawable = (ProgressDrawableType) ((IntrinsicPaddingDrawable) getDrawable(2));

    public BaseProgressLayerDrawable(Drawable[] drawableArr, Context context) {
        super(drawableArr);
        this.mBackgroundAlpha = ThemeUtils.getFloatFromAttrRes(16842803, 0.0f, context);
        setId(0, 16908288);
        setId(1, 16908303);
        setId(2, 16908301);
        setTint(ThemeUtils.getColorFromAttrRes(C1055R.attr.colorControlActivated, ViewCompat.MEASURED_STATE_MASK, context));
    }

    @Override // p006me.zhanghai.android.materialprogressbar.ShowBackgroundDrawable
    public boolean getShowBackground() {
        return this.mBackgroundDrawable.getShowBackground();
    }

    @Override // p006me.zhanghai.android.materialprogressbar.ShowBackgroundDrawable
    public void setShowBackground(boolean z) {
        if (this.mBackgroundDrawable.getShowBackground() != z) {
            this.mBackgroundDrawable.setShowBackground(z);
            this.mSecondaryProgressDrawable.setShowBackground(!z);
        }
    }

    @Override // p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable
    public boolean getUseIntrinsicPadding() {
        return this.mBackgroundDrawable.getUseIntrinsicPadding();
    }

    @Override // p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable
    public void setUseIntrinsicPadding(boolean z) {
        this.mBackgroundDrawable.setUseIntrinsicPadding(z);
        this.mSecondaryProgressDrawable.setUseIntrinsicPadding(z);
        this.mProgressDrawable.setUseIntrinsicPadding(z);
    }

    @Override // android.graphics.drawable.Drawable, p006me.zhanghai.android.materialprogressbar.TintableDrawable
    @SuppressLint({"NewApi"})
    public void setTint(@ColorInt int i) {
        int alphaComponent = ColorUtils.setAlphaComponent(i, Math.round(((float) Color.alpha(i)) * this.mBackgroundAlpha));
        this.mBackgroundDrawable.setTint(alphaComponent);
        this.mSecondaryProgressDrawable.setTint(alphaComponent);
        this.mProgressDrawable.setTint(i);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable, p006me.zhanghai.android.materialprogressbar.TintableDrawable
    @SuppressLint({"NewApi"})
    public void setTintList(@Nullable ColorStateList colorStateList) {
        ColorStateList colorStateList2;
        if (colorStateList != null) {
            if (!colorStateList.isOpaque()) {
                Log.w(getClass().getSimpleName(), "setTintList() called with a non-opaque ColorStateList, its original alpha will be discarded");
            }
            colorStateList2 = colorStateList.withAlpha(Math.round(this.mBackgroundAlpha * 255.0f));
        } else {
            colorStateList2 = null;
        }
        this.mBackgroundDrawable.setTintList(colorStateList2);
        this.mSecondaryProgressDrawable.setTintList(colorStateList2);
        this.mProgressDrawable.setTintList(colorStateList);
    }

    @Override // android.graphics.drawable.Drawable, p006me.zhanghai.android.materialprogressbar.TintableDrawable
    @SuppressLint({"NewApi"})
    public void setTintMode(@NonNull PorterDuff.Mode mode) {
        this.mBackgroundDrawable.setTintMode(mode);
        this.mSecondaryProgressDrawable.setTintMode(mode);
        this.mProgressDrawable.setTintMode(mode);
    }
}
