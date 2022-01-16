package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleDetach */
/* loaded from: classes.dex */
public final class SingleDetach<T> extends Single<T> {
    final SingleSource<T> source;

    public SingleDetach(SingleSource<T> source) {
        this.source = source;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        this.source.subscribe(new DetachSingleObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDetach$DetachSingleObserver */
    /* loaded from: classes.dex */
    static final class DetachSingleObserver<T> implements SingleObserver<T>, Disposable {
        SingleObserver<? super T> downstream;
        Disposable upstream;

        DetachSingleObserver(SingleObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.downstream = null;
            this.upstream.dispose();
            this.upstream = DisposableHelper.DISPOSED;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.upstream = DisposableHelper.DISPOSED;
            SingleObserver<? super T> a = this.downstream;
            if (a != null) {
                this.downstream = null;
                a.onSuccess(value);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.upstream = DisposableHelper.DISPOSED;
            SingleObserver<? super T> a = this.downstream;
            if (a != null) {
                this.downstream = null;
                a.onError(e);
            }
        }
    }
}
