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

    public SingleUsing(Callable<U> resourceSupplier, Function<? super U, ? extends SingleSource<? extends T>> singleFunction, Consumer<? super U> disposer, boolean eager) {
        this.resourceSupplier = resourceSupplier;
        this.singleFunction = singleFunction;
        this.disposer = disposer;
        this.eager = eager;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        try {
            U resource = this.resourceSupplier.call();
            try {
                ((SingleSource) ObjectHelper.requireNonNull(this.singleFunction.apply(resource), "The singleFunction returned a null SingleSource")).subscribe(new UsingSingleObserver<>(observer, resource, this.eager, this.disposer));
            } catch (Throwable th) {
                ex = th;
                Exceptions.throwIfFatal(ex);
                if (this.eager) {
                    try {
                        this.disposer.accept(resource);
                    } catch (Throwable exc) {
                        Exceptions.throwIfFatal(exc);
                        ex = new CompositeException(ex, exc);
                    }
                }
                EmptyDisposable.error(ex, observer);
                if (!this.eager) {
                    try {
                        this.disposer.accept(resource);
                    } catch (Throwable exc2) {
                        Exceptions.throwIfFatal(exc2);
                        RxJavaPlugins.onError(exc2);
                    }
                }
            }
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
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

        UsingSingleObserver(SingleObserver<? super T> actual, U resource, boolean eager, Consumer<? super U> disposer) {
            super(resource);
            this.downstream = actual;
            this.eager = eager;
            this.disposer = disposer;
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
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.upstream = DisposableHelper.DISPOSED;
            if (this.eager) {
                Object u = getAndSet(this);
                if (u != this) {
                    try {
                        this.disposer.accept(u);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        this.downstream.onError(ex);
                        return;
                    }
                } else {
                    return;
                }
            }
            this.downstream.onSuccess(value);
            if (!this.eager) {
                disposeAfter();
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.upstream = DisposableHelper.DISPOSED;
            if (this.eager) {
                Object u = getAndSet(this);
                if (u != this) {
                    try {
                        this.disposer.accept(u);
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        e = new CompositeException(e, ex);
                    }
                } else {
                    return;
                }
            }
            this.downstream.onError(e);
            if (!this.eager) {
                disposeAfter();
            }
        }

        void disposeAfter() {
            Object u = getAndSet(this);
            if (u != this) {
                try {
                    this.disposer.accept(u);
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    RxJavaPlugins.onError(ex);
                }
            }
        }
    }
}
