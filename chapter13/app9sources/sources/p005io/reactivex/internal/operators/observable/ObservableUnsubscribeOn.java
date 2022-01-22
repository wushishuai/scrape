package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableUnsubscribeOn */
/* loaded from: classes.dex */
public final class ObservableUnsubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;

    public ObservableUnsubscribeOn(ObservableSource<T> observableSource, Scheduler scheduler) {
        super(observableSource);
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new UnsubscribeObserver(observer, this.scheduler));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableUnsubscribeOn$UnsubscribeObserver */
    /* loaded from: classes.dex */
    static final class UnsubscribeObserver<T> extends AtomicBoolean implements Observer<T>, Disposable {
        private static final long serialVersionUID = 1015244841293359600L;
        final Observer<? super T> downstream;
        final Scheduler scheduler;
        Disposable upstream;

        UnsubscribeObserver(Observer<? super T> observer, Scheduler scheduler) {
            this.downstream = observer;
            this.scheduler = scheduler;
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
            if (!get()) {
                this.downstream.onNext(t);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (get()) {
                RxJavaPlugins.onError(th);
            } else {
                this.downstream.onError(th);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!get()) {
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (compareAndSet(false, true)) {
                this.scheduler.scheduleDirect(new DisposeTask());
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get();
        }

        /* renamed from: io.reactivex.internal.operators.observable.ObservableUnsubscribeOn$UnsubscribeObserver$DisposeTask */
        /* loaded from: classes.dex */
        final class DisposeTask implements Runnable {
            DisposeTask() {
            }

            @Override // java.lang.Runnable
            public void run() {
                UnsubscribeObserver.this.upstream.dispose();
            }
        }
    }
}
