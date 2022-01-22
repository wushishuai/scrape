package p005io.reactivex.internal.schedulers;

import java.util.concurrent.TimeUnit;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.disposables.Disposables;

/* renamed from: io.reactivex.internal.schedulers.ImmediateThinScheduler */
/* loaded from: classes.dex */
public final class ImmediateThinScheduler extends Scheduler {
    public static final Scheduler INSTANCE = new ImmediateThinScheduler();
    static final Scheduler.Worker WORKER = new ImmediateThinWorker();
    static final Disposable DISPOSED = Disposables.empty();

    static {
        DISPOSED.dispose();
    }

    private ImmediateThinScheduler() {
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable runnable) {
        runnable.run();
        return DISPOSED;
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable scheduleDirect(@NonNull Runnable runnable, long j, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("This scheduler doesn't support delayed execution");
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Disposable schedulePeriodicallyDirect(@NonNull Runnable runnable, long j, long j2, TimeUnit timeUnit) {
        throw new UnsupportedOperationException("This scheduler doesn't support periodic execution");
    }

    @Override // p005io.reactivex.Scheduler
    @NonNull
    public Scheduler.Worker createWorker() {
        return WORKER;
    }

    /* renamed from: io.reactivex.internal.schedulers.ImmediateThinScheduler$ImmediateThinWorker */
    /* loaded from: classes.dex */
    static final class ImmediateThinWorker extends Scheduler.Worker {
        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return false;
        }

        ImmediateThinWorker() {
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable) {
            runnable.run();
            return ImmediateThinScheduler.DISPOSED;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedule(@NonNull Runnable runnable, long j, @NonNull TimeUnit timeUnit) {
            throw new UnsupportedOperationException("This scheduler doesn't support delayed execution");
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @NonNull
        public Disposable schedulePeriodically(@NonNull Runnable runnable, long j, long j2, TimeUnit timeUnit) {
            throw new UnsupportedOperationException("This scheduler doesn't support periodic execution");
        }
    }
}
