package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.TextSwitcher;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.functions.Consumer;

/* loaded from: classes.dex */
public final class RxTextSwitcher {
    @CheckResult
    @NonNull
    public static Consumer<? super CharSequence> text(@NonNull final TextSwitcher textSwitcher) {
        Preconditions.checkNotNull(textSwitcher, "view == null");
        return new Consumer<CharSequence>() { // from class: com.jakewharton.rxbinding2.widget.RxTextSwitcher.1
            public void accept(CharSequence charSequence) {
                textSwitcher.setText(charSequence);
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super CharSequence> currentText(@NonNull final TextSwitcher textSwitcher) {
        Preconditions.checkNotNull(textSwitcher, "view == null");
        return new Consumer<CharSequence>() { // from class: com.jakewharton.rxbinding2.widget.RxTextSwitcher.2
            public void accept(CharSequence charSequence) {
                textSwitcher.setCurrentText(charSequence);
            }
        };
    }

    private RxTextSwitcher() {
        throw new AssertionError("No instances.");
    }
}
