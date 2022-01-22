package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import com.google.auto.value.AutoValue;

@AutoValue
/* loaded from: classes.dex */
public abstract class AdapterViewItemSelectionEvent extends AdapterViewSelectionEvent {
    /* renamed from: id */
    public abstract long mo41id();

    public abstract int position();

    @NonNull
    public abstract View selectedView();

    @CheckResult
    @NonNull
    public static AdapterViewSelectionEvent create(@NonNull AdapterView<?> adapterView, @NonNull View view, int i, long j) {
        return new AutoValue_AdapterViewItemSelectionEvent(adapterView, view, i, j);
    }
}
