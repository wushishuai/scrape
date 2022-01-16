package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiConsumer;

/* renamed from: io.reactivex.internal.operators.single.SingleDoOnEvent */
/* loaded from: classes.dex */
public final class SingleDoOnEvent<T> extends Single<T> {
    final BiConsumer<? super T, ? super Throwable> onEvent;
    final SingleSource<T> source;

    public SingleDoOnEvent(SingleSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {
        this.source = source;
        this.onEvent = onEvent;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        this.source.subscribe(new DoOnEvent(observer));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDoOnEvent$DoOnEvent */
    /* loaded from: classes.dex */
    final class DoOnEvent implements SingleObserver<T> {
        private final SingleObserver<? super T> downstream;

        DoOnEvent(SingleObserver<? super T> observer) {
            this.downstream = observer;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.downstream.onSubscribe(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            try {
                SingleDoOnEvent.this.onEvent.accept(value, null);
                this.downstream.onSuccess(value);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                this.downstream.onError(ex);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            try {
                SingleDoOnEvent.this.onEvent.accept(null, e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }
            this.downstream.onError(e);
        }
    }
}
