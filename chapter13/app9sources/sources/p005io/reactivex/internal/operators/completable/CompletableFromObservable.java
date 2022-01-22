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

    public CompletableFromObservable(ObservableSource<T> observableSource) {
        this.observable = observableSource;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        this.observable.subscribe(new CompletableFromObservableObserver(completableObserver));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableFromObservable$CompletableFromObservableObserver */
    /* loaded from: classes.dex */
    static final class CompletableFromObservableObserver<T> implements Observer<T> {

        /* renamed from: co */
        final CompletableObserver f111co;

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
        }

        CompletableFromObservableObserver(CompletableObserver completableObserver) {
            this.f111co = completableObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.f111co.onSubscribe(disposable);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.f111co.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.f111co.onComplete();
        }
    }
}
