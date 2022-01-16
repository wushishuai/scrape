package p005io.reactivex.internal.operators.mixed;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.ErrorMode;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.mixed.ObservableConcatMapMaybe */
/* loaded from: classes.dex */
public final class ObservableConcatMapMaybe<T, R> extends Observable<R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
    final int prefetch;
    final Observable<T> source;

    public ObservableConcatMapMaybe(Observable<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper, ErrorMode errorMode, int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super R> observer) {
        if (!ScalarXMapZHelper.tryAsMaybe(this.source, this.mapper, observer)) {
            this.source.subscribe(new ConcatMapMaybeMainObserver(observer, this.mapper, this.prefetch, this.errorMode));
        }
    }

    /* renamed from: io.reactivex.internal.operators.mixed.ObservableConcatMapMaybe$ConcatMapMaybeMainObserver */
    /* loaded from: classes.dex */
    static final class ConcatMapMaybeMainObserver<T, R> extends AtomicInteger implements Observer<T>, Disposable {
        static final int STATE_ACTIVE = 1;
        static final int STATE_INACTIVE = 0;
        static final int STATE_RESULT_VALUE = 2;
        private static final long serialVersionUID = -9140123220065488293L;
        volatile boolean cancelled;
        volatile boolean done;
        final Observer<? super R> downstream;
        final ErrorMode errorMode;
        final AtomicThrowable errors = new AtomicThrowable();
        final ConcatMapMaybeObserver<R> inner = new ConcatMapMaybeObserver<>(this);
        R item;
        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
        final SimplePlainQueue<T> queue;
        volatile int state;
        Disposable upstream;

        ConcatMapMaybeMainObserver(Observer<? super R> downstream, Function<? super T, ? extends MaybeSource<? extends R>> mapper, int prefetch, ErrorMode errorMode) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.errorMode = errorMode;
            this.queue = new SpscLinkedArrayQueue(prefetch);
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
            this.queue.offer(t);
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (this.errors.addThrowable(t)) {
                if (this.errorMode == ErrorMode.IMMEDIATE) {
                    this.inner.dispose();
                }
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.cancelled = true;
            this.upstream.dispose();
            this.inner.dispose();
            if (getAndIncrement() == 0) {
                this.queue.clear();
                this.item = null;
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void innerSuccess(R item) {
            this.item = item;
            this.state = 2;
            drain();
        }

        void innerComplete() {
            this.state = 0;
            drain();
        }

        void innerError(Throwable ex) {
            if (this.errors.addThrowable(ex)) {
                if (this.errorMode != ErrorMode.END) {
                    this.upstream.dispose();
                }
                this.state = 0;
                drain();
                return;
            }
            RxJavaPlugins.onError(ex);
        }

        void drain() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                Observer<? super R> downstream = this.downstream;
                ErrorMode errorMode = this.errorMode;
                SimplePlainQueue<T> queue = this.queue;
                AtomicThrowable errors = this.errors;
                while (true) {
                    if (this.cancelled) {
                        queue.clear();
                        this.item = null;
                    } else {
                        int s = this.state;
                        if (errors.get() == null || !(errorMode == ErrorMode.IMMEDIATE || (errorMode == ErrorMode.BOUNDARY && s == 0))) {
                            boolean empty = false;
                            if (s == 0) {
                                boolean d = this.done;
                                T v = queue.poll();
                                if (v == null) {
                                    empty = true;
                                }
                                if (d && empty) {
                                    Throwable ex = errors.terminate();
                                    if (ex == null) {
                                        downstream.onComplete();
                                        return;
                                    } else {
                                        downstream.onError(ex);
                                        return;
                                    }
                                } else if (!empty) {
                                    try {
                                        MaybeSource<? extends R> ms = (MaybeSource) ObjectHelper.requireNonNull(this.mapper.apply(v), "The mapper returned a null MaybeSource");
                                        this.state = 1;
                                        ms.subscribe(this.inner);
                                    } catch (Throwable ex2) {
                                        Exceptions.throwIfFatal(ex2);
                                        this.upstream.dispose();
                                        queue.clear();
                                        errors.addThrowable(ex2);
                                        downstream.onError(errors.terminate());
                                        return;
                                    }
                                }
                            } else if (s == 2) {
                                this.item = null;
                                downstream.onNext((R) this.item);
                                this.state = 0;
                            }
                        }
                    }
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        return;
                    }
                }
                queue.clear();
                this.item = null;
                downstream.onError(errors.terminate());
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.mixed.ObservableConcatMapMaybe$ConcatMapMaybeMainObserver$ConcatMapMaybeObserver */
        /* loaded from: classes.dex */
        public static final class ConcatMapMaybeObserver<R> extends AtomicReference<Disposable> implements MaybeObserver<R> {
            private static final long serialVersionUID = -3051469169682093892L;
            final ConcatMapMaybeMainObserver<?, R> parent;

            ConcatMapMaybeObserver(ConcatMapMaybeMainObserver<?, R> parent) {
                this.parent = parent;
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSuccess(R t) {
                this.parent.innerSuccess(t);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onError(Throwable e) {
                this.parent.innerError(e);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.parent.innerComplete();
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
