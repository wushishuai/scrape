package p005io.reactivex.internal.schedulers;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.schedulers.IoScheduler */
/* loaded from: classes.dex */
public final class IoScheduler extends Scheduler {
    static final RxThreadFactory EVICTOR_THREAD_FACTORY;
    private static final String EVICTOR_THREAD_NAME_PREFIX = "RxCachedWorkerPoolEvictor";
    private static final long KEEP_ALIVE_TIME = 60;
    private static final String KEY_IO_PRIORITY = "rx2.io-priority";
    static final CachedWorkerPool NONE;
    static final RxThreadFactory WORKER_THREAD_FACTORY;
    private static final String WORKER_THREAD_NAME_PREFIX = "RxCachedThreadScheduler";
    final AtomicReference<CachedWorkerPool> pool;
    final ThreadFactory threadFactory;
    private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.SECONDS;
    static final ThreadWorker SHUTDOWN_THREAD_WORKER = new ThreadWorker(new RxThreadFactory("RxCachedThreadSchedulerShutdown"));

    static {
        SHUTDOWN_THREAD_WORKER.dispose();
        int max = Math.max(1, Math.min(10, Integer.getInteger(KEY_IO_PRIORITY, 5).intValue()));
        WORKER_THREAD_FACTORY = new RxThreadFactory(WORKER_THREAD_NAME_PREFIX, max);
        EVICTOR_THREAD_FACTORY = new RxThreadFactory(EVICTOR_THREAD_NAME_PREFIX, max);
        NONE = new CachedWorkerPool(0, null, WORKER_THREAD_FACTORY);
        NONE.shutdown();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.IoScheduler$CachedWorkerPool */
    /* loaded from: classes.dex */
    public static final class CachedWorkerPool implements Runnable {
        final CompositeDisposable allWorkers;
        private final ScheduledExecutorService evictorService;
        private final Future<?> evictorTask;
        private final ConcurrentLinkedQueue<ThreadWorker> expiringWorkerQueue;
        private final long keepAliveTime;
        private final ThreadFactory threadFactory;

        CachedWorkerPool(long j, TimeUnit timeUnit, ThreadFactory threadFactory) {
            ScheduledFuture<?> scheduledFuture;
            this.keepAliveTime = timeUnit != null ? timeUnit.toNanos(j) : 0;
            this.expiringWorkerQueue = new ConcurrentLinkedQueue<>();
            this.allWorkers = new CompositeDisposable();
            this.threadFactory = threadFactory;
            ScheduledExecutorService scheduledExecutorService = null;
            if (timeUnit != null) {
                scheduledExecutorService = Executors.newScheduledThreadPool(1, IoScheduler.EVICTOR_THREAD_FACTORY);
                long j2 = this.keepAliveTime;
                scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(this, j2, j2, TimeUnit.NANOSECONDS);
            } else {
                scheduledFuture = null;
            }
            this.evictorService = scheduledExecutorService;
            this.evictorTask = scheduledFuture;
        }

        @Override // java.lang.Runnable
        public void run() {
            evictExpiredWorkers();
        }

        ThreadWorker get() {
            if (this.allWorkers.isDisposed()) {
                return IoScheduler.SHUTDOWN_THREAD_WORKER;
            }
            while (!this.expiringWorkerQueue.isEmpty()) {
                ThreadWorker poll = this.expiringWorkerQueue.poll();
                if (poll != null) {
                    return poll;
                }
            }
            ThreadWorker threadWorker = new ThreadWorker(this.threadFactory);
            this.allWorkers.add(threadWorker);
            return threadWorker;
        }

        void release(ThreadWorker threadWorker) {
            threadWorker.setExpirationTime(now() + this.keepAliveTime);
            this.expiringWorkerQueue.offer(threadWorker);
        }

        void evictExpiredWorkers() {
            if (!this.expiringWorkerQueue.isEmpty()) {
                long now = now();
                Iterator<ThreadWorker> it = this.expiringWorkerQueue.iterator();
                while (it.hasNext()) {
                    ThreadWorker next = it.next();
                    if (next.getExpirationTime() > now) {
                        return;
                    }
                    if (this.expiringWorkerQueue.remove(next)) {
                        this.allWorkers.remove(next);
                    }
                }
            }
        }

        long now() {
            return System.nanoTime();
        }

        void shutdown() {
            this.allWorkers.dispose();
            Future<?> future = this.evictorTask;
            if (future != null) {
                future.cancel(true);
            }
            ScheduledExecutorService scheduledExecutorService = this.evictorService;
            if (scheduledExecutorService != null) {
                scheduledExecutorService.shutdownNow();
            }
        }
    }

    public IoScheduler() {
        this(WORKER_THREAD_FACTORY);
    }

    public IoScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<>(NONE);
        start();
    }

    @Override // p005io.reactivex.Scheduler
    public void start() {
        CachedWorkerPool cachedWorkerPool = new CachedWorkerPool(KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT, this.threadFactory);
        if (!this.pool.compareAndSet(NONE, cachedWorkerPool)) {
            cachedWorkerPool.shutdown();
        }
    }

    @Override // p005io.reactivex.Scheduler
    public void shutdown() {
        CachedWorkerPool cachedWorkerPool;
        CachedWorkerPool cachedWorkerPool2;
        do {
            cachedWorkerPool = this.pool.get();
            cachedWorkerPool2 = NONE;
            if (cachedWorkerPool == cachedWorkerPool2) {
                return;
            }
        } while (!this.pool.compareAndSet(cachedWorkerPool, cachedWorkerPool2));
        cachedWorkerPool.shutdown();
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new EventLoopWorker(this.pool.get());
    }

    public int size() {
        return this.pool.get().allWorkers.size();
    }

    /* renamed from: io.reactivex.internal.schedulers.IoScheduler$EventLoopWorker */
    /* loaded from: classes.dex */
    static final class EventLoopWorker extends Scheduler.Worker {
        private final CachedWorkerPool pool;
        private final ThreadWorker threadWorker;
        final AtomicBoolean once = new AtomicBoolean();
        private final CompositeDisposable tasks = new CompositeDisposable();

        EventLoopWorker(CachedWorkerPool cachedWorkerPool) {
            this.pool = cachedWorkerPool;
            this.threadWorker = cachedWorkerPool.get();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (this.once.compareAndSet(false, true)) {
                this.tasks.dispose();
                this.pool.release(this.threadWorker);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.once.get();
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable, long j, @NonNull TimeUnit timeUnit) {
            if (this.tasks.isDisposed()) {
                return EmptyDisposable.INSTANCE;
            }
            return this.threadWorker.scheduleActual(runnable, j, timeUnit, this.tasks);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.schedulers.IoScheduler$ThreadWorker */
    /* loaded from: classes.dex */
    public static final class ThreadWorker extends NewThreadWorker {
        private long expirationTime = 0;

        ThreadWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }

        public long getExpirationTime() {
            return this.expirationTime;
        }

        public void setExpirationTime(long j) {
            this.expirationTime = j;
        }
    }
}
