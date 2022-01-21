package p005io.reactivex.internal.operators.observable;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.QueueDisposable;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.observers.InnerQueuedObserver;
import p005io.reactivex.internal.observers.InnerQueuedObserverSupport;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.ErrorMode;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableConcatMapEager */
/* loaded from: classes.dex */
public final class ObservableConcatMapEager<T, R> extends AbstractObservableWithUpstream<T, R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends ObservableSource<? extends R>> mapper;
    final int maxConcurrency;
    final int prefetch;

    public ObservableConcatMapEager(ObservableSource<T> source, Function<? super T, ? extends ObservableSource<? extends R>> mapper, ErrorMode errorMode, int maxConcurrency, int prefetch) {
        super(source);
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.maxConcurrency = maxConcurrency;
        this.prefetch = prefetch;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super R> observer) {
        this.source.subscribe(new ConcatMapEagerMainObserver(observer, this.mapper, this.maxConcurrency, this.prefetch, this.errorMode));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableConcatMapEager$ConcatMapEagerMainObserver */
    /* loaded from: classes.dex */
    static final class ConcatMapEagerMainObserver<T, R> extends AtomicInteger implements Observer<T>, Disposable, InnerQueuedObserverSupport<R> {
        private static final long serialVersionUID = 8080567949447303262L;
        int activeCount;
        volatile boolean cancelled;
        InnerQueuedObserver<R> current;
        volatile boolean done;
        final Observer<? super R> downstream;
        final ErrorMode errorMode;
        final Function<? super T, ? extends ObservableSource<? extends R>> mapper;
        final int maxConcurrency;
        final int prefetch;
        SimpleQueue<T> queue;
        int sourceMode;
        Disposable upstream;
        final AtomicThrowable error = new AtomicThrowable();
        final ArrayDeque<InnerQueuedObserver<R>> observers = new ArrayDeque<>();

        ConcatMapEagerMainObserver(Observer<? super R> actual, Function<? super T, ? extends ObservableSource<? extends R>> mapper, int maxConcurrency, int prefetch, ErrorMode errorMode) {
            this.downstream = actual;
            this.mapper = mapper;
            this.maxConcurrency = maxConcurrency;
            this.prefetch = prefetch;
            this.errorMode = errorMode;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                if (d instanceof QueueDisposable) {
                    QueueDisposable<T> qd = (QueueDisposable) d;
                    int m = qd.requestFusion(3);
                    if (m == 1) {
                        this.sourceMode = m;
                        this.queue = qd;
                        this.done = true;
                        this.downstream.onSubscribe(this);
                        drain();
                        return;
                    } else if (m == 2) {
                        this.sourceMode = m;
                        this.queue = qd;
                        this.downstream.onSubscribe(this);
                        return;
                    }
                }
                this.queue = new SpscLinkedArrayQueue(this.prefetch);
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T value) {
            if (this.sourceMode == 0) {
                this.queue.offer(value);
            }
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            if (this.error.addThrowable(e)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(e);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.cancelled = true;
            if (getAndIncrement() == 0) {
                this.queue.clear();
                disposeAll();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void disposeAll() {
            InnerQueuedObserver<R> inner = this.current;
            if (inner != null) {
                inner.dispose();
            }
            while (true) {
                InnerQueuedObserver<R> inner2 = this.observers.poll();
                if (inner2 != null) {
                    inner2.dispose();
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.observers.InnerQueuedObserverSupport
        public void innerNext(InnerQueuedObserver<R> inner, R value) {
            inner.queue().offer(value);
            drain();
        }

        @Override // p005io.reactivex.internal.observers.InnerQueuedObserverSupport
        public void innerError(InnerQueuedObserver<R> inner, Throwable e) {
            if (this.error.addThrowable(e)) {
                if (this.errorMode == ErrorMode.IMMEDIATE) {
                    this.upstream.dispose();
                }
                inner.setDone();
                drain();
                return;
            }
            RxJavaPlugins.onError(e);
        }

        @Override // p005io.reactivex.internal.observers.InnerQueuedObserverSupport
        public void innerComplete(InnerQueuedObserver<R> inner) {
            inner.setDone();
            drain();
        }

        /* JADX INFO: Multiple debug info for r6v3 io.reactivex.internal.observers.InnerQueuedObserver<R>: [D('active' io.reactivex.internal.observers.InnerQueuedObserver<R>), D('ex' java.lang.Throwable)] */
        /* JADX INFO: Multiple debug info for r9v2 boolean: [D('ex' java.lang.Throwable), D('d' boolean)] */
        @Override // p005io.reactivex.internal.observers.InnerQueuedObserverSupport
        public void drain() {
            R w;
            boolean empty;
            if (getAndIncrement() == 0) {
                int missed = 1;
                SimpleQueue<T> q = this.queue;
                ArrayDeque<InnerQueuedObserver<R>> observers = this.observers;
                Observer<? super R> a = this.downstream;
                ErrorMode errorMode = this.errorMode;
                while (true) {
                    int ac = this.activeCount;
                    while (ac != this.maxConcurrency) {
                        if (this.cancelled) {
                            q.clear();
                            disposeAll();
                            return;
                        } else if (errorMode != ErrorMode.IMMEDIATE || this.error.get() == null) {
                            try {
                                T v = q.poll();
                                if (v == null) {
                                    break;
                                }
                                ObservableSource<? extends R> source = (ObservableSource) ObjectHelper.requireNonNull(this.mapper.apply(v), "The mapper returned a null ObservableSource");
                                InnerQueuedObserver<R> inner = new InnerQueuedObserver<>(this, this.prefetch);
                                observers.offer(inner);
                                source.subscribe(inner);
                                ac++;
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                this.upstream.dispose();
                                q.clear();
                                disposeAll();
                                this.error.addThrowable(ex);
                                a.onError(this.error.terminate());
                                return;
                            }
                        } else {
                            q.clear();
                            disposeAll();
                            a.onError(this.error.terminate());
                            return;
                        }
                    }
                    this.activeCount = ac;
                    if (this.cancelled) {
                        q.clear();
                        disposeAll();
                        return;
                    } else if (errorMode != ErrorMode.IMMEDIATE || this.error.get() == null) {
                        InnerQueuedObserver<R> active = this.current;
                        if (active == null) {
                            if (errorMode != ErrorMode.BOUNDARY || this.error.get() == null) {
                                boolean d = this.done;
                                active = observers.poll();
                                boolean empty2 = active == null;
                                if (!d || !empty2) {
                                    if (!empty2) {
                                        this.current = active;
                                    }
                                } else if (this.error.get() != null) {
                                    q.clear();
                                    disposeAll();
                                    a.onError(this.error.terminate());
                                    return;
                                } else {
                                    a.onComplete();
                                    return;
                                }
                            } else {
                                q.clear();
                                disposeAll();
                                a.onError(this.error.terminate());
                                return;
                            }
                        }
                        if (active != null) {
                            SimpleQueue<R> aq = active.queue();
                            while (!this.cancelled) {
                                boolean d2 = active.isDone();
                                if (errorMode != ErrorMode.IMMEDIATE || this.error.get() == null) {
                                    try {
                                        w = aq.poll();
                                        empty = w == null;
                                    } catch (Throwable ex2) {
                                        Exceptions.throwIfFatal(ex2);
                                        this.error.addThrowable(ex2);
                                        this.current = null;
                                        this.activeCount--;
                                    }
                                    if (d2 && empty) {
                                        this.current = null;
                                        this.activeCount--;
                                    } else if (!empty) {
                                        a.onNext(w);
                                    }
                                } else {
                                    q.clear();
                                    disposeAll();
                                    a.onError(this.error.terminate());
                                    return;
                                }
                            }
                            q.clear();
                            disposeAll();
                            return;
                        }
                        missed = addAndGet(-missed);
                        if (missed == 0) {
                            return;
                        }
                    } else {
                        q.clear();
                        disposeAll();
                        a.onError(this.error.terminate());
                        return;
                    }
                }
            }
        }
    }
}
