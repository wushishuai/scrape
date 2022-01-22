package p005io.reactivex.internal.schedulers;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.disposables.ListCompositeDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport;

/* renamed from: io.reactivex.internal.schedulers.ComputationScheduler */
/* loaded from: classes.dex */
public final class ComputationScheduler extends Scheduler implements SchedulerMultiWorkerSupport {
    final AtomicReference<FixedSchedulerPool> pool;
    final ThreadFactory threadFactory;
    static final String KEY_MAX_THREADS = "rx2.computation-threads";
    static final int MAX_THREADS = cap(Runtime.getRuntime().availableProcessors(), Integer.getInteger(KEY_MAX_THREADS, 0).intValue());
    static final PoolWorker SHUTDOWN_WORKER = new PoolWorker(new RxThreadFactory("RxComputationShutdown"));
    private static final String THREAD_NAME_PREFIX = "RxComputationThreadPool";
    private static final String KEY_COMPUTATION_PRIORITY = "rx2.computation-priority";
    static final RxThreadFactory THREAD_FACTORY = new RxThreadFactory(THREAD_NAME_PREFIX, Math.max(1, Math.min(10, Integer.getInteger(KEY_COMPUTATION_PRIORITY, 5).intValue())), true);
    static final FixedSchedulerPool NONE = new FixedSchedulerPool(0, THREAD_FACTORY);

    static int cap(int i, int i2) {
        return (i2 <= 0 || i2 > i) ? i : i2;
    }

    static {
        SHUTDOWN_WORKER.dispose();
        NONE.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.ComputationScheduler$FixedSchedulerPool */
    /* loaded from: classes.dex */
    public static final class FixedSchedulerPool implements SchedulerMultiWorkerSupport {
        final int cores;
        final PoolWorker[] eventLoops;

        /* renamed from: n */
        long f177n;

        FixedSchedulerPool(int i, ThreadFactory threadFactory) {
            this.cores = i;
            this.eventLoops = new PoolWorker[i];
            for (int i2 = 0; i2 < i; i2++) {
                this.eventLoops[i2] = new PoolWorker(threadFactory);
            }
        }

        public PoolWorker getEventLoop() {
            int i = this.cores;
            if (i == 0) {
                return ComputationScheduler.SHUTDOWN_WORKER;
            }
            PoolWorker[] poolWorkerArr = this.eventLoops;
            long j = this.f177n;
            this.f177n = 1 + j;
            return poolWorkerArr[(int) (j % ((long) i))];
        }

        public void shutdown() {
            for (PoolWorker poolWorker : this.eventLoops) {
                poolWorker.dispose();
            }
        }

        @Override // p005io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport
        public void createWorkers(int i, SchedulerMultiWorkerSupport.WorkerCallback workerCallback) {
            int i2 = this.cores;
            if (i2 == 0) {
                for (int i3 = 0; i3 < i; i3++) {
                    workerCallback.onWorker(i3, ComputationScheduler.SHUTDOWN_WORKER);
                }
                return;
            }
            int i4 = ((int) this.f177n) % i2;
            for (int i5 = 0; i5 < i; i5++) {
                workerCallback.onWorker(i5, new EventLoopWorker(this.eventLoops[i4]));
                i4++;
                if (i4 == i2) {
                    i4 = 0;
                }
            }
            this.f177n = (long) i4;
        }
    }

    public ComputationScheduler() {
        this(THREAD_FACTORY);
    }

    public ComputationScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<>(NONE);
        start();
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new EventLoopWorker(this.pool.get().getEventLoop());
    }

    @Override // p005io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport
    public void createWorkers(int i, SchedulerMultiWorkerSupport.WorkerCallback workerCallback) {
        ObjectHelper.verifyPositive(i, "number > 0 required");
        this.pool.get().createWorkers(i, workerCallback);
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable runnable, long j, TimeUnit timeUnit) {
        return this.pool.get().getEventLoop().scheduleDirect(runnable, j, timeUnit);
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable runnable, long j, long j2, TimeUnit timeUnit) {
        return this.pool.get().getEventLoop().schedulePeriodicallyDirect(runnable, j, j2, timeUnit);
    }

    @Override // p005io.reactivex.Scheduler
    public void start() {
        FixedSchedulerPool fixedSchedulerPool = new FixedSchedulerPool(MAX_THREADS, this.threadFactory);
        if (!this.pool.compareAndSet(NONE, fixedSchedulerPool)) {
            fixedSchedulerPool.shutdown();
        }
    }

    @Override // p005io.reactivex.Scheduler
    public void shutdown() {
        FixedSchedulerPool fixedSchedulerPool;
        FixedSchedulerPool fixedSchedulerPool2;
        do {
            fixedSchedulerPool = this.pool.get();
            fixedSchedulerPool2 = NONE;
            if (fixedSchedulerPool == fixedSchedulerPool2) {
                return;
            }
        } while (!this.pool.compareAndSet(fixedSchedulerPool, fixedSchedulerPool2));
        fixedSchedulerPool.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.ComputationScheduler$EventLoopWorker */
    /* loaded from: classes.dex */
    public static final class EventLoopWorker extends Scheduler.Worker {
        volatile boolean disposed;
        private final PoolWorker poolWorker;
        private final ListCompositeDisposable serial = new ListCompositeDisposable();
        private final CompositeDisposable timed = new CompositeDisposable();
        private final ListCompositeDisposable both = new ListCompositeDisposable();

        EventLoopWorker(PoolWorker poolWorker) {
            this.poolWorker = poolWorker;
            this.both.add(this.serial);
            this.both.add(this.timed);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.disposed) {
                this.disposed = true;
                this.both.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            return this.poolWorker.scheduleActual(runnable, 0, TimeUnit.MILLISECONDS, this.serial);
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable, long j, @NonNull TimeUnit timeUnit) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            return this.poolWorker.scheduleActual(runnable, j, timeUnit, this.timed);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.ComputationScheduler$PoolWorker */
    /* loaded from: classes.dex */
    public static final class PoolWorker extends NewThreadWorker {
        PoolWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }
}
