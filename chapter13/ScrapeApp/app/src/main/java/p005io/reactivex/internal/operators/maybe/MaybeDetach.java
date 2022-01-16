package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeDetach */
/* loaded from: classes.dex */
public final class MaybeDetach<T> extends AbstractMaybeWithUpstream<T, T> {
    public MaybeDetach(MaybeSource<T> source) {
        super(source);
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        this.source.subscribe(new DetachMaybeObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeDetach$DetachMaybeObserver */
    /* loaded from: classes.dex */
    static final class DetachMaybeObserver<T> implements MaybeObserver<T>, Disposable {
        MaybeObserver<? super T> downstream;
        Disposable upstream;

        DetachMaybeObserver(MaybeObserver<? super T> downstream) {
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

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T value) {
            this.upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> a = this.downstream;
            if (a != null) {
                this.downstream = null;
                a.onSuccess(value);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable e) {
            this.upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> a = this.downstream;
            if (a != null) {
                this.downstream = null;
                a.onError(e);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.upstream = DisposableHelper.DISPOSED;
            MaybeObserver<? super T> a = this.downstream;
            if (a != null) {
                this.downstream = null;
                a.onComplete();
            }
        }
    }
}
