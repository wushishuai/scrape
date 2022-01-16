package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleDoOnSubscribe */
/* loaded from: classes.dex */
public final class SingleDoOnSubscribe<T> extends Single<T> {
    final Consumer<? super Disposable> onSubscribe;
    final SingleSource<T> source;

    public SingleDoOnSubscribe(SingleSource<T> source, Consumer<? super Disposable> onSubscribe) {
        this.source = source;
        this.onSubscribe = onSubscribe;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        this.source.subscribe(new DoOnSubscribeSingleObserver(observer, this.onSubscribe));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDoOnSubscribe$DoOnSubscribeSingleObserver */
    /* loaded from: classes.dex */
    static final class DoOnSubscribeSingleObserver<T> implements SingleObserver<T> {
        boolean done;
        final SingleObserver<? super T> downstream;
        final Consumer<? super Disposable> onSubscribe;

        DoOnSubscribeSingleObserver(SingleObserver<? super T> actual, Consumer<? super Disposable> onSubscribe) {
            this.downstream = actual;
            this.onSubscribe = onSubscribe;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            try {
                this.onSubscribe.accept(d);
                this.downstream.onSubscribe(d);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                this.done = true;
                d.dispose();
                EmptyDisposable.error(ex, this.downstream);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            if (!this.done) {
                this.downstream.onSuccess(value);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            if (this.done) {
                RxJavaPlugins.onError(e);
            } else {
                this.downstream.onError(e);
            }
        }
    }
}
