package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableThrottleLatest */
/* loaded from: classes.dex */
public final class ObservableThrottleLatest<T> extends AbstractObservableWithUpstream<T, T> {
    final boolean emitLast;
    final Scheduler scheduler;
    final long timeout;
    final TimeUnit unit;

    public ObservableThrottleLatest(Observable<T> observable, long j, TimeUnit timeUnit, Scheduler scheduler, boolean z) {
        super(observable);
        this.timeout = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.emitLast = z;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new ThrottleLatestObserver(observer, this.timeout, this.unit, this.scheduler.createWorker(), this.emitLast));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableThrottleLatest$ThrottleLatestObserver */
    /* loaded from: classes.dex */
    static final class ThrottleLatestObserver<T> extends AtomicInteger implements Observer<T>, Disposable, Runnable {
        private static final long serialVersionUID = -8296689127439125014L;
        volatile boolean cancelled;
        volatile boolean done;
        final Observer<? super T> downstream;
        final boolean emitLast;
        Throwable error;
        final AtomicReference<T> latest = new AtomicReference<>();
        final long timeout;
        volatile boolean timerFired;
        boolean timerRunning;
        final TimeUnit unit;
        Disposable upstream;
        final Scheduler.Worker worker;

        ThrottleLatestObserver(Observer<? super T> observer, long j, TimeUnit timeUnit, Scheduler.Worker worker, boolean z) {
            this.downstream = observer;
            this.timeout = j;
            this.unit = timeUnit;
            this.worker = worker;
            this.emitLast = z;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.latest.set(t);
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.error = th;
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.cancelled = true;
            this.upstream.dispose();
            this.worker.dispose();
            if (getAndIncrement() == 0) {
                this.latest.lazySet(null);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.timerFired = true;
            drain();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                AtomicReference<T> atomicReference = this.latest;
                Observer<? super T> observer = this.downstream;
                int i = 1;
                while (!this.cancelled) {
                    boolean z = this.done;
                    if (!z || this.error == null) {
                        boolean z2 = atomicReference.get() == null;
                        if (z) {
                            T andSet = atomicReference.getAndSet(null);
                            if (!z2 && this.emitLast) {
                                observer.onNext(andSet);
                            }
                            observer.onComplete();
                            this.worker.dispose();
                            return;
                        }
                        if (z2) {
                            if (this.timerFired) {
                                this.timerRunning = false;
                                this.timerFired = false;
                            }
                        } else if (!this.timerRunning || this.timerFired) {
                            observer.onNext(atomicReference.getAndSet(null));
                            this.timerFired = false;
                            this.timerRunning = true;
                            this.worker.schedule(this, this.timeout, this.unit);
                        }
                        i = addAndGet(-i);
                        if (i == 0) {
                            return;
                        }
                    } else {
                        atomicReference.lazySet(null);
                        observer.onError(this.error);
                        this.worker.dispose();
                        return;
                    }
                }
                atomicReference.lazySet(null);
            }
        }
    }
}
