package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextView;
import com.google.auto.value.AutoValue;

@AutoValue
/* loaded from: classes.dex */
public abstract class TextViewTextChangeEvent {
    public abstract int before();

    public abstract int count();

    public abstract int start();

    @NonNull
    public abstract CharSequence text();

    @NonNull
    public abstract TextView view();

    @CheckResult
    @NonNull
    public static TextViewTextChangeEvent create(@NonNull TextView textView, @NonNull CharSequence charSequence, int i, int i2, int i3) {
        return new AutoValue_TextViewTextChangeEvent(textView, charSequence, i, i2, i3);
    }
}
