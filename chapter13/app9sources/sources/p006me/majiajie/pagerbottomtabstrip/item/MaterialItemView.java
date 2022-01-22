package p006me.majiajie.pagerbottomtabstrip.item;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import p006me.majiajie.pagerbottomtabstrip.C1028R;
import p006me.majiajie.pagerbottomtabstrip.internal.RoundMessageView;
import p006me.majiajie.pagerbottomtabstrip.internal.Utils;

/* renamed from: me.majiajie.pagerbottomtabstrip.item.MaterialItemView */
/* loaded from: classes.dex */
public class MaterialItemView extends BaseTabItem {
    private ValueAnimator mAnimator;
    private float mAnimatorValue;
    private boolean mChecked;
    private int mCheckedColor;
    private Drawable mCheckedDrawable;
    private int mDefaultColor;
    private Drawable mDefaultDrawable;
    private boolean mHideTitle;
    private final ImageView mIcon;
    private boolean mIsMeasured;
    private final TextView mLabel;
    private final RoundMessageView mMessages;
    private final int mTopMargin;
    private final int mTopMarginHideTitle;
    private final float mTranslation;
    private final float mTranslationHideTitle;

    public MaterialItemView(@NonNull Context context) {
        this(context, null);
    }

    public MaterialItemView(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MaterialItemView(@NonNull Context context, @Nullable AttributeSet attributeSet, @AttrRes int i) {
        super(context, attributeSet, i);
        this.mAnimatorValue = 1.0f;
        this.mIsMeasured = false;
        float f = context.getResources().getDisplayMetrics().density;
        this.mTranslation = 2.0f * f;
        this.mTranslationHideTitle = 10.0f * f;
        this.mTopMargin = (int) (8.0f * f);
        this.mTopMarginHideTitle = (int) (f * 16.0f);
        LayoutInflater.from(context).inflate(C1028R.layout.item_material, (ViewGroup) this, true);
        this.mIcon = (ImageView) findViewById(C1028R.C1030id.icon);
        this.mLabel = (TextView) findViewById(C1028R.C1030id.label);
        this.mMessages = (RoundMessageView) findViewById(C1028R.C1030id.messages);
    }

    public void initialization(String str, Drawable drawable, Drawable drawable2, int i, int i2) {
        this.mDefaultColor = i;
        this.mCheckedColor = i2;
        this.mDefaultDrawable = Utils.tint(drawable, this.mDefaultColor);
        this.mCheckedDrawable = Utils.tint(drawable2, this.mCheckedColor);
        this.mLabel.setText(str);
        this.mLabel.setTextColor(i);
        this.mIcon.setImageDrawable(this.mDefaultDrawable);
        this.mAnimator = ValueAnimator.ofFloat(1.0f);
        this.mAnimator.setDuration(115L);
        this.mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: me.majiajie.pagerbottomtabstrip.item.MaterialItemView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MaterialItemView.this.mAnimatorValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (MaterialItemView.this.mHideTitle) {
                    MaterialItemView.this.mIcon.setTranslationY((-MaterialItemView.this.mTranslationHideTitle) * MaterialItemView.this.mAnimatorValue);
                } else {
                    MaterialItemView.this.mIcon.setTranslationY((-MaterialItemView.this.mTranslation) * MaterialItemView.this.mAnimatorValue);
                }
                MaterialItemView.this.mLabel.setTextSize(2, (MaterialItemView.this.mAnimatorValue * 2.0f) + 12.0f);
            }
        });
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.mIsMeasured = true;
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public void setChecked(boolean z) {
        if (this.mChecked != z) {
            this.mChecked = z;
            if (this.mHideTitle) {
                this.mLabel.setVisibility(this.mChecked ? 0 : 4);
            }
            if (this.mIsMeasured) {
                if (this.mChecked) {
                    this.mAnimator.start();
                } else {
                    this.mAnimator.reverse();
                }
            } else if (this.mChecked) {
                if (this.mHideTitle) {
                    this.mIcon.setTranslationY(-this.mTranslationHideTitle);
                } else {
                    this.mIcon.setTranslationY(-this.mTranslation);
                }
                this.mLabel.setTextSize(2, 14.0f);
            } else {
                this.mIcon.setTranslationY(0.0f);
                this.mLabel.setTextSize(2, 12.0f);
            }
            if (this.mChecked) {
                this.mIcon.setImageDrawable(this.mCheckedDrawable);
                this.mLabel.setTextColor(this.mCheckedColor);
                return;
            }
            this.mIcon.setImageDrawable(this.mDefaultDrawable);
            this.mLabel.setTextColor(this.mDefaultColor);
        }
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public void setMessageNumber(int i) {
        this.mMessages.setVisibility(0);
        this.mMessages.setMessageNumber(i);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public void setHasMessage(boolean z) {
        this.mMessages.setVisibility(0);
        this.mMessages.setHasMessage(z);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem
    public String getTitle() {
        return this.mLabel.getText().toString();
    }

    public float getAnimValue() {
        return this.mAnimatorValue;
    }

    public void setHideTitle(boolean z) {
        this.mHideTitle = z;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mIcon.getLayoutParams();
        if (this.mHideTitle) {
            layoutParams.topMargin = this.mTopMarginHideTitle;
        } else {
            layoutParams.topMargin = this.mTopMargin;
        }
        this.mLabel.setVisibility(this.mChecked ? 0 : 4);
        this.mIcon.setLayoutParams(layoutParams);
    }

    public void setMessageBackgroundColor(@ColorInt int i) {
        this.mMessages.tintMessageBackground(i);
    }

    public void setMessageNumberColor(@ColorInt int i) {
        this.mMessages.setMessageNumberColor(i);
    }
}
