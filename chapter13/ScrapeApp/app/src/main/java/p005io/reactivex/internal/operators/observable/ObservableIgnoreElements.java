package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableIgnoreElements */
/* loaded from: classes.dex */
public final class ObservableIgnoreElements<T> extends AbstractObservableWithUpstream<T, T> {
    public ObservableIgnoreElements(ObservableSource<T> source) {
        super(source);
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> t) {
        this.source.subscribe(new IgnoreObservable(t));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableIgnoreElements$IgnoreObservable */
    /* loaded from: classes.dex */
    static final class IgnoreObservable<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        Disposable upstream;

        IgnoreObservable(Observer<? super T> t) {
            this.downstream = t;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.upstream = d;
            this.downstream.onSubscribe(this);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T v) {
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            this.downstream.onError(e);
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
