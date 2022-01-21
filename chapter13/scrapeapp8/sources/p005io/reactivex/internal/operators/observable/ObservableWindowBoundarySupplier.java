package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.observers.DisposableObserver;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.subjects.UnicastSubject;

/* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySupplier */
/* loaded from: classes.dex */
public final class ObservableWindowBoundarySupplier<T, B> extends AbstractObservableWithUpstream<T, Observable<T>> {
    final int capacityHint;
    final Callable<? extends ObservableSource<B>> other;

    public ObservableWindowBoundarySupplier(ObservableSource<T> source, Callable<? extends ObservableSource<B>> other, int capacityHint) {
        super(source);
        this.other = other;
        this.capacityHint = capacityHint;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Observable<T>> observer) {
        this.source.subscribe(new WindowBoundaryMainObserver<>(observer, this.capacityHint, this.other));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySupplier$WindowBoundaryMainObserver */
    /* loaded from: classes.dex */
    static final class WindowBoundaryMainObserver<T, B> extends AtomicInteger implements Observer<T>, Disposable, Runnable {
        static final WindowBoundaryInnerObserver<Object, Object> BOUNDARY_DISPOSED = new WindowBoundaryInnerObserver<>(null);
        static final Object NEXT_WINDOW = new Object();
        private static final long serialVersionUID = 2233020065421370272L;
        final int capacityHint;
        volatile boolean done;
        final Observer<? super Observable<T>> downstream;
        final Callable<? extends ObservableSource<B>> other;
        Disposable upstream;
        UnicastSubject<T> window;
        final AtomicReference<WindowBoundaryInnerObserver<T, B>> boundaryObserver = new AtomicReference<>();
        final AtomicInteger windows = new AtomicInteger(1);
        final MpscLinkedQueue<Object> queue = new MpscLinkedQueue<>();
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicBoolean stopWindows = new AtomicBoolean();

        WindowBoundaryMainObserver(Observer<? super Observable<T>> downstream, int capacityHint, Callable<? extends ObservableSource<B>> other) {
            this.downstream = downstream;
            this.capacityHint = capacityHint;
            this.other = other;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
                this.queue.offer(NEXT_WINDOW);
                drain();
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            disposeBoundary();
            if (this.errors.addThrowable(e)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(e);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            disposeBoundary();
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (this.stopWindows.compareAndSet(false, true)) {
                disposeBoundary();
                if (this.windows.decrementAndGet() == 0) {
                    this.upstream.dispose();
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        void disposeBoundary() {
            Disposable d = (Disposable) this.boundaryObserver.getAndSet(BOUNDARY_DISPOSED);
            if (d != null && d != BOUNDARY_DISPOSED) {
                d.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.stopWindows.get();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.windows.decrementAndGet() == 0) {
                this.upstream.dispose();
            }
        }

        void innerNext(WindowBoundaryInnerObserver<T, B> sender) {
            this.boundaryObserver.compareAndSet(sender, null);
            this.queue.offer(NEXT_WINDOW);
            drain();
        }

        void innerError(Throwable e) {
            this.upstream.dispose();
            if (this.errors.addThrowable(e)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(e);
        }

        void innerComplete() {
            this.upstream.dispose();
            this.done = true;
            drain();
        }

        /* JADX WARN: Multi-variable type inference failed */
        void drain() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                Observer<? super Observable<T>> downstream = this.downstream;
                MpscLinkedQueue<Object> queue = this.queue;
                AtomicThrowable errors = this.errors;
                while (this.windows.get() != 0) {
                    UnicastSubject<T> w = this.window;
                    boolean d = this.done;
                    if (!d || errors.get() == null) {
                        Object v = queue.poll();
                        boolean empty = v == null;
                        if (d && empty) {
                            Throwable ex = errors.terminate();
                            if (ex == null) {
                                if (w != 0) {
                                    this.window = null;
                                    w.onComplete();
                                }
                                downstream.onComplete();
                                return;
                            }
                            if (w != 0) {
                                this.window = null;
                                w.onError(ex);
                            }
                            downstream.onError(ex);
                            return;
                        } else if (empty) {
                            missed = addAndGet(-missed);
                            if (missed == 0) {
                                return;
                            }
                        } else if (v != NEXT_WINDOW) {
                            w.onNext(v);
                        } else {
                            if (w != 0) {
                                this.window = null;
                                w.onComplete();
                            }
                            if (!this.stopWindows.get()) {
                                UnicastSubject<T> w2 = UnicastSubject.create(this.capacityHint, this);
                                this.window = w2;
                                this.windows.getAndIncrement();
                                try {
                                    ObservableSource<B> otherSource = (ObservableSource) ObjectHelper.requireNonNull(this.other.call(), "The other Callable returned a null ObservableSource");
                                    WindowBoundaryInnerObserver<T, B> bo = new WindowBoundaryInnerObserver<>(this);
                                    if (this.boundaryObserver.compareAndSet(null, bo)) {
                                        otherSource.subscribe(bo);
                                        downstream.onNext(w2);
                                    }
                                } catch (Throwable ex2) {
                                    Exceptions.throwIfFatal(ex2);
                                    errors.addThrowable(ex2);
                                    this.done = true;
                                }
                            }
                        }
                    } else {
                        queue.clear();
                        Throwable ex3 = errors.terminate();
                        if (w != 0) {
                            this.window = null;
                            w.onError(ex3);
                        }
                        downstream.onError(ex3);
                        return;
                    }
                }
                queue.clear();
                this.window = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySupplier$WindowBoundaryInnerObserver */
    /* loaded from: classes.dex */
    public static final class WindowBoundaryInnerObserver<T, B> extends DisposableObserver<B> {
        boolean done;
        final WindowBoundaryMainObserver<T, B> parent;

        WindowBoundaryInnerObserver(WindowBoundaryMainObserver<T, B> parent) {
            this.parent = parent;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(B t) {
            if (!this.done) {
                this.done = true;
                dispose();
                this.parent.innerNext(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.parent.innerError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.parent.innerComplete();
            }
        }
    }
}
