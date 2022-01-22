package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.Callable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;

/* renamed from: io.reactivex.internal.operators.completable.CompletableToSingle */
/* loaded from: classes.dex */
public final class CompletableToSingle<T> extends Single<T> {
    final T completionValue;
    final Callable<? extends T> completionValueSupplier;
    final CompletableSource source;

    public CompletableToSingle(CompletableSource completableSource, Callable<? extends T> callable, T t) {
        this.source = completableSource;
        this.completionValue = t;
        this.completionValueSupplier = callable;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(new ToSingle(singleObserver));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableToSingle$ToSingle */
    /* loaded from: classes.dex */
    final class ToSingle implements CompletableObserver {
        private final SingleObserver<? super T> observer;

        ToSingle(SingleObserver<? super T> singleObserver) {
            this.observer = singleObserver;
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            T t;
            if (CompletableToSingle.this.completionValueSupplier != null) {
                try {
                    t = (Object) CompletableToSingle.this.completionValueSupplier.call();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.observer.onError(th);
                    return;
                }
            } else {
                t = CompletableToSingle.this.completionValue;
            }
            if (t == null) {
                this.observer.onError(new NullPointerException("The value supplied is null"));
            } else {
                this.observer.onSuccess(t);
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            this.observer.onError(th);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.observer.onSubscribe(disposable);
        }
    }
}
