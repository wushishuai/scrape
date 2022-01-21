package p006me.zhanghai.android.materialprogressbar;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/* renamed from: me.zhanghai.android.materialprogressbar.TintableDrawable */
/* loaded from: classes.dex */
public interface TintableDrawable {
    @Override // p006me.zhanghai.android.materialprogressbar.TintableDrawable
    void setTint(@ColorInt int i);

    @Override // p006me.zhanghai.android.materialprogressbar.TintableDrawable
    void setTintList(@Nullable ColorStateList colorStateList);

    @Override // p006me.zhanghai.android.materialprogressbar.TintableDrawable
    void setTintMode(@NonNull PorterDuff.Mode mode);
}
