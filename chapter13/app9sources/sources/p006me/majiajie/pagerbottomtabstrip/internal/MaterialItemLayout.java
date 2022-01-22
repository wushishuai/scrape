package p006me.majiajie.pagerbottomtabstrip.internal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.p000v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import p006me.goldze.mvvmhabit.utils.constant.MemoryConstants;
import p006me.majiajie.pagerbottomtabstrip.C1028R;
import p006me.majiajie.pagerbottomtabstrip.ItemController;
import p006me.majiajie.pagerbottomtabstrip.item.MaterialItemView;
import p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

/* renamed from: me.majiajie.pagerbottomtabstrip.internal.MaterialItemLayout */
/* loaded from: classes.dex */
public class MaterialItemLayout extends ViewGroup implements ItemController {
    private final int ANIM_TIME;
    private final int DEFAULT_SELECTED;
    private final int MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH;
    private final int MATERIAL_BOTTOM_NAVIGATION_ITEM_HEIGHT;
    private final int MATERIAL_BOTTOM_NAVIGATION_ITEM_MAX_WIDTH;
    private final int MATERIAL_BOTTOM_NAVIGATION_ITEM_MIN_WIDTH;
    private boolean mChangeBackgroundMode;
    private List<Integer> mColors;
    private boolean mHideTitle;
    private Interpolator mInterpolator;
    private int mItemTotalWidth;
    private List<MaterialItemView> mItems;
    private float mLastUpX;
    private float mLastUpY;
    private List<OnTabItemSelectedListener> mListeners;
    private int mOldSelected;
    private List<Oval> mOvals;
    private Paint mPaint;
    private int mSelected;
    private int[] mTempChildWidths;
    private RectF mTempRectF;

    public MaterialItemLayout(Context context) {
        this(context, null);
    }

