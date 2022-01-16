package p005io.reactivex.internal.schedulers;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.SchedulerRunnableIntrospection;
import p005io.reactivex.schedulers.Schedulers;

/* renamed from: io.reactivex.internal.schedulers.ExecutorScheduler */
/* loaded from: classes.dex */
public final class ExecutorScheduler extends Scheduler {
    static final Scheduler HELPER = Schedulers.single();
    @NonNull
    final Executor executor;

    public ExecutorScheduler(@NonNull Executor executor) {
        this.executor = executor;
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return new ExecutorWorker(this.executor);
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run) {
        Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
        try {
            if (this.executor instanceof ExecutorService) {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                task.setFuture(((ExecutorService) this.executor).submit(task));
                return task;
            }
            ExecutorWorker.BooleanRunnable br = new ExecutorWorker.BooleanRunnable(decoratedRun);
            this.executor.execute(br);
            return br;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
        Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
        if (this.executor instanceof ScheduledExecutorService) {
            try {
                ScheduledDirectTask task = new ScheduledDirectTask(decoratedRun);
                task.setFuture(((ScheduledExecutorService) this.executor).schedule(task, delay, unit));
                return task;
            } catch (RejectedExecutionException ex) {
                RxJavaPlugins.onError(ex);
                return EmptyDisposable.INSTANCE;
            }
        } else {
            DelayedRunnable dr = new DelayedRunnable(decoratedRun);
            dr.timed.replace(HELPER.scheduleDirect(new DelayedDispose(dr), delay, unit));
            return dr;
        }
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, TimeUnit unit) {
        if (!(this.executor instanceof ScheduledExecutorService)) {
            return super.schedulePeriodicallyDirect(run, initialDelay, period, unit);
        }
        try {
            ScheduledDirectPeriodicTask task = new ScheduledDirectPeriodicTask(RxJavaPlugins.onSchedule(run));
            task.setFuture(((ScheduledExecutorService) this.executor).scheduleAtFixedRate(task, initialDelay, period, unit));
            return task;
        } catch (RejectedExecutionException ex) {
            RxJavaPlugins.onError(ex);
            return EmptyDisposable.INSTANCE;
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.ExecutorScheduler$ExecutorWorker */
    /* loaded from: classes.dex */
    public static final class ExecutorWorker extends Scheduler.Worker implements Runnable {
        volatile boolean disposed;
        final Executor executor;
        final AtomicInteger wip = new AtomicInteger();
        final CompositeDisposable tasks = new CompositeDisposable();
        final MpscLinkedQueue<Runnable> queue = new MpscLinkedQueue<>();

        public ExecutorWorker(Executor executor) {
            this.executor = executor;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable run) {
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            BooleanRunnable br = new BooleanRunnable(RxJavaPlugins.onSchedule(run));
            this.queue.offer(br);
            if (this.wip.getAndIncrement() == 0) {
                try {
                    this.executor.execute(this);
                } catch (RejectedExecutionException ex) {
                    this.disposed = true;
                    this.queue.clear();
                    RxJavaPlugins.onError(ex);
                    return EmptyDisposable.INSTANCE;
                }
            }
            return br;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            if (delay <= 0) {
                return schedule(run);
            }
            if (this.disposed) {
                return EmptyDisposable.INSTANCE;
            }
            SequentialDisposable first = new SequentialDisposable();
            SequentialDisposable mar = new SequentialDisposable(first);
            ScheduledRunnable sr = new ScheduledRunnable(new SequentialDispose(mar, RxJavaPlugins.onSchedule(run)), this.tasks);
            this.tasks.add(sr);
            Executor executor = this.executor;
            if (executor instanceof ScheduledExecutorService) {
                try {
                    sr.setFuture(((ScheduledExecutorService) executor).schedule((Callable) sr, delay, unit));
                } catch (RejectedExecutionException ex) {
                    this.disposed = true;
                    RxJavaPlugins.onError(ex);
                    return EmptyDisposable.INSTANCE;
                }
            } else {
                sr.setFuture(new DisposeOnCancel(ExecutorScheduler.HELPER.scheduleDirect(sr, delay, unit)));
            }
            first.replace(sr);
            return mar;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.disposed) {
                this.disposed = true;
                this.tasks.dispose();
                if (this.wip.getAndIncrement() == 0) {
                    this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        /* JADX WARN: Code restructure failed: missing block: B:10:0x0016, code lost:
            if (r4.disposed == false) goto L_0x001c;
         */
        /* JADX WARN: Code restructure failed: missing block: B:11:0x0018, code lost:
            r1.clear();
         */
        /* JADX WARN: Code restructure failed: missing block: B:12:0x001b, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:13:0x001c, code lost:
            r0 = r4.wip.addAndGet(-r0);
         */
        /* JADX WARN: Code restructure failed: missing block: B:14:0x0023, code lost:
            if (r0 != 0) goto L_0x0003;
         */
        /* JADX WARN: Code restructure failed: missing block: B:15:0x0026, code lost:
            return;
         */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void run() {
            /*
                r4 = this;
                r0 = 1
                io.reactivex.internal.queue.MpscLinkedQueue<java.lang.Runnable> r1 = r4.queue
            L_0x0003:
                boolean r2 = r4.disposed
                if (r2 == 0) goto L_0x000b
                r1.clear()
                return
            L_0x000b:
                java.lang.Object r2 = r1.poll()
                java.lang.Runnable r2 = (java.lang.Runnable) r2
                if (r2 != 0) goto L_0x0027
                boolean r2 = r4.disposed
                if (r2 == 0) goto L_0x001c
                r1.clear()
                return
            L_0x001c:
                java.util.concurrent.atomic.AtomicInteger r2 = r4.wip
                int r3 = -r0
                int r0 = r2.addAndGet(r3)
                if (r0 != 0) goto L_0x0003
                return
            L_0x0027:
                r2.run()
                boolean r3 = r4.disposed
                if (r3 == 0) goto L_0x0032
                r1.clear()
                return
            L_0x0032:
                goto L_0x000b
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.schedulers.ExecutorScheduler.ExecutorWorker.run():void");
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.schedulers.ExecutorScheduler$ExecutorWorker$BooleanRunnable */
        /* loaded from: classes.dex */
        public static final class BooleanRunnable extends AtomicBoolean implements Runnable, Disposable {
            private static final long serialVersionUID = -2421395018820541164L;
            final Runnable actual;

            BooleanRunnable(Runnable actual) {
                this.actual = actual;
            }

            @Override // java.lang.Runnable
            public void run() {
                if (!get()) {
                    try {
                        this.actual.run();
                    } finally {
                        lazySet(true);
                    }
                }
            }

            @Override // p005io.reactivex.disposables.Disposable
            public void dispose() {
                lazySet(true);
            }

            @Override // p005io.reactivex.disposables.Disposable
            public boolean isDisposed() {
                return get();
            }
        }

        /* renamed from: io.reactivex.internal.schedulers.ExecutorScheduler$ExecutorWorker$SequentialDispose */
        /* loaded from: classes.dex */
        final class SequentialDispose implements Runnable {
            private final Runnable decoratedRun;
            private final SequentialDisposable mar;

            SequentialDispose(SequentialDisposable mar, Runnable decoratedRun) {
                this.mar = mar;
                this.decoratedRun = decoratedRun;
            }

            @Override // java.lang.Runnable
            public void run() {
                this.mar.replace(ExecutorWorker.this.schedule(this.decoratedRun));
            }
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.ExecutorScheduler$DelayedRunnable */
    /* loaded from: classes.dex */
    static final class DelayedRunnable extends AtomicReference<Runnable> implements Runnable, Disposable, SchedulerRunnableIntrospection {
        private static final long serialVersionUID = -4101336210206799084L;
        final SequentialDisposable timed = new SequentialDisposable();
        final SequentialDisposable direct = new SequentialDisposable();

        DelayedRunnable(Runnable run) {
            super(run);
        }

        @Override // java.lang.Runnable
        public void run() {
            Runnable r = get();
            if (r != null) {
                try {
                    r.run();
                } finally {
                    lazySet(null);
                    this.timed.lazySet(DisposableHelper.DISPOSED);
                    this.direct.lazySet(DisposableHelper.DISPOSED);
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() == null;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (getAndSet(null) != null) {
                this.timed.dispose();
                this.direct.dispose();
            }
        }

        @Override // p005io.reactivex.schedulers.SchedulerRunnableIntrospection
        public Runnable getWrappedRunnable() {
            Runnable r = get();
            return r != null ? r : Functions.EMPTY_RUNNABLE;
        }
    }

    /* renamed from: io.reactivex.internal.schedulers.ExecutorScheduler$DelayedDispose */
    /* loaded from: classes.dex */
    final class DelayedDispose implements Runnable {

        /* renamed from: dr */
        private final DelayedRunnable f178dr;

        DelayedDispose(DelayedRunnable dr) {
            this.f178dr = dr;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.f178dr.direct.replace(ExecutorScheduler.this.scheduleDirect(this.f178dr));
        }
    }
}
