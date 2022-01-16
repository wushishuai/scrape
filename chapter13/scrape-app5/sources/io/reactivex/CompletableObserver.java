package io.reactivex;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
/* loaded from: classes.dex */
public interface CompletableObserver {
    @Override // io.reactivex.MaybeObserver
    void onComplete();

    void onError(@NonNull Throwable th);

    void onSubscribe(@NonNull Disposable disposable);
}
