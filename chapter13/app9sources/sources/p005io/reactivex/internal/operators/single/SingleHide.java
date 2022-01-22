package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleHide */
/* loaded from: classes.dex */
public final class SingleHide<T> extends Single<T> {
    final SingleSource<? extends T> source;

    public SingleHide(SingleSource<? extends T> singleSource) {
        this.source = singleSource;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(new HideSingleObserver(singleObserver));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleHide$HideSingleObserver */
    /* loaded from: classes.dex */
    static final class HideSingleObserver<T> implements SingleObserver<T>, Disposable {
        final SingleObserver<? super T> downstream;
        Disposable upstream;

        HideSingleObserver(SingleObserver<? super T> singleObserver) {
            this.downstream = singleObserver;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
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
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }
    }
}
