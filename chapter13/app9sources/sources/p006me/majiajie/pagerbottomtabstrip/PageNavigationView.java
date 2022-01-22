package p006me.majiajie.pagerbottomtabstrip;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.ArrayList;
import java.util.List;
import p006me.majiajie.pagerbottomtabstrip.internal.CustomItemLayout;
import p006me.majiajie.pagerbottomtabstrip.internal.CustomItemVerticalLayout;
import p006me.majiajie.pagerbottomtabstrip.internal.MaterialItemLayout;
import p006me.majiajie.pagerbottomtabstrip.internal.MaterialItemVerticalLayout;
import p006me.majiajie.pagerbottomtabstrip.internal.Utils;
import p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import p006me.majiajie.pagerbottomtabstrip.item.MaterialItemView;
import p006me.majiajie.pagerbottomtabstrip.item.OnlyIconMaterialItemView;
import p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

/* renamed from: me.majiajie.pagerbottomtabstrip.PageNavigationView */
/* loaded from: classes.dex */
public class PageNavigationView extends ViewGroup {
    private static final String INSTANCE_STATUS = "INSTANCE_STATUS";
    private final String STATUS_SELECTED;
    private boolean mEnableVerticalLayout;
    private NavigationController mNavigationController;
    private ViewPagerPageChangeListener mPageChangeListener;
    private OnTabItemSelectedListener mTabItemListener;
    private int mTabPaddingBottom;
    private int mTabPaddingTop;
    private ViewPager mViewPager;

    public PageNavigationView(Context context) {
        this(context, null);
    }

