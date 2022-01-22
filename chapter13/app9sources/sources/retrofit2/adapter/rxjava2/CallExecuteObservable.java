package retrofit2.adapter.rxjava2;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.plugins.RxJavaPlugins;
import retrofit2.Call;
import retrofit2.Response;

/* loaded from: classes.dex */
final class CallExecuteObservable<T> extends Observable<Response<T>> {
    private final Call<T> originalCall;

    /* JADX INFO: Access modifiers changed from: package-private */
    public CallExecuteObservable(Call<T> call) {
        this.originalCall = call;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Response<T>> observer) {
        boolean z;
        Throwable th;
        Call<T> clone = this.originalCall.clone();
        CallDisposable callDisposable = new CallDisposable(clone);
        observer.onSubscribe(callDisposable);
        try {
            Response<T> execute = clone.execute();
            if (!callDisposable.isDisposed()) {
                observer.onNext(execute);
            }
            if (!callDisposable.isDisposed()) {
                try {
                    observer.onComplete();
                } catch (Throwable th2) {
                    th = th2;
                    z = true;
                    Exceptions.throwIfFatal(th);
                    if (z) {
                        RxJavaPlugins.onError(th);
                    } else if (!callDisposable.isDisposed()) {
                        try {
                            observer.onError(th);
                        } catch (Throwable th3) {
                            Exceptions.throwIfFatal(th3);
                            RxJavaPlugins.onError(new CompositeException(th, th3));
                        }
                    }
                }
            }
        } catch (Throwable th4) {
            th = th4;
            z = false;
        }
    }

    /* loaded from: classes.dex */
    private static final class CallDisposable implements Disposable {
        private final Call<?> call;
        private volatile boolean disposed;

        CallDisposable(Call<?> call) {
            this.call = call;
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
