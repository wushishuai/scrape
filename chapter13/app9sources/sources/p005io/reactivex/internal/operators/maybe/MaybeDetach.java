package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeDetach */
/* loaded from: classes.dex */
public final class MaybeDetach<T> extends AbstractMaybeWithUpstream<T, T> {
    public MaybeDetach(MaybeSource<T> maybeSource) {
        super(maybeSource);
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new DetachMaybeObserver(maybeObserver));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeDetach$DetachMaybeObserver */
    /* loaded from: classes.dex */
    static final class DetachMaybeObserver<T> implements MaybeObserver<T>, Disposable {
        MaybeObserver<? super T> downstream;
        Disposable upstream;

        DetachMaybeObserver(MaybeObserver<? super T> maybeObserver) {
            this.downstream = maybeObserver;
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

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            this.upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> maybeObserver = this.downstream;
            if (maybeObserver != null) {
                this.downstream = null;
                maybeObserver.onSuccess(t);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> maybeObserver = this.downstream;
            if (maybeObserver != null) {
                this.downstream = null;
                maybeObserver.onError(th);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> maybeObserver = this.downstream;
            if (maybeObserver != null) {
                this.downstream = null;
                maybeObserver.onComplete();
            }
        }
    }
}
