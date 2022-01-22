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

    public ObservableDelaySubscriptionOther(ObservableSource<? extends T> observableSource, ObservableSource<U> observableSource2) {
        this.main = observableSource;
        this.other = observableSource2;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        SequentialDisposable sequentialDisposable = new SequentialDisposable();
        observer.onSubscribe(sequentialDisposable);
        this.other.subscribe(new DelayObserver(sequentialDisposable, observer));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableDelaySubscriptionOther$DelayObserver */
    /* loaded from: classes.dex */
    final class DelayObserver implements Observer<U> {
        final Observer<? super T> child;
        boolean done;
        final SequentialDisposable serial;

        DelayObserver(SequentialDisposable sequentialDisposable, Observer<? super T> observer) {
            this.serial = sequentialDisposable;
            this.child = observer;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.serial.update(disposable);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(U u) {
            onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.child.onError(th);
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
            public void onSubscribe(Disposable disposable) {
                DelayObserver.this.serial.update(disposable);
            }

            @Override // p005io.reactivex.Observer
            public void onNext(T t) {
                DelayObserver.this.child.onNext(t);
            }

            @Override // p005io.reactivex.Observer
            public void onError(Throwable th) {
                DelayObserver.this.child.onError(th);
            }

            @Override // p005io.reactivex.Observer
            public void onComplete() {
                DelayObserver.this.child.onComplete();
            }
        }
    }
}
