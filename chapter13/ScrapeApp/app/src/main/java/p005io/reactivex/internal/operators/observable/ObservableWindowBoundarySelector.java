package p005io.reactivex.internal.operators.observable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.QueueDrainObserver;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.observers.DisposableObserver;
import p005io.reactivex.observers.SerializedObserver;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.subjects.UnicastSubject;

/* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySelector */
/* loaded from: classes.dex */
public final class ObservableWindowBoundarySelector<T, B, V> extends AbstractObservableWithUpstream<T, Observable<T>> {
    final int bufferSize;
    final Function<? super B, ? extends ObservableSource<V>> close;
    final ObservableSource<B> open;

    public ObservableWindowBoundarySelector(ObservableSource<T> source, ObservableSource<B> open, Function<? super B, ? extends ObservableSource<V>> close, int bufferSize) {
        super(source);
        this.open = open;
        this.close = close;
        this.bufferSize = bufferSize;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Observable<T>> t) {
        this.source.subscribe(new WindowBoundaryMainObserver(new SerializedObserver(t), this.open, this.close, this.bufferSize));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySelector$WindowBoundaryMainObserver */
    /* loaded from: classes.dex */
    public static final class WindowBoundaryMainObserver<T, B, V> extends QueueDrainObserver<T, Object, Observable<T>> implements Disposable {
        final int bufferSize;
        final Function<? super B, ? extends ObservableSource<V>> close;
        final ObservableSource<B> open;
        Disposable upstream;
        final AtomicReference<Disposable> boundary = new AtomicReference<>();
        final AtomicLong windows = new AtomicLong();
        final CompositeDisposable resources = new CompositeDisposable();

        /* renamed from: ws */
        final List<UnicastSubject<T>> f165ws = new ArrayList();

        WindowBoundaryMainObserver(Observer<? super Observable<T>> actual, ObservableSource<B> open, Function<? super B, ? extends ObservableSource<V>> close, int bufferSize) {
            super(actual, new MpscLinkedQueue());
            this.open = open;
            this.close = close;
            this.bufferSize = bufferSize;
            this.windows.lazySet(1);
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
                if (!this.cancelled) {
                    OperatorWindowBoundaryOpenObserver<T, B> os = new OperatorWindowBoundaryOpenObserver<>(this);
                    if (this.boundary.compareAndSet(null, os)) {
                        this.windows.getAndIncrement();
                        this.open.subscribe(os);
                    }
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (fastEnter()) {
                for (UnicastSubject<T> w : this.f165ws) {
                    w.onNext(t);
                }
                if (leave(-1) == 0) {
                    return;
                }
            } else {
                this.queue.offer(NotificationLite.next(t));
                if (!enter()) {
                    return;
                }
            }
            drainLoop();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.error = t;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            if (this.windows.decrementAndGet() == 0) {
                this.resources.dispose();
            }
            this.downstream.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                if (enter()) {
                    drainLoop();
                }
                if (this.windows.decrementAndGet() == 0) {
                    this.resources.dispose();
                }
                this.downstream.onComplete();
            }
        }

        void error(Throwable t) {
            this.upstream.dispose();
            this.resources.dispose();
            onError(t);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.cancelled = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void disposeBoundary() {
            this.resources.dispose();
            DisposableHelper.dispose(this.boundary);
        }

        void drainLoop() {
            MpscLinkedQueue<Object> q = (MpscLinkedQueue) this.queue;
            Observer<? super Observable<T>> a = this.downstream;
            List<UnicastSubject<T>> ws = this.f165ws;
            int missed = 1;
            while (true) {
                boolean d = this.done;
                Object o = q.poll();
                boolean empty = o == null;
                if (d && empty) {
                    disposeBoundary();
                    Throwable e = this.error;
                    if (e != null) {
                        for (UnicastSubject<T> w : ws) {
                            w.onError(e);
                        }
                    } else {
                        for (UnicastSubject<T> w2 : ws) {
                            w2.onComplete();
                        }
                    }
                    ws.clear();
                    return;
                } else if (empty) {
                    missed = leave(-missed);
                    if (missed == 0) {
                        return;
                    }
                } else if (o instanceof WindowOperation) {
                    WindowOperation<T, B> wo = (WindowOperation) o;
                    if (wo.f166w != null) {
                        if (ws.remove(wo.f166w)) {
                            wo.f166w.onComplete();
                            if (this.windows.decrementAndGet() == 0) {
                                disposeBoundary();
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (!this.cancelled) {
                        UnicastSubject<T> w3 = UnicastSubject.create(this.bufferSize);
                        ws.add(w3);
                        a.onNext(w3);
                        try {
                            ObservableSource<V> p = (ObservableSource) ObjectHelper.requireNonNull(this.close.apply((B) wo.open), "The ObservableSource supplied is null");
                            OperatorWindowBoundaryCloseObserver<T, V> cl = new OperatorWindowBoundaryCloseObserver<>(this, w3);
                            if (this.resources.add(cl)) {
                                this.windows.getAndIncrement();
                                p.subscribe(cl);
                            }
                        } catch (Throwable e2) {
                            Exceptions.throwIfFatal(e2);
                            this.cancelled = true;
                            a.onError(e2);
                        }
                    }
                } else {
                    for (UnicastSubject<T> w4 : ws) {
                        w4.onNext((T) NotificationLite.getValue(o));
                    }
                }
            }
        }

        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public void accept(Observer<? super Observable<T>> a, Object v) {
        }

        void open(B b) {
            this.queue.offer(new WindowOperation(null, b));
            if (enter()) {
                drainLoop();
            }
        }

        void close(OperatorWindowBoundaryCloseObserver<T, V> w) {
            this.resources.delete(w);
            this.queue.offer(new WindowOperation(w.f164w, null));
            if (enter()) {
                drainLoop();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySelector$WindowOperation */
    /* loaded from: classes.dex */
    public static final class WindowOperation<T, B> {
        final B open;

        /* renamed from: w */
        final UnicastSubject<T> f166w;

        WindowOperation(UnicastSubject<T> w, B open) {
            this.f166w = w;
            this.open = open;
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySelector$OperatorWindowBoundaryOpenObserver */
    /* loaded from: classes.dex */
    static final class OperatorWindowBoundaryOpenObserver<T, B> extends DisposableObserver<B> {
        final WindowBoundaryMainObserver<T, B, ?> parent;

        OperatorWindowBoundaryOpenObserver(WindowBoundaryMainObserver<T, B, ?> parent) {
            this.parent = parent;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(B t) {
            this.parent.open(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.parent.error(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.parent.onComplete();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySelector$OperatorWindowBoundaryCloseObserver */
    /* loaded from: classes.dex */
    public static final class OperatorWindowBoundaryCloseObserver<T, V> extends DisposableObserver<V> {
        boolean done;
        final WindowBoundaryMainObserver<T, ?, V> parent;

        /* renamed from: w */
        final UnicastSubject<T> f164w;

        OperatorWindowBoundaryCloseObserver(WindowBoundaryMainObserver<T, ?, V> parent, UnicastSubject<T> w) {
            this.parent = parent;
            this.f164w = w;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(V t) {
            dispose();
            onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.parent.error(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.parent.close(this);
            }
        }
    }
}
