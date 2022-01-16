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
    public HandlerScheduler(Handler handler, boolean async) {
        this.handler = handler;
        this.async = async;
    }

    @Override // p005io.reactivex.Scheduler
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        if (run == null) {
            throw new NullPointerException("run == null");
        } else if (unit != null) {
            ScheduledRunnable scheduled = new ScheduledRunnable(this.handler, RxJavaPlugins.onSchedule(run));
            this.handler.postDelayed(scheduled, unit.toMillis(delay));
            return scheduled;
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

        HandlerWorker(Handler handler, boolean async) {
            this.handler = handler;
            this.async = async;
        }

        @Override // p005io.reactivex.Scheduler.Worker
        @SuppressLint({"NewApi"})
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (run == null) {
                throw new NullPointerException("run == null");
            } else if (unit == null) {
                throw new NullPointerException("unit == null");
            } else if (this.disposed) {
                return Disposables.disposed();
            } else {
                ScheduledRunnable scheduled = new ScheduledRunnable(this.handler, RxJavaPlugins.onSchedule(run));
                Message message = Message.obtain(this.handler, scheduled);
                message.obj = this;
                if (this.async) {
                    message.setAsynchronous(true);
                }
                this.handler.sendMessageDelayed(message, unit.toMillis(delay));
                if (!this.disposed) {
                    return scheduled;
                }
                this.handler.removeCallbacks(scheduled);
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

        ScheduledRunnable(Handler handler, Runnable delegate) {
            this.handler = handler;
            this.delegate = delegate;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.delegate.run();
            } catch (Throwable t) {
                RxJavaPlugins.onError(t);
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
