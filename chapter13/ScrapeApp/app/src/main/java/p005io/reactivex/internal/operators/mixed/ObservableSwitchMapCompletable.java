package p005io.reactivex.internal.operators.mixed;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.mixed.ObservableSwitchMapCompletable */
/* loaded from: classes.dex */
public final class ObservableSwitchMapCompletable<T> extends Completable {
    final boolean delayErrors;
    final Function<? super T, ? extends CompletableSource> mapper;
    final Observable<T> source;

    public ObservableSwitchMapCompletable(Observable<T> source, Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayErrors;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        if (!ScalarXMapZHelper.tryAsCompletable(this.source, this.mapper, observer)) {
            this.source.subscribe(new SwitchMapCompletableObserver(observer, this.mapper, this.delayErrors));
        }
    }

    /* renamed from: io.reactivex.internal.operators.mixed.ObservableSwitchMapCompletable$SwitchMapCompletableObserver */
    /* loaded from: classes.dex */
    static final class SwitchMapCompletableObserver<T> implements Observer<T>, Disposable {
        static final SwitchMapInnerObserver INNER_DISPOSED = new SwitchMapInnerObserver(null);
        final boolean delayErrors;
        volatile boolean done;
        final CompletableObserver downstream;
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicReference<SwitchMapInnerObserver> inner = new AtomicReference<>();
        final Function<? super T, ? extends CompletableSource> mapper;
        Disposable upstream;

        SwitchMapCompletableObserver(CompletableObserver downstream, Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            SwitchMapInnerObserver current;
            try {
                CompletableSource c = (CompletableSource) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null CompletableSource");
                SwitchMapInnerObserver o = new SwitchMapInnerObserver(this);
                do {
                    current = this.inner.get();
                    if (current == INNER_DISPOSED) {
                        return;
                    }
                } while (!this.inner.compareAndSet(current, o));
                if (current != null) {
                    current.dispose();
                }
                c.subscribe(o);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                this.upstream.dispose();
                onError(ex);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (!this.errors.addThrowable(t)) {
                RxJavaPlugins.onError(t);
            } else if (this.delayErrors) {
                onComplete();
            } else {
                disposeInner();
                Throwable ex = this.errors.terminate();
                if (ex != ExceptionHelper.TERMINATED) {
                    this.downstream.onError(ex);
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.done = true;
            if (this.inner.get() == null) {
                Throwable ex = this.errors.terminate();
                if (ex == null) {
                    this.downstream.onComplete();
                } else {
                    this.downstream.onError(ex);
                }
            }
        }

        void disposeInner() {
            SwitchMapInnerObserver o = this.inner.getAndSet(INNER_DISPOSED);
            if (o != null && o != INNER_DISPOSED) {
                o.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            disposeInner();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.inner.get() == INNER_DISPOSED;
        }

        void innerError(SwitchMapInnerObserver sender, Throwable error) {
            if (!this.inner.compareAndSet(sender, null) || !this.errors.addThrowable(error)) {
                RxJavaPlugins.onError(error);
            } else if (!this.delayErrors) {
                dispose();
                Throwable ex = this.errors.terminate();
                if (ex != ExceptionHelper.TERMINATED) {
                    this.downstream.onError(ex);
                }
            } else if (this.done) {
                this.downstream.onError(this.errors.terminate());
            }
        }

        void innerComplete(SwitchMapInnerObserver sender) {
            if (this.inner.compareAndSet(sender, null) && this.done) {
                Throwable ex = this.errors.terminate();
                if (ex == null) {
                    this.downstream.onComplete();
                } else {
                    this.downstream.onError(ex);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.mixed.ObservableSwitchMapCompletable$SwitchMapCompletableObserver$SwitchMapInnerObserver */
        /* loaded from: classes.dex */
        public static final class SwitchMapInnerObserver extends AtomicReference<Disposable> implements CompletableObserver {
            private static final long serialVersionUID = -8003404460084760287L;
            final SwitchMapCompletableObserver<?> parent;

            SwitchMapInnerObserver(SwitchMapCompletableObserver<?> parent) {
                this.parent = parent;
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onError(Throwable e) {
                this.parent.innerError(this, e);
            }

            @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.parent.innerComplete(this);
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
