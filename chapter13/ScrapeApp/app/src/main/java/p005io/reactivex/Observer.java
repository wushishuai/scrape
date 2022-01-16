package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.Observer */
/* loaded from: classes.dex */
public interface Observer<T> {
    void onComplete();

    void onError(@NonNull Throwable th);

    void onNext(@NonNull T t);

    void onSubscribe(@NonNull Disposable disposable);
}
