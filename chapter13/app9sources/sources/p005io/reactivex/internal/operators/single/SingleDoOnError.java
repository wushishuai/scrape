package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;

/* renamed from: io.reactivex.internal.operators.single.SingleDoOnError */
/* loaded from: classes.dex */
public final class SingleDoOnError<T> extends Single<T> {
    final Consumer<? super Throwable> onError;
    final SingleSource<T> source;

    public SingleDoOnError(SingleSource<T> singleSource, Consumer<? super Throwable> consumer) {
        this.source = singleSource;
        this.onError = consumer;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(new DoOnError(singleObserver));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDoOnError$DoOnError */
    /* loaded from: classes.dex */
    final class DoOnError implements SingleObserver<T> {
        private final SingleObserver<? super T> downstream;

        DoOnError(SingleObserver<? super T> singleObserver) {
            this.downstream = singleObserver;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            this.downstream.onSubscribe(disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.downstream.onSuccess(t);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            try {
                SingleDoOnError.this.onError.accept(th);
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                th = new CompositeException(th, th2);
            }
            this.downstream.onError(th);
        }
    }
}
