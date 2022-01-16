package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableDelaySubscriptionOther */
/* loaded from: classes.dex */
public final class ObservableDelaySubscriptionOther<T, U> extends Observable<T> {
    final ObservableSource<? extends T> main;
    final ObservableSource<U> other;

    public ObservableDelaySubscriptionOther(ObservableSource<? extends T> main, ObservableSource<U> other) {
        this.main = main;
        this.other = other;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> child) {
        SequentialDisposable serial = new SequentialDisposable();
        child.onSubscribe(serial);
        this.other.subscribe(new DelayObserver(serial, child));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableDelaySubscriptionOther$DelayObserver */
    /* loaded from: classes.dex */
    final class DelayObserver implements Observer<U> {
        final Observer<? super T> child;
        boolean done;
        final SequentialDisposable serial;

        DelayObserver(SequentialDisposable serial, Observer<? super T> child) {
            this.serial = serial;
            this.child = child;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.serial.update(d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(U t) {
            onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            if (this.done) {
                RxJavaPlugins.onError(e);
                return;
            }
            this.done = true;
            this.child.onError(e);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                ObservableDelaySubscriptionOther.this.main.subscribe(new OnComplete());
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.observable.ObservableDelaySubscriptionOther$DelayObserver$OnComplete */
        /* loaded from: classes.dex */
        public final class OnComplete implements Observer<T> {
            OnComplete() {
            }

            @Override // p005io.reactivex.Observer
            public void onSubscribe(Disposable d) {
                DelayObserver.this.serial.update(d);
            }

            @Override // p005io.reactivex.Observer
            public void onNext(T value) {
                DelayObserver.this.child.onNext(value);
            }

            @Override // p005io.reactivex.Observer
            public void onError(Throwable e) {
                DelayObserver.this.child.onError(e);
            }

            @Override // p005io.reactivex.Observer
            public void onComplete() {
                DelayObserver.this.child.onComplete();
            }
        }
    }
}
