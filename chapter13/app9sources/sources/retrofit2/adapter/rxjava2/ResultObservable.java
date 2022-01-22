package retrofit2.adapter.rxjava2;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Response;

/* loaded from: classes.dex */
final class ResultObservable<T> extends Observable<Result<T>> {
    private final Observable<Response<T>> upstream;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ResultObservable(Observable<Response<T>> observable) {
        this.upstream = observable;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Result<T>> observer) {
        this.upstream.subscribe(new ResultObserver(observer));
    }

    /* loaded from: classes.dex */
    private static class ResultObserver<R> implements Observer<Response<R>> {
        private final Observer<? super Result<R>> observer;

        @Override // p005io.reactivex.Observer
        public /* bridge */ /* synthetic */ void onNext(Object obj) {
            onNext((Response) ((Response) obj));
        }

        ResultObserver(Observer<? super Result<R>> observer) {
            this.observer = observer;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.observer.onSubscribe(disposable);
        }

        public void onNext(Response<R> response) {
            this.observer.onNext(Result.response(response));
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            try {
                this.observer.onNext(Result.error(th));
                this.observer.onComplete();
            } catch (Throwable th2) {
                try {
                    this.observer.onError(th2);
                } catch (Throwable th3) {
                    Exceptions.throwIfFatal(th3);
                    RxJavaPlugins.onError(new CompositeException(th2, th3));
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.observer.onComplete();
        }
    }
}
