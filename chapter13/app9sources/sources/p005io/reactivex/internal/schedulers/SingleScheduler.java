package p005io.reactivex.internal.schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.schedulers.SingleScheduler */
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

    @Override // p005io.reactivex.Scheduler
    public void start() {
        ScheduledExecutorService scheduledExecutorService;
        ScheduledExecutorService scheduledExecutorService2 = null;
        do {
            scheduledExecutorService = this.executor.get();
            if (scheduledExecutorService != SHUTDOWN) {
                if (scheduledExecutorService2 != null) {
                    scheduledExecutorService2.shutdown();
                    return;
                }
                return;
            } else if (scheduledExecutorService2 == null) {
                scheduledExecutorService2 = createExecutor(this.threadFactory);
            }
        } while (!this.executor.compareAndSet(scheduledExecutorService, scheduledExecutorService2));
    }

    @Override // p005io.reactivex.Scheduler
    public void shutdown() {
        ScheduledExecutorService andSet;
        ScheduledExecutorService scheduledExecutorService = this.executor.get();
        ScheduledExecutorService scheduledExecutorService2 = SHUTDOWN;
        if (scheduledExecutorService != scheduledExecutorService2 && (andSet = this.executor.getAndSet(scheduledExecutorService2)) != SHUTDOWN) {
            andSet.shutdownNow();
        }
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new ScheduledWorker(this.executor.get());
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable runnable, long j, TimeUnit timeUnit) {
        Future<?> future;
        ScheduledDirectTask scheduledDirectTask = new ScheduledDirectTask(RxJavaPlugins.onSchedule(runnable));
        try {
            if (j <= 0) {
                future = this.executor.get().submit(scheduledDirectTask);
            } else {
                future = this.executor.get().schedule(scheduledDirectTask, j, timeUnit);
            }
            scheduledDirectTask.setFuture(future);
            return scheduledDirectTask;
        } catch (RejectedExecutionException e) {
            RxJavaPlugins.onError(e);
            return EmptyDisposable.INSTANCE;
        }
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable runnable, long j, long j2, TimeUnit timeUnit) {
        Future<?> future;
        Runnable onSchedule = RxJavaPlugins.onSchedule(runnable);
        if (j2 <= 0) {
            ScheduledExecutorService scheduledExecutorService = this.executor.get();
            InstantPeriodicTask instantPeriodicTask = new InstantPeriodicTask(onSchedule, scheduledExecutorService);
            try {
                if (j <= 0) {
                    future = scheduledExecutorService.submit(instantPeriodicTask);
                } else {
                    future = scheduledExecutorService.schedule(instantPeriodicTask, j, timeUnit);
                }
                instantPeriodicTask.setFirst(future);
                return instantPeriodicTask;
            } catch (RejectedExecutionException e) {
                RxJavaPlugins.onError(e);
                return EmptyDisposable.INSTANCE;
            }
        } else {
            ScheduledDirectPeriodicTask scheduledDirectPeriodicTask = new ScheduledDirectPeriodicTask(onSchedule);
            try {
                scheduledDirectPeriodicTask.setFuture(this.executor.get().scheduleAtFixedRate(scheduledDirectPeriodicTask, j, j2, timeUnit));
                return scheduledDirectPeriodicTask;
            } catch (RejectedExecutionException e2) {
                RxJavaPlugins.onError(e2);
                return EmptyDisposable.INSTANCE;
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.SingleScheduler$ScheduledWorker */
    /* loaded from: classes.dex */
    static final class ScheduledWorker extends Scheduler.Worker {
        volatile boolean disposed;
        final ScheduledExecutorService executor;
        final CompositeDisposable tasks = new CompositeDisposable();

        ScheduledWorker(ScheduledExecutorService scheduledExecutorService) {
            this.executor = scheduledExecutorService;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable, long j, @NonNull TimeUnit timeUnit) {
            Future<?> future;
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            ScheduledRunnable scheduledRunnable = new ScheduledRunnable(RxJavaPlugins.onSchedule(runnable), this.tasks);
            this.tasks.add(scheduledRunnable);
            try {
                if (j <= 0) {
                    future = this.executor.submit((Callable) scheduledRunnable);
                } else {
                    future = this.executor.schedule((Callable) scheduledRunnable, j, timeUnit);
                }
                scheduledRunnable.setFuture(future);
                return scheduledRunnable;
            } catch (RejectedExecutionException e) {
                dispose();
                RxJavaPlugins.onError(e);
                return EmptyDisposable.INSTANCE;
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.disposed) {
                this.disposed = true;
                this.tasks.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }
    }
}
