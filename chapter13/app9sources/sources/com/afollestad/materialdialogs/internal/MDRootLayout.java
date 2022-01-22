package com.afollestad.materialdialogs.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.p003v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ScrollView;
import com.afollestad.materialdialogs.C0582R;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.StackingBehavior;
import com.afollestad.materialdialogs.util.DialogUtils;
import p006me.goldze.mvvmhabit.utils.constant.MemoryConstants;

/* loaded from: classes.dex */
public class MDRootLayout extends ViewGroup {
    private static final int INDEX_NEGATIVE = 1;
    private static final int INDEX_NEUTRAL = 0;
    private static final int INDEX_POSITIVE = 2;
    private ViewTreeObserver.OnScrollChangedListener bottomOnScrollChangedListener;
    private int buttonBarHeight;
    private GravityEnum buttonGravity;
    private int buttonHorizontalEdgeMargin;
    private int buttonPaddingFull;
    private final MDButton[] buttons;
    private View content;
    private Paint dividerPaint;
    private int dividerWidth;
    private boolean drawBottomDivider;
    private boolean drawTopDivider;
    private boolean isStacked;
    private int maxHeight;
    private boolean noTitleNoPadding;
    private int noTitlePaddingFull;
    private boolean reducePaddingNoTitleNoButtons;
    private StackingBehavior stackBehavior;
    private View titleBar;
    private ViewTreeObserver.OnScrollChangedListener topOnScrollChangedListener;
    private boolean useFullPadding;

    public MDRootLayout(Context context) {
        super(context);
        this.buttons = new MDButton[3];
        this.drawTopDivider = false;
        this.drawBottomDivider = false;
        this.stackBehavior = StackingBehavior.ADAPTIVE;
        this.isStacked = false;
        this.useFullPadding = true;
        this.buttonGravity = GravityEnum.START;
        init(context, null, 0);
    }

