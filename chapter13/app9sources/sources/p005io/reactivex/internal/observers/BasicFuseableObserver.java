package p005io.reactivex.internal.observers;

import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.QueueDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.observers.BasicFuseableObserver */
/* loaded from: classes.dex */
public abstract class BasicFuseableObserver<T, R> implements Observer<T>, QueueDisposable<R> {
    protected boolean done;
    protected final Observer<? super R> downstream;

    /* renamed from: qd */
    protected QueueDisposable<T> f99qd;
    protected int sourceMode;
    protected Disposable upstream;

    protected void afterDownstream() {
    }

    protected boolean beforeDownstream() {
        return true;
    }

    public BasicFuseableObserver(Observer<? super R> observer) {
        this.downstream = observer;
    }

    @Override // p005io.reactivex.Observer
    public final void onSubscribe(Disposable disposable) {
        if (DisposableHelper.validate(this.upstream, disposable)) {
            this.upstream = disposable;
            if (disposable instanceof QueueDisposable) {
                this.f99qd = (QueueDisposable) disposable;
            }
            if (beforeDownstream()) {
                this.downstream.onSubscribe(this);
                afterDownstream();
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        if (this.done) {
            RxJavaPlugins.onError(th);
            return;
        }
        this.done = true;
        this.downstream.onError(th);
    }

    protected final void fail(Throwable th) {
        Exceptions.throwIfFatal(th);
        this.upstream.dispose();
        onError(th);
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.done) {
            this.done = true;
            this.downstream.onComplete();
        }
    }

    protected final int transitiveBoundaryFusion(int i) {
        QueueDisposable<T> queueDisposable = this.f99qd;
        if (queueDisposable == null || (i & 4) != 0) {
            return 0;
        }
        int requestFusion = queueDisposable.requestFusion(i);
        if (requestFusion != 0) {
            this.sourceMode = requestFusion;
        }
        return requestFusion;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        this.upstream.dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.upstream.isDisposed();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return this.f99qd.isEmpty();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
        this.f99qd.clear();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(R r) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(R r, R r2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
