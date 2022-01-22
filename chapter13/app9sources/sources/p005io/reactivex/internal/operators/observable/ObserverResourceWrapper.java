package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObserverResourceWrapper */
/* loaded from: classes.dex */
public final class ObserverResourceWrapper<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {
    private static final long serialVersionUID = -8612022020200669122L;
    final Observer<? super T> downstream;
    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    public ObserverResourceWrapper(Observer<? super T> observer) {
        this.downstream = observer;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        if (DisposableHelper.setOnce(this.upstream, disposable)) {
            this.downstream.onSubscribe(this);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        this.downstream.onNext(t);
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        dispose();
        this.downstream.onError(th);
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        dispose();
        this.downstream.onComplete();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        DisposableHelper.dispose(this.upstream);
        DisposableHelper.dispose(this);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.upstream.get() == DisposableHelper.DISPOSED;
    }

    public void setResource(Disposable disposable) {
        DisposableHelper.set(this, disposable);
    }
}
