package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.BasicIntQueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableObserveOn */
/* loaded from: classes.dex */
public final class FlowableObserveOn<T> extends AbstractFlowableWithUpstream<T, T> {
    final boolean delayError;
    final int prefetch;
    final Scheduler scheduler;

    public FlowableObserveOn(Flowable<T> flowable, Scheduler scheduler, boolean z, int i) {
        super(flowable);
        this.scheduler = scheduler;
        this.delayError = z;
        this.prefetch = i;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        Scheduler.Worker createWorker = this.scheduler.createWorker();
        if (subscriber instanceof ConditionalSubscriber) {
            this.source.subscribe((FlowableSubscriber) new ObserveOnConditionalSubscriber((ConditionalSubscriber) subscriber, createWorker, this.delayError, this.prefetch));
        } else {
            this.source.subscribe((FlowableSubscriber) new ObserveOnSubscriber(subscriber, createWorker, this.delayError, this.prefetch));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableObserveOn$BaseObserveOnSubscriber */
    /* loaded from: classes.dex */
    static abstract class BaseObserveOnSubscriber<T> extends BasicIntQueueSubscription<T> implements FlowableSubscriber<T>, Runnable {
        private static final long serialVersionUID = -8241002408341274697L;
        volatile boolean cancelled;
        final boolean delayError;
        volatile boolean done;
        Throwable error;
        final int limit;
        boolean outputFused;
        final int prefetch;
        long produced;
        SimpleQueue<T> queue;
        final AtomicLong requested = new AtomicLong();
        int sourceMode;
        Subscription upstream;
        final Scheduler.Worker worker;

        abstract void runAsync();

        abstract void runBackfused();

        abstract void runSync();

        BaseObserveOnSubscriber(Scheduler.Worker worker, boolean z, int i) {
            this.worker = worker;
            this.delayError = z;
            this.prefetch = i;
            this.limit = i - (i >> 2);
        }

        @Override // org.reactivestreams.Subscriber
        public final void onNext(T t) {
            if (!this.done) {
                if (this.sourceMode == 2) {
                    trySchedule();
                    return;
                }
                if (!this.queue.offer(t)) {
                    this.upstream.cancel();
                    this.error = new MissingBackpressureException("Queue is full?!");
                    this.done = true;
                }
                trySchedule();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public final void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.error = th;
            this.done = true;
            trySchedule();
        }

        @Override // org.reactivestreams.Subscriber
        public final void onComplete() {
            if (!this.done) {
                this.done = true;
                trySchedule();
            }
        }

        @Override // org.reactivestreams.Subscription
        public final void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                trySchedule();
            }
        }

        @Override // org.reactivestreams.Subscription
        public final void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.cancel();
                this.worker.dispose();
                if (getAndIncrement() == 0) {
                    this.queue.clear();
                }
            }
        }

        final void trySchedule() {
            if (getAndIncrement() == 0) {
                this.worker.schedule(this);
            }
        }

        @Override // java.lang.Runnable
        public final void run() {
            if (this.outputFused) {
                runBackfused();
            } else if (this.sourceMode == 1) {
                runSync();
            } else {
                runAsync();
            }
        }

