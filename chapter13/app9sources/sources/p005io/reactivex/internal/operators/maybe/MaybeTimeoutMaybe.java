package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeTimeoutMaybe */
/* loaded from: classes.dex */
public final class MaybeTimeoutMaybe<T, U> extends AbstractMaybeWithUpstream<T, T> {
    final MaybeSource<? extends T> fallback;
    final MaybeSource<U> other;

    public MaybeTimeoutMaybe(MaybeSource<T> maybeSource, MaybeSource<U> maybeSource2, MaybeSource<? extends T> maybeSource3) {
        super(maybeSource);
        this.other = maybeSource2;
        this.fallback = maybeSource3;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        TimeoutMainMaybeObserver timeoutMainMaybeObserver = new TimeoutMainMaybeObserver(maybeObserver, this.fallback);
        maybeObserver.onSubscribe(timeoutMainMaybeObserver);
        this.other.subscribe(timeoutMainMaybeObserver.other);
        this.source.subscribe(timeoutMainMaybeObserver);
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeTimeoutMaybe$TimeoutMainMaybeObserver */
    /* loaded from: classes.dex */
    static final class TimeoutMainMaybeObserver<T, U> extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {
        private static final long serialVersionUID = -5955289211445418871L;
        final MaybeObserver<? super T> downstream;
        final MaybeSource<? extends T> fallback;
        final TimeoutOtherMaybeObserver<T, U> other = new TimeoutOtherMaybeObserver<>(this);
        final TimeoutFallbackMaybeObserver<T> otherObserver;

        TimeoutMainMaybeObserver(MaybeObserver<? super T> maybeObserver, MaybeSource<? extends T> maybeSource) {
            this.downstream = maybeObserver;
            this.fallback = maybeSource;
            this.otherObserver = maybeSource != null ? new TimeoutFallbackMaybeObserver<>(maybeObserver) : null;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
            DisposableHelper.dispose(this.other);
            TimeoutFallbackMaybeObserver<T> timeoutFallbackMaybeObserver = this.otherObserver;
            if (timeoutFallbackMaybeObserver != null) {
                DisposableHelper.dispose(timeoutFallbackMaybeObserver);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            DisposableHelper.dispose(this.other);
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                this.downstream.onSuccess(t);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            DisposableHelper.dispose(this.other);
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                this.downstream.onError(th);
            } else {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            DisposableHelper.dispose(this.other);
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                this.downstream.onComplete();
            }
        }

        public void otherError(Throwable th) {
            if (DisposableHelper.dispose(this)) {
                this.downstream.onError(th);
            } else {
                RxJavaPlugins.onError(th);
            }
        }

        public void otherComplete() {
            if (DisposableHelper.dispose(this)) {
                MaybeSource<? extends T> maybeSource = this.fallback;
                if (maybeSource == null) {
                    this.downstream.onError(new TimeoutException());
                } else {
                    maybeSource.subscribe(this.otherObserver);
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeTimeoutMaybe$TimeoutOtherMaybeObserver */
    /* loaded from: classes.dex */
    static final class TimeoutOtherMaybeObserver<T, U> extends AtomicReference<Disposable> implements MaybeObserver<Object> {
        private static final long serialVersionUID = 8663801314800248617L;
        final TimeoutMainMaybeObserver<T, U> parent;

        TimeoutOtherMaybeObserver(TimeoutMainMaybeObserver<T, U> timeoutMainMaybeObserver) {
            this.parent = timeoutMainMaybeObserver;
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(Object obj) {
            this.parent.otherComplete();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.parent.otherError(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.parent.otherComplete();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.maybe.MaybeTimeoutMaybe$TimeoutFallbackMaybeObserver */
    /* loaded from: classes.dex */
    public static final class TimeoutFallbackMaybeObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T> {
        private static final long serialVersionUID = 8663801314800248617L;
        final MaybeObserver<? super T> downstream;

        TimeoutFallbackMaybeObserver(MaybeObserver<? super T> maybeObserver) {
            this.downstream = maybeObserver;
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
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
