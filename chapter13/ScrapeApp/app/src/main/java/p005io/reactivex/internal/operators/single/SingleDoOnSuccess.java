package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;

/* renamed from: io.reactivex.internal.operators.single.SingleDoOnSuccess */
/* loaded from: classes.dex */
public final class SingleDoOnSuccess<T> extends Single<T> {
    final Consumer<? super T> onSuccess;
    final SingleSource<T> source;

    public SingleDoOnSuccess(SingleSource<T> source, Consumer<? super T> onSuccess) {
        this.source = source;
        this.onSuccess = onSuccess;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        this.source.subscribe(new DoOnSuccess(observer));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDoOnSuccess$DoOnSuccess */
    /* loaded from: classes.dex */
    final class DoOnSuccess implements SingleObserver<T> {
        final SingleObserver<? super T> downstream;

        DoOnSuccess(SingleObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.downstream.onSubscribe(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            try {
                SingleDoOnSuccess.this.onSuccess.accept(value);
                this.downstream.onSuccess(value);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                this.downstream.onError(ex);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }
    }
}
