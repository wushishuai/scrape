package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeSwitchIfEmpty */
/* loaded from: classes.dex */
public final class MaybeSwitchIfEmpty<T> extends AbstractMaybeWithUpstream<T, T> {
    final MaybeSource<? extends T> other;

    public MaybeSwitchIfEmpty(MaybeSource<T> maybeSource, MaybeSource<? extends T> maybeSource2) {
        super(maybeSource);
        this.other = maybeSource2;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new SwitchIfEmptyMaybeObserver(maybeObserver, this.other));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeSwitchIfEmpty$SwitchIfEmptyMaybeObserver */
    /* loaded from: classes.dex */
    static final class SwitchIfEmptyMaybeObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {
        private static final long serialVersionUID = -2223459372976438024L;
        final MaybeObserver<? super T> downstream;
        final MaybeSource<? extends T> other;

        SwitchIfEmptyMaybeObserver(MaybeObserver<? super T> maybeObserver, MaybeSource<? extends T> maybeSource) {
            this.downstream = maybeObserver;
            this.other = maybeSource;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.setOnce(this, disposable)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            this.downstream.onSuccess(t);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            Disposable disposable = get();
            if (disposable != DisposableHelper.DISPOSED && compareAndSet(disposable, null)) {
                this.other.subscribe(new OtherMaybeObserver(this.downstream, this));
            }
        }

        /* renamed from: io.reactivex.internal.operators.maybe.MaybeSwitchIfEmpty$SwitchIfEmptyMaybeObserver$OtherMaybeObserver */
        /* loaded from: classes.dex */
        static final class OtherMaybeObserver<T> implements MaybeObserver<T> {
            final MaybeObserver<? super T> downstream;
            final AtomicReference<Disposable> parent;

            OtherMaybeObserver(MaybeObserver<? super T> maybeObserver, AtomicReference<Disposable> atomicReference) {
                this.downstream = maybeObserver;
                this.parent = atomicReference;
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(this.parent, disposable);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSuccess(T t) {
                this.downstream.onSuccess(t);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onError(Throwable th) {
                this.downstream.onError(th);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.downstream.onComplete();
            }
        }
    }
}
