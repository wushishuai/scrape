package android.support.p003v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AbstractC0071Px;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.p003v7.cardview.C0460R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/* renamed from: android.support.v7.widget.CardView */
/* loaded from: classes.dex */
public class CardView extends FrameLayout {
    private static final int[] COLOR_BACKGROUND_ATTR = {16842801};
    private static final CardViewImpl IMPL;
    private final CardViewDelegate mCardViewDelegate;
    private boolean mCompatPadding;
    final Rect mContentPadding;
    private boolean mPreventCornerOverlap;
    final Rect mShadowBounds;
    int mUserSetMinHeight;
    int mUserSetMinWidth;

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            IMPL = new CardViewApi21Impl();
        } else if (Build.VERSION.SDK_INT >= 17) {
            IMPL = new CardViewApi17Impl();
        } else {
            IMPL = new CardViewBaseImpl();
        }
        IMPL.initStatic();
    }

    public CardView(@NonNull Context context) {
        this(context, null);
    }

    public CardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, C0460R.attr.cardViewStyle);
    }

    public CardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ColorStateList backgroundColor;
        float maxElevation;
        int i;
        this.mContentPadding = new Rect();
        this.mShadowBounds = new Rect();
        this.mCardViewDelegate = new CardViewDelegate() { // from class: android.support.v7.widget.CardView.1
            private Drawable mCardBackground;

            @Override // android.support.p003v7.widget.CardViewDelegate
            public void setCardBackground(Drawable drawable) {
                this.mCardBackground = drawable;
                CardView.this.setBackgroundDrawable(drawable);
            }

            @Override // android.support.p003v7.widget.CardViewDelegate
            public boolean getUseCompatPadding() {
                return CardView.this.getUseCompatPadding();
            }

            @Override // android.support.p003v7.widget.CardViewDelegate
            public boolean getPreventCornerOverlap() {
                return CardView.this.getPreventCornerOverlap();
            }

            @Override // android.support.p003v7.widget.CardViewDelegate
            public void setShadowPadding(int left, int top, int right, int bottom) {
                CardView.this.mShadowBounds.set(left, top, right, bottom);
                CardView cardView = CardView.this;
                CardView.super.setPadding(cardView.mContentPadding.left + left, CardView.this.mContentPadding.top + top, CardView.this.mContentPadding.right + right, CardView.this.mContentPadding.bottom + bottom);
            }

            @Override // android.support.p003v7.widget.CardViewDelegate
            public void setMinWidthHeightInternal(int width, int height) {
                if (width > CardView.this.mUserSetMinWidth) {
                    CardView.super.setMinimumWidth(width);
                }
                if (height > CardView.this.mUserSetMinHeight) {
                    CardView.super.setMinimumHeight(height);
                }
            }

            @Override // android.support.p003v7.widget.CardViewDelegate
            public Drawable getCardBackground() {
                return this.mCardBackground;
            }

            @Override // android.support.p003v7.widget.CardViewDelegate
            public View getCardView() {
                return CardView.this;
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, C0460R.styleable.CardView, defStyleAttr, C0460R.style.CardView);
        if (a.hasValue(C0460R.styleable.CardView_cardBackgroundColor)) {
            backgroundColor = a.getColorStateList(C0460R.styleable.CardView_cardBackgroundColor);
        } else {
            TypedArray aa = getContext().obtainStyledAttributes(COLOR_BACKGROUND_ATTR);
            int themeColorBackground = aa.getColor(0, 0);
            aa.recycle();
            float[] hsv = new float[3];
            Color.colorToHSV(themeColorBackground, hsv);
            if (hsv[2] > 0.5f) {
                i = getResources().getColor(C0460R.C0461color.cardview_light_background);
            } else {
                i = getResources().getColor(C0460R.C0461color.cardview_dark_background);
            }
            backgroundColor = ColorStateList.valueOf(i);
        }
        float radius = a.getDimension(C0460R.styleable.CardView_cardCornerRadius, 0.0f);
        float elevation = a.getDimension(C0460R.styleable.CardView_cardElevation, 0.0f);
        float maxElevation2 = a.getDimension(C0460R.styleable.CardView_cardMaxElevation, 0.0f);
        this.mCompatPadding = a.getBoolean(C0460R.styleable.CardView_cardUseCompatPadding, false);
        this.mPreventCornerOverlap = a.getBoolean(C0460R.styleable.CardView_cardPreventCornerOverlap, true);
        int defaultPadding = a.getDimensionPixelSize(C0460R.styleable.CardView_contentPadding, 0);
        this.mContentPadding.left = a.getDimensionPixelSize(C0460R.styleable.CardView_contentPaddingLeft, defaultPadding);
        this.mContentPadding.top = a.getDimensionPixelSize(C0460R.styleable.CardView_contentPaddingTop, defaultPadding);
        this.mContentPadding.right = a.getDimensionPixelSize(C0460R.styleable.CardView_contentPaddingRight, defaultPadding);
        this.mContentPadding.bottom = a.getDimensionPixelSize(C0460R.styleable.CardView_contentPaddingBottom, defaultPadding);
        if (elevation > maxElevation2) {
            maxElevation = elevation;
        } else {
            maxElevation = maxElevation2;
        }
        this.mUserSetMinWidth = a.getDimensionPixelSize(C0460R.styleable.CardView_android_minWidth, 0);
        this.mUserSetMinHeight = a.getDimensionPixelSize(C0460R.styleable.CardView_android_minHeight, 0);
        a.recycle();
        IMPL.initialize(this.mCardViewDelegate, context, backgroundColor, radius, elevation, maxElevation);
    }

    @Override // android.view.View
    public void setPadding(int left, int top, int right, int bottom) {
    }

    @Override // android.view.View
    public void setPaddingRelative(int start, int top, int end, int bottom) {
    }

    public boolean getUseCompatPadding() {
        return this.mCompatPadding;
    }

    public void setUseCompatPadding(boolean useCompatPadding) {
        if (this.mCompatPadding != useCompatPadding) {
            this.mCompatPadding = useCompatPadding;
            IMPL.onCompatPaddingChanged(this.mCardViewDelegate);
        }
    }

    public void setContentPadding(@AbstractC0071Px int left, @AbstractC0071Px int top, @AbstractC0071Px int right, @AbstractC0071Px int bottom) {
        this.mContentPadding.set(left, top, right, bottom);
        IMPL.updatePadding(this.mCardViewDelegate);
    }

    @Override // android.widget.FrameLayout, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!(IMPL instanceof CardViewApi21Impl)) {
            int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
            if (widthMode == Integer.MIN_VALUE || widthMode == 1073741824) {
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max((int) Math.ceil((double) IMPL.getMinWidth(this.mCardViewDelegate)), View.MeasureSpec.getSize(widthMeasureSpec)), widthMode);
            }
            int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
            if (heightMode == Integer.MIN_VALUE || heightMode == 1073741824) {
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max((int) Math.ceil((double) IMPL.getMinHeight(this.mCardViewDelegate)), View.MeasureSpec.getSize(heightMeasureSpec)), heightMode);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // android.view.View
    public void setMinimumWidth(int minWidth) {
        this.mUserSetMinWidth = minWidth;
        super.setMinimumWidth(minWidth);
    }

    @Override // android.view.View
    public void setMinimumHeight(int minHeight) {
        this.mUserSetMinHeight = minHeight;
        super.setMinimumHeight(minHeight);
    }

    public void setCardBackgroundColor(@ColorInt int color) {
        IMPL.setBackgroundColor(this.mCardViewDelegate, ColorStateList.valueOf(color));
    }

    public void setCardBackgroundColor(@Nullable ColorStateList color) {
        IMPL.setBackgroundColor(this.mCardViewDelegate, color);
    }

    @NonNull
    public ColorStateList getCardBackgroundColor() {
        return IMPL.getBackgroundColor(this.mCardViewDelegate);
    }

    @AbstractC0071Px
    public int getContentPaddingLeft() {
        return this.mContentPadding.left;
    }

    @AbstractC0071Px
    public int getContentPaddingRight() {
        return this.mContentPadding.right;
    }

    @AbstractC0071Px
    public int getContentPaddingTop() {
        return this.mContentPadding.top;
    }

    @AbstractC0071Px
    public int getContentPaddingBottom() {
        return this.mContentPadding.bottom;
    }

    public void setRadius(float radius) {
        IMPL.setRadius(this.mCardViewDelegate, radius);
    }

    public float getRadius() {
        return IMPL.getRadius(this.mCardViewDelegate);
    }

    public void setCardElevation(float elevation) {
        IMPL.setElevation(this.mCardViewDelegate, elevation);
    }

    public float getCardElevation() {
        return IMPL.getElevation(this.mCardViewDelegate);
    }

    public void setMaxCardElevation(float maxElevation) {
        IMPL.setMaxElevation(this.mCardViewDelegate, maxElevation);
    }

    public float getMaxCardElevation() {
        return IMPL.getMaxElevation(this.mCardViewDelegate);
    }

    public boolean getPreventCornerOverlap() {
        return this.mPreventCornerOverlap;
    }

    public void setPreventCornerOverlap(boolean preventCornerOverlap) {
        if (preventCornerOverlap != this.mPreventCornerOverlap) {
            this.mPreventCornerOverlap = preventCornerOverlap;
            IMPL.onPreventCornerOverlapChanged(this.mCardViewDelegate);
        }
    }
}
