package android.support.p003v7.widget.helper;

import android.graphics.Canvas;
import android.os.Build;
import android.support.p000v4.view.ViewCompat;
import android.support.p003v7.recyclerview.C0464R;
import android.support.p003v7.widget.RecyclerView;
import android.view.View;

/* renamed from: android.support.v7.widget.helper.ItemTouchUIUtilImpl */
/* loaded from: classes.dex */
class ItemTouchUIUtilImpl implements ItemTouchUIUtil {
    static final ItemTouchUIUtil INSTANCE = new ItemTouchUIUtilImpl();

    ItemTouchUIUtilImpl() {
    }

    @Override // android.support.p003v7.widget.helper.ItemTouchUIUtil
    public void onDraw(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (Build.VERSION.SDK_INT >= 21 && isCurrentlyActive && view.getTag(C0464R.C0467id.item_touch_helper_previous_elevation) == null) {
            Object originalElevation = Float.valueOf(ViewCompat.getElevation(view));
            ViewCompat.setElevation(view, findMaxElevation(recyclerView, view) + 1.0f);
            view.setTag(C0464R.C0467id.item_touch_helper_previous_elevation, originalElevation);
        }
        view.setTranslationX(dX);
        view.setTranslationY(dY);
    }

    private static float findMaxElevation(RecyclerView recyclerView, View itemView) {
        int childCount = recyclerView.getChildCount();
        float max = 0.0f;
        for (int i = 0; i < childCount; i++) {
            View child = recyclerView.getChildAt(i);
            if (child != itemView) {
                float elevation = ViewCompat.getElevation(child);
                if (elevation > max) {
                    max = elevation;
                }
            }
        }
        return max;
    }

    @Override // android.support.p003v7.widget.helper.ItemTouchUIUtil
    public void onDrawOver(Canvas c, RecyclerView recyclerView, View view, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    }

    @Override // android.support.p003v7.widget.helper.ItemTouchUIUtil
    public void clearView(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            Object tag = view.getTag(C0464R.C0467id.item_touch_helper_previous_elevation);
            if (tag != null && (tag instanceof Float)) {
                ViewCompat.setElevation(view, ((Float) tag).floatValue());
            }
            view.setTag(C0464R.C0467id.item_touch_helper_previous_elevation, null);
        }
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
    }

    @Override // android.support.p003v7.widget.helper.ItemTouchUIUtil
    public void onSelected(View view) {
    }
}
