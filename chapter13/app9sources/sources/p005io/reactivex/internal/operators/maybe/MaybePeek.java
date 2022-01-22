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

    public MaybePeek(MaybeSource<T> maybeSource, Consumer<? super Disposable> consumer, Consumer<? super T> consumer2, Consumer<? super Throwable> consumer3, Action action, Action action2, Action action3) {
        super(maybeSource);
        this.onSubscribeCall = consumer;
        this.onSuccessCall = consumer2;
        this.onErrorCall = consumer3;
        this.onCompleteCall = action;
        this.onAfterTerminate = action2;
        this.onDisposeCall = action3;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new MaybePeekObserver(maybeObserver, this));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybePeek$MaybePeekObserver */
    /* loaded from: classes.dex */
    static final class MaybePeekObserver<T> implements MaybeObserver<T>, Disposable {
        final MaybeObserver<? super T> downstream;
        final MaybePeek<T> parent;
        Disposable upstream;

        MaybePeekObserver(MaybeObserver<? super T> maybeObserver, MaybePeek<T> maybePeek) {
            this.downstream = maybeObserver;
            this.parent = maybePeek;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            try {
                this.parent.onDisposeCall.run();
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(th);
            }
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
                try {
                    this.parent.onSubscribeCall.accept(disposable);
                    this.upstream = disposable;
                    this.downstream.onSubscribe(this);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    disposable.dispose();
                    this.upstream = DisposableHelper.DISPOSED;
                    EmptyDisposable.error(th, this.downstream);
                }
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            if (this.upstream != DisposableHelper.DISPOSED) {
                try {
                    this.parent.onSuccessCall.accept(t);
                    this.upstream = DisposableHelper.DISPOSED;
                    this.downstream.onSuccess(t);
                    onAfterTerminate();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    onErrorInner(th);
                }
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            if (this.upstream == DisposableHelper.DISPOSED) {
                RxJavaPlugins.onError(th);
            } else {
                onErrorInner(th);
            }
        }

        void onErrorInner(Throwable th) {
            try {
                this.parent.onErrorCall.accept(th);
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                th = new CompositeException(th, th2);
            }
            this.upstream = DisposableHelper.DISPOSED;
            this.downstream.onError(th);
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
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    onErrorInner(th);
                }
            }
        }

        void onAfterTerminate() {
            try {
                this.parent.onAfterTerminate.run();
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(th);
            }
        }
    }
}
