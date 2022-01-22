package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.RatingBar;
import com.google.auto.value.AutoValue;

@AutoValue
/* loaded from: classes.dex */
public abstract class RatingBarChangeEvent {
    public abstract boolean fromUser();

    public abstract float rating();

    @NonNull
    public abstract RatingBar view();

    @CheckResult
    @NonNull
    public static RatingBarChangeEvent create(@NonNull RatingBar ratingBar, float f, boolean z) {
        return new AutoValue_RatingBarChangeEvent(ratingBar, f, z);
    }
}
