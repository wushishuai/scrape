package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.AbsListView;
import com.google.auto.value.AutoValue;

@AutoValue
/* loaded from: classes.dex */
public abstract class AbsListViewScrollEvent {
    public abstract int firstVisibleItem();

    public abstract int scrollState();

    public abstract int totalItemCount();

    @NonNull
    public abstract AbsListView view();

    public abstract int visibleItemCount();

    @CheckResult
    @NonNull
    public static AbsListViewScrollEvent create(AbsListView absListView, int i, int i2, int i3, int i4) {
        return new AutoValue_AbsListViewScrollEvent(absListView, i, i2, i3, i4);
    }
}
