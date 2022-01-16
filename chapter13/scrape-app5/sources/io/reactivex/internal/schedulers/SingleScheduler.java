package io.reactivex.internal.schedulers;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
/* loaded from: classes.dex */
public final class SingleScheduler extends Scheduler {
    final AtomicReference<ScheduledExecutorService> executor;
    final ThreadFactory threadFactory;
    static final ScheduledExecutorService SHUTDOWN = Executors.newScheduledThreadPool(0);
    private static final String THREAD_NAME_PREFIX = "RxSingleScheduler";
    private static final String KEY_SINGLE_PRIORITY = "rx2.single-priority";
    static final RxThreadFactory SINGLE_THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, Math.max(1, Math.min(10, Integer.getInteger(KEY_SINGLE_PRIORITY, 5).intValue())), true);

    static {
        SHUTDOWN.shutdown();
    }

    public SingleScheduler() {
        this(SINGLE_THREAD_FACTORY);
    }

    public SingleScheduler(ThreadFactory threadFactory) {
        this.executor = new AtomicReference<>();
        this.threadFactory = threadFactory;
        this.executor.lazySet(createExecutor(threadFactory));
    }

    static ScheduledExecutorService createExecutor(ThreadFactory threadFactory) {
        return SchedulerPoolFactory.create(threadFactory);
    }

    @Override // io.reactivex.Scheduler
    public void start() {
        ScheduledExecutorService current;
        ScheduledExecutorService next = null;
        do {
            current = this.executor.get();
            if (current != SHUTDOWN) {
                if (next != null) {
                    next.shutdown();
                    return;
                }
                return;
            } else if (next == null) {
                next = createExecutor(this.threadFactory);
            }
        } while (!this.executor.compareAndSet(current, next));
    }

    @Override // io.reactivex.Scheduler
    public void shutdown() {
        ScheduledExecutorService current;
        ScheduledExecutorService current2 = this.executor.get();
        ScheduledExecutorService scheduledExecutorService = SHUTDOWN;
        if (current2 != scheduledExecutorService && (current = this.executor.getAndSet(scheduledExecutorService)) != SHUTDOWN) {
            current.shutdownNow();
        }
    }

    @Override // io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new ScheduledWorker(this.executor.get());
    }

    @Override // io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
        Future<?> f;
        ScheduledDirectTask task = new ScheduledDirectTask(RxJavaPlugins.onSchedule(run));
        try {
            if (delay <= 0) {
                f = this.executor.get().submit(task);
            } else {
                f = this.executor.get().schedule(task, delay, unit);
            }
            task.setFuture(f);
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    @Override // io.reactivex.Scheduler
    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
        Future<?> f;
        Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
        if (period <= 0) {
            ScheduledExecutorService exec = this.executor.get();
            InstantPeriodicTask periodicWrapper = new InstantPeriodicTask(decoratedRun, exec);
            try {
                if (initialDelay <= 0) {
                    f = exec.submit(periodicWrapper);
                } else {
                    f = exec.schedule(periodicWrapper, initialDelay, unit);
                }
                periodicWrapper.setFirst(f);
                return periodicWrapper;
            } catch (RejectedExecutionException ex) {
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        } else {
            ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(decoratedRun);
            try {
                task.setFuture(this.executor.get().scheduleAtFixedRate(task, initialDelay, period, unit));
                return task;
            } catch (RejectedExecutionException ex2) {
                RxJavaPlugins.onError(ex2);
                return EmptyDisposable.INSTANCE;
            }
        }
    }

    /* loaded from: classes.dex */
    static final class ScheduledWorker extends Scheduler.Worker {
        volatile boolean disposed;
        final ScheduledExecutorService executor;
        final CompositeDisposable tasks = new CompositeDisposable();

        ScheduledWorker(ScheduledExecutorService executor) {
            this.executor = executor;
        }

        @Override // io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            Future<?> f;
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            ScheduledRunnable sr = new ScheduledRunnable(RxJavaPlugins.onSchedule(run), this.tasks);
            this.tasks.add(sr);
            try {
                if (delay <= 0) {
                    f = this.executor.submit((Callable) sr);
                } else {
                    f = this.executor.schedule((Callable) sr, delay, unit);
                }
                sr.setFuture(f);
                return sr;
            } catch (RejectedExecutionException ex) {
                dispose();
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        }

        @Override // io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.disposed) {
                this.disposed = true;
                this.tasks.dispose();
            }
        }

        @Override // io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }
    }
}