        final boolean checkTerminated(boolean z, boolean z2, Subscriber<?> subscriber) {
            if (this.cancelled) {
                clear();
                return true;
            } else if (!z) {
                return false;
            } else {
                if (!this.delayError) {
                    Throwable th = this.error;
                    if (th != null) {
                        this.cancelled = true;
                        clear();
                        subscriber.onError(th);
                        this.worker.dispose();
                        return true;
                    } else if (!z2) {
                        return false;
                    } else {
                        this.cancelled = true;
                        subscriber.onComplete();
                        this.worker.dispose();
                        return true;
                    }
                } else if (!z2) {
                    return false;
                } else {
                    this.cancelled = true;
                    Throwable th2 = this.error;
                    if (th2 != null) {
                        subscriber.onError(th2);
                    } else {
                        subscriber.onComplete();
                    }
                    this.worker.dispose();
                    return true;
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public final int requestFusion(int i) {
            if ((i & 2) == 0) {
                return 0;
            }
            this.outputFused = true;
            return 2;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final void clear() {
            this.queue.clear();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final boolean isEmpty() {
            return this.queue.isEmpty();
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableObserveOn$ObserveOnSubscriber */
    /* loaded from: classes.dex */
    static final class ObserveOnSubscriber<T> extends BaseObserveOnSubscriber<T> implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -4547113800637756442L;
        final Subscriber<? super T> downstream;

        ObserveOnSubscriber(Subscriber<? super T> subscriber, Scheduler.Worker worker, boolean z, int i) {
            super(worker, z, i);
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                if (subscription instanceof QueueSubscription) {
                    QueueSubscription queueSubscription = (QueueSubscription) subscription;
                    int requestFusion = queueSubscription.requestFusion(7);
                    if (requestFusion == 1) {
                        this.sourceMode = 1;
                        this.queue = queueSubscription;
                        this.done = true;
                        this.downstream.onSubscribe(this);
                        return;
                    } else if (requestFusion == 2) {
                        this.sourceMode = 2;
                        this.queue = queueSubscription;
                        this.downstream.onSubscribe(this);
                        subscription.request((long) this.prefetch);
                        return;
                    }
                }
                this.queue = new SpscArrayQueue(this.prefetch);
                this.downstream.onSubscribe(this);
                subscription.request((long) this.prefetch);
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableObserveOn.BaseObserveOnSubscriber
        void runSync() {
            Subscriber<? super T> subscriber = this.downstream;
            SimpleQueue simpleQueue = this.queue;
            long j = this.produced;
            int i = 1;
            while (true) {
                long j2 = this.requested.get();
                while (j != j2) {
                    try {
                        Object obj = (Object) simpleQueue.poll();
                        if (!this.cancelled) {
                            if (obj == 0) {
                                this.cancelled = true;
                                subscriber.onComplete();
                                this.worker.dispose();
                                return;
                            }
                            subscriber.onNext(obj);
                            j++;
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.cancelled = true;
                        this.upstream.cancel();
                        subscriber.onError(th);
                        this.worker.dispose();
                        return;
                    }
                }
                if (!this.cancelled) {
                    if (simpleQueue.isEmpty()) {
                        this.cancelled = true;
                        subscriber.onComplete();
                        this.worker.dispose();
                        return;
                    }
                    int i2 = get();
                    if (i == i2) {
                        this.produced = j;
                        i = addAndGet(-i);
                        if (i == 0) {
                            return;
                        }
                    } else {
                        i = i2;
                    }
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableObserveOn.BaseObserveOnSubscriber
        void runAsync() {
            Subscriber<? super T> subscriber = this.downstream;
            SimpleQueue simpleQueue = this.queue;
            long j = this.produced;
            int i = 1;
            while (true) {
                long j2 = this.requested.get();
                while (j != j2) {
                    boolean z = this.done;
                    try {
                        Object obj = (Object) simpleQueue.poll();
                        boolean z2 = obj == 0;
                        if (!checkTerminated(z, z2, subscriber)) {
                            if (z2) {
                                break;
                            }
                            subscriber.onNext(obj);
                            j++;
                            if (j == ((long) this.limit)) {
                                if (j2 != Long.MAX_VALUE) {
                                    j2 = this.requested.addAndGet(-j);
                                }
                                this.upstream.request(j);
                                j = 0;
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.cancelled = true;
                        this.upstream.cancel();
                        simpleQueue.clear();
                        subscriber.onError(th);
                        this.worker.dispose();
                        return;
                    }
                }
                if (j != j2 || !checkTerminated(this.done, simpleQueue.isEmpty(), subscriber)) {
                    int i2 = get();
                    if (i == i2) {
                        this.produced = j;
                        i = addAndGet(-i);
                        if (i == 0) {
                            return;
                        }
                    } else {
                        i = i2;
                    }
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableObserveOn.BaseObserveOnSubscriber
        void runBackfused() {
            int i = 1;
            while (!this.cancelled) {
                boolean z = this.done;
                this.downstream.onNext(null);
                if (z) {
                    this.cancelled = true;
                    Throwable th = this.error;
                    if (th != null) {
                        this.downstream.onError(th);
                    } else {
                        this.downstream.onComplete();
                    }
                    this.worker.dispose();
                    return;
                }
                i = addAndGet(-i);
                if (i == 0) {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T t = (T) this.queue.poll();
            if (!(t == null || this.sourceMode == 1)) {
                long j = this.produced + 1;
                if (j == ((long) this.limit)) {
                    this.produced = 0;
                    this.upstream.request(j);
                } else {
                    this.produced = j;
                }
            }
            return t;
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableObserveOn$ObserveOnConditionalSubscriber */
    /* loaded from: classes.dex */
    static final class ObserveOnConditionalSubscriber<T> extends BaseObserveOnSubscriber<T> {
        private static final long serialVersionUID = 644624475404284533L;
        long consumed;
        final ConditionalSubscriber<? super T> downstream;

        ObserveOnConditionalSubscriber(ConditionalSubscriber<? super T> conditionalSubscriber, Scheduler.Worker worker, boolean z, int i) {
            super(worker, z, i);
            this.downstream = conditionalSubscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                if (subscription instanceof QueueSubscription) {
                    QueueSubscription queueSubscription = (QueueSubscription) subscription;
                    int requestFusion = queueSubscription.requestFusion(7);
                    if (requestFusion == 1) {
                        this.sourceMode = 1;
                        this.queue = queueSubscription;
                        this.done = true;
                        this.downstream.onSubscribe(this);
                        return;
                    } else if (requestFusion == 2) {
                        this.sourceMode = 2;
                        this.queue = queueSubscription;
                        this.downstream.onSubscribe(this);
                        subscription.request((long) this.prefetch);
                        return;
                    }
                }
                this.queue = new SpscArrayQueue(this.prefetch);
                this.downstream.onSubscribe(this);
                subscription.request((long) this.prefetch);
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableObserveOn.BaseObserveOnSubscriber
        void runSync() {
            ConditionalSubscriber<? super T> conditionalSubscriber = this.downstream;
            SimpleQueue simpleQueue = this.queue;
            long j = this.produced;
            int i = 1;
            while (true) {
                long j2 = this.requested.get();
                while (j != j2) {
                    try {
                        Object obj = (Object) simpleQueue.poll();
                        if (!this.cancelled) {
                            if (obj == 0) {
                                this.cancelled = true;
                                conditionalSubscriber.onComplete();
                                this.worker.dispose();
                                return;
                            } else if (conditionalSubscriber.tryOnNext(obj)) {
                                j++;
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.cancelled = true;
                        this.upstream.cancel();
                        conditionalSubscriber.onError(th);
                        this.worker.dispose();
                        return;
                    }
                }
                if (!this.cancelled) {
                    if (simpleQueue.isEmpty()) {
                        this.cancelled = true;
                        conditionalSubscriber.onComplete();
                        this.worker.dispose();
                        return;
                    }
                    int i2 = get();
                    if (i == i2) {
                        this.produced = j;
                        i = addAndGet(-i);
                        if (i == 0) {
                            return;
                        }
                    } else {
                        i = i2;
                    }
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableObserveOn.BaseObserveOnSubscriber
        void runAsync() {
            ConditionalSubscriber<? super T> conditionalSubscriber = this.downstream;
            SimpleQueue simpleQueue = this.queue;
            long j = this.produced;
            long j2 = this.consumed;
            int i = 1;
            while (true) {
                long j3 = this.requested.get();
                while (j != j3) {
                    boolean z = this.done;
                    try {
                        Object obj = (Object) simpleQueue.poll();
                        boolean z2 = obj == 0;
                        if (!checkTerminated(z, z2, conditionalSubscriber)) {
                            if (z2) {
                                break;
                            }
                            if (conditionalSubscriber.tryOnNext(obj)) {
                                j++;
                            }
                            j2++;
                            if (j2 == ((long) this.limit)) {
                                this.upstream.request(j2);
                                j2 = 0;
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.cancelled = true;
                        this.upstream.cancel();
                        simpleQueue.clear();
                        conditionalSubscriber.onError(th);
                        this.worker.dispose();
                        return;
                    }
                }
                if (j != j3 || !checkTerminated(this.done, simpleQueue.isEmpty(), conditionalSubscriber)) {
                    int i2 = get();
                    if (i == i2) {
                        this.produced = j;
                        this.consumed = j2;
                        i = addAndGet(-i);
                        if (i == 0) {
                            return;
                        }
                    } else {
                        i = i2;
                    }
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableObserveOn.BaseObserveOnSubscriber
        void runBackfused() {
            int i = 1;
            while (!this.cancelled) {
                boolean z = this.done;
                this.downstream.onNext(null);
                if (z) {
                    this.cancelled = true;
                    Throwable th = this.error;
                    if (th != null) {
                        this.downstream.onError(th);
                    } else {
                        this.downstream.onComplete();
                    }
                    this.worker.dispose();
                    return;
                }
                i = addAndGet(-i);
                if (i == 0) {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T t = (T) this.queue.poll();
            if (!(t == null || this.sourceMode == 1)) {
                long j = this.consumed + 1;
                if (j == ((long) this.limit)) {
                    this.consumed = 0;
                    this.upstream.request(j);
                } else {
                    this.consumed = j;
                }
            }
            return t;
        }
    }
}
