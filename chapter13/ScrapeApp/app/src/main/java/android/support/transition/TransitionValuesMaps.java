package android.support.transition;

import android.support.p000v4.util.ArrayMap;
import android.support.p000v4.util.LongSparseArray;
import android.util.SparseArray;
import android.view.View;

/* loaded from: classes.dex */
class TransitionValuesMaps {
    final ArrayMap<View, TransitionValues> mViewValues = new ArrayMap<>();
    final SparseArray<View> mIdValues = new SparseArray<>();
    final LongSparseArray<View> mItemIdValues = new LongSparseArray<>();
    final ArrayMap<String, View> mNameValues = new ArrayMap<>();
}
