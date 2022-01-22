package p005io.reactivex.subjects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.observers.BasicIntQueueDisposable;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.subjects.UnicastSubject */
/* loaded from: classes.dex */
public final class UnicastSubject<T> extends Subject<T> {
    final boolean delayError;
    volatile boolean disposed;
    volatile boolean done;
    final AtomicReference<Observer<? super T>> downstream;
    boolean enableOperatorFusion;
    Throwable error;
    final AtomicReference<Runnable> onTerminate;
    final AtomicBoolean once;
    final SpscLinkedArrayQueue<T> queue;
    final BasicIntQueueDisposable<T> wip;

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create() {
        return new UnicastSubject<>(bufferSize(), true);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int i) {
        return new UnicastSubject<>(i, true);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int i, Runnable runnable) {
        return new UnicastSubject<>(i, runnable, true);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int i, Runnable runnable, boolean z) {
        return new UnicastSubject<>(i, runnable, z);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(boolean z) {
        return new UnicastSubject<>(bufferSize(), z);
    }

    UnicastSubject(int i, boolean z) {
        this.queue = new SpscLinkedArrayQueue<>(ObjectHelper.verifyPositive(i, "capacityHint"));
        this.onTerminate = new AtomicReference<>();
        this.delayError = z;
        this.downstream = new AtomicReference<>();
        this.once = new AtomicBoolean();
        this.wip = new UnicastQueueDisposable();
    }

    UnicastSubject(int i, Runnable runnable) {
        this(i, runnable, true);
    }

    UnicastSubject(int i, Runnable runnable, boolean z) {
        this.queue = new SpscLinkedArrayQueue<>(ObjectHelper.verifyPositive(i, "capacityHint"));
        this.onTerminate = new AtomicReference<>(ObjectHelper.requireNonNull(runnable, "onTerminate"));
        this.delayError = z;
        this.downstream = new AtomicReference<>();
        this.once = new AtomicBoolean();
        this.wip = new UnicastQueueDisposable();
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        if (this.once.get() || !this.once.compareAndSet(false, true)) {
            EmptyDisposable.error(new IllegalStateException("Only a single observer allowed."), observer);
            return;
        }
        observer.onSubscribe(this.wip);
        this.downstream.lazySet(observer);
        if (this.disposed) {
            this.downstream.lazySet(null);
        } else {
            drain();
        }
    }

    void doTerminate() {
        Runnable runnable = this.onTerminate.get();
        if (runnable != null && this.onTerminate.compareAndSet(runnable, null)) {
            runnable.run();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        if (this.done || this.disposed) {
            disposable.dispose();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        ObjectHelper.requireNonNull(t, "onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (!this.done && !this.disposed) {
            this.queue.offer(t);
            drain();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        ObjectHelper.requireNonNull(th, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.done || this.disposed) {
            RxJavaPlugins.onError(th);
            return;
        }
        this.error = th;
        this.done = true;
        doTerminate();
        drain();
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.done && !this.disposed) {
            this.done = true;
            doTerminate();
            drain();
        }
    }

    void drainNormal(Observer<? super T> observer) {
        SpscLinkedArrayQueue<T> spscLinkedArrayQueue = this.queue;
        boolean z = !this.delayError;
        boolean z2 = true;
        int i = 1;
        while (!this.disposed) {
            boolean z3 = this.done;
            Object obj = (T) this.queue.poll();
            boolean z4 = obj == null;
            if (z3) {
                if (z && z2) {
                    if (!failedFast(spscLinkedArrayQueue, observer)) {
                        z2 = false;
                    } else {
                        return;
                    }
                }
                if (z4) {
                    errorOrComplete(observer);
                    return;
                }
            }
            if (z4) {
                i = this.wip.addAndGet(-i);
                if (i == 0) {
                    return;
                }
            } else {
                observer.onNext(obj);
            }
        }
        this.downstream.lazySet(null);
        spscLinkedArrayQueue.clear();
    }

    void drainFused(Observer<? super T> observer) {
        SpscLinkedArrayQueue<T> spscLinkedArrayQueue = this.queue;
        int i = 1;
        boolean z = !this.delayError;
        while (!this.disposed) {
            boolean z2 = this.done;
            if (!z || !z2 || !failedFast(spscLinkedArrayQueue, observer)) {
                observer.onNext(null);
                if (z2) {
                    errorOrComplete(observer);
                    return;
                }
                i = this.wip.addAndGet(-i);
                if (i == 0) {
                    return;
                }
            } else {
                return;
            }
        }
        this.downstream.lazySet(null);
        spscLinkedArrayQueue.clear();
    }

    void errorOrComplete(Observer<? super T> observer) {
        this.downstream.lazySet(null);
        Throwable th = this.error;
        if (th != null) {
            observer.onError(th);
        } else {
            observer.onComplete();
        }
    }

    boolean failedFast(SimpleQueue<T> simpleQueue, Observer<? super T> observer) {
        Throwable th = this.error;
        if (th == null) {
            return false;
        }
        this.downstream.lazySet(null);
        simpleQueue.clear();
        observer.onError(th);
        return true;
    }

    void drain() {
        if (this.wip.getAndIncrement() == 0) {
            Observer<? super T> observer = this.downstream.get();
            int i = 1;
            while (observer == null) {
                i = this.wip.addAndGet(-i);
                if (i != 0) {
                    observer = this.downstream.get();
                } else {
                    return;
                }
            }
            if (this.enableOperatorFusion) {
                drainFused(observer);
            } else {
                drainNormal(observer);
            }
        }
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasObservers() {
        return this.downstream.get() != null;
    }

    @Override // p005io.reactivex.subjects.Subject
    @Nullable
    public Throwable getThrowable() {
        if (this.done) {
            return this.error;
        }
        return null;
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasThrowable() {
        return this.done && this.error != null;
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasComplete() {
        return this.done && this.error == null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.subjects.UnicastSubject$UnicastQueueDisposable */
    /* loaded from: classes.dex */
    public final class UnicastQueueDisposable extends BasicIntQueueDisposable<T> {
        private static final long serialVersionUID = 7926949470189395511L;

        UnicastQueueDisposable() {
            UnicastSubject.this = r1;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            if ((i & 2) == 0) {
                return 0;
            }
            UnicastSubject.this.enableOperatorFusion = true;
            return 2;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            return UnicastSubject.this.queue.poll();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return UnicastSubject.this.queue.isEmpty();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            UnicastSubject.this.queue.clear();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!UnicastSubject.this.disposed) {
                UnicastSubject unicastSubject = UnicastSubject.this;
                unicastSubject.disposed = true;
                unicastSubject.doTerminate();
                UnicastSubject.this.downstream.lazySet(null);
                if (UnicastSubject.this.wip.getAndIncrement() == 0) {
                    UnicastSubject.this.downstream.lazySet(null);
                    UnicastSubject.this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return UnicastSubject.this.disposed;
        }
    }
}
