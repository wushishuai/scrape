package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.CompletableObserver */
/* loaded from: classes.dex */
public interface CompletableObserver {
    @Override // p005io.reactivex.MaybeObserver
    void onComplete();

    void onError(@NonNull Throwable th);

    void onSubscribe(@NonNull Disposable disposable);
}
