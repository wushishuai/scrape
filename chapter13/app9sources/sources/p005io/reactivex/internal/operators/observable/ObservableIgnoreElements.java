package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableIgnoreElements */
/* loaded from: classes.dex */
public final class ObservableIgnoreElements<T> extends AbstractObservableWithUpstream<T, T> {
    public ObservableIgnoreElements(ObservableSource<T> observableSource) {
        super(observableSource);
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new IgnoreObservable(observer));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableIgnoreElements$IgnoreObservable */
    /* loaded from: classes.dex */
    static final class IgnoreObservable<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        Disposable upstream;

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
        }

        IgnoreObservable(Observer<? super T> observer) {
            this.downstream = observer;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.upstream = disposable;
            this.downstream.onSubscribe(this);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }
    }
}
