package p005io.reactivex.internal.observers;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.util.NotificationLite;

/* renamed from: io.reactivex.internal.observers.BlockingObserver */
/* loaded from: classes.dex */
public final class BlockingObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {
    public static final Object TERMINATED = new Object();
    private static final long serialVersionUID = -4875965440900746268L;
    final Queue<Object> queue;

    public BlockingObserver(Queue<Object> queue) {
        this.queue = queue;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        DisposableHelper.setOnce(this, disposable);
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        this.queue.offer(NotificationLite.next(t));
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        this.queue.offer(NotificationLite.error(th));
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        this.queue.offer(NotificationLite.complete());
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        if (DisposableHelper.dispose(this)) {
            this.queue.offer(TERMINATED);
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }
}
