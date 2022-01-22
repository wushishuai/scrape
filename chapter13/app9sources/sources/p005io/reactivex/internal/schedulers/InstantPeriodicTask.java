package p005io.reactivex.internal.schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.schedulers.InstantPeriodicTask */
/* loaded from: classes.dex */
final class InstantPeriodicTask implements Callable<Void>, Disposable {
    static final FutureTask<Void> CANCELLED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
    final ExecutorService executor;
    final AtomicReference<Future<?>> first = new AtomicReference<>();
    final AtomicReference<Future<?>> rest = new AtomicReference<>();
    Thread runner;
    final Runnable task;

    /* JADX INFO: Access modifiers changed from: package-private */
    public InstantPeriodicTask(Runnable runnable, ExecutorService executorService) {
        this.task = runnable;
        this.executor = executorService;
    }

    @Override // java.util.concurrent.Callable
    public Void call() throws Exception {
        this.runner = Thread.currentThread();
        try {
            this.task.run();
            setRest(this.executor.submit(this));
            this.runner = null;
        } catch (Throwable th) {
            this.runner = null;
            RxJavaPlugins.onError(th);
        }
        return null;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        Future<?> andSet = this.first.getAndSet(CANCELLED);
        boolean z = true;
        if (!(andSet == null || andSet == CANCELLED)) {
            andSet.cancel(this.runner != Thread.currentThread());
        }
        Future<?> andSet2 = this.rest.getAndSet(CANCELLED);
        if (andSet2 != null && andSet2 != CANCELLED) {
            if (this.runner == Thread.currentThread()) {
                z = false;
            }
            andSet2.cancel(z);
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.first.get() == CANCELLED;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setFirst(Future<?> future) {
        Future<?> future2;
        do {
            future2 = this.first.get();
            if (future2 == CANCELLED) {
                future.cancel(this.runner != Thread.currentThread());
                return;
            }
        } while (!this.first.compareAndSet(future2, future));
    }

    void setRest(Future<?> future) {
        Future<?> future2;
        do {
            future2 = this.rest.get();
            if (future2 == CANCELLED) {
                future.cancel(this.runner != Thread.currentThread());
                return;
            }
        } while (!this.rest.compareAndSet(future2, future));
    }
}
