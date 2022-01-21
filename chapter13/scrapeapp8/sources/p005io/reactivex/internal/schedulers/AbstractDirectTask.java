package p005io.reactivex.internal.schedulers;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.schedulers.SchedulerRunnableIntrospection;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: io.reactivex.internal.schedulers.AbstractDirectTask */
/* loaded from: classes.dex */
public abstract class AbstractDirectTask extends AtomicReference<Future<?>> implements Disposable, SchedulerRunnableIntrospection {
    private static final long serialVersionUID = 1811839108042568751L;
    protected final Runnable runnable;
    protected Thread runner;
    protected static final FutureTask<Void> FINISHED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
    protected static final FutureTask<Void> DISPOSED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);

    public AbstractDirectTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        FutureTask<Void> futureTask;
        Future<?> f = get();
        if (f != FINISHED && f != (futureTask = DISPOSED) && compareAndSet(f, futureTask) && f != null) {
            f.cancel(this.runner != Thread.currentThread());
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        Future<?> f = get();
        return f == FINISHED || f == DISPOSED;
    }

    public final void setFuture(Future<?> future) {
        Future<?> f;
        do {
            f = get();
            if (f != FINISHED) {
                if (f == DISPOSED) {
                    future.cancel(this.runner != Thread.currentThread());
                    return;
                }
            } else {
                return;
            }
        } while (!compareAndSet(f, future));
    }

    @Override // p005io.reactivex.schedulers.SchedulerRunnableIntrospection
    public Runnable getWrappedRunnable() {
        return this.runnable;
    }
}
