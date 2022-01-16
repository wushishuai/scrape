package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybePeek */
/* loaded from: classes.dex */
public final class MaybePeek<T> extends AbstractMaybeWithUpstream<T, T> {
    final Action onAfterTerminate;
    final Action onCompleteCall;
    final Action onDisposeCall;
    final Consumer<? super Throwable> onErrorCall;
    final Consumer<? super Disposable> onSubscribeCall;
    final Consumer<? super T> onSuccessCall;

    public MaybePeek(MaybeSource<T> source, Consumer<? super Disposable> onSubscribeCall, Consumer<? super T> onSuccessCall, Consumer<? super Throwable> onErrorCall, Action onCompleteCall, Action onAfterTerminate, Action onDispose) {
        super(source);
        this.onSubscribeCall = onSubscribeCall;
        this.onSuccessCall = onSuccessCall;
        this.onErrorCall = onErrorCall;
        this.onCompleteCall = onCompleteCall;
        this.onAfterTerminate = onAfterTerminate;
        this.onDisposeCall = onDispose;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        this.source.subscribe(new MaybePeekObserver(observer, this));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybePeek$MaybePeekObserver */
    /* loaded from: classes.dex */
    static final class MaybePeekObserver<T> implements MaybeObserver<T>, Disposable {
        final MaybeObserver<? super T> downstream;
        final MaybePeek<T> parent;
        Disposable upstream;

        MaybePeekObserver(MaybeObserver<? super T> actual, MaybePeek<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            try {
                this.parent.onDisposeCall.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
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
                try {
                    this.parent.onSubscribeCall.accept(d);
                    this.upstream = d;
                    this.downstream.onSubscribe(this);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    d.dispose();
                    this.upstream = DisposableHelper.DISPOSED;
                    EmptyDisposable.error(ex, this.downstream);
                }
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T value) {
            if (this.upstream != DisposableHelper.DISPOSED) {
                try {
                    this.parent.onSuccessCall.accept(value);
                    this.upstream = DisposableHelper.DISPOSED;
                    this.downstream.onSuccess(value);
                    onAfterTerminate();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    onErrorInner(ex);
                }
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable e) {
            if (this.upstream == DisposableHelper.DISPOSED) {
                RxJavaPlugins.onError(e);
            } else {
                onErrorInner(e);
            }
        }

        void onErrorInner(Throwable e) {
            try {
                this.parent.onErrorCall.accept(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onError(e);
            onAfterTerminate();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            if (this.upstream != DisposableHelper.DISPOSED) {
                try {
                    this.parent.onCompleteCall.run();
                    this.upstream = DisposableHelper.DISPOSED;
                    this.downstream.onComplete();
                    onAfterTerminate();
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    onErrorInner(ex);
                }
            }
        }

        void onAfterTerminate() {
            try {
                this.parent.onAfterTerminate.run();
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
        }
    }
}
