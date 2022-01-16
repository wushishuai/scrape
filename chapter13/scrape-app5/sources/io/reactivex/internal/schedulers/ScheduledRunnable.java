package io.reactivex.internal.schedulers;

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableContainer;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;
/* loaded from: classes.dex */
public final class ScheduledRunnable extends AtomicReferenceArray<Object> implements Runnable, Callable<Object>, Disposable {
    static final int FUTURE_INDEX = 1;
    static final int PARENT_INDEX = 0;
    static final int THREAD_INDEX = 2;
    private static final long serialVersionUID = -6120223772001106981L;
    final Runnable actual;
    static final Object PARENT_DISPOSED = new Object();
    static final Object SYNC_DISPOSED = new Object();
    static final Object ASYNC_DISPOSED = new Object();
    static final Object DONE = new Object();

    public ScheduledRunnable(Runnable actual, DisposableContainer parent) {
        super(3);
        this.actual = actual;
        lazySet(0, parent);
    }

    @Override // java.util.concurrent.Callable
    public Object call() {
        run();
        return null;
    }

    @Override // java.lang.Runnable
    public void run() {
        Object o;
        Object o2;
        lazySet(2, Thread.currentThread());
        try {
            this.actual.run();
            lazySet(2, null);
            Object o3 = get(0);
            if (!(o3 == PARENT_DISPOSED || !compareAndSet(0, o3, DONE) || o3 == null)) {
                ((DisposableContainer) o3).delete(this);
            }
            do {
                o2 = get(1);
                if (o2 == SYNC_DISPOSED || o2 == ASYNC_DISPOSED) {
                    return;
                }
            } while (!compareAndSet(1, o2, DONE));
        } catch (Throwable th) {
            lazySet(2, null);
            Object o4 = get(0);
            if (!(o4 == PARENT_DISPOSED || !compareAndSet(0, o4, DONE) || o4 == null)) {
                ((DisposableContainer) o4).delete(this);
            }
            do {
                o = get(1);
                if (o == SYNC_DISPOSED || o == ASYNC_DISPOSED) {
                    break;
                }
            } while (!compareAndSet(1, o, DONE));
            throw th;
        }
    }

    public void setFuture(Future<?> f) {
        Object o;
        do {
            o = get(1);
            if (o != DONE) {
                if (o == SYNC_DISPOSED) {
                    f.cancel(false);
                    return;
                } else if (o == ASYNC_DISPOSED) {
                    f.cancel(true);
                    return;
                }
            } else {
                return;
            }
        } while (!compareAndSet(1, o, f));
    }

    @Override // io.reactivex.disposables.Disposable
    public void dispose() {
        Object o;
        Object obj;
        while (true) {
            Object o2 = get(1);
            if (o2 == DONE || o2 == SYNC_DISPOSED || o2 == ASYNC_DISPOSED) {
                break;
            }
            boolean async = get(2) != Thread.currentThread();
            if (compareAndSet(1, o2, async ? ASYNC_DISPOSED : SYNC_DISPOSED)) {
                if (o2 != null) {
                    ((Future) o2).cancel(async);
                }
            }
        }
        do {
            o = get(0);
            if (o == DONE || o == (obj = PARENT_DISPOSED) || o == null) {
                return;
            }
        } while (!compareAndSet(0, o, obj));
        ((DisposableContainer) o).delete(this);
    }

    @Override // io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        Object o = get(0);
        if (o == PARENT_DISPOSED || o == DONE) {
            return true;
        }
        return false;
    }
}
