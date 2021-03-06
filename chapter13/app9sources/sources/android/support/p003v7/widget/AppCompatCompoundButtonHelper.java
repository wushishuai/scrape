package android.support.p003v7.widget;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.p000v4.graphics.drawable.DrawableCompat;
import android.support.p000v4.widget.CompoundButtonCompat;
import android.support.p003v7.appcompat.C0452R;
import android.support.p003v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.widget.CompoundButton;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: android.support.v7.widget.AppCompatCompoundButtonHelper */
/* loaded from: classes.dex */
public class AppCompatCompoundButtonHelper {
    private ColorStateList mButtonTintList = null;
    private PorterDuff.Mode mButtonTintMode = null;
    private boolean mHasButtonTint = false;
    private boolean mHasButtonTintMode = false;
    private boolean mSkipNextApply;
    private final CompoundButton mView;

    /* renamed from: android.support.v7.widget.AppCompatCompoundButtonHelper$DirectSetButtonDrawableInterface */
    /* loaded from: classes.dex */
    interface DirectSetButtonDrawableInterface {
        void setButtonDrawable(Drawable drawable);
    }

    public AppCompatCompoundButtonHelper(CompoundButton compoundButton) {
        this.mView = compoundButton;
    }

    public void loadFromAttributes(AttributeSet attributeSet, int i) {
        int resourceId;
        TypedArray obtainStyledAttributes = this.mView.getContext().obtainStyledAttributes(attributeSet, C0452R.styleable.CompoundButton, i, 0);
        try {
            if (obtainStyledAttributes.hasValue(C0452R.styleable.CompoundButton_android_button) && (resourceId = obtainStyledAttributes.getResourceId(C0452R.styleable.CompoundButton_android_button, 0)) != 0) {
                this.mView.setButtonDrawable(AppCompatResources.getDrawable(this.mView.getContext(), resourceId));
            }
            if (obtainStyledAttributes.hasValue(C0452R.styleable.CompoundButton_buttonTint)) {
                CompoundButtonCompat.setButtonTintList(this.mView, obtainStyledAttributes.getColorStateList(C0452R.styleable.CompoundButton_buttonTint));
            }
            if (obtainStyledAttributes.hasValue(C0452R.styleable.CompoundButton_buttonTintMode)) {
                CompoundButtonCompat.setButtonTintMode(this.mView, DrawableUtils.parseTintMode(obtainStyledAttributes.getInt(C0452R.styleable.CompoundButton_buttonTintMode, -1), null));
            }
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public void setSupportButtonTintList(ColorStateList colorStateList) {
        this.mButtonTintList = colorStateList;
        this.mHasButtonTint = true;
        applyButtonTint();
    }

    public ColorStateList getSupportButtonTintList() {
        return this.mButtonTintList;
    }

    public void setSupportButtonTintMode(@Nullable PorterDuff.Mode mode) {
        this.mButtonTintMode = mode;
        this.mHasButtonTintMode = true;
        applyButtonTint();
    }

    public PorterDuff.Mode getSupportButtonTintMode() {
        return this.mButtonTintMode;
    }

    public void onSetButtonDrawable() {
        if (this.mSkipNextApply) {
            this.mSkipNextApply = false;
            return;
        }
        this.mSkipNextApply = true;
        applyButtonTint();
    }

    void applyButtonTint() {
        Drawable buttonDrawable = CompoundButtonCompat.getButtonDrawable(this.mView);
        if (buttonDrawable == null) {
            return;
        }
        if (this.mHasButtonTint || this.mHasButtonTintMode) {
            Drawable mutate = DrawableCompat.wrap(buttonDrawable).mutate();
            if (this.mHasButtonTint) {
                DrawableCompat.setTintList(mutate, this.mButtonTintList);
            }
            if (this.mHasButtonTintMode) {
                DrawableCompat.setTintMode(mutate, this.mButtonTintMode);
            }
            if (mutate.isStateful()) {
                mutate.setState(this.mView.getDrawableState());
            }
            this.mView.setButtonDrawable(mutate);
        }
    }

    public int getCompoundPaddingLeft(int i) {
        Drawable buttonDrawable;
        return (Build.VERSION.SDK_INT >= 17 || (buttonDrawable = CompoundButtonCompat.getButtonDrawable(this.mView)) == null) ? i : i + buttonDrawable.getIntrinsicWidth();
    }
}
