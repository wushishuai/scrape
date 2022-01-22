package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.TimeUnit;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.observers.SerializedObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableDelay */
/* loaded from: classes.dex */
public final class ObservableDelay<T> extends AbstractObservableWithUpstream<T, T> {
    final long delay;
    final boolean delayError;
    final Scheduler scheduler;
    final TimeUnit unit;

    public ObservableDelay(ObservableSource<T> observableSource, long j, TimeUnit timeUnit, Scheduler scheduler, boolean z) {
        super(observableSource);
        this.delay = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.delayError = z;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        Observer<? super T> observer2;
        if (this.delayError) {
            observer2 = observer;
        } else {
            observer2 = new SerializedObserver<>(observer);
        }
        this.source.subscribe(new DelayObserver(observer2, this.delay, this.unit, this.scheduler.createWorker(), this.delayError));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableDelay$DelayObserver */
    /* loaded from: classes.dex */
    static final class DelayObserver<T> implements Observer<T>, Disposable {
        final long delay;
        final boolean delayError;
        final Observer<? super T> downstream;
        final TimeUnit unit;
        Disposable upstream;

        /* renamed from: w */
        final Scheduler.Worker f150w;

        DelayObserver(Observer<? super T> observer, long j, TimeUnit timeUnit, Scheduler.Worker worker, boolean z) {
            this.downstream = observer;
            this.delay = j;
            this.unit = timeUnit;
            this.f150w = worker;
            this.delayError = z;
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
            this.f150w.schedule(new OnNext(t), this.delay, this.unit);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.f150w.schedule(new OnError(th), this.delayError ? this.delay : 0, this.unit);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.f150w.schedule(new OnComplete(), this.delay, this.unit);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            this.f150w.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.f150w.isDisposed();
        }

        /* renamed from: io.reactivex.internal.operators.observable.ObservableDelay$DelayObserver$OnNext */
        /* loaded from: classes.dex */
        final class OnNext implements Runnable {

            /* renamed from: t */
            private final T f151t;

            OnNext(T t) {
                this.f151t = t;
            }

            @Override // java.lang.Runnable
            public void run() {
                DelayObserver.this.downstream.onNext((T) this.f151t);
            }
        }

        /* renamed from: io.reactivex.internal.operators.observable.ObservableDelay$DelayObserver$OnError */
        /* loaded from: classes.dex */
        final class OnError implements Runnable {
            private final Throwable throwable;

            OnError(Throwable th) {
                this.throwable = th;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    DelayObserver.this.downstream.onError(this.throwable);
                } finally {
                    DelayObserver.this.f150w.dispose();
                }
            }
        }

        /* renamed from: io.reactivex.internal.operators.observable.ObservableDelay$DelayObserver$OnComplete */
        /* loaded from: classes.dex */
        final class OnComplete implements Runnable {
            OnComplete() {
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    DelayObserver.this.downstream.onComplete();
                } finally {
                    DelayObserver.this.f150w.dispose();
                }
            }
        }
    }
}
