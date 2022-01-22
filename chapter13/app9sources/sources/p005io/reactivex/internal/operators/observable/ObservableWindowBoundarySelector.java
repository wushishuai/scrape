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

    public ObservableWindowBoundarySelector(ObservableSource<T> observableSource, ObservableSource<B> observableSource2, Function<? super B, ? extends ObservableSource<V>> function, int i) {
        super(observableSource);
        this.open = observableSource2;
        this.close = function;
        this.bufferSize = i;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Observable<T>> observer) {
        this.source.subscribe(new WindowBoundaryMainObserver(new SerializedObserver(observer), this.open, this.close, this.bufferSize));
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

        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public void accept(Observer<? super Observable<T>> observer, Object obj) {
        }

        WindowBoundaryMainObserver(Observer<? super Observable<T>> observer, ObservableSource<B> observableSource, Function<? super B, ? extends ObservableSource<V>> function, int i) {
            super(observer, new MpscLinkedQueue());
            this.open = observableSource;
            this.close = function;
            this.bufferSize = i;
            this.windows.lazySet(1);
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
                if (!this.cancelled) {
                    OperatorWindowBoundaryOpenObserver operatorWindowBoundaryOpenObserver = new OperatorWindowBoundaryOpenObserver(this);
                    if (this.boundary.compareAndSet(null, operatorWindowBoundaryOpenObserver)) {
                        this.windows.getAndIncrement();
                        this.open.subscribe(operatorWindowBoundaryOpenObserver);
                    }
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (fastEnter()) {
                for (UnicastSubject<T> unicastSubject : this.f165ws) {
                    unicastSubject.onNext(t);
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
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.error = th;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            if (this.windows.decrementAndGet() == 0) {
                this.resources.dispose();
            }
            this.downstream.onError(th);
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

        void error(Throwable th) {
            this.upstream.dispose();
            this.resources.dispose();
            onError(th);
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
            MpscLinkedQueue mpscLinkedQueue = (MpscLinkedQueue) this.queue;
            Observer observer = this.downstream;
            List<UnicastSubject<T>> list = this.f165ws;
            int i = 1;
            while (true) {
                boolean z = this.done;
                Object poll = mpscLinkedQueue.poll();
                boolean z2 = poll == null;
                if (z && z2) {
                    disposeBoundary();
                    Throwable th = this.error;
                    if (th != null) {
                        for (UnicastSubject<T> unicastSubject : list) {
                            unicastSubject.onError(th);
                        }
                    } else {
                        for (UnicastSubject<T> unicastSubject2 : list) {
                            unicastSubject2.onComplete();
                        }
                    }
                    list.clear();
                    return;
                } else if (z2) {
                    i = leave(-i);
                    if (i == 0) {
                        return;
                    }
                } else if (poll instanceof WindowOperation) {
                    WindowOperation windowOperation = (WindowOperation) poll;
                    if (windowOperation.f166w != null) {
                        if (list.remove(windowOperation.f166w)) {
                            windowOperation.f166w.onComplete();
                            if (this.windows.decrementAndGet() == 0) {
                                disposeBoundary();
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (!this.cancelled) {
                        UnicastSubject<T> create = UnicastSubject.create(this.bufferSize);
                        list.add(create);
                        observer.onNext(create);
                        try {
                            ObservableSource observableSource = (ObservableSource) ObjectHelper.requireNonNull(this.close.apply((B) windowOperation.open), "The ObservableSource supplied is null");
                            OperatorWindowBoundaryCloseObserver operatorWindowBoundaryCloseObserver = new OperatorWindowBoundaryCloseObserver(this, create);
                            if (this.resources.add(operatorWindowBoundaryCloseObserver)) {
                                this.windows.getAndIncrement();
                                observableSource.subscribe(operatorWindowBoundaryCloseObserver);
                            }
                        } catch (Throwable th2) {
                            Exceptions.throwIfFatal(th2);
                            this.cancelled = true;
                            observer.onError(th2);
                        }
                    }
                } else {
                    for (UnicastSubject<T> unicastSubject3 : list) {
                        unicastSubject3.onNext((T) NotificationLite.getValue(poll));
                    }
                }
            }
        }

        void open(B b) {
            this.queue.offer(new WindowOperation(null, b));
            if (enter()) {
                drainLoop();
            }
        }

        void close(OperatorWindowBoundaryCloseObserver<T, V> operatorWindowBoundaryCloseObserver) {
            this.resources.delete(operatorWindowBoundaryCloseObserver);
            this.queue.offer(new WindowOperation(operatorWindowBoundaryCloseObserver.f164w, null));
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

        WindowOperation(UnicastSubject<T> unicastSubject, B b) {
            this.f166w = unicastSubject;
            this.open = b;
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundarySelector$OperatorWindowBoundaryOpenObserver */
    /* loaded from: classes.dex */
    static final class OperatorWindowBoundaryOpenObserver<T, B> extends DisposableObserver<B> {
        final WindowBoundaryMainObserver<T, B, ?> parent;

        OperatorWindowBoundaryOpenObserver(WindowBoundaryMainObserver<T, B, ?> windowBoundaryMainObserver) {
            this.parent = windowBoundaryMainObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(B b) {
            this.parent.open(b);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.parent.error(th);
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

        OperatorWindowBoundaryCloseObserver(WindowBoundaryMainObserver<T, ?, V> windowBoundaryMainObserver, UnicastSubject<T> unicastSubject) {
            this.parent = windowBoundaryMainObserver;
            this.f164w = unicastSubject;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(V v) {
            dispose();
            onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.parent.error(th);
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
