package p005io.reactivex.internal.operators.observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.QueueDrainObserver;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.util.QueueDrainHelper;
import p005io.reactivex.observers.SerializedObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableBufferTimed */
/* loaded from: classes.dex */
public final class ObservableBufferTimed<T, U extends Collection<? super T>> extends AbstractObservableWithUpstream<T, U> {
    final Callable<U> bufferSupplier;
    final int maxSize;
    final boolean restartTimerOnMaxSize;
    final Scheduler scheduler;
    final long timeskip;
    final long timespan;
    final TimeUnit unit;

    public ObservableBufferTimed(ObservableSource<T> observableSource, long j, long j2, TimeUnit timeUnit, Scheduler scheduler, Callable<U> callable, int i, boolean z) {
        super(observableSource);
        this.timespan = j;
        this.timeskip = j2;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.bufferSupplier = callable;
        this.maxSize = i;
        this.restartTimerOnMaxSize = z;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super U> observer) {
        if (this.timespan == this.timeskip && this.maxSize == Integer.MAX_VALUE) {
            this.source.subscribe(new BufferExactUnboundedObserver(new SerializedObserver(observer), this.bufferSupplier, this.timespan, this.unit, this.scheduler));
            return;
        }
        Scheduler.Worker createWorker = this.scheduler.createWorker();
        if (this.timespan == this.timeskip) {
            this.source.subscribe(new BufferExactBoundedObserver(new SerializedObserver(observer), this.bufferSupplier, this.timespan, this.unit, this.maxSize, this.restartTimerOnMaxSize, createWorker));
        } else {
            this.source.subscribe(new BufferSkipBoundedObserver(new SerializedObserver(observer), this.bufferSupplier, this.timespan, this.timeskip, this.unit, createWorker));
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferTimed$BufferExactUnboundedObserver */
    /* loaded from: classes.dex */
    static final class BufferExactUnboundedObserver<T, U extends Collection<? super T>> extends QueueDrainObserver<T, U, U> implements Runnable, Disposable {
        U buffer;
        final Callable<U> bufferSupplier;
        final Scheduler scheduler;
        final AtomicReference<Disposable> timer = new AtomicReference<>();
        final long timespan;
        final TimeUnit unit;
        Disposable upstream;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public /* bridge */ /* synthetic */ void accept(Observer observer, Object obj) {
            accept((Observer<? super Observer>) observer, (Observer) ((Collection) obj));
        }

        BufferExactUnboundedObserver(Observer<? super U> observer, Callable<U> callable, long j, TimeUnit timeUnit, Scheduler scheduler) {
            super(observer, new MpscLinkedQueue());
            this.bufferSupplier = callable;
            this.timespan = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                try {
                    this.buffer = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                    this.downstream.onSubscribe(this);
                    if (!this.cancelled) {
                        Scheduler scheduler = this.scheduler;
                        long j = this.timespan;
                        Disposable schedulePeriodicallyDirect = scheduler.schedulePeriodicallyDirect(this, j, j, this.unit);
                        if (!this.timer.compareAndSet(null, schedulePeriodicallyDirect)) {
                            schedulePeriodicallyDirect.dispose();
                        }
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    dispose();
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
            synchronized (this) {
                this.buffer = null;
            }
            this.downstream.onError(th);
            DisposableHelper.dispose(this.timer);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            U u;
            synchronized (this) {
                u = this.buffer;
                this.buffer = null;
            }
            if (u != null) {
                this.queue.offer(u);
                this.done = true;
                if (enter()) {
                    QueueDrainHelper.drainLoop(this.queue, this.downstream, false, null, this);
                }
            }
            DisposableHelper.dispose(this.timer);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this.timer);
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.timer.get() == DisposableHelper.DISPOSED;
        }

        @Override // java.lang.Runnable
        public void run() {
            U u;
            try {
                U u2 = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The bufferSupplier returned a null buffer"));
                synchronized (this) {
                    u = this.buffer;
                    if (u != null) {
                        this.buffer = u2;
                    }
                }
                if (u == null) {
                    DisposableHelper.dispose(this.timer);
                } else {
                    fastPathEmit(u, false, this);
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.downstream.onError(th);
                dispose();
            }
        }

        public void accept(Observer<? super U> observer, U u) {
            this.downstream.onNext(u);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferTimed$BufferSkipBoundedObserver */
    /* loaded from: classes.dex */
    static final class BufferSkipBoundedObserver<T, U extends Collection<? super T>> extends QueueDrainObserver<T, U, U> implements Runnable, Disposable {
        final Callable<U> bufferSupplier;
        final List<U> buffers = new LinkedList();
        final long timeskip;
        final long timespan;
        final TimeUnit unit;
        Disposable upstream;

        /* renamed from: w */
        final Scheduler.Worker f146w;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public /* bridge */ /* synthetic */ void accept(Observer observer, Object obj) {
            accept((Observer<? super Observer>) observer, (Observer) ((Collection) obj));
        }

        BufferSkipBoundedObserver(Observer<? super U> observer, Callable<U> callable, long j, long j2, TimeUnit timeUnit, Scheduler.Worker worker) {
            super(observer, new MpscLinkedQueue());
            this.bufferSupplier = callable;
            this.timespan = j;
            this.timeskip = j2;
            this.unit = timeUnit;
            this.f146w = worker;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                try {
                    Collection collection = (Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null");
                    this.buffers.add(collection);
                    this.downstream.onSubscribe(this);
                    Scheduler.Worker worker = this.f146w;
                    long j = this.timeskip;
                    worker.schedulePeriodically(this, j, j, this.unit);
                    this.f146w.schedule(new RemoveFromBufferEmit(collection), this.timespan, this.unit);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    disposable.dispose();
                    EmptyDisposable.error(th, this.downstream);
                    this.f146w.dispose();
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            synchronized (this) {
                for (U u : this.buffers) {
                    u.add(t);
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.done = true;
            clear();
            this.downstream.onError(th);
            this.f146w.dispose();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            ArrayList<Collection> arrayList;
            synchronized (this) {
                arrayList = new ArrayList(this.buffers);
                this.buffers.clear();
            }
            for (Collection collection : arrayList) {
                this.queue.offer(collection);
            }
            this.done = true;
            if (enter()) {
                QueueDrainHelper.drainLoop(this.queue, this.downstream, false, this.f146w, this);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                clear();
                this.upstream.dispose();
                this.f146w.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void clear() {
            synchronized (this) {
                this.buffers.clear();
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.lang.Runnable
        public void run() {
            if (!this.cancelled) {
                try {
                    Collection collection = (Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The bufferSupplier returned a null buffer");
                    synchronized (this) {
                        if (!this.cancelled) {
                            this.buffers.add(collection);
                            this.f146w.schedule(new RemoveFromBuffer(collection), this.timespan, this.unit);
                        }
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.downstream.onError(th);
                    dispose();
                }
            }
        }

        public void accept(Observer<? super U> observer, U u) {
            observer.onNext(u);
        }

        /* JADX WARN: Incorrect field signature: TU; */
        /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferTimed$BufferSkipBoundedObserver$RemoveFromBuffer */
        /* loaded from: classes.dex */
        final class RemoveFromBuffer implements Runnable {

            /* renamed from: b */
            private final Collection f147b;

            RemoveFromBuffer(U u) {
                this.f147b = u;
            }

            @Override // java.lang.Runnable
            public void run() {
                synchronized (BufferSkipBoundedObserver.this) {
                    BufferSkipBoundedObserver.this.buffers.remove(this.f147b);
                }
                BufferSkipBoundedObserver bufferSkipBoundedObserver = BufferSkipBoundedObserver.this;
                bufferSkipBoundedObserver.fastPathOrderedEmit(this.f147b, false, bufferSkipBoundedObserver.f146w);
            }
        }

        /* JADX WARN: Incorrect field signature: TU; */
        /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferTimed$BufferSkipBoundedObserver$RemoveFromBufferEmit */
        /* loaded from: classes.dex */
        final class RemoveFromBufferEmit implements Runnable {
            private final Collection buffer;

            RemoveFromBufferEmit(U u) {
                this.buffer = u;
            }

            @Override // java.lang.Runnable
            public void run() {
                synchronized (BufferSkipBoundedObserver.this) {
                    BufferSkipBoundedObserver.this.buffers.remove(this.buffer);
                }
                BufferSkipBoundedObserver bufferSkipBoundedObserver = BufferSkipBoundedObserver.this;
                bufferSkipBoundedObserver.fastPathOrderedEmit(this.buffer, false, bufferSkipBoundedObserver.f146w);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableBufferTimed$BufferExactBoundedObserver */
    /* loaded from: classes.dex */
    static final class BufferExactBoundedObserver<T, U extends Collection<? super T>> extends QueueDrainObserver<T, U, U> implements Runnable, Disposable {
        U buffer;
        final Callable<U> bufferSupplier;
        long consumerIndex;
        final int maxSize;
        long producerIndex;
        final boolean restartTimerOnMaxSize;
        Disposable timer;
        final long timespan;
        final TimeUnit unit;
        Disposable upstream;

        /* renamed from: w */
        final Scheduler.Worker f145w;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.observers.QueueDrainObserver, p005io.reactivex.internal.util.ObservableQueueDrain
        public /* bridge */ /* synthetic */ void accept(Observer observer, Object obj) {
            accept((Observer<? super Observer>) observer, (Observer) ((Collection) obj));
        }

        BufferExactBoundedObserver(Observer<? super U> observer, Callable<U> callable, long j, TimeUnit timeUnit, int i, boolean z, Scheduler.Worker worker) {
            super(observer, new MpscLinkedQueue());
            this.bufferSupplier = callable;
            this.timespan = j;
            this.unit = timeUnit;
            this.maxSize = i;
            this.restartTimerOnMaxSize = z;
            this.f145w = worker;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                try {
                    this.buffer = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                    this.downstream.onSubscribe(this);
                    Scheduler.Worker worker = this.f145w;
                    long j = this.timespan;
                    this.timer = worker.schedulePeriodically(this, j, j, this.unit);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    disposable.dispose();
                    EmptyDisposable.error(th, this.downstream);
                    this.f145w.dispose();
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            synchronized (this) {
                U u = this.buffer;
                if (u != null) {
                    u.add(t);
                    if (u.size() >= this.maxSize) {
                        this.buffer = null;
                        this.producerIndex++;
                        if (this.restartTimerOnMaxSize) {
                            this.timer.dispose();
                        }
                        fastPathOrderedEmit(u, false, this);
                        try {
                            U u2 = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                            synchronized (this) {
                                this.buffer = u2;
                                this.consumerIndex++;
                            }
                            if (this.restartTimerOnMaxSize) {
                                Scheduler.Worker worker = this.f145w;
                                long j = this.timespan;
                                this.timer = worker.schedulePeriodically(this, j, j, this.unit);
                            }
                        } catch (Throwable th) {
                            Exceptions.throwIfFatal(th);
                            this.downstream.onError(th);
                            dispose();
                        }
                    }
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            synchronized (this) {
                this.buffer = null;
            }
            this.downstream.onError(th);
            this.f145w.dispose();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            U u;
            this.f145w.dispose();
            synchronized (this) {
                u = this.buffer;
                this.buffer = null;
            }
            this.queue.offer(u);
            this.done = true;
            if (enter()) {
                QueueDrainHelper.drainLoop(this.queue, this.downstream, false, this, this);
            }
        }

        public void accept(Observer<? super U> observer, U u) {
            observer.onNext(u);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.dispose();
                this.f145w.dispose();
                synchronized (this) {
                    this.buffer = null;
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                U u = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The bufferSupplier returned a null buffer"));
                synchronized (this) {
                    U u2 = this.buffer;
                    if (u2 != null && this.producerIndex == this.consumerIndex) {
                        this.buffer = u;
                        fastPathOrderedEmit(u2, false, this);
                    }
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                dispose();
                this.downstream.onError(th);
            }
        }
    }
}
