package me.zhanghai.android.materialprogressbar;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
/* loaded from: classes.dex */
public interface TintableDrawable {
    @Override // me.zhanghai.android.materialprogressbar.TintableDrawable
    void setTint(@ColorInt int i);

    @Override // me.zhanghai.android.materialprogressbar.TintableDrawable
    void setTintList(@Nullable ColorStateList colorStateList);

    @Override // me.zhanghai.android.materialprogressbar.TintableDrawable
    void setTintMode(@NonNull PorterDuff.Mode mode);
}
