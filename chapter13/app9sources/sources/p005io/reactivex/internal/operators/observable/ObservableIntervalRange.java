package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.schedulers.TrampolineScheduler;

/* renamed from: io.reactivex.internal.operators.observable.ObservableIntervalRange */
/* loaded from: classes.dex */
public final class ObservableIntervalRange extends Observable<Long> {
    final long end;
    final long initialDelay;
    final long period;
    final Scheduler scheduler;
    final long start;
    final TimeUnit unit;

    public ObservableIntervalRange(long j, long j2, long j3, long j4, TimeUnit timeUnit, Scheduler scheduler) {
        this.initialDelay = j3;
        this.period = j4;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.start = j;
        this.end = j2;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Long> observer) {
        IntervalRangeObserver intervalRangeObserver = new IntervalRangeObserver(observer, this.start, this.end);
        observer.onSubscribe(intervalRangeObserver);
        Scheduler scheduler = this.scheduler;
        if (scheduler instanceof TrampolineScheduler) {
            Scheduler.Worker createWorker = scheduler.createWorker();
            intervalRangeObserver.setResource(createWorker);
            createWorker.schedulePeriodically(intervalRangeObserver, this.initialDelay, this.period, this.unit);
            return;
        }
        intervalRangeObserver.setResource(scheduler.schedulePeriodicallyDirect(intervalRangeObserver, this.initialDelay, this.period, this.unit));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableIntervalRange$IntervalRangeObserver */
    /* loaded from: classes.dex */
    static final class IntervalRangeObserver extends AtomicReference<Disposable> implements Disposable, Runnable {
        private static final long serialVersionUID = 1891866368734007884L;
        long count;
        final Observer<? super Long> downstream;
        final long end;

        IntervalRangeObserver(Observer<? super Long> observer, long j, long j2) {
            this.downstream = observer;
            this.count = j;
            this.end = j2;
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
            if (!isDisposed()) {
                long j = this.count;
                this.downstream.onNext(Long.valueOf(j));
                if (j == this.end) {
                    DisposableHelper.dispose(this);
                    this.downstream.onComplete();
                    return;
                }
                this.count = j + 1;
            }
        }

        public void setResource(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }
    }
}
