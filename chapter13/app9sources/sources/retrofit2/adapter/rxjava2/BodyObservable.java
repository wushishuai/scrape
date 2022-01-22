package retrofit2.adapter.rxjava2;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Response;

/* loaded from: classes.dex */
final class BodyObservable<T> extends Observable<T> {
    private final Observable<Response<T>> upstream;

    /* JADX INFO: Access modifiers changed from: package-private */
    public BodyObservable(Observable<Response<T>> observable) {
        this.upstream = observable;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.upstream.subscribe(new BodyObserver(observer));
    }

    /* loaded from: classes.dex */
    private static class BodyObserver<R> implements Observer<Response<R>> {
        private final Observer<? super R> observer;
        private boolean terminated;

        @Override // p005io.reactivex.Observer
        public /* bridge */ /* synthetic */ void onNext(Object obj) {
            onNext((Response) ((Response) obj));
        }

        BodyObserver(Observer<? super R> observer) {
            this.observer = observer;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            this.observer.onSubscribe(disposable);
        }

        public void onNext(Response<R> response) {
            if (response.isSuccessful()) {
                this.observer.onNext(response.body());
                return;
            }
            this.terminated = true;
            HttpException httpException = new HttpException(response);
            try {
                this.observer.onError(httpException);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(new CompositeException(httpException, th));
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.terminated) {
                this.observer.onComplete();
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (!this.terminated) {
                this.observer.onError(th);
                return;
            }
            AssertionError assertionError = new AssertionError("This should never happen! Report as a bug with the full stacktrace.");
            assertionError.initCause(th);
            RxJavaPlugins.onError(assertionError);
        }
    }
}
