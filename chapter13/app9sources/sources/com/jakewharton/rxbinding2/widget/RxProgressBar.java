package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.functions.Consumer;

/* loaded from: classes.dex */
public final class RxProgressBar {
    @CheckResult
    @NonNull
    public static Consumer<? super Integer> incrementProgressBy(@NonNull final ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar, "view == null");
        return new Consumer<Integer>() { // from class: com.jakewharton.rxbinding2.widget.RxProgressBar.1
            public void accept(Integer num) {
                progressBar.incrementProgressBy(num.intValue());
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Integer> incrementSecondaryProgressBy(@NonNull final ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar, "view == null");
        return new Consumer<Integer>() { // from class: com.jakewharton.rxbinding2.widget.RxProgressBar.2
            public void accept(Integer num) {
                progressBar.incrementSecondaryProgressBy(num.intValue());
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Boolean> indeterminate(@NonNull final ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar, "view == null");
        return new Consumer<Boolean>() { // from class: com.jakewharton.rxbinding2.widget.RxProgressBar.3
            public void accept(Boolean bool) {
                progressBar.setIndeterminate(bool.booleanValue());
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Integer> max(@NonNull final ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar, "view == null");
        return new Consumer<Integer>() { // from class: com.jakewharton.rxbinding2.widget.RxProgressBar.4
            public void accept(Integer num) {
                progressBar.setMax(num.intValue());
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Integer> progress(@NonNull final ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar, "view == null");
        return new Consumer<Integer>() { // from class: com.jakewharton.rxbinding2.widget.RxProgressBar.5
            public void accept(Integer num) {
                progressBar.setProgress(num.intValue());
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Integer> secondaryProgress(@NonNull final ProgressBar progressBar) {
        Preconditions.checkNotNull(progressBar, "view == null");
        return new Consumer<Integer>() { // from class: com.jakewharton.rxbinding2.widget.RxProgressBar.6
            public void accept(Integer num) {
                progressBar.setSecondaryProgress(num.intValue());
            }
        };
    }

    private RxProgressBar() {
        throw new AssertionError("No instances.");
    }
}
