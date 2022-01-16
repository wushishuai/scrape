package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableFromObservable */
/* loaded from: classes.dex */
public final class CompletableFromObservable<T> extends Completable {
    final ObservableSource<T> observable;

    public CompletableFromObservable(ObservableSource<T> observable) {
        this.observable = observable;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        this.observable.subscribe(new CompletableFromObservableObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableFromObservable$CompletableFromObservableObserver */
    /* loaded from: classes.dex */
    static final class CompletableFromObservableObserver<T> implements Observer<T> {

        /* renamed from: co */
        final CompletableObserver f111co;

        CompletableFromObservableObserver(CompletableObserver co) {
            this.f111co = co;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.f111co.onSubscribe(d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T value) {
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            this.f111co.onError(e);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.f111co.onComplete();
        }
    }
}
