package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Cancellable;

/* renamed from: io.reactivex.ObservableEmitter */
/* loaded from: classes.dex */
public interface ObservableEmitter<T> extends Emitter<T> {
    @Override // p005io.reactivex.disposables.Disposable
    boolean isDisposed();

    @NonNull
    ObservableEmitter<T> serialize();

    void setCancellable(@Nullable Cancellable cancellable);

    void setDisposable(@Nullable Disposable disposable);

    boolean tryOnError(@NonNull Throwable th);
}
