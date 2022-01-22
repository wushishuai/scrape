package p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.p003v7.widget.RecyclerView;
import android.view.View;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.DividerLine */
/* loaded from: classes.dex */
public class DividerLine extends RecyclerView.ItemDecoration {
    private static final int DEFAULT_DIVIDER_SIZE = 1;
    private Drawable dividerDrawable;
    private int dividerSize;
    private Context mContext;
    private LineDrawMode mMode;
    private static final String TAG = DividerLine.class.getCanonicalName();
    private static final int[] ATTRS = {16843284};

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.DividerLine$LineDrawMode */
    /* loaded from: classes.dex */
    public enum LineDrawMode {
        HORIZONTAL,
        VERTICAL,
        BOTH
    }

    public DividerLine(Context context) {
        this.mMode = null;
        this.mContext = context;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(ATTRS);
        this.dividerDrawable = obtainStyledAttributes.getDrawable(0);
        obtainStyledAttributes.recycle();
    }

    public DividerLine(Context context, LineDrawMode lineDrawMode) {
        this(context);
        this.mMode = lineDrawMode;
    }

    public DividerLine(Context context, int i, LineDrawMode lineDrawMode) {
        this(context, lineDrawMode);
        this.dividerSize = i;
    }

    public int getDividerSize() {
        return this.dividerSize;
    }

    public void setDividerSize(int i) {
        this.dividerSize = i;
    }

    public LineDrawMode getMode() {
        return this.mMode;
    }

    public void setMode(LineDrawMode lineDrawMode) {
        this.mMode = lineDrawMode;
    }

    @Override // android.support.p003v7.widget.RecyclerView.ItemDecoration
    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        super.onDrawOver(canvas, recyclerView, state);
        if (getMode() != null) {
            switch (getMode()) {
                case VERTICAL:
                    drawVertical(canvas, recyclerView, state);
                    return;
                case HORIZONTAL:
                    drawHorizontal(canvas, recyclerView, state);
                    return;
                case BOTH:
                    drawHorizontal(canvas, recyclerView, state);
                    drawVertical(canvas, recyclerView, state);
                    return;
                default:
                    return;
            }
        } else {
            throw new IllegalStateException("assign LineDrawMode,please!");
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = recyclerView.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childAt.getLayoutParams();
            int top = childAt.getTop() - layoutParams.topMargin;
            int bottom = childAt.getBottom() + layoutParams.bottomMargin;
            int right = childAt.getRight() + layoutParams.rightMargin;
            this.dividerDrawable.setBounds(right, top, (getDividerSize() == 0 ? dip2px(this.mContext, 1.0f) : getDividerSize()) + right, bottom);
            this.dividerDrawable.draw(canvas);
        }
    }

    private void drawHorizontal(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = recyclerView.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childAt.getLayoutParams();
            int left = childAt.getLeft() - layoutParams.leftMargin;
            int bottom = childAt.getBottom() + layoutParams.topMargin;
            this.dividerDrawable.setBounds(left, bottom, childAt.getRight() - layoutParams.rightMargin, (getDividerSize() == 0 ? dip2px(this.mContext, 1.0f) : getDividerSize()) + bottom);
            this.dividerDrawable.draw(canvas);
        }
    }

    @Override // android.support.p003v7.widget.RecyclerView.ItemDecoration
    public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        super.getItemOffsets(rect, view, recyclerView, state);
    }

    public static int dip2px(Context context, float f) {
        return (int) ((f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