    public PageNavigationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PageNavigationView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTabItemListener = new OnTabItemSelectedListener() { // from class: me.majiajie.pagerbottomtabstrip.PageNavigationView.1
            @Override // p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener
            public void onRepeat(int i2) {
            }

            @Override // p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener
            public void onSelected(int i2, int i3) {
                if (PageNavigationView.this.mViewPager != null) {
                    PageNavigationView.this.mViewPager.setCurrentItem(i2, false);
                }
            }
        };
        this.STATUS_SELECTED = "STATUS_SELECTED";
        setPadding(0, 0, 0, 0);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C1028R.styleable.PageNavigationView);
        if (obtainStyledAttributes.hasValue(C1028R.styleable.PageNavigationView_NavigationPaddingTop)) {
            this.mTabPaddingTop = obtainStyledAttributes.getDimensionPixelSize(C1028R.styleable.PageNavigationView_NavigationPaddingTop, 0);
        }
        if (obtainStyledAttributes.hasValue(C1028R.styleable.PageNavigationView_NavigationPaddingBottom)) {
            this.mTabPaddingBottom = obtainStyledAttributes.getDimensionPixelSize(C1028R.styleable.PageNavigationView_NavigationPaddingBottom, 0);
        }
        obtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int childCount = getChildCount();
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                measureChild(childAt, i, i2);
                size = Math.max(size, childAt.getMeasuredWidth());
                size2 = Math.max(size2, childAt.getMeasuredHeight());
            }
        }
        setMeasuredDimension(size, size2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        int i5 = i3 - i;
        int i6 = i4 - i2;
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                childAt.layout(0, 0, i5, i6);
            }
        }
    }

    public MaterialBuilder material() {
        return new MaterialBuilder();
    }

    public CustomBuilder custom() {
        return new CustomBuilder();
    }

    /* renamed from: me.majiajie.pagerbottomtabstrip.PageNavigationView$CustomBuilder */
    /* loaded from: classes.dex */
    public class CustomBuilder {
        boolean enableVerticalLayout;
        List<BaseTabItem> items = new ArrayList();

        CustomBuilder() {
        }

        /* JADX WARN: Multi-variable type inference failed */
        public NavigationController build() {
            CustomItemLayout customItemLayout;
            PageNavigationView.this.mEnableVerticalLayout = this.enableVerticalLayout;
            if (this.items.size() == 0) {
                return null;
            }
            if (this.enableVerticalLayout) {
                CustomItemVerticalLayout customItemVerticalLayout = new CustomItemVerticalLayout(PageNavigationView.this.getContext());
                customItemVerticalLayout.initialize(this.items);
                customItemVerticalLayout.setPadding(0, PageNavigationView.this.mTabPaddingTop, 0, PageNavigationView.this.mTabPaddingBottom);
                PageNavigationView.this.removeAllViews();
                PageNavigationView.this.addView(customItemVerticalLayout);
                customItemLayout = customItemVerticalLayout;
            } else {
                CustomItemLayout customItemLayout2 = new CustomItemLayout(PageNavigationView.this.getContext());
                customItemLayout2.initialize(this.items);
                customItemLayout2.setPadding(0, PageNavigationView.this.mTabPaddingTop, 0, PageNavigationView.this.mTabPaddingBottom);
                PageNavigationView.this.removeAllViews();
                PageNavigationView.this.addView(customItemLayout2);
                customItemLayout = customItemLayout2;
            }
            PageNavigationView pageNavigationView = PageNavigationView.this;
            pageNavigationView.mNavigationController = new NavigationController(new Controller(), customItemLayout);
            PageNavigationView.this.mNavigationController.addTabItemSelectedListener(PageNavigationView.this.mTabItemListener);
            return PageNavigationView.this.mNavigationController;
        }

        public CustomBuilder addItem(BaseTabItem baseTabItem) {
            this.items.add(baseTabItem);
            return this;
        }

        public CustomBuilder enableVerticalLayout() {
            this.enableVerticalLayout = true;
            return this;
        }
    }

    /* renamed from: me.majiajie.pagerbottomtabstrip.PageNavigationView$MaterialBuilder */
    /* loaded from: classes.dex */
    public class MaterialBuilder {
        int defaultColor;
        boolean enableVerticalLayout;
        List<ViewData> itemDatas = new ArrayList();
        int messageBackgroundColor;
        int messageNumberColor;
        int mode;

        MaterialBuilder() {
        }

        /* JADX WARN: Multi-variable type inference failed */
        public NavigationController build() {
            MaterialItemLayout materialItemLayout;
            PageNavigationView.this.mEnableVerticalLayout = this.enableVerticalLayout;
            if (this.itemDatas.size() == 0) {
                return null;
            }
            if (this.defaultColor == 0) {
                this.defaultColor = 1442840576;
            }
            if (this.enableVerticalLayout) {
                ArrayList arrayList = new ArrayList();
                for (ViewData viewData : this.itemDatas) {
                    OnlyIconMaterialItemView onlyIconMaterialItemView = new OnlyIconMaterialItemView(PageNavigationView.this.getContext());
                    onlyIconMaterialItemView.initialization(viewData.title, viewData.drawable, viewData.checkedDrawable, this.defaultColor, viewData.chekedColor);
                    int i = this.messageBackgroundColor;
                    if (i != 0) {
                        onlyIconMaterialItemView.setMessageBackgroundColor(i);
                    }
                    int i2 = this.messageNumberColor;
                    if (i2 != 0) {
                        onlyIconMaterialItemView.setMessageNumberColor(i2);
                    }
                    arrayList.add(onlyIconMaterialItemView);
                }
                MaterialItemVerticalLayout materialItemVerticalLayout = new MaterialItemVerticalLayout(PageNavigationView.this.getContext());
                materialItemVerticalLayout.initialize(arrayList);
                materialItemVerticalLayout.setPadding(0, PageNavigationView.this.mTabPaddingTop, 0, PageNavigationView.this.mTabPaddingBottom);
                PageNavigationView.this.removeAllViews();
                PageNavigationView.this.addView(materialItemVerticalLayout);
                materialItemLayout = materialItemVerticalLayout;
            } else {
                boolean z = (this.mode & 2) > 0;
                ArrayList arrayList2 = new ArrayList();
                ArrayList arrayList3 = new ArrayList();
                for (ViewData viewData2 : this.itemDatas) {
                    arrayList3.add(Integer.valueOf(viewData2.chekedColor));
                    MaterialItemView materialItemView = new MaterialItemView(PageNavigationView.this.getContext());
                    if (z) {
                        materialItemView.initialization(viewData2.title, viewData2.drawable, viewData2.checkedDrawable, this.defaultColor, -1);
                    } else {
                        materialItemView.initialization(viewData2.title, viewData2.drawable, viewData2.checkedDrawable, this.defaultColor, viewData2.chekedColor);
                    }
                    int i3 = this.messageBackgroundColor;
                    if (i3 != 0) {
                        materialItemView.setMessageBackgroundColor(i3);
                    }
                    int i4 = this.messageNumberColor;
                    if (i4 != 0) {
                        materialItemView.setMessageNumberColor(i4);
                    }
                    arrayList2.add(materialItemView);
                }
                materialItemLayout = new MaterialItemLayout(PageNavigationView.this.getContext());
                materialItemLayout.initialize(arrayList2, arrayList3, this.mode);
                materialItemLayout.setPadding(0, PageNavigationView.this.mTabPaddingTop, 0, PageNavigationView.this.mTabPaddingBottom);
                PageNavigationView.this.removeAllViews();
                PageNavigationView.this.addView(materialItemLayout);
            }
            PageNavigationView pageNavigationView = PageNavigationView.this;
            pageNavigationView.mNavigationController = new NavigationController(new Controller(), materialItemLayout);
            PageNavigationView.this.mNavigationController.addTabItemSelectedListener(PageNavigationView.this.mTabItemListener);
            return PageNavigationView.this.mNavigationController;
        }

        public MaterialBuilder addItem(@DrawableRes int i, String str) {
            addItem(i, i, str, Utils.getColorPrimary(PageNavigationView.this.getContext()));
            return this;
        }

        public MaterialBuilder addItem(@DrawableRes int i, @DrawableRes int i2, String str) {
            addItem(i, i2, str, Utils.getColorPrimary(PageNavigationView.this.getContext()));
            return this;
        }

        public MaterialBuilder addItem(@DrawableRes int i, String str, @ColorInt int i2) {
            addItem(i, i, str, i2);
            return this;
        }

        public MaterialBuilder addItem(@DrawableRes int i, @DrawableRes int i2, String str, @ColorInt int i3) {
            addItem(ContextCompat.getDrawable(PageNavigationView.this.getContext(), i), ContextCompat.getDrawable(PageNavigationView.this.getContext(), i2), str, i3);
            return this;
        }

        public MaterialBuilder addItem(Drawable drawable, String str) {
            addItem(drawable, Utils.newDrawable(drawable), str, Utils.getColorPrimary(PageNavigationView.this.getContext()));
            return this;
        }

        public MaterialBuilder addItem(Drawable drawable, Drawable drawable2, String str) {
            addItem(drawable, drawable2, str, Utils.getColorPrimary(PageNavigationView.this.getContext()));
            return this;
        }

        public MaterialBuilder addItem(Drawable drawable, String str, @ColorInt int i) {
            addItem(drawable, Utils.newDrawable(drawable), str, i);
            return this;
        }

        public MaterialBuilder addItem(Drawable drawable, Drawable drawable2, String str, @ColorInt int i) {
            ViewData viewData = new ViewData();
            viewData.drawable = drawable;
            viewData.checkedDrawable = drawable2;
            viewData.title = str;
            viewData.chekedColor = i;
            this.itemDatas.add(viewData);
            return this;
        }

        public MaterialBuilder setDefaultColor(@ColorInt int i) {
            this.defaultColor = i;
            return this;
        }

        public MaterialBuilder setMessageBackgroundColor(@ColorInt int i) {
            this.messageBackgroundColor = i;
            return this;
        }

        public MaterialBuilder setMessageNumberColor(@ColorInt int i) {
            this.messageNumberColor = i;
            return this;
        }

        public MaterialBuilder setMode(int i) {
            this.mode = i;
            return this;
        }

        public MaterialBuilder enableVerticalLayout() {
            this.enableVerticalLayout = true;
            return this;
        }

        /* JADX INFO: Access modifiers changed from: private */
        /* renamed from: me.majiajie.pagerbottomtabstrip.PageNavigationView$MaterialBuilder$ViewData */
        /* loaded from: classes.dex */
        public class ViewData {
            Drawable checkedDrawable;
            @ColorInt
            int chekedColor;
            Drawable drawable;
            String title;

            private ViewData() {
            }
        }
    }

    /* renamed from: me.majiajie.pagerbottomtabstrip.PageNavigationView$Controller */
    /* loaded from: classes.dex */
    private class Controller implements BottomLayoutController {
        private ObjectAnimator animator;
        private boolean hide;

        private Controller() {
            this.hide = false;
        }

        @Override // p006me.majiajie.pagerbottomtabstrip.BottomLayoutController
        public void setupWithViewPager(ViewPager viewPager) {
            if (viewPager != null) {
                PageNavigationView.this.mViewPager = viewPager;
                if (PageNavigationView.this.mPageChangeListener != null) {
                    PageNavigationView.this.mViewPager.removeOnPageChangeListener(PageNavigationView.this.mPageChangeListener);
                } else {
                    PageNavigationView pageNavigationView = PageNavigationView.this;
                    pageNavigationView.mPageChangeListener = new ViewPagerPageChangeListener();
                }
                if (PageNavigationView.this.mNavigationController != null) {
                    int currentItem = PageNavigationView.this.mViewPager.getCurrentItem();
                    if (PageNavigationView.this.mNavigationController.getSelected() != currentItem) {
                        PageNavigationView.this.mNavigationController.setSelect(currentItem);
                    }
                    PageNavigationView.this.mViewPager.addOnPageChangeListener(PageNavigationView.this.mPageChangeListener);
                }
            }
        }

        @Override // p006me.majiajie.pagerbottomtabstrip.BottomLayoutController
        public void hideBottomLayout() {
            if (!this.hide) {
                this.hide = true;
                getAnimator().start();
            }
        }

        @Override // p006me.majiajie.pagerbottomtabstrip.BottomLayoutController
        public void showBottomLayout() {
            if (this.hide) {
                this.hide = false;
                getAnimator().reverse();
            }
        }

        private ObjectAnimator getAnimator() {
            if (this.animator == null) {
                if (PageNavigationView.this.mEnableVerticalLayout) {
                    PageNavigationView pageNavigationView = PageNavigationView.this;
                    this.animator = ObjectAnimator.ofFloat(pageNavigationView, "translationX", 0.0f, (float) (-pageNavigationView.getWidth()));
                } else {
                    PageNavigationView pageNavigationView2 = PageNavigationView.this;
                    this.animator = ObjectAnimator.ofFloat(pageNavigationView2, "translationY", 0.0f, (float) pageNavigationView2.getHeight());
                }
                this.animator.setDuration(300L);
                this.animator.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            return this.animator;
        }
    }

    /* renamed from: me.majiajie.pagerbottomtabstrip.PageNavigationView$ViewPagerPageChangeListener */
    /* loaded from: classes.dex */
    private class ViewPagerPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override // android.support.p000v4.view.ViewPager.OnPageChangeListener
        public void onPageScrollStateChanged(int i) {
        }

        @Override // android.support.p000v4.view.ViewPager.OnPageChangeListener
        public void onPageScrolled(int i, float f, int i2) {
        }

        private ViewPagerPageChangeListener() {
        }

        @Override // android.support.p000v4.view.ViewPager.OnPageChangeListener
        public void onPageSelected(int i) {
            if (PageNavigationView.this.mNavigationController != null && PageNavigationView.this.mNavigationController.getSelected() != i) {
                PageNavigationView.this.mNavigationController.setSelect(i);
            }
        }
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        if (this.mNavigationController == null) {
            return super.onSaveInstanceState();
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
        bundle.putInt("STATUS_SELECTED", this.mNavigationController.getSelected());
        return bundle;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        NavigationController navigationController;
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            int i = bundle.getInt("STATUS_SELECTED", 0);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
            if (i != 0 && (navigationController = this.mNavigationController) != null) {
                navigationController.setSelect(i);
                return;
            }
            return;
        }
        super.onRestoreInstanceState(parcelable);
    }
}
