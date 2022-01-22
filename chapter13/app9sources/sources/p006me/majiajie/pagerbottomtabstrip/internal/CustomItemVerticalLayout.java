package p006me.majiajie.pagerbottomtabstrip.internal;

import android.animation.LayoutTransition;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import p006me.goldze.mvvmhabit.utils.constant.MemoryConstants;
import p006me.majiajie.pagerbottomtabstrip.ItemController;
import p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

/* renamed from: me.majiajie.pagerbottomtabstrip.internal.CustomItemVerticalLayout */
/* loaded from: classes.dex */
public class CustomItemVerticalLayout extends ViewGroup implements ItemController {
    private List<BaseTabItem> mItems;
    private List<OnTabItemSelectedListener> mListeners;
    private int mSelected;

    public CustomItemVerticalLayout(Context context) {
        this(context, null);
    }

    public CustomItemVerticalLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomItemVerticalLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mListeners = new ArrayList();
        this.mSelected = -1;
        setLayoutTransition(new LayoutTransition());
    }

    public void initialize(List<BaseTabItem> list) {
        this.mItems = list;
        int size = this.mItems.size();
        for (final int i = 0; i < size; i++) {
            BaseTabItem baseTabItem = this.mItems.get(i);
            baseTabItem.setChecked(false);
            addView(baseTabItem);
            baseTabItem.setOnClickListener(new View.OnClickListener() { // from class: me.majiajie.pagerbottomtabstrip.internal.CustomItemVerticalLayout.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    CustomItemVerticalLayout.this.setSelect(i);
                }
            });
        }
        this.mSelected = 0;
        this.mItems.get(0).setChecked(true);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i2), 0);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i), MemoryConstants.f216GB);
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                childAt.measure(makeMeasureSpec2, getChildMeasureSpec(makeMeasureSpec, getPaddingTop() + getPaddingBottom(), childAt.getLayoutParams().height));
                paddingTop += childAt.getMeasuredHeight();
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(i), paddingTop);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        int paddingTop = getPaddingTop();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt.getVisibility() != 8) {
                childAt.layout(0, paddingTop, childAt.getMeasuredWidth(), childAt.getMeasuredHeight() + paddingTop);
                paddingTop += childAt.getMeasuredHeight();
            }
        }
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setSelect(int i) {
        int i2 = this.mSelected;
        if (i == i2) {
            for (OnTabItemSelectedListener onTabItemSelectedListener : this.mListeners) {
                onTabItemSelectedListener.onRepeat(this.mSelected);
            }
            return;
        }
        this.mSelected = i;
        if (i2 >= 0) {
            this.mItems.get(i2).setChecked(false);
        }
        this.mItems.get(this.mSelected).setChecked(true);
        for (OnTabItemSelectedListener onTabItemSelectedListener2 : this.mListeners) {
            onTabItemSelectedListener2.onSelected(this.mSelected, i2);
        }
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setMessageNumber(int i, int i2) {
        this.mItems.get(i).setMessageNumber(i2);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setHasMessage(int i, boolean z) {
        this.mItems.get(i).setHasMessage(z);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void addTabItemSelectedListener(OnTabItemSelectedListener onTabItemSelectedListener) {
        this.mListeners.add(onTabItemSelectedListener);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public int getSelected() {
        return this.mSelected;
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public int getItemCount() {
        return this.mItems.size();
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public String getItemTitle(int i) {
        return this.mItems.get(i).getTitle();
    }
}
