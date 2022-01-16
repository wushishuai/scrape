package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.MaybeObserver */
/* loaded from: classes.dex */
public interface MaybeObserver<T> {
    void onComplete();

    void onError(@NonNull Throwable th);

    void onSubscribe(@NonNull Disposable disposable);

    void onSuccess(@NonNull T t);
}
