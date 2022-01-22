package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleDoAfterSuccess */
/* loaded from: classes.dex */
public final class SingleDoAfterSuccess<T> extends Single<T> {
    final Consumer<? super T> onAfterSuccess;
    final SingleSource<T> source;

    public SingleDoAfterSuccess(SingleSource<T> singleSource, Consumer<? super T> consumer) {
        this.source = singleSource;
        this.onAfterSuccess = consumer;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(new DoAfterObserver(singleObserver, this.onAfterSuccess));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDoAfterSuccess$DoAfterObserver */
    /* loaded from: classes.dex */
    static final class DoAfterObserver<T> implements SingleObserver<T>, Disposable {
        final SingleObserver<? super T> downstream;
        final Consumer<? super T> onAfterSuccess;
        Disposable upstream;

        DoAfterObserver(SingleObserver<? super T> singleObserver, Consumer<? super T> consumer) {
            this.downstream = singleObserver;
            this.onAfterSuccess = consumer;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.downstream.onSuccess(t);
            try {
                this.onAfterSuccess.accept(t);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }
    }
}
