package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.observers.DisposableObserver;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.subjects.UnicastSubject;

/* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundary */
/* loaded from: classes.dex */
public final class ObservableWindowBoundary<T, B> extends AbstractObservableWithUpstream<T, Observable<T>> {
    final int capacityHint;
    final ObservableSource<B> other;

    public ObservableWindowBoundary(ObservableSource<T> observableSource, ObservableSource<B> observableSource2, int i) {
        super(observableSource);
        this.other = observableSource2;
        this.capacityHint = i;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Observable<T>> observer) {
        WindowBoundaryMainObserver windowBoundaryMainObserver = new WindowBoundaryMainObserver(observer, this.capacityHint);
        observer.onSubscribe(windowBoundaryMainObserver);
        this.other.subscribe(windowBoundaryMainObserver.boundaryObserver);
        this.source.subscribe(windowBoundaryMainObserver);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundary$WindowBoundaryMainObserver */
    /* loaded from: classes.dex */
    static final class WindowBoundaryMainObserver<T, B> extends AtomicInteger implements Observer<T>, Disposable, Runnable {
        static final Object NEXT_WINDOW = new Object();
        private static final long serialVersionUID = 2233020065421370272L;
        final int capacityHint;
        volatile boolean done;
        final Observer<? super Observable<T>> downstream;
        UnicastSubject<T> window;
        final WindowBoundaryInnerObserver<T, B> boundaryObserver = new WindowBoundaryInnerObserver<>(this);
        final AtomicReference<Disposable> upstream = new AtomicReference<>();
        final AtomicInteger windows = new AtomicInteger(1);
        final MpscLinkedQueue<Object> queue = new MpscLinkedQueue<>();
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicBoolean stopWindows = new AtomicBoolean();

        WindowBoundaryMainObserver(Observer<? super Observable<T>> observer, int i) {
            this.downstream = observer;
            this.capacityHint = i;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.setOnce(this.upstream, disposable)) {
                innerNext();
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            drain();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.boundaryObserver.dispose();
            if (this.errors.addThrowable(th)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.boundaryObserver.dispose();
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (this.stopWindows.compareAndSet(false, true)) {
                this.boundaryObserver.dispose();
                if (this.windows.decrementAndGet() == 0) {
                    DisposableHelper.dispose(this.upstream);
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.stopWindows.get();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.windows.decrementAndGet() == 0) {
                DisposableHelper.dispose(this.upstream);
            }
        }

        void innerNext() {
            this.queue.offer(NEXT_WINDOW);
            drain();
        }

        void innerError(Throwable th) {
            DisposableHelper.dispose(this.upstream);
            if (this.errors.addThrowable(th)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        void innerComplete() {
            DisposableHelper.dispose(this.upstream);
            this.done = true;
            drain();
        }

        /* JADX WARN: Multi-variable type inference failed */
        void drain() {
            if (getAndIncrement() == 0) {
                Observer<? super Observable<T>> observer = this.downstream;
                MpscLinkedQueue<Object> mpscLinkedQueue = this.queue;
                AtomicThrowable atomicThrowable = this.errors;
                int i = 1;
                while (this.windows.get() != 0) {
                    UnicastSubject<T> unicastSubject = this.window;
                    boolean z = this.done;
                    if (!z || atomicThrowable.get() == null) {
                        Object poll = mpscLinkedQueue.poll();
                        boolean z2 = poll == null;
                        if (z && z2) {
                            Throwable terminate = atomicThrowable.terminate();
                            if (terminate == null) {
                                if (unicastSubject != 0) {
                                    this.window = null;
                                    unicastSubject.onComplete();
                                }
                                observer.onComplete();
                                return;
                            }
                            if (unicastSubject != 0) {
                                this.window = null;
                                unicastSubject.onError(terminate);
                            }
                            observer.onError(terminate);
                            return;
                        } else if (z2) {
                            i = addAndGet(-i);
                            if (i == 0) {
                                return;
                            }
                        } else if (poll != NEXT_WINDOW) {
                            unicastSubject.onNext(poll);
                        } else {
                            if (unicastSubject != 0) {
                                this.window = null;
                                unicastSubject.onComplete();
                            }
                            if (!this.stopWindows.get()) {
                                UnicastSubject<T> create = UnicastSubject.create(this.capacityHint, this);
                                this.window = create;
                                this.windows.getAndIncrement();
                                observer.onNext(create);
                            }
                        }
                    } else {
                        mpscLinkedQueue.clear();
                        Throwable terminate2 = atomicThrowable.terminate();
                        if (unicastSubject != 0) {
                            this.window = null;
                            unicastSubject.onError(terminate2);
                        }
                        observer.onError(terminate2);
                        return;
                    }
                }
                mpscLinkedQueue.clear();
                this.window = null;
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableWindowBoundary$WindowBoundaryInnerObserver */
    /* loaded from: classes.dex */
    static final class WindowBoundaryInnerObserver<T, B> extends DisposableObserver<B> {
        boolean done;
        final WindowBoundaryMainObserver<T, B> parent;

        WindowBoundaryInnerObserver(WindowBoundaryMainObserver<T, B> windowBoundaryMainObserver) {
            this.parent = windowBoundaryMainObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(B b) {
            if (!this.done) {
                this.parent.innerNext();
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.parent.innerError(th);
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
