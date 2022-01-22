package p006me.zhanghai.android.materialprogressbar;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: me.zhanghai.android.materialprogressbar.BaseProgressDrawable */
/* loaded from: classes.dex */
public abstract class BaseProgressDrawable extends BasePaintDrawable implements IntrinsicPaddingDrawable {
    protected boolean mUseIntrinsicPadding = true;

    @Override // p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable
    public boolean getUseIntrinsicPadding() {
        return this.mUseIntrinsicPadding;
    }

    @Override // p006me.zhanghai.android.materialprogressbar.IntrinsicPaddingDrawable
    public void setUseIntrinsicPadding(boolean z) {
        if (this.mUseIntrinsicPadding != z) {
            this.mUseIntrinsicPadding = z;
            invalidateSelf();
        }
    }
}
