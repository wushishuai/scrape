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

    public MaybeSwitchIfEmpty(MaybeSource<T> source, MaybeSource<? extends T> other) {
        super(source);
        this.other = other;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        this.source.subscribe(new SwitchIfEmptyMaybeObserver(observer, this.other));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeSwitchIfEmpty$SwitchIfEmptyMaybeObserver */
    /* loaded from: classes.dex */
    static final class SwitchIfEmptyMaybeObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {
        private static final long serialVersionUID = -2223459372976438024L;
        final MaybeObserver<? super T> downstream;
        final MaybeSource<? extends T> other;

        SwitchIfEmptyMaybeObserver(MaybeObserver<? super T> actual, MaybeSource<? extends T> other) {
            this.downstream = actual;
            this.other = other;
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
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T value) {
            this.downstream.onSuccess(value);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            Disposable d = get();
            if (d != DisposableHelper.DISPOSED && compareAndSet(d, null)) {
                this.other.subscribe(new OtherMaybeObserver(this.downstream, this));
            }
        }

        /* renamed from: io.reactivex.internal.operators.maybe.MaybeSwitchIfEmpty$SwitchIfEmptyMaybeObserver$OtherMaybeObserver */
        /* loaded from: classes.dex */
        static final class OtherMaybeObserver<T> implements MaybeObserver<T> {
            final MaybeObserver<? super T> downstream;
            final AtomicReference<Disposable> parent;

            OtherMaybeObserver(MaybeObserver<? super T> actual, AtomicReference<Disposable> parent) {
                this.downstream = actual;
                this.parent = parent;
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this.parent, d);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSuccess(T value) {
                this.downstream.onSuccess(value);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onError(Throwable e) {
                this.downstream.onError(e);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.downstream.onComplete();
            }
        }
    }
}
