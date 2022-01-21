package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Cancellable;

/* renamed from: io.reactivex.CompletableEmitter */
/* loaded from: classes.dex */
public interface CompletableEmitter {
    @Override // p005io.reactivex.disposables.Disposable
    boolean isDisposed();

    void onComplete();

    void onError(@NonNull Throwable th);

    void setCancellable(@Nullable Cancellable cancellable);

    void setDisposable(@Nullable Disposable disposable);

    boolean tryOnError(@NonNull Throwable th);
}
