package p006me.zhanghai.android.materialprogressbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/* renamed from: me.zhanghai.android.materialprogressbar.CircularProgressDrawable */
/* loaded from: classes.dex */
public class CircularProgressDrawable extends BaseProgressLayerDrawable<SingleCircularProgressDrawable, CircularProgressBackgroundDrawable> {
    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, p006me.zhanghai.android.materialprogressbar.ShowBackgroundDrawable
    public /* bridge */ /* synthetic */ boolean getShowBackground() {
        return super.getShowBackground();
    }

    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable
    public /* bridge */ /* synthetic */ boolean getUseIntrinsicPadding() {
        return super.getUseIntrinsicPadding();
    }

    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, p006me.zhanghai.android.materialprogressbar.ShowBackgroundDrawable
    public /* bridge */ /* synthetic */ void setShowBackground(boolean z) {
        super.setShowBackground(z);
    }

    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, android.graphics.drawable.Drawable, p006me.zhanghai.android.materialprogressbar.TintableDrawable
    @SuppressLint({"NewApi"})
    public /* bridge */ /* synthetic */ void setTint(@ColorInt int i) {
        super.setTint(i);
    }

    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable, p006me.zhanghai.android.materialprogressbar.TintableDrawable
    @SuppressLint({"NewApi"})
    public /* bridge */ /* synthetic */ void setTintList(@Nullable ColorStateList colorStateList) {
        super.setTintList(colorStateList);
    }

    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, android.graphics.drawable.Drawable, p006me.zhanghai.android.materialprogressbar.TintableDrawable
    @SuppressLint({"NewApi"})
    public /* bridge */ /* synthetic */ void setTintMode(@NonNull PorterDuff.Mode mode) {
        super.setTintMode(mode);
    }

    @Override // p006me.zhanghai.android.materialprogressbar.BaseProgressLayerDrawable, p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable
    public /* bridge */ /* synthetic */ void setUseIntrinsicPadding(boolean z) {
        super.setUseIntrinsicPadding(z);
    }

    public CircularProgressDrawable(int style, Context context) {
        super(new Drawable[]{new CircularProgressBackgroundDrawable(), new SingleCircularProgressDrawable(style), new SingleCircularProgressDrawable(style)}, context);
    }
}
