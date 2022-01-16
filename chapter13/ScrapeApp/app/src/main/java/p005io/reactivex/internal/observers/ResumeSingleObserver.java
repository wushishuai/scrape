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

    public ResumeSingleObserver(AtomicReference<Disposable> parent, SingleObserver<? super T> downstream) {
        this.parent = parent;
        this.downstream = downstream;
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSubscribe(Disposable d) {
        DisposableHelper.replace(this.parent, d);
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSuccess(T value) {
        this.downstream.onSuccess(value);
    }

    @Override // p005io.reactivex.SingleObserver
    public void onError(Throwable e) {
        this.downstream.onError(e);
    }
}
