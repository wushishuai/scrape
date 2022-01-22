package p005io.reactivex.internal.operators.observable;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.QueueDrainObserver;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.util.QueueDrainHelper;
import p005io.reactivex.observers.DisposableObserver;
import p005io.reactivex.observers.SerializedObserver;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableBufferBoundarySupplier */
/* loaded from: classes.dex */
public final class ObservableBufferBoundarySupplier<T, U extends Collection<? super T>, B> extends AbstractObservableWithUpstream<T, U> {
    final Callable<? extends ObservableSource<B>> boundarySupplier;
    final Callable<U> bufferSupplier;

    public ObservableBufferBoundarySupplier(ObservableSource<T> observableSource, Callable<? extends ObservableSource<B>> callable, Callable<U> callable2) {
        super(observableSource);
        this.boundarySupplier = callable;
        this.bufferSupplier = callable2;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super U> observer) {
        this.source.subscribe(new BufferBoundarySupplierObserver(new SerializedObserver(observer), this.bufferSupplier, this.boundarySupplier));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferBoundarySupplier$BufferBoundarySupplierObserver */
    /* loaded from: classes.dex */
    static final class BufferBoundarySupplierObserver<T, U extends Collection<? super T>, B> extends QueueDrainObserver<T, U, U> implements Observer<T>, Disposable {
        final Callable<? extends ObservableSource<B>> boundarySupplier;
        U buffer;
        final Callable<U> bufferSupplier;
        final AtomicReference<Disposable> other = new AtomicReference<>();
        Disposable upstream;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public /* bridge */ /* synthetic */ void accept(Observer observer, Object obj) {
            accept((Observer<? super Observer>) observer, (Observer) ((Collection) obj));
        }

        BufferBoundarySupplierObserver(Observer<? super U> observer, Callable<U> callable, Callable<? extends ObservableSource<B>> callable2) {
            super(observer, new MpscLinkedQueue());
            this.bufferSupplier = callable;
            this.boundarySupplier = callable2;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                Observer observer = this.downstream;
                try {
                    this.buffer = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                    try {
                        ObservableSource observableSource = (ObservableSource) ObjectHelper.requireNonNull(this.boundarySupplier.call(), "The boundary ObservableSource supplied is null");
                        BufferBoundaryObserver bufferBoundaryObserver = new BufferBoundaryObserver(this);
                        this.other.set(bufferBoundaryObserver);
                        observer.onSubscribe(this);
                        if (!this.cancelled) {
                            observableSource.subscribe(bufferBoundaryObserver);
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.cancelled = true;
                        disposable.dispose();
                        EmptyDisposable.error(th, observer);
                    }
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    this.cancelled = true;
                    disposable.dispose();
                    EmptyDisposable.error(th2, observer);
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            synchronized (this) {
                U u = this.buffer;
                if (u != null) {
                    u.add(t);
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            dispose();
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            synchronized (this) {
                U u = this.buffer;
                if (u != null) {
                    this.buffer = null;
                    this.queue.offer(u);
                    this.done = true;
                    if (enter()) {
                        QueueDrainHelper.drainLoop(this.queue, this.downstream, false, this, this);
                    }
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.dispose();
                disposeOther();
                if (enter()) {
                    this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void disposeOther() {
            DisposableHelper.dispose(this.other);
        }

        void next() {
            try {
                U u = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                try {
                    ObservableSource observableSource = (ObservableSource) ObjectHelper.requireNonNull(this.boundarySupplier.call(), "The boundary ObservableSource supplied is null");
                    BufferBoundaryObserver bufferBoundaryObserver = new BufferBoundaryObserver(this);
                    if (DisposableHelper.replace(this.other, bufferBoundaryObserver)) {
                        synchronized (this) {
                            U u2 = this.buffer;
                            if (u2 != null) {
                                this.buffer = u;
                                observableSource.subscribe(bufferBoundaryObserver);
                                fastPathEmit(u2, false, this);
                            }
                        }
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.cancelled = true;
                    this.upstream.dispose();
                    this.downstream.onError(th);
                }
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                dispose();
                this.downstream.onError(th2);
            }
        }

        public void accept(Observer<? super U> observer, U u) {
            this.downstream.onNext(u);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferBoundarySupplier$BufferBoundaryObserver */
    /* loaded from: classes.dex */
    public static final class BufferBoundaryObserver<T, U extends Collection<? super T>, B> extends DisposableObserver<B> {
        boolean once;
        final BufferBoundarySupplierObserver<T, U, B> parent;

        BufferBoundaryObserver(BufferBoundarySupplierObserver<T, U, B> bufferBoundarySupplierObserver) {
            this.parent = bufferBoundarySupplierObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(B b) {
            if (!this.once) {
                this.once = true;
                dispose();
                this.parent.next();
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.once) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.once = true;
            this.parent.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.once) {
                this.once = true;
                this.parent.next();
            }
        }
    }
}
