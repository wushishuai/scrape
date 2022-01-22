package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.SeekBar;
import com.google.auto.value.AutoValue;

@AutoValue
/* loaded from: classes.dex */
public abstract class SeekBarProgressChangeEvent extends SeekBarChangeEvent {
    public abstract boolean fromUser();

    public abstract int progress();

    @CheckResult
    @NonNull
    public static SeekBarProgressChangeEvent create(@NonNull SeekBar seekBar, int i, boolean z) {
        return new AutoValue_SeekBarProgressChangeEvent(seekBar, i, z);
    }
}
