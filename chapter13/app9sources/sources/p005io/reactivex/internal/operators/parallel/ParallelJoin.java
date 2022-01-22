package p005io.reactivex.internal.operators.parallel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.parallel.ParallelFlowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelJoin */
/* loaded from: classes.dex */
public final class ParallelJoin<T> extends Flowable<T> {
    final boolean delayErrors;
    final int prefetch;
    final ParallelFlowable<? extends T> source;

    public ParallelJoin(ParallelFlowable<? extends T> parallelFlowable, int i, boolean z) {
        this.source = parallelFlowable;
        this.prefetch = i;
        this.delayErrors = z;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        JoinSubscriptionBase joinSubscriptionBase;
        if (this.delayErrors) {
            joinSubscriptionBase = new JoinSubscriptionDelayError(subscriber, this.source.parallelism(), this.prefetch);
        } else {
            joinSubscriptionBase = new JoinSubscription(subscriber, this.source.parallelism(), this.prefetch);
        }
        subscriber.onSubscribe(joinSubscriptionBase);
        this.source.subscribe(joinSubscriptionBase.subscribers);
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelJoin$JoinSubscriptionBase */
    /* loaded from: classes.dex */
    static abstract class JoinSubscriptionBase<T> extends AtomicInteger implements Subscription {
        private static final long serialVersionUID = 3100232009247827843L;
        volatile boolean cancelled;
        final Subscriber<? super T> downstream;
        final JoinInnerSubscriber<T>[] subscribers;
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicLong requested = new AtomicLong();
        final AtomicInteger done = new AtomicInteger();

        abstract void drain();

        abstract void onComplete();

        abstract void onError(Throwable th);

        abstract void onNext(JoinInnerSubscriber<T> joinInnerSubscriber, T t);

        JoinSubscriptionBase(Subscriber<? super T> subscriber, int i, int i2) {
            this.downstream = subscriber;
            JoinInnerSubscriber<T>[] joinInnerSubscriberArr = new JoinInnerSubscriber[i];
            for (int i3 = 0; i3 < i; i3++) {
                joinInnerSubscriberArr[i3] = new JoinInnerSubscriber<>(this, i2);
            }
            this.subscribers = joinInnerSubscriberArr;
            this.done.lazySet(i);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelAll();
                if (getAndIncrement() == 0) {
                    cleanup();
                }
            }
        }

        void cancelAll() {
            int i = 0;
            while (true) {
                JoinInnerSubscriber<T>[] joinInnerSubscriberArr = this.subscribers;
                if (i < joinInnerSubscriberArr.length) {
                    joinInnerSubscriberArr[i].cancel();
                    i++;
                } else {
                    return;
                }
            }
        }

        void cleanup() {
            int i = 0;
            while (true) {
                JoinInnerSubscriber<T>[] joinInnerSubscriberArr = this.subscribers;
                if (i < joinInnerSubscriberArr.length) {
                    joinInnerSubscriberArr[i].queue = null;
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelJoin$JoinSubscription */
    /* loaded from: classes.dex */
    static final class JoinSubscription<T> extends JoinSubscriptionBase<T> {
        private static final long serialVersionUID = 6312374661811000451L;

        JoinSubscription(Subscriber<? super T> subscriber, int i, int i2) {
            super(subscriber, i, i2);
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onNext(JoinInnerSubscriber<T> joinInnerSubscriber, T t) {
            if (get() == 0 && compareAndSet(0, 1)) {
                if (this.requested.get() != 0) {
                    this.downstream.onNext(t);
                    if (this.requested.get() != Long.MAX_VALUE) {
                        this.requested.decrementAndGet();
                    }
                    joinInnerSubscriber.request(1);
                } else if (!joinInnerSubscriber.getQueue().offer(t)) {
                    cancelAll();
                    MissingBackpressureException missingBackpressureException = new MissingBackpressureException("Queue full?!");
                    if (this.errors.compareAndSet(null, missingBackpressureException)) {
                        this.downstream.onError(missingBackpressureException);
                        return;
                    } else {
                        RxJavaPlugins.onError(missingBackpressureException);
                        return;
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else if (!joinInnerSubscriber.getQueue().offer(t)) {
                cancelAll();
                onError(new MissingBackpressureException("Queue full?!"));
                return;
            } else if (getAndIncrement() != 0) {
                return;
            }
            drainLoop();
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onError(Throwable th) {
            if (this.errors.compareAndSet(null, th)) {
                cancelAll();
                drain();
            } else if (th != this.errors.get()) {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onComplete() {
            this.done.decrementAndGet();
            drain();
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:30:0x005f, code lost:
            if (r13 == false) goto L_0x0067;
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x0061, code lost:
            if (r11 == false) goto L_0x0067;
         */
        /* JADX WARN: Code restructure failed: missing block: B:32:0x0063, code lost:
            r3.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:33:0x0066, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:34:0x0067, code lost:
            if (r11 == false) goto L_0x006b;
         */
        /* JADX WARN: Code restructure failed: missing block: B:35:0x0069, code lost:
            r10 = r14;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                Method dump skipped, instructions count: 219
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscription.drainLoop():void");
        }
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelJoin$JoinSubscriptionDelayError */
    /* loaded from: classes.dex */
    static final class JoinSubscriptionDelayError<T> extends JoinSubscriptionBase<T> {
        private static final long serialVersionUID = -5737965195918321883L;

        JoinSubscriptionDelayError(Subscriber<? super T> subscriber, int i, int i2) {
            super(subscriber, i, i2);
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onNext(JoinInnerSubscriber<T> joinInnerSubscriber, T t) {
            if (get() != 0 || !compareAndSet(0, 1)) {
                if (!joinInnerSubscriber.getQueue().offer(t) && joinInnerSubscriber.cancel()) {
                    this.errors.addThrowable(new MissingBackpressureException("Queue full?!"));
                    this.done.decrementAndGet();
                }
                if (getAndIncrement() != 0) {
                    return;
                }
            } else {
                if (this.requested.get() != 0) {
                    this.downstream.onNext(t);
                    if (this.requested.get() != Long.MAX_VALUE) {
                        this.requested.decrementAndGet();
                    }
                    joinInnerSubscriber.request(1);
                } else if (!joinInnerSubscriber.getQueue().offer(t)) {
                    joinInnerSubscriber.cancel();
                    this.errors.addThrowable(new MissingBackpressureException("Queue full?!"));
                    this.done.decrementAndGet();
                    drainLoop();
                    return;
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onError(Throwable th) {
            this.errors.addThrowable(th);
            this.done.decrementAndGet();
            drain();
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onComplete() {
            this.done.decrementAndGet();
            drain();
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:25:0x004d, code lost:
            if (r13 == false) goto L_0x0069;
         */
        /* JADX WARN: Code restructure failed: missing block: B:26:0x004f, code lost:
            if (r11 == false) goto L_0x0069;
         */
        /* JADX WARN: Code restructure failed: missing block: B:28:0x0059, code lost:
            if (r17.errors.get() == null) goto L_0x0065;
         */
        /* JADX WARN: Code restructure failed: missing block: B:29:0x005b, code lost:
            r3.onError(r17.errors.terminate());
         */
        /* JADX WARN: Code restructure failed: missing block: B:30:0x0065, code lost:
            r3.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x0068, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:32:0x0069, code lost:
            if (r11 == false) goto L_0x006d;
         */
        /* JADX WARN: Code restructure failed: missing block: B:33:0x006b, code lost:
            r10 = r14;
         */
        /* JADX WARN: Code restructure failed: missing block: B:94:?, code lost:
            return;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                Method dump skipped, instructions count: 224
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionDelayError.drainLoop():void");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelJoin$JoinInnerSubscriber */
    /* loaded from: classes.dex */
    public static final class JoinInnerSubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T> {
        private static final long serialVersionUID = 8410034718427740355L;
        final int limit;
        final JoinSubscriptionBase<T> parent;
        final int prefetch;
        long produced;
        volatile SimplePlainQueue<T> queue;

        JoinInnerSubscriber(JoinSubscriptionBase<T> joinSubscriptionBase, int i) {
            this.parent = joinSubscriptionBase;
            this.prefetch = i;
            this.limit = i - (i >> 2);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this, subscription, (long) this.prefetch);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.parent.onNext(this, t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.parent.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.parent.onComplete();
        }

        public void requestOne() {
            long j = this.produced + 1;
            if (j == ((long) this.limit)) {
                this.produced = 0;
                get().request(j);
                return;
            }
            this.produced = j;
        }

        public void request(long j) {
            long j2 = this.produced + j;
            if (j2 >= ((long) this.limit)) {
                this.produced = 0;
                get().request(j2);
                return;
            }
            this.produced = j2;
        }

        public boolean cancel() {
            return SubscriptionHelper.cancel(this);
        }

        SimplePlainQueue<T> getQueue() {
            SimplePlainQueue<T> simplePlainQueue = this.queue;
            if (simplePlainQueue != null) {
                return simplePlainQueue;
            }
            SpscArrayQueue spscArrayQueue = new SpscArrayQueue(this.prefetch);
            this.queue = spscArrayQueue;
            return spscArrayQueue;
        }
    }
}
