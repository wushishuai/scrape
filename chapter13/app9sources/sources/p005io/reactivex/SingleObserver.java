package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.SingleObserver */
/* loaded from: classes.dex */
public interface SingleObserver<T> {
    void onError(@NonNull Throwable th);

    void onSubscribe(@NonNull Disposable disposable);

    void onSuccess(@NonNull T t);
}
