package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.HasUpstreamMaybeSource;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeCount */
/* loaded from: classes.dex */
public final class MaybeCount<T> extends Single<Long> implements HasUpstreamMaybeSource<T> {
    final MaybeSource<T> source;

    public MaybeCount(MaybeSource<T> maybeSource) {
        this.source = maybeSource;
    }

    @Override // p005io.reactivex.internal.fuseable.HasUpstreamMaybeSource
    public MaybeSource<T> source() {
        return this.source;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Long> singleObserver) {
        this.source.subscribe(new CountMaybeObserver(singleObserver));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeCount$CountMaybeObserver */
    /* loaded from: classes.dex */
    static final class CountMaybeObserver implements MaybeObserver<Object>, Disposable {
        final SingleObserver<? super Long> downstream;
        Disposable upstream;

        CountMaybeObserver(SingleObserver<? super Long> singleObserver) {
            this.downstream = singleObserver;
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(Object obj) {
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onSuccess(1L);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onSuccess(0L);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            this.upstream = DisposableHelper.DISPOSED;
        }
    }
}
