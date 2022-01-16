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

    public ObservableDelay(ObservableSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        super(source);
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
        this.delayError = delayError;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> t) {
        Observer<? super T> observer;
        if (this.delayError) {
            observer = t;
        } else {
            observer = new SerializedObserver<>(t);
        }
        this.source.subscribe(new DelayObserver(observer, this.delay, this.unit, this.scheduler.createWorker(), this.delayError));
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

        DelayObserver(Observer<? super T> actual, long delay, TimeUnit unit, Scheduler.Worker w, boolean delayError) {
            this.downstream = actual;
            this.delay = delay;
            this.unit = unit;
            this.f150w = w;
            this.delayError = delayError;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.f150w.schedule(new OnNext(t), this.delay, this.unit);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.f150w.schedule(new OnError(t), this.delayError ? this.delay : 0, this.unit);
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

            OnError(Throwable throwable) {
                this.throwable = throwable;
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
