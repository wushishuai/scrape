package p006me.majiajie.pagerbottomtabstrip.internal;

import android.animation.LayoutTransition;
import android.content.Context;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import p006me.goldze.mvvmhabit.utils.constant.MemoryConstants;
import p006me.majiajie.pagerbottomtabstrip.ItemController;
import p006me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

/* renamed from: me.majiajie.pagerbottomtabstrip.internal.CustomItemLayout */
/* loaded from: classes.dex */
public class CustomItemLayout extends ViewGroup implements ItemController {
    private List<BaseTabItem> mItems;
    private List<OnTabItemSelectedListener> mListeners;
    private int mSelected;

    public CustomItemLayout(Context context) {
        this(context, null);
    }

    public CustomItemLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomItemLayout(Context context, AttributeSet attributeSet, int i) {
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
            baseTabItem.setOnClickListener(new View.OnClickListener() { // from class: me.majiajie.pagerbottomtabstrip.internal.CustomItemLayout.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    CustomItemLayout.this.setSelect(i);
                }
            });
        }
        this.mSelected = 0;
        this.mItems.get(0).setChecked(true);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int childCount = getChildCount();
        int i3 = 0;
        for (int i4 = 0; i4 < childCount; i4++) {
            if (getChildAt(i4).getVisibility() != 8) {
                i3++;
            }
        }
        if (i3 != 0) {
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i) / i3, MemoryConstants.f216GB);
            int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(Math.max(0, (View.MeasureSpec.getSize(i2) - getPaddingBottom()) - getPaddingTop()), MemoryConstants.f216GB);
            for (int i5 = 0; i5 < childCount; i5++) {
                View childAt = getChildAt(i5);
                if (childAt.getVisibility() != 8) {
                    childAt.measure(makeMeasureSpec, makeMeasureSpec2);
                }
            }
            super.onMeasure(i, i2);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        int i5 = i3 - i;
        int i6 = i4 - i2;
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int i7 = 0;
        for (int i8 = 0; i8 < childCount; i8++) {
            View childAt = getChildAt(i8);
            if (childAt.getVisibility() != 8) {
                if (ViewCompat.getLayoutDirection(this) == 1) {
                    int i9 = i5 - i7;
                    childAt.layout(i9 - childAt.getMeasuredWidth(), paddingTop, i9, i6 - paddingBottom);
                } else {
                    childAt.layout(i7, paddingTop, childAt.getMeasuredWidth() + i7, i6 - paddingBottom);
                }
                i7 += childAt.getMeasuredWidth();
            }
        }
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setSelect(int i) {
        int i2 = this.mSelected;
        if (i == i2) {
            for (OnTabItemSelectedListener onTabItemSelectedListener : this.mListeners) {
                this.mItems.get(this.mSelected).onRepeat();
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
