package p005io.reactivex.internal.operators.observable;

import java.util.Collection;
import java.util.concurrent.Callable;
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

/* renamed from: io.reactivex.internal.operators.observable.ObservableBufferExactBoundary */
/* loaded from: classes.dex */
public final class ObservableBufferExactBoundary<T, U extends Collection<? super T>, B> extends AbstractObservableWithUpstream<T, U> {
    final ObservableSource<B> boundary;
    final Callable<U> bufferSupplier;

    public ObservableBufferExactBoundary(ObservableSource<T> observableSource, ObservableSource<B> observableSource2, Callable<U> callable) {
        super(observableSource);
        this.boundary = observableSource2;
        this.bufferSupplier = callable;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super U> observer) {
        this.source.subscribe(new BufferExactBoundaryObserver(new SerializedObserver(observer), this.bufferSupplier, this.boundary));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferExactBoundary$BufferExactBoundaryObserver */
    /* loaded from: classes.dex */
    static final class BufferExactBoundaryObserver<T, U extends Collection<? super T>, B> extends QueueDrainObserver<T, U, U> implements Observer<T>, Disposable {
        final ObservableSource<B> boundary;
        U buffer;
        final Callable<U> bufferSupplier;
        Disposable other;
        Disposable upstream;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public /* bridge */ /* synthetic */ void accept(Observer observer, Object obj) {
            accept((Observer<? super Observer>) observer, (Observer) ((Collection) obj));
        }

        BufferExactBoundaryObserver(Observer<? super U> observer, Callable<U> callable, ObservableSource<B> observableSource) {
            super(observer, new MpscLinkedQueue());
            this.bufferSupplier = callable;
            this.boundary = observableSource;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                try {
                    this.buffer = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                    BufferBoundaryObserver bufferBoundaryObserver = new BufferBoundaryObserver(this);
                    this.other = bufferBoundaryObserver;
                    this.downstream.onSubscribe(this);
                    if (!this.cancelled) {
                        this.boundary.subscribe(bufferBoundaryObserver);
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.cancelled = true;
                    disposable.dispose();
                    EmptyDisposable.error(th, this.downstream);
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
                this.other.dispose();
                this.upstream.dispose();
                if (enter()) {
                    this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void next() {
            try {
                U u = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                synchronized (this) {
                    U u2 = this.buffer;
                    if (u2 != null) {
                        this.buffer = u;
                        fastPathEmit(u2, false, this);
                    }
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                dispose();
                this.downstream.onError(th);
            }
        }

        public void accept(Observer<? super U> observer, U u) {
            this.downstream.onNext(u);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferExactBoundary$BufferBoundaryObserver */
    /* loaded from: classes.dex */
    static final class BufferBoundaryObserver<T, U extends Collection<? super T>, B> extends DisposableObserver<B> {
        final BufferExactBoundaryObserver<T, U, B> parent;

        BufferBoundaryObserver(BufferExactBoundaryObserver<T, U, B> bufferExactBoundaryObserver) {
            this.parent = bufferExactBoundaryObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(B b) {
            this.parent.next();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.parent.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.parent.onComplete();
        }
    }
}