    public MDRootLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.buttons = new MDButton[3];
        this.drawTopDivider = false;
        this.drawBottomDivider = false;
        this.stackBehavior = StackingBehavior.ADAPTIVE;
        this.isStacked = false;
        this.useFullPadding = true;
        this.buttonGravity = GravityEnum.START;
        init(context, attributeSet, 0);
    }

    @TargetApi(11)
    public MDRootLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.buttons = new MDButton[3];
        this.drawTopDivider = false;
        this.drawBottomDivider = false;
        this.stackBehavior = StackingBehavior.ADAPTIVE;
        this.isStacked = false;
        this.useFullPadding = true;
        this.buttonGravity = GravityEnum.START;
        init(context, attributeSet, i);
    }

    @TargetApi(21)
    public MDRootLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.buttons = new MDButton[3];
        this.drawTopDivider = false;
        this.drawBottomDivider = false;
        this.stackBehavior = StackingBehavior.ADAPTIVE;
        this.isStacked = false;
        this.useFullPadding = true;
        this.buttonGravity = GravityEnum.START;
        init(context, attributeSet, i);
    }

    private static boolean isVisible(View view) {
        boolean z = (view == null || view.getVisibility() == 8) ? false : true;
        if (!z || !(view instanceof MDButton)) {
            return z;
        }
        return ((MDButton) view).getText().toString().trim().length() > 0;
    }

    public static boolean canRecyclerViewScroll(RecyclerView recyclerView) {
        return (recyclerView == null || recyclerView.getLayoutManager() == null || !recyclerView.getLayoutManager().canScrollVertically()) ? false : true;
    }

    private static boolean canScrollViewScroll(ScrollView scrollView) {
        if (scrollView.getChildCount() != 0 && (scrollView.getMeasuredHeight() - scrollView.getPaddingTop()) - scrollView.getPaddingBottom() < scrollView.getChildAt(0).getMeasuredHeight()) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean canWebViewScroll(WebView webView) {
        return ((float) webView.getMeasuredHeight()) < ((float) webView.getContentHeight()) * webView.getScale();
    }

    private static boolean canAdapterViewScroll(AdapterView adapterView) {
        if (adapterView.getLastVisiblePosition() == -1) {
            return false;
        }
        boolean z = adapterView.getFirstVisiblePosition() == 0;
        boolean z2 = adapterView.getLastVisiblePosition() == adapterView.getCount() - 1;
        if (!z || !z2 || adapterView.getChildCount() <= 0 || adapterView.getChildAt(0).getTop() < adapterView.getPaddingTop() || adapterView.getChildAt(adapterView.getChildCount() - 1).getBottom() > adapterView.getHeight() - adapterView.getPaddingBottom()) {
            return true;
        }
        return false;
    }

    @Nullable
    private static View getBottomView(ViewGroup viewGroup) {
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return null;
        }
        for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = viewGroup.getChildAt(childCount);
            if (childAt.getVisibility() == 0 && childAt.getBottom() == viewGroup.getMeasuredHeight()) {
                return childAt;
            }
        }
        return null;
    }

    @Nullable
    private static View getTopView(ViewGroup viewGroup) {
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return null;
        }
        for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = viewGroup.getChildAt(childCount);
            if (childAt.getVisibility() == 0 && childAt.getTop() == 0) {
                return childAt;
            }
        }
        return null;
    }

    private void init(Context context, AttributeSet attributeSet, int i) {
        Resources resources = context.getResources();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C0582R.styleable.MDRootLayout, i, 0);
        this.reducePaddingNoTitleNoButtons = obtainStyledAttributes.getBoolean(C0582R.styleable.MDRootLayout_md_reduce_padding_no_title_no_buttons, true);
        obtainStyledAttributes.recycle();
        this.noTitlePaddingFull = resources.getDimensionPixelSize(C0582R.dimen.md_notitle_vertical_padding);
        this.buttonPaddingFull = resources.getDimensionPixelSize(C0582R.dimen.md_button_frame_vertical_padding);
        this.buttonHorizontalEdgeMargin = resources.getDimensionPixelSize(C0582R.dimen.md_button_padding_frame_side);
        this.buttonBarHeight = resources.getDimensionPixelSize(C0582R.dimen.md_button_height);
        this.dividerPaint = new Paint();
        this.dividerWidth = resources.getDimensionPixelSize(C0582R.dimen.md_divider_height);
        this.dividerPaint.setColor(DialogUtils.resolveColor(context, C0582R.attr.md_divider_color));
        setWillNotDraw(false);
    }

    public void setMaxHeight(int i) {
        this.maxHeight = i;
    }

    public void noTitleNoPadding() {
        this.noTitleNoPadding = true;
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getId() == C0582R.C0585id.md_titleFrame) {
                this.titleBar = childAt;
            } else if (childAt.getId() == C0582R.C0585id.md_buttonDefaultNeutral) {
                this.buttons[0] = (MDButton) childAt;
            } else if (childAt.getId() == C0582R.C0585id.md_buttonDefaultNegative) {
                this.buttons[1] = (MDButton) childAt;
            } else if (childAt.getId() == C0582R.C0585id.md_buttonDefaultPositive) {
                this.buttons[2] = (MDButton) childAt;
            } else {
                this.content = childAt;
            }
        }
    }

    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        boolean z;
        boolean z2;
        int i3;
        int i4;
        int i5;
        int i6;
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        int i7 = this.maxHeight;
        if (size2 > i7) {
            size2 = i7;
        }
        this.useFullPadding = true;
        int i8 = 0;
        if (this.stackBehavior == StackingBehavior.ALWAYS) {
            z2 = true;
            z = false;
        } else if (this.stackBehavior == StackingBehavior.NEVER) {
            z2 = false;
            z = false;
        } else {
            MDButton[] mDButtonArr = this.buttons;
            int i9 = 0;
            z = false;
            for (MDButton mDButton : mDButtonArr) {
                if (mDButton != null && isVisible(mDButton)) {
                    mDButton.setStacked(false, false);
                    measureChild(mDButton, i, i2);
                    i9 += mDButton.getMeasuredWidth();
                    z = true;
                }
            }
            z2 = i9 > size - (getContext().getResources().getDimensionPixelSize(C0582R.dimen.md_neutral_button_margin) * 2);
        }
        this.isStacked = z2;
        if (z2) {
            MDButton[] mDButtonArr2 = this.buttons;
            i3 = 0;
            for (MDButton mDButton2 : mDButtonArr2) {
                if (mDButton2 != null && isVisible(mDButton2)) {
                    mDButton2.setStacked(true, false);
                    measureChild(mDButton2, i, i2);
                    i3 += mDButton2.getMeasuredHeight();
                    z = true;
                }
            }
        } else {
            i3 = 0;
        }
        if (!z) {
            i6 = (this.buttonPaddingFull * 2) + 0;
            i5 = size2;
            i4 = 0;
        } else if (this.isStacked) {
            i5 = size2 - i3;
            int i10 = this.buttonPaddingFull;
            i6 = (i10 * 2) + 0;
            i4 = (i10 * 2) + 0;
        } else {
            i5 = size2 - this.buttonBarHeight;
            i6 = (this.buttonPaddingFull * 2) + 0;
            i4 = 0;
        }
        if (isVisible(this.titleBar)) {
            this.titleBar.measure(View.MeasureSpec.makeMeasureSpec(size, MemoryConstants.f216GB), 0);
            i5 -= this.titleBar.getMeasuredHeight();
        } else if (!this.noTitleNoPadding) {
            i6 += this.noTitlePaddingFull;
        }
        if (isVisible(this.content)) {
            this.content.measure(View.MeasureSpec.makeMeasureSpec(size, MemoryConstants.f216GB), View.MeasureSpec.makeMeasureSpec(i5 - i4, Integer.MIN_VALUE));
            if (this.content.getMeasuredHeight() > i5 - i6) {
                this.useFullPadding = false;
            } else if (!this.reducePaddingNoTitleNoButtons || isVisible(this.titleBar) || z) {
                this.useFullPadding = true;
                i8 = i5 - (this.content.getMeasuredHeight() + i6);
            } else {
                this.useFullPadding = false;
                i8 = i5 - (this.content.getMeasuredHeight() + i4);
            }
        } else {
            i8 = i5;
        }
        setMeasuredDimension(size, size2 - i8);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        View view = this.content;
        if (view != null) {
            if (this.drawTopDivider) {
                int top = view.getTop();
                canvas.drawRect(0.0f, (float) (top - this.dividerWidth), (float) getMeasuredWidth(), (float) top, this.dividerPaint);
            }
            if (this.drawBottomDivider) {
                int bottom = this.content.getBottom();
                canvas.drawRect(0.0f, (float) bottom, (float) getMeasuredWidth(), (float) (bottom + this.dividerWidth), this.dividerPaint);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        if (isVisible(this.titleBar)) {
            int measuredHeight = this.titleBar.getMeasuredHeight() + i2;
            this.titleBar.layout(i, i2, i3, measuredHeight);
            i2 = measuredHeight;
        } else if (!this.noTitleNoPadding && this.useFullPadding) {
            i2 += this.noTitlePaddingFull;
        }
        if (isVisible(this.content)) {
            View view = this.content;
            view.layout(i, i2, i3, view.getMeasuredHeight() + i2);
        }
        if (this.isStacked) {
            int i13 = i4 - this.buttonPaddingFull;
            MDButton[] mDButtonArr = this.buttons;
            for (MDButton mDButton : mDButtonArr) {
                if (isVisible(mDButton)) {
                    mDButton.layout(i, i13 - mDButton.getMeasuredHeight(), i3, i13);
                    i13 -= mDButton.getMeasuredHeight();
                }
            }
        } else {
            if (this.useFullPadding) {
                i4 -= this.buttonPaddingFull;
            }
            int i14 = i4 - this.buttonBarHeight;
            int i15 = this.buttonHorizontalEdgeMargin;
            if (isVisible(this.buttons[2])) {
                if (this.buttonGravity == GravityEnum.END) {
                    i12 = i + i15;
                    i11 = this.buttons[2].getMeasuredWidth() + i12;
                    i5 = -1;
                } else {
                    i11 = i3 - i15;
                    i12 = i11 - this.buttons[2].getMeasuredWidth();
                    i5 = i12;
                }
                this.buttons[2].layout(i12, i14, i11, i4);
                i15 += this.buttons[2].getMeasuredWidth();
            } else {
                i5 = -1;
            }
            if (isVisible(this.buttons[1])) {
                if (this.buttonGravity == GravityEnum.END) {
                    i10 = i15 + i;
                    i9 = this.buttons[1].getMeasuredWidth() + i10;
                    i6 = -1;
                } else if (this.buttonGravity == GravityEnum.START) {
                    i9 = i3 - i15;
                    i10 = i9 - this.buttons[1].getMeasuredWidth();
                    i6 = -1;
                } else {
                    i10 = this.buttonHorizontalEdgeMargin + i;
                    i9 = this.buttons[1].getMeasuredWidth() + i10;
                    i6 = i9;
                }
                this.buttons[1].layout(i10, i14, i9, i4);
            } else {
                i6 = -1;
            }
            if (isVisible(this.buttons[0])) {
                if (this.buttonGravity == GravityEnum.END) {
                    i7 = i3 - this.buttonHorizontalEdgeMargin;
                    i8 = i7 - this.buttons[0].getMeasuredWidth();
                } else if (this.buttonGravity == GravityEnum.START) {
                    i8 = i + this.buttonHorizontalEdgeMargin;
                    i7 = this.buttons[0].getMeasuredWidth() + i8;
                } else if (i6 == -1 && i5 != -1) {
                    i8 = i5 - this.buttons[0].getMeasuredWidth();
                    i7 = i5;
                } else if (i5 == -1 && i6 != -1) {
                    i7 = i6 + this.buttons[0].getMeasuredWidth();
                    i8 = i6;
                } else if (i5 == -1) {
                    int measuredWidth = ((i3 - i) / 2) - (this.buttons[0].getMeasuredWidth() / 2);
                    i7 = measuredWidth + this.buttons[0].getMeasuredWidth();
                    i8 = measuredWidth;
                } else {
                    i8 = i6;
                    i7 = i5;
                }
                this.buttons[0].layout(i8, i14, i7, i4);
            }
        }
        setUpDividersVisibility(this.content, true, true);
    }

    public void setStackingBehavior(StackingBehavior stackingBehavior) {
        this.stackBehavior = stackingBehavior;
        invalidate();
    }

    public void setDividerColor(int i) {
        this.dividerPaint.setColor(i);
        invalidate();
    }

    public void setButtonGravity(GravityEnum gravityEnum) {
        this.buttonGravity = gravityEnum;
        invertGravityIfNecessary();
    }

    private void invertGravityIfNecessary() {
        if (Build.VERSION.SDK_INT >= 17 && getResources().getConfiguration().getLayoutDirection() == 1) {
            switch (this.buttonGravity) {
                case START:
                    this.buttonGravity = GravityEnum.END;
                    return;
                case END:
                    this.buttonGravity = GravityEnum.START;
                    return;
                default:
                    return;
            }
        }
    }

    public void setButtonStackedGravity(GravityEnum gravityEnum) {
        MDButton[] mDButtonArr = this.buttons;
        for (MDButton mDButton : mDButtonArr) {
            if (mDButton != null) {
                mDButton.setStackedGravity(gravityEnum);
            }
        }
    }

    private void setUpDividersVisibility(final View view, final boolean z, final boolean z2) {
        if (view != null) {
            if (view instanceof ScrollView) {
                ScrollView scrollView = (ScrollView) view;
                if (canScrollViewScroll(scrollView)) {
                    addScrollListener(scrollView, z, z2);
                    return;
                }
                if (z) {
                    this.drawTopDivider = false;
                }
                if (z2) {
                    this.drawBottomDivider = false;
                }
            } else if (view instanceof AdapterView) {
                AdapterView adapterView = (AdapterView) view;
                if (canAdapterViewScroll(adapterView)) {
                    addScrollListener(adapterView, z, z2);
                    return;
                }
                if (z) {
                    this.drawTopDivider = false;
                }
                if (z2) {
                    this.drawBottomDivider = false;
                }
            } else if (view instanceof WebView) {
                view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.afollestad.materialdialogs.internal.MDRootLayout.1
                    @Override // android.view.ViewTreeObserver.OnPreDrawListener
                    public boolean onPreDraw() {
                        if (view.getMeasuredHeight() == 0) {
                            return true;
                        }
                        if (!MDRootLayout.canWebViewScroll((WebView) view)) {
                            if (z) {
                                MDRootLayout.this.drawTopDivider = false;
                            }
                            if (z2) {
                                MDRootLayout.this.drawBottomDivider = false;
                            }
                        } else {
                            MDRootLayout.this.addScrollListener((ViewGroup) view, z, z2);
                        }
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
            } else if (view instanceof RecyclerView) {
                boolean canRecyclerViewScroll = canRecyclerViewScroll((RecyclerView) view);
                if (z) {
                    this.drawTopDivider = canRecyclerViewScroll;
                }
                if (z2) {
                    this.drawBottomDivider = canRecyclerViewScroll;
                }
                if (canRecyclerViewScroll) {
                    addScrollListener((ViewGroup) view, z, z2);
                }
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                View topView = getTopView(viewGroup);
                setUpDividersVisibility(topView, z, z2);
                View bottomView = getBottomView(viewGroup);
                if (bottomView != topView) {
                    setUpDividersVisibility(bottomView, false, true);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addScrollListener(final ViewGroup viewGroup, final boolean z, final boolean z2) {
        if ((!z2 && this.topOnScrollChangedListener == null) || (z2 && this.bottomOnScrollChangedListener == null)) {
            if (viewGroup instanceof RecyclerView) {
                C06022 r0 = new RecyclerView.OnScrollListener() { // from class: com.afollestad.materialdialogs.internal.MDRootLayout.2
                    @Override // android.support.p003v7.widget.RecyclerView.OnScrollListener
                    public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                        super.onScrolled(recyclerView, i, i2);
                        MDButton[] mDButtonArr = MDRootLayout.this.buttons;
                        int length = mDButtonArr.length;
                        boolean z3 = false;
                        int i3 = 0;
                        while (true) {
                            if (i3 < length) {
                                MDButton mDButton = mDButtonArr[i3];
                                if (mDButton != null && mDButton.getVisibility() != 8) {
                                    z3 = true;
                                    break;
                                }
                                i3++;
                            } else {
                                break;
                            }
                        }
                        MDRootLayout.this.invalidateDividersForScrollingView(viewGroup, z, z2, z3);
                        MDRootLayout.this.invalidate();
                    }
                };
                RecyclerView recyclerView = (RecyclerView) viewGroup;
                recyclerView.addOnScrollListener(r0);
                r0.onScrolled(recyclerView, 0, 0);
                return;
            }
            ViewTreeObserver$OnScrollChangedListenerC06033 r02 = new ViewTreeObserver.OnScrollChangedListener() { // from class: com.afollestad.materialdialogs.internal.MDRootLayout.3
                @Override // android.view.ViewTreeObserver.OnScrollChangedListener
                public void onScrollChanged() {
                    MDButton[] mDButtonArr = MDRootLayout.this.buttons;
                    int length = mDButtonArr.length;
                    boolean z3 = false;
                    int i = 0;
                    while (true) {
                        if (i < length) {
                            MDButton mDButton = mDButtonArr[i];
                            if (mDButton != null && mDButton.getVisibility() != 8) {
                                z3 = true;
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                    ViewGroup viewGroup2 = viewGroup;
                    if (viewGroup2 instanceof WebView) {
                        MDRootLayout.this.invalidateDividersForWebView((WebView) viewGroup2, z, z2, z3);
                    } else {
                        MDRootLayout.this.invalidateDividersForScrollingView(viewGroup2, z, z2, z3);
                    }
                    MDRootLayout.this.invalidate();
                }
            };
            if (!z2) {
                this.topOnScrollChangedListener = r02;
                viewGroup.getViewTreeObserver().addOnScrollChangedListener(this.topOnScrollChangedListener);
            } else {
                this.bottomOnScrollChangedListener = r02;
                viewGroup.getViewTreeObserver().addOnScrollChangedListener(this.bottomOnScrollChangedListener);
            }
            r02.onScrollChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidateDividersForScrollingView(ViewGroup viewGroup, boolean z, boolean z2, boolean z3) {
        boolean z4 = true;
        if (z && viewGroup.getChildCount() > 0) {
            View view = this.titleBar;
            this.drawTopDivider = (view == null || view.getVisibility() == 8 || viewGroup.getScrollY() + viewGroup.getPaddingTop() <= viewGroup.getChildAt(0).getTop()) ? false : true;
        }
        if (z2 && viewGroup.getChildCount() > 0) {
            if (!z3 || (viewGroup.getScrollY() + viewGroup.getHeight()) - viewGroup.getPaddingBottom() >= viewGroup.getChildAt(viewGroup.getChildCount() - 1).getBottom()) {
                z4 = false;
            }
            this.drawBottomDivider = z4;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidateDividersForWebView(WebView webView, boolean z, boolean z2, boolean z3) {
        boolean z4 = true;
        if (z) {
            View view = this.titleBar;
            this.drawTopDivider = (view == null || view.getVisibility() == 8 || webView.getScrollY() + webView.getPaddingTop() <= 0) ? false : true;
        }
        if (z2) {
            if (!z3 || ((float) ((webView.getScrollY() + webView.getMeasuredHeight()) - webView.getPaddingBottom())) >= ((float) webView.getContentHeight()) * webView.getScale()) {
                z4 = false;
            }
            this.drawBottomDivider = z4;
        }
    }
}
