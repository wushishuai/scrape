package p005io.reactivex.internal.observers;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.QueueDisposable;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.util.QueueDrainHelper;

/* renamed from: io.reactivex.internal.observers.InnerQueuedObserver */
/* loaded from: classes.dex */
public final class InnerQueuedObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {
    private static final long serialVersionUID = -5417183359794346637L;
    volatile boolean done;
    int fusionMode;
    final InnerQueuedObserverSupport<T> parent;
    final int prefetch;
    SimpleQueue<T> queue;

    public InnerQueuedObserver(InnerQueuedObserverSupport<T> innerQueuedObserverSupport, int i) {
        this.parent = innerQueuedObserverSupport;
        this.prefetch = i;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        if (DisposableHelper.setOnce(this, disposable)) {
            if (disposable instanceof QueueDisposable) {
                QueueDisposable queueDisposable = (QueueDisposable) disposable;
                int requestFusion = queueDisposable.requestFusion(3);
                if (requestFusion == 1) {
                    this.fusionMode = requestFusion;
                    this.queue = queueDisposable;
                    this.done = true;
                    this.parent.innerComplete(this);
                    return;
                } else if (requestFusion == 2) {
                    this.fusionMode = requestFusion;
                    this.queue = queueDisposable;
                    return;
                }
            }
            this.queue = QueueDrainHelper.createQueue(-this.prefetch);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        if (this.fusionMode == 0) {
            this.parent.innerNext(this, t);
        } else {
            this.parent.drain();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        this.parent.innerError(this, th);
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        this.parent.innerComplete(this);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(get());
    }

    public boolean isDone() {
        return this.done;
    }

    public void setDone() {
        this.done = true;
    }

    public SimpleQueue<T> queue() {
        return this.queue;
    }

    public int fusionMode() {
        return this.fusionMode;
    }
}
