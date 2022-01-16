package p005io.reactivex;

import java.util.concurrent.TimeUnit;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.schedulers.NewThreadWorker;
import p005io.reactivex.internal.schedulers.SchedulerWhen;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.SchedulerRunnableIntrospection;

/* renamed from: io.reactivex.Scheduler */
/* loaded from: classes.dex */
public abstract class Scheduler {
    static final long CLOCK_DRIFT_TOLERANCE_NANOSECONDS = TimeUnit.MINUTES.toNanos(Long.getLong("rx2.scheduler.drift-tolerance", 15).longValue());

    @NonNull
    public abstract Worker createWorker();

    public static long clockDriftTolerance() {
        return CLOCK_DRIFT_TOLERANCE_NANOSECONDS;
    }

    public long now(@NonNull TimeUnit unit) {
        return unit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public void start() {
    }

    public void shutdown() {
    }

    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run) {
        return scheduleDirect(run, 0, TimeUnit.NANOSECONDS);
    }

    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
        Worker w = createWorker();
        DisposeTask task = new DisposeTask(RxJavaPlugins.onSchedule(run), w);
        w.schedule(task, delay, unit);
        return task;
    }

    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
        Worker w = createWorker();
        PeriodicDirectTask periodicTask = new PeriodicDirectTask(RxJavaPlugins.onSchedule(run), w);
        Disposable d = w.schedulePeriodically(periodicTask, initialDelay, period, unit);
        if (d == EmptyDisposable.INSTANCE) {
            return d;
        }
        return periodicTask;
    }

    @NonNull
    public <S extends Scheduler & Disposable> S when(@NonNull Function<Flowable<Flowable<Completable>>, Completable> combine) {
        return new SchedulerWhen(combine, this);
    }

    /* renamed from: io.reactivex.Scheduler$Worker */
    /* loaded from: classes.dex */
    public static abstract class Worker implements Disposable {
        @NonNull
        public abstract Disposable schedule(@NonNull Runnable runnable, long j, @NonNull TimeUnit timeUnit);

        @NonNull
        public Disposable schedule(@NonNull Runnable run) {
            return schedule(run, 0, TimeUnit.NANOSECONDS);
        }

        @NonNull
        public Disposable schedulePeriodically(@NonNull Runnable run, long initialDelay, long period, @NonNull TimeUnit unit) {
            SequentialDisposable first = new SequentialDisposable();
            SequentialDisposable sd = new SequentialDisposable(first);
            Runnable decoratedRun = RxJavaPlugins.onSchedule(run);
            long periodInNanoseconds = unit.toNanos(period);
            long firstNowNanoseconds = now(TimeUnit.NANOSECONDS);
            Disposable d = schedule(new PeriodicTask(firstNowNanoseconds + unit.toNanos(initialDelay), decoratedRun, firstNowNanoseconds, sd, periodInNanoseconds), initialDelay, unit);
            if (d == EmptyDisposable.INSTANCE) {
                return d;
            }
            first.replace(d);
            return sd;
        }

        public long now(@NonNull TimeUnit unit) {
            return unit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        /* renamed from: io.reactivex.Scheduler$Worker$PeriodicTask */
        /* loaded from: classes.dex */
        public final class PeriodicTask implements Runnable, SchedulerRunnableIntrospection {
            long count;
            @NonNull
            final Runnable decoratedRun;
            long lastNowNanoseconds;
            final long periodInNanoseconds;
            @NonNull

            /* renamed from: sd */
            final SequentialDisposable f89sd;
            long startInNanoseconds;

            PeriodicTask(long firstStartInNanoseconds, @NonNull Runnable decoratedRun, long firstNowNanoseconds, @NonNull SequentialDisposable sd, long periodInNanoseconds) {
                Worker.this = this$0;
                this.decoratedRun = decoratedRun;
                this.f89sd = sd;
                this.periodInNanoseconds = periodInNanoseconds;
                this.lastNowNanoseconds = firstNowNanoseconds;
                this.startInNanoseconds = firstStartInNanoseconds;
            }

            @Override // java.lang.Runnable
            public void run() {
                long nextTick;
                this.decoratedRun.run();
                if (!this.f89sd.isDisposed()) {
                    long nowNanoseconds = Worker.this.now(TimeUnit.NANOSECONDS);
                    long j = this.lastNowNanoseconds;
                    if (Scheduler.CLOCK_DRIFT_TOLERANCE_NANOSECONDS + nowNanoseconds < j || nowNanoseconds >= j + this.periodInNanoseconds + Scheduler.CLOCK_DRIFT_TOLERANCE_NANOSECONDS) {
                        long j2 = this.periodInNanoseconds;
                        long nextTick2 = nowNanoseconds + j2;
                        long j3 = this.count + 1;
                        this.count = j3;
                        this.startInNanoseconds = nextTick2 - (j2 * j3);
                        nextTick = nextTick2;
                    } else {
                        long j4 = this.startInNanoseconds;
                        long j5 = this.count + 1;
                        this.count = j5;
                        nextTick = j4 + (j5 * this.periodInNanoseconds);
                    }
                    this.lastNowNanoseconds = nowNanoseconds;
                    this.f89sd.replace(Worker.this.schedule(this, nextTick - nowNanoseconds, TimeUnit.NANOSECONDS));
                }
            }

            @Override // p005io.reactivex.schedulers.SchedulerRunnableIntrospection
            public Runnable getWrappedRunnable() {
                return this.decoratedRun;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.Scheduler$PeriodicDirectTask */
    /* loaded from: classes.dex */
    public static final class PeriodicDirectTask implements Disposable, Runnable, SchedulerRunnableIntrospection {
        volatile boolean disposed;
        @NonNull
        final Runnable run;
        @NonNull
        final Worker worker;

        PeriodicDirectTask(@NonNull Runnable run, @NonNull Worker worker) {
            this.run = run;
            this.worker = worker;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (!this.disposed) {
                try {
                    this.run.run();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    this.worker.dispose();
                    throw ExceptionHelper.wrapOrThrow(ex);
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
            this.worker.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        @Override // p005io.reactivex.schedulers.SchedulerRunnableIntrospection
        public Runnable getWrappedRunnable() {
            return this.run;
        }
    }

    /* renamed from: io.reactivex.Scheduler$DisposeTask */
    /* loaded from: classes.dex */
    public static final class DisposeTask implements Disposable, Runnable, SchedulerRunnableIntrospection {
        @NonNull
        final Runnable decoratedRun;
        @Nullable
        Thread runner;
        @NonNull

        /* renamed from: w */
        final Worker f88w;

        DisposeTask(@NonNull Runnable decoratedRun, @NonNull Worker w) {
            this.decoratedRun = decoratedRun;
            this.f88w = w;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.runner = Thread.currentThread();
            try {
                this.decoratedRun.run();
            } finally {
                dispose();
                this.runner = null;
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (this.runner == Thread.currentThread()) {
                Worker worker = this.f88w;
                if (worker instanceof NewThreadWorker) {
                    ((NewThreadWorker) worker).shutdown();
                    return;
                }
            }
            this.f88w.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.f88w.isDisposed();
        }

        @Override // p005io.reactivex.schedulers.SchedulerRunnableIntrospection
        public Runnable getWrappedRunnable() {
            return this.decoratedRun;
        }
    }
}
