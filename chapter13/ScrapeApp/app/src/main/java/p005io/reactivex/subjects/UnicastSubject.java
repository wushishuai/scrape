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
    public static <T> UnicastSubject<T> create(int capacityHint) {
        return new UnicastSubject<>(capacityHint, true);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int capacityHint, Runnable onTerminate) {
        return new UnicastSubject<>(capacityHint, onTerminate, true);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(int capacityHint, Runnable onTerminate, boolean delayError) {
        return new UnicastSubject<>(capacityHint, onTerminate, delayError);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastSubject<T> create(boolean delayError) {
        return new UnicastSubject<>(bufferSize(), delayError);
    }

    UnicastSubject(int capacityHint, boolean delayError) {
        this.queue = new SpscLinkedArrayQueue<>(ObjectHelper.verifyPositive(capacityHint, "capacityHint"));
        this.onTerminate = new AtomicReference<>();
        this.delayError = delayError;
        this.downstream = new AtomicReference<>();
        this.once = new AtomicBoolean();
        this.wip = new UnicastQueueDisposable();
    }

    UnicastSubject(int capacityHint, Runnable onTerminate) {
        this(capacityHint, onTerminate, true);
    }

    UnicastSubject(int capacityHint, Runnable onTerminate, boolean delayError) {
        this.queue = new SpscLinkedArrayQueue<>(ObjectHelper.verifyPositive(capacityHint, "capacityHint"));
        this.onTerminate = new AtomicReference<>(ObjectHelper.requireNonNull(onTerminate, "onTerminate"));
        this.delayError = delayError;
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
        Runnable r = this.onTerminate.get();
        if (r != null && this.onTerminate.compareAndSet(r, null)) {
            r.run();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable d) {
        if (this.done || this.disposed) {
            d.dispose();
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
    public void onError(Throwable t) {
        ObjectHelper.requireNonNull(t, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.done || this.disposed) {
            RxJavaPlugins.onError(t);
            return;
        }
        this.error = t;
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

    void drainNormal(Observer<? super T> a) {
        int missed = 1;
        SimpleQueue<T> q = this.queue;
        boolean failFast = !this.delayError;
        boolean canBeError = true;
        while (!this.disposed) {
            boolean d = this.done;
            Object obj = (T) this.queue.poll();
            boolean empty = obj == null;
            if (d) {
                if (failFast && canBeError) {
                    if (!failedFast(q, a)) {
                        canBeError = false;
                    } else {
                        return;
                    }
                }
                if (empty) {
                    errorOrComplete(a);
                    return;
                }
            }
            if (empty) {
                missed = this.wip.addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            } else {
                a.onNext(obj);
            }
        }
        this.downstream.lazySet(null);
        q.clear();
    }

    void drainFused(Observer<? super T> a) {
        int missed = 1;
        SpscLinkedArrayQueue<T> q = this.queue;
        boolean failFast = !this.delayError;
        while (!this.disposed) {
            boolean d = this.done;
            if (!failFast || !d || !failedFast(q, a)) {
                a.onNext(null);
                if (d) {
                    errorOrComplete(a);
                    return;
                }
                missed = this.wip.addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            } else {
                return;
            }
        }
        this.downstream.lazySet(null);
        q.clear();
    }

    void errorOrComplete(Observer<? super T> a) {
        this.downstream.lazySet(null);
        Throwable ex = this.error;
        if (ex != null) {
            a.onError(ex);
        } else {
            a.onComplete();
        }
    }

    boolean failedFast(SimpleQueue<T> q, Observer<? super T> a) {
        Throwable ex = this.error;
        if (ex == null) {
            return false;
        }
        this.downstream.lazySet(null);
        q.clear();
        a.onError(ex);
        return true;
    }

    void drain() {
        if (this.wip.getAndIncrement() == 0) {
            Observer<? super T> a = this.downstream.get();
            int missed = 1;
            while (a == null) {
                missed = this.wip.addAndGet(-missed);
                if (missed != 0) {
                    a = this.downstream.get();
                } else {
                    return;
                }
            }
            if (this.enableOperatorFusion) {
                drainFused(a);
            } else {
                drainNormal(a);
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
            UnicastSubject.this = this$0;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            if ((mode & 2) == 0) {
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
