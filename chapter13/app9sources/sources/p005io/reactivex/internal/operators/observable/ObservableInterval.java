package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.schedulers.TrampolineScheduler;

/* renamed from: io.reactivex.internal.operators.observable.ObservableInterval */
/* loaded from: classes.dex */
public final class ObservableInterval extends Observable<Long> {
    final long initialDelay;
    final long period;
    final Scheduler scheduler;
    final TimeUnit unit;

    public ObservableInterval(long j, long j2, TimeUnit timeUnit, Scheduler scheduler) {
        this.initialDelay = j;
        this.period = j2;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Long> observer) {
        IntervalObserver intervalObserver = new IntervalObserver(observer);
        observer.onSubscribe(intervalObserver);
        Scheduler scheduler = this.scheduler;
        if (scheduler instanceof TrampolineScheduler) {
            Scheduler.Worker createWorker = scheduler.createWorker();
            intervalObserver.setResource(createWorker);
            createWorker.schedulePeriodically(intervalObserver, this.initialDelay, this.period, this.unit);
            return;
        }
        intervalObserver.setResource(scheduler.schedulePeriodicallyDirect(intervalObserver, this.initialDelay, this.period, this.unit));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableInterval$IntervalObserver */
    /* loaded from: classes.dex */
    static final class IntervalObserver extends AtomicReference<Disposable> implements Disposable, Runnable {
        private static final long serialVersionUID = 346773832286157679L;
        long count;
        final Observer<? super Long> downstream;

        IntervalObserver(Observer<? super Long> observer) {
            this.downstream = observer;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() == DisposableHelper.DISPOSED;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (get() != DisposableHelper.DISPOSED) {
                Observer<? super Long> observer = this.downstream;
                long j = this.count;
                this.count = 1 + j;
                observer.onNext(Long.valueOf(j));
            }
        }

        public void setResource(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }
    }
}
