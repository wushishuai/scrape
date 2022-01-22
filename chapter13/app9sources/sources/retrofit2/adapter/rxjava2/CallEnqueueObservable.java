package retrofit2.adapter.rxjava2;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* loaded from: classes.dex */
final class CallEnqueueObservable<T> extends Observable<Response<T>> {
    private final Call<T> originalCall;

    /* JADX INFO: Access modifiers changed from: package-private */
    public CallEnqueueObservable(Call<T> call) {
        this.originalCall = call;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Response<T>> observer) {
        Call<T> clone = this.originalCall.clone();
        CallCallback callCallback = new CallCallback(clone, observer);
        observer.onSubscribe(callCallback);
        clone.enqueue(callCallback);
    }

    /* loaded from: classes.dex */
    private static final class CallCallback<T> implements Disposable, Callback<T> {
        private final Call<?> call;
        private volatile boolean disposed;
        private final Observer<? super Response<T>> observer;
        boolean terminated = false;

        CallCallback(Call<?> call, Observer<? super Response<T>> observer) {
            this.call = call;
            this.observer = observer;
        }

        @Override // retrofit2.Callback
        public void onResponse(Call<T> call, Response<T> response) {
            if (!this.disposed) {
                try {
                    this.observer.onNext(response);
                    if (!this.disposed) {
                        this.terminated = true;
                        this.observer.onComplete();
                    }
                } catch (Throwable th) {
                    if (this.terminated) {
                        RxJavaPlugins.onError(th);
                    } else if (!this.disposed) {
                        try {
                            this.observer.onError(th);
                        } catch (Throwable th2) {
                            Exceptions.throwIfFatal(th2);
                            RxJavaPlugins.onError(new CompositeException(th, th2));
                        }
                    }
                }
            }
        }

        @Override // retrofit2.Callback
        public void onFailure(Call<T> call, Throwable th) {
            if (!call.isCanceled()) {
                try {
                    this.observer.onError(th);
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    RxJavaPlugins.onError(new CompositeException(th, th2));
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
            this.call.cancel();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }
    }
}
