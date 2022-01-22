package p005io.reactivex.internal.observers;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.observers.ResumeSingleObserver */
/* loaded from: classes.dex */
public final class ResumeSingleObserver<T> implements SingleObserver<T> {
    final SingleObserver<? super T> downstream;
    final AtomicReference<Disposable> parent;

    public ResumeSingleObserver(AtomicReference<Disposable> atomicReference, SingleObserver<? super T> singleObserver) {
        this.parent = atomicReference;
        this.downstream = singleObserver;
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSubscribe(Disposable disposable) {
        DisposableHelper.replace(this.parent, disposable);
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSuccess(T t) {
        this.downstream.onSuccess(t);
    }

    @Override // p005io.reactivex.SingleObserver
    public void onError(Throwable th) {
        this.downstream.onError(th);
    }
}