    public MaterialItemLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MaterialItemLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.DEFAULT_SELECTED = 0;
        this.mListeners = new ArrayList();
        this.mSelected = -1;
        this.mOldSelected = -1;
        this.ANIM_TIME = 300;
        Resources resources = getResources();
        this.MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH = resources.getDimensionPixelSize(C1028R.dimen.material_bottom_navigation_active_item_max_width);
        this.MATERIAL_BOTTOM_NAVIGATION_ITEM_MAX_WIDTH = resources.getDimensionPixelSize(C1028R.dimen.material_bottom_navigation_item_max_width);
        this.MATERIAL_BOTTOM_NAVIGATION_ITEM_MIN_WIDTH = resources.getDimensionPixelSize(C1028R.dimen.material_bottom_navigation_item_min_width);
        this.MATERIAL_BOTTOM_NAVIGATION_ITEM_HEIGHT = resources.getDimensionPixelSize(C1028R.dimen.material_bottom_navigation_height);
        this.mTempChildWidths = new int[5];
    }

    public void initialize(List<MaterialItemView> list, List<Integer> list2, int i) {
        this.mItems = list;
        if ((i & 2) > 0) {
            this.mChangeBackgroundMode = true;
            this.mOvals = new ArrayList();
            this.mColors = list2;
            this.mInterpolator = new AccelerateDecelerateInterpolator();
            this.mTempRectF = new RectF();
            this.mPaint = new Paint();
            setBackgroundColor(this.mColors.get(0).intValue());
        } else {
            for (int i2 = 0; i2 < this.mItems.size(); i2++) {
                MaterialItemView materialItemView = this.mItems.get(i2);
                if (Build.VERSION.SDK_INT >= 21) {
                    materialItemView.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{(16777215 & list2.get(i2).intValue()) | 1442840576}), null, null));
                } else {
                    materialItemView.setBackgroundResource(C1028R.C1029drawable.material_item_background);
                }
            }
        }
        if ((i & 1) > 0) {
            this.mHideTitle = true;
            for (MaterialItemView materialItemView2 : this.mItems) {
                materialItemView2.setHideTitle(true);
            }
        }
        int size = this.mItems.size();
        for (final int i3 = 0; i3 < size; i3++) {
            MaterialItemView materialItemView3 = this.mItems.get(i3);
            materialItemView3.setChecked(false);
            addView(materialItemView3);
            materialItemView3.setOnClickListener(new View.OnClickListener() { // from class: me.majiajie.pagerbottomtabstrip.internal.MaterialItemLayout.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    MaterialItemLayout materialItemLayout = MaterialItemLayout.this;
                    materialItemLayout.setSelect(i3, materialItemLayout.mLastUpX, MaterialItemLayout.this.mLastUpY);
                }
            });
        }
        this.mSelected = 0;
        this.mItems.get(0).setChecked(true);
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        List<MaterialItemView> list = this.mItems;
        if (list == null || list.size() <= 0) {
            super.onMeasure(i, i2);
            return;
        }
        int size = View.MeasureSpec.getSize(i);
        int childCount = getChildCount();
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.MATERIAL_BOTTOM_NAVIGATION_ITEM_HEIGHT, MemoryConstants.f216GB);
        if (this.mHideTitle) {
            int i3 = childCount - 1;
            int min = Math.min(size - (this.MATERIAL_BOTTOM_NAVIGATION_ITEM_MIN_WIDTH * i3), this.MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH);
            int min2 = Math.min((size - min) / i3, this.MATERIAL_BOTTOM_NAVIGATION_ITEM_MAX_WIDTH);
            for (int i4 = 0; i4 < childCount; i4++) {
                int i5 = this.mSelected;
                if (i4 == i5) {
                    this.mTempChildWidths[i4] = (int) ((((float) (min - min2)) * this.mItems.get(i5).getAnimValue()) + ((float) min2));
                } else if (i4 == this.mOldSelected) {
                    this.mTempChildWidths[i4] = (int) (((float) min) - (((float) (min - min2)) * this.mItems.get(i5).getAnimValue()));
                } else {
                    this.mTempChildWidths[i4] = min2;
                }
            }
        } else {
            int min3 = Math.min(size / (childCount == 0 ? 1 : childCount), this.MATERIAL_BOTTOM_NAVIGATION_ACTIVE_ITEM_MAX_WIDTH);
            for (int i6 = 0; i6 < childCount; i6++) {
                this.mTempChildWidths[i6] = min3;
            }
        }
        this.mItemTotalWidth = 0;
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt = getChildAt(i7);
            if (childAt.getVisibility() != 8) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(this.mTempChildWidths[i7], MemoryConstants.f216GB), makeMeasureSpec);
                childAt.getLayoutParams().width = childAt.getMeasuredWidth();
                this.mItemTotalWidth += childAt.getMeasuredWidth();
            }
        }
        super.onMeasure(i, i2);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int childCount = getChildCount();
        int i5 = i3 - i;
        int i6 = i4 - i2;
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int i7 = this.mItemTotalWidth;
        int i8 = (i7 <= 0 || i7 >= i5) ? 0 : (i5 - i7) / 2;
        for (int i9 = 0; i9 < childCount; i9++) {
            View childAt = getChildAt(i9);
            if (childAt.getVisibility() != 8) {
                if (ViewCompat.getLayoutDirection(this) == 1) {
                    int i10 = i5 - i8;
                    childAt.layout(i10 - childAt.getMeasuredWidth(), paddingTop, i10, i6 - paddingBottom);
                } else {
                    childAt.layout(i8, paddingTop, childAt.getMeasuredWidth() + i8, i6 - paddingBottom);
                }
                i8 += childAt.getMeasuredWidth();
            }
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mChangeBackgroundMode) {
            int width = getWidth();
            int height = getHeight();
            Iterator<Oval> it = this.mOvals.iterator();
            while (it.hasNext()) {
                Oval next = it.next();
                this.mPaint.setColor(next.color);
                if (next.f219r < next.maxR) {
                    this.mTempRectF.set(next.getLeft(), next.getTop(), next.getRight(), next.getBottom());
                    canvas.drawOval(this.mTempRectF, this.mPaint);
                } else {
                    setBackgroundColor(next.color);
                    canvas.drawRect(0.0f, 0.0f, (float) width, (float) height, this.mPaint);
                    it.remove();
                }
                invalidate();
            }
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 1) {
            this.mLastUpX = motionEvent.getX();
            this.mLastUpY = motionEvent.getY();
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setSelect(int i) {
        if (i < this.mItems.size() && i >= 0) {
            MaterialItemView materialItemView = this.mItems.get(i);
            setSelect(i, materialItemView.getX() + (((float) materialItemView.getWidth()) / 2.0f), materialItemView.getY() + (((float) materialItemView.getHeight()) / 2.0f));
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

    /* JADX INFO: Access modifiers changed from: private */
    public void setSelect(int i, float f, float f2) {
        int i2 = this.mSelected;
        if (i == i2) {
            for (OnTabItemSelectedListener onTabItemSelectedListener : this.mListeners) {
                onTabItemSelectedListener.onRepeat(this.mSelected);
            }
            return;
        }
        this.mOldSelected = i2;
        this.mSelected = i;
        if (this.mChangeBackgroundMode) {
            addOvalColor(this.mColors.get(this.mSelected).intValue(), f, f2);
        }
        int i3 = this.mOldSelected;
        if (i3 >= 0) {
            this.mItems.get(i3).setChecked(false);
        }
        this.mItems.get(this.mSelected).setChecked(true);
        for (OnTabItemSelectedListener onTabItemSelectedListener2 : this.mListeners) {
            onTabItemSelectedListener2.onSelected(this.mSelected, this.mOldSelected);
        }
    }

    private void addOvalColor(int i, float f, float f2) {
        final Oval oval = new Oval(i, 2.0f, f, f2);
        oval.maxR = getR(f, f2);
        this.mOvals.add(oval);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(oval.f219r, oval.maxR);
        ofFloat.setInterpolator(this.mInterpolator);
        ofFloat.setDuration(300L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: me.majiajie.pagerbottomtabstrip.internal.MaterialItemLayout.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                oval.f219r = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: me.majiajie.pagerbottomtabstrip.internal.MaterialItemLayout.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                MaterialItemLayout.this.invalidate();
            }
        });
        ofFloat.start();
    }

    private float getR(float f, float f2) {
        float f3 = f * f;
        float f4 = f2 * f2;
        float width = ((float) getWidth()) - f;
        float f5 = width * width;
        float height = ((float) getHeight()) - f2;
        float f6 = height * height;
        return (float) Math.sqrt(Math.max(Math.max((double) (f3 + f4), (double) (f4 + f5)), Math.max((double) (f5 + f6), (double) (f3 + f6))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: me.majiajie.pagerbottomtabstrip.internal.MaterialItemLayout$Oval */
    /* loaded from: classes.dex */
    public class Oval {
        int color;
        float maxR;

        /* renamed from: r */
        float f219r;

        /* renamed from: x */
        float f220x;

        /* renamed from: y */
        float f221y;

        Oval(int i, float f, float f2, float f3) {
            this.color = i;
            this.f219r = f;
            this.f220x = f2;
            this.f221y = f3;
        }

        float getLeft() {
            return this.f220x - this.f219r;
        }

        float getTop() {
            return this.f221y - this.f219r;
        }

        float getRight() {
            return this.f220x + this.f219r;
        }

        float getBottom() {
            return this.f221y + this.f219r;
        }
    }
}
