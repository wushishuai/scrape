package com.trello.rxlifecycle2;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import p005io.reactivex.Observable;

/* loaded from: classes.dex */
public interface LifecycleProvider<E> {
    @Nonnull
    @CheckReturnValue
    <T> LifecycleTransformer<T> bindToLifecycle();

    @Nonnull
    @CheckReturnValue
    <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull E e);

    @Nonnull
    @CheckReturnValue
    Observable<E> lifecycle();
}
