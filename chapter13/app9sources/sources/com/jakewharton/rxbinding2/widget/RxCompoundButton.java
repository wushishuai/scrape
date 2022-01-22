package com.jakewharton.rxbinding2.widget;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.CompoundButton;
import com.jakewharton.rxbinding2.InitialValueObservable;
import com.jakewharton.rxbinding2.internal.Preconditions;
import p005io.reactivex.functions.Consumer;

/* loaded from: classes.dex */
public final class RxCompoundButton {
    @CheckResult
    @NonNull
    public static InitialValueObservable<Boolean> checkedChanges(@NonNull CompoundButton compoundButton) {
        Preconditions.checkNotNull(compoundButton, "view == null");
        return new CompoundButtonCheckedChangeObservable(compoundButton);
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Boolean> checked(@NonNull final CompoundButton compoundButton) {
        Preconditions.checkNotNull(compoundButton, "view == null");
        return new Consumer<Boolean>() { // from class: com.jakewharton.rxbinding2.widget.RxCompoundButton.1
            public void accept(Boolean bool) throws Exception {
                compoundButton.setChecked(bool.booleanValue());
            }
        };
    }

    @CheckResult
    @NonNull
    public static Consumer<? super Object> toggle(@NonNull final CompoundButton compoundButton) {
        Preconditions.checkNotNull(compoundButton, "view == null");
        return new Consumer<Object>() { // from class: com.jakewharton.rxbinding2.widget.RxCompoundButton.2
            @Override // p005io.reactivex.functions.Consumer
            public void accept(Object obj) {
                compoundButton.toggle();
            }
        };
    }

    private RxCompoundButton() {
        throw new AssertionError("No instances.");
    }
}
