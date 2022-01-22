package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableTimer */
/* loaded from: classes.dex */
public final class ObservableTimer extends Observable<Long> {
    final long delay;
    final Scheduler scheduler;
    final TimeUnit unit;

    public ObservableTimer(long j, TimeUnit timeUnit, Scheduler scheduler) {
        this.delay = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Long> observer) {
        TimerObserver timerObserver = new TimerObserver(observer);
        observer.onSubscribe(timerObserver);
        timerObserver.setResource(this.scheduler.scheduleDirect(timerObserver, this.delay, this.unit));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTimer$TimerObserver */
    /* loaded from: classes.dex */
    static final class TimerObserver extends AtomicReference<Disposable> implements Disposable, Runnable {
        private static final long serialVersionUID = -2809475196591179431L;
        final Observer<? super Long> downstream;

        TimerObserver(Observer<? super Long> observer) {
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
            if (!isDisposed()) {
                this.downstream.onNext(0L);
                lazySet(EmptyDisposable.INSTANCE);
                this.downstream.onComplete();
            }
        }

        public void setResource(Disposable disposable) {
            DisposableHelper.trySet(this, disposable);
        }
    }
}
