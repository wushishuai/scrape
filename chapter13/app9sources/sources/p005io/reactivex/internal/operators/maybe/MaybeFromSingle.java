package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.HasUpstreamSingleSource;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeFromSingle */
/* loaded from: classes.dex */
public final class MaybeFromSingle<T> extends Maybe<T> implements HasUpstreamSingleSource<T> {
    final SingleSource<T> source;

    public MaybeFromSingle(SingleSource<T> singleSource) {
        this.source = singleSource;
    }

    @Override // p005io.reactivex.internal.fuseable.HasUpstreamSingleSource
    public SingleSource<T> source() {
        return this.source;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new FromSingleObserver(maybeObserver));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeFromSingle$FromSingleObserver */
    /* loaded from: classes.dex */
    static final class FromSingleObserver<T> implements SingleObserver<T>, Disposable {
        final MaybeObserver<? super T> downstream;
        Disposable upstream;

        FromSingleObserver(MaybeObserver<? super T> maybeObserver) {
            this.downstream = maybeObserver;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            this.upstream = DisposableHelper.DISPOSED;
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
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onSuccess(t);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onError(th);
        }
    }
}
