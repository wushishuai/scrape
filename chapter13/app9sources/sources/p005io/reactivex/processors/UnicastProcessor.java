package p005io.reactivex.processors;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.subscriptions.BasicIntQueueSubscription;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.processors.UnicastProcessor */
/* loaded from: classes.dex */
public final class UnicastProcessor<T> extends FlowableProcessor<T> {
    volatile boolean cancelled;
    final boolean delayError;
    volatile boolean done;
    final AtomicReference<Subscriber<? super T>> downstream;
    boolean enableOperatorFusion;
    Throwable error;
    final AtomicReference<Runnable> onTerminate;
    final AtomicBoolean once;
    final SpscLinkedArrayQueue<T> queue;
    final AtomicLong requested;
    final BasicIntQueueSubscription<T> wip;

    @CheckReturnValue
    @NonNull
    public static <T> UnicastProcessor<T> create() {
        return new UnicastProcessor<>(bufferSize());
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastProcessor<T> create(int i) {
        return new UnicastProcessor<>(i);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastProcessor<T> create(boolean z) {
        return new UnicastProcessor<>(bufferSize(), null, z);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastProcessor<T> create(int i, Runnable runnable) {
        ObjectHelper.requireNonNull(runnable, "onTerminate");
        return new UnicastProcessor<>(i, runnable);
    }

    @CheckReturnValue
    @NonNull
    public static <T> UnicastProcessor<T> create(int i, Runnable runnable, boolean z) {
        ObjectHelper.requireNonNull(runnable, "onTerminate");
        return new UnicastProcessor<>(i, runnable, z);
    }

    UnicastProcessor(int i) {
        this(i, null, true);
    }

    UnicastProcessor(int i, Runnable runnable) {
        this(i, runnable, true);
    }

    UnicastProcessor(int i, Runnable runnable, boolean z) {
        this.queue = new SpscLinkedArrayQueue<>(ObjectHelper.verifyPositive(i, "capacityHint"));
        this.onTerminate = new AtomicReference<>(runnable);
        this.delayError = z;
        this.downstream = new AtomicReference<>();
        this.once = new AtomicBoolean();
        this.wip = new UnicastQueueSubscription();
        this.requested = new AtomicLong();
    }

    void doTerminate() {
        Runnable andSet = this.onTerminate.getAndSet(null);
        if (andSet != null) {
            andSet.run();
        }
    }

    void drainRegular(Subscriber<? super T> subscriber) {
        long j;
        SpscLinkedArrayQueue<T> spscLinkedArrayQueue = this.queue;
        boolean z = !this.delayError;
        int i = 1;
        do {
            long j2 = this.requested.get();
            long j3 = 0;
            while (true) {
                if (j2 == j3) {
                    j = j3;
                    break;
                }
                boolean z2 = this.done;
                T poll = spscLinkedArrayQueue.poll();
                boolean z3 = poll == null;
                j = j3;
                if (!checkTerminated(z, z2, z3, subscriber, spscLinkedArrayQueue)) {
                    if (z3) {
                        break;
                    }
                    subscriber.onNext(poll);
                    j3 = 1 + j;
                } else {
                    return;
                }
            }
            if (j2 != j || !checkTerminated(z, this.done, spscLinkedArrayQueue.isEmpty(), subscriber, spscLinkedArrayQueue)) {
                if (!(j == 0 || j2 == Long.MAX_VALUE)) {
                    this.requested.addAndGet(-j);
                }
                i = this.wip.addAndGet(-i);
            } else {
                return;
            }
        } while (i != 0);
    }

    void drainFused(Subscriber<? super T> subscriber) {
        SpscLinkedArrayQueue<T> spscLinkedArrayQueue = this.queue;
        int i = 1;
        boolean z = !this.delayError;
        while (!this.cancelled) {
            boolean z2 = this.done;
            if (!z || !z2 || this.error == null) {
                subscriber.onNext(null);
                if (z2) {
                    this.downstream.lazySet(null);
                    Throwable th = this.error;
                    if (th != null) {
                        subscriber.onError(th);
                        return;
                    } else {
                        subscriber.onComplete();
                        return;
                    }
                } else {
                    i = this.wip.addAndGet(-i);
                    if (i == 0) {
                        return;
                    }
                }
            } else {
                spscLinkedArrayQueue.clear();
                this.downstream.lazySet(null);
                subscriber.onError(this.error);
                return;
            }
        }
        spscLinkedArrayQueue.clear();
        this.downstream.lazySet(null);
    }

    void drain() {
        if (this.wip.getAndIncrement() == 0) {
            int i = 1;
            Subscriber<? super T> subscriber = this.downstream.get();
            while (subscriber == null) {
                i = this.wip.addAndGet(-i);
                if (i != 0) {
                    subscriber = this.downstream.get();
                } else {
                    return;
                }
            }
            if (this.enableOperatorFusion) {
                drainFused(subscriber);
            } else {
                drainRegular(subscriber);
            }
        }
    }

    boolean checkTerminated(boolean z, boolean z2, boolean z3, Subscriber<? super T> subscriber, SpscLinkedArrayQueue<T> spscLinkedArrayQueue) {
        if (this.cancelled) {
            spscLinkedArrayQueue.clear();
            this.downstream.lazySet(null);
            return true;
        } else if (!z2) {
            return false;
        } else {
            if (z && this.error != null) {
                spscLinkedArrayQueue.clear();
                this.downstream.lazySet(null);
                subscriber.onError(this.error);
                return true;
            } else if (!z3) {
                return false;
            } else {
                Throwable th = this.error;
                this.downstream.lazySet(null);
                if (th != null) {
                    subscriber.onError(th);
                } else {
                    subscriber.onComplete();
                }
                return true;
            }
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onSubscribe(Subscription subscription) {
        if (this.done || this.cancelled) {
            subscription.cancel();
        } else {
            subscription.request(Long.MAX_VALUE);
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onNext(T t) {
        ObjectHelper.requireNonNull(t, "onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (!this.done && !this.cancelled) {
            this.queue.offer(t);
            drain();
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable th) {
        ObjectHelper.requireNonNull(th, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.done || this.cancelled) {
            RxJavaPlugins.onError(th);
            return;
        }
        this.error = th;
        this.done = true;
        doTerminate();
        drain();
    }

    @Override // org.reactivestreams.Subscriber
    public void onComplete() {
        if (!this.done && !this.cancelled) {
            this.done = true;
            doTerminate();
            drain();
        }
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        if (this.once.get() || !this.once.compareAndSet(false, true)) {
            EmptySubscription.error(new IllegalStateException("This processor allows only a single Subscriber"), subscriber);
            return;
        }
        subscriber.onSubscribe(this.wip);
        this.downstream.set(subscriber);
        if (this.cancelled) {
            this.downstream.lazySet(null);
        } else {
            drain();
        }
    }

    /* renamed from: io.reactivex.processors.UnicastProcessor$UnicastQueueSubscription */
    /* loaded from: classes.dex */
    final class UnicastQueueSubscription extends BasicIntQueueSubscription<T> {
        private static final long serialVersionUID = -4896760517184205454L;

        UnicastQueueSubscription() {
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() {
            return UnicastProcessor.this.queue.poll();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return UnicastProcessor.this.queue.isEmpty();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            UnicastProcessor.this.queue.clear();
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            if ((i & 2) == 0) {
                return 0;
            }
            UnicastProcessor.this.enableOperatorFusion = true;
            return 2;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(UnicastProcessor.this.requested, j);
                UnicastProcessor.this.drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!UnicastProcessor.this.cancelled) {
                UnicastProcessor unicastProcessor = UnicastProcessor.this;
                unicastProcessor.cancelled = true;
                unicastProcessor.doTerminate();
                if (!UnicastProcessor.this.enableOperatorFusion && UnicastProcessor.this.wip.getAndIncrement() == 0) {
                    UnicastProcessor.this.queue.clear();
                    UnicastProcessor.this.downstream.lazySet(null);
                }
            }
        }
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasSubscribers() {
        return this.downstream.get() != null;
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    @Nullable
    public Throwable getThrowable() {
        if (this.done) {
            return this.error;
        }
        return null;
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasComplete() {
        return this.done && this.error == null;
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasThrowable() {
        return this.done && this.error != null;
    }
}
