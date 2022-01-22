package p005io.reactivex.internal.operators.single;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleUsing */
/* loaded from: classes.dex */
public final class SingleUsing<T, U> extends Single<T> {
    final Consumer<? super U> disposer;
    final boolean eager;
    final Callable<U> resourceSupplier;
    final Function<? super U, ? extends SingleSource<? extends T>> singleFunction;

    public SingleUsing(Callable<U> callable, Function<? super U, ? extends SingleSource<? extends T>> function, Consumer<? super U> consumer, boolean z) {
        this.resourceSupplier = callable;
        this.singleFunction = function;
        this.disposer = consumer;
        this.eager = z;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        try {
            U call = this.resourceSupplier.call();
            try {
                ((SingleSource) ObjectHelper.requireNonNull(this.singleFunction.apply(call), "The singleFunction returned a null SingleSource")).subscribe(new UsingSingleObserver(singleObserver, call, this.eager, this.disposer));
            } catch (Throwable th) {
                th = th;
                Exceptions.throwIfFatal(th);
                if (this.eager) {
                    try {
                        this.disposer.accept(call);
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        th = new CompositeException(th, th2);
                    }
                }
                EmptyDisposable.error(th, singleObserver);
                if (!this.eager) {
                    try {
                        this.disposer.accept(call);
                    } catch (Throwable th3) {
                        Exceptions.throwIfFatal(th3);
                        RxJavaPlugins.onError(th3);
                    }
                }
            }
        } catch (Throwable th4) {
            Exceptions.throwIfFatal(th4);
            EmptyDisposable.error(th4, singleObserver);
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleUsing$UsingSingleObserver */
    /* loaded from: classes.dex */
    static final class UsingSingleObserver<T, U> extends AtomicReference<Object> implements SingleObserver<T>, Disposable {
        private static final long serialVersionUID = -5331524057054083935L;
        final Consumer<? super U> disposer;
        final SingleObserver<? super T> downstream;
        final boolean eager;
        Disposable upstream;

        UsingSingleObserver(SingleObserver<? super T> singleObserver, U u, boolean z, Consumer<? super U> consumer) {
            super(u);
            this.downstream = singleObserver;
            this.eager = z;
            this.disposer = consumer;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            this.upstream = DisposableHelper.DISPOSED;
            disposeAfter();
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
            if (this.eager) {
                Object andSet = getAndSet(this);
                if (andSet != this) {
                    try {
                        this.disposer.accept(andSet);
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.downstream.onError(th);
                        return;
                    }
                } else {
                    return;
                }
            }
            this.downstream.onSuccess(t);
            if (!this.eager) {
                disposeAfter();
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.upstream = DisposableHelper.DISPOSED;
            if (this.eager) {
                Object andSet = getAndSet(this);
                if (andSet != this) {
                    try {
                        this.disposer.accept(andSet);
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        th = new CompositeException(th, th2);
                    }
                } else {
                    return;
                }
            }
            this.downstream.onError(th);
            if (!this.eager) {
                disposeAfter();
            }
        }

        void disposeAfter() {
            Object andSet = getAndSet(this);
            if (andSet != this) {
                try {
                    this.disposer.accept(andSet);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    RxJavaPlugins.onError(th);
                }
            }
        }
    }
}
