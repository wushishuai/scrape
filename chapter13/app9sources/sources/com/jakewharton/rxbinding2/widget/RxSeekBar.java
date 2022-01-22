package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.SeekBar;
import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.internal.Preconditions;

/* loaded from: classes.dex */
public final class RxSeekBar {
    @CheckResult
    @NonNull
    public static InitialValueObservable<Integer> changes(@NonNull SeekBar seekBar) {
        Preconditions.checkNotNull(seekBar, "view == null");
        return new SeekBarChangeObservable(seekBar, null);
    }

    @CheckResult
    @NonNull
    public static InitialValueObservable<Integer> userChanges(@NonNull SeekBar seekBar) {
        Preconditions.checkNotNull(seekBar, "view == null");
        return new SeekBarChangeObservable(seekBar, true);
    }

    @CheckResult
    @NonNull
    public static InitialValueObservable<Integer> systemChanges(@NonNull SeekBar seekBar) {
        Preconditions.checkNotNull(seekBar, "view == null");
        return new SeekBarChangeObservable(seekBar, false);
    }

    @CheckResult
    @NonNull
    public static InitialValueObservable<SeekBarChangeEvent> changeEvents(@NonNull SeekBar seekBar) {
        Preconditions.checkNotNull(seekBar, "view == null");
        return new SeekBarChangeEventObservable(seekBar);
    }

    private RxSeekBar() {
        throw new AssertionError("No instances.");
    }
}
