package p005io.reactivex.android.schedulers;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.disposables.Disposables;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.android.schedulers.HandlerScheduler */
/* loaded from: classes.dex */
final class HandlerScheduler extends Scheduler {
    private final boolean async;
    private final Handler handler;

    /* JADX INFO: Access modifiers changed from: package-private */
    public HandlerScheduler(Handler handler, boolean z) {
        this.handler = handler;
        this.async = z;
    }

    @Override // p005io.reactivex.Scheduler
    public Disposable scheduleDirect(Runnable runnable, long j, TimeUnit timeUnit) {
        if (runnable == null) {
            throw new NullPointerException("run == null");
        } else if (timeUnit != null) {
            ScheduledRunnable scheduledRunnable = new ScheduledRunnable(this.handler, RxJavaPlugins.onSchedule(runnable));
            this.handler.postDelayed(scheduledRunnable, timeUnit.toMillis(j));
            return scheduledRunnable;
        } else {
            throw new NullPointerException("unit == null");
        }
    }

    @Override // p005io.reactivex.Scheduler
    public Scheduler.Worker createWorker() {
        return new HandlerWorker(this.handler, this.async);
    }

    /* renamed from: io.reactivex.android.schedulers.HandlerScheduler$HandlerWorker */
    /* loaded from: classes.dex */
    private static final class HandlerWorker extends Scheduler.Worker {
        private final boolean async;
        private volatile boolean disposed;
        private final Handler handler;

        HandlerWorker(Handler handler, boolean z) {
            this.handler = handler;
            this.async = z;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @SuppressLint({"NewApi"})
        public Disposable schedule(Runnable runnable, long j, TimeUnit timeUnit) {
            if (runnable == null) {
                throw new NullPointerException("run == null");
            } else if (timeUnit == null) {
                throw new NullPointerException("unit == null");
            } else if (this.disposed) {
                return Disposables.disposed();
            } else {
                ScheduledRunnable scheduledRunnable = new ScheduledRunnable(this.handler, RxJavaPlugins.onSchedule(runnable));
                Message obtain = Message.obtain(this.handler, scheduledRunnable);
                obtain.obj = this;
                if (this.async) {
                    obtain.setAsynchronous(true);
                }
                this.handler.sendMessageDelayed(obtain, timeUnit.toMillis(j));
                if (!this.disposed) {
                    return scheduledRunnable;
                }
                this.handler.removeCallbacks(scheduledRunnable);
                return Disposables.disposed();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
            this.handler.removeCallbacksAndMessages(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }
    }

    /* renamed from: io.reactivex.android.schedulers.HandlerScheduler$ScheduledRunnable */
    /* loaded from: classes.dex */
    private static final class ScheduledRunnable implements Runnable, Disposable {
        private final Runnable delegate;
        private volatile boolean disposed;
        private final Handler handler;

        ScheduledRunnable(Handler handler, Runnable runnable) {
            this.handler = handler;
            this.delegate = runnable;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.delegate.run();
            } catch (Throwable th) {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.handler.removeCallbacks(this);
            this.disposed = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }
    }
}
