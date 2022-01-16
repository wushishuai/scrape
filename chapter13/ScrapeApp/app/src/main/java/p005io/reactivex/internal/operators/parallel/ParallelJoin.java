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

    public ParallelJoin(ParallelFlowable<? extends T> source, int prefetch, boolean delayErrors) {
        this.source = source;
        this.prefetch = prefetch;
        this.delayErrors = delayErrors;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        JoinSubscriptionBase<T> parent;
        if (this.delayErrors) {
            parent = new JoinSubscriptionDelayError<>(s, this.source.parallelism(), this.prefetch);
        } else {
            parent = new JoinSubscription<>(s, this.source.parallelism(), this.prefetch);
        }
        s.onSubscribe(parent);
        this.source.subscribe(parent.subscribers);
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

        JoinSubscriptionBase(Subscriber<? super T> actual, int n, int prefetch) {
            this.downstream = actual;
            JoinInnerSubscriber<T>[] a = new JoinInnerSubscriber[n];
            for (int i = 0; i < n; i++) {
                a[i] = new JoinInnerSubscriber<>(this, prefetch);
            }
            this.subscribers = a;
            this.done.lazySet(n);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
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

        JoinSubscription(Subscriber<? super T> actual, int n, int prefetch) {
            super(actual, n, prefetch);
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onNext(JoinInnerSubscriber<T> inner, T value) {
            if (get() == 0 && compareAndSet(0, 1)) {
                if (this.requested.get() != 0) {
                    this.downstream.onNext(value);
                    if (this.requested.get() != Long.MAX_VALUE) {
                        this.requested.decrementAndGet();
                    }
                    inner.request(1);
                } else if (!inner.getQueue().offer(value)) {
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
            } else if (!inner.getQueue().offer(value)) {
                cancelAll();
                onError(new MissingBackpressureException("Queue full?!"));
                return;
            } else if (getAndIncrement() != 0) {
                return;
            }
            drainLoop();
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onError(Throwable e) {
            if (this.errors.compareAndSet(null, e)) {
                cancelAll();
                drain();
            } else if (e != this.errors.get()) {
                RxJavaPlugins.onError(e);
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

        /* JADX WARN: Code restructure failed: missing block: B:28:0x005e, code lost:
            if (r12 == false) goto L_0x0066;
         */
        /* JADX WARN: Code restructure failed: missing block: B:29:0x0060, code lost:
            if (r13 == false) goto L_0x0066;
         */
        /* JADX WARN: Code restructure failed: missing block: B:30:0x0062, code lost:
            r4.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x0065, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:32:0x0066, code lost:
            if (r13 == false) goto L_0x0010;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                Method dump skipped, instructions count: 218
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscription.drainLoop():void");
        }
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelJoin$JoinSubscriptionDelayError */
    /* loaded from: classes.dex */
    static final class JoinSubscriptionDelayError<T> extends JoinSubscriptionBase<T> {
        private static final long serialVersionUID = -5737965195918321883L;

        JoinSubscriptionDelayError(Subscriber<? super T> actual, int n, int prefetch) {
            super(actual, n, prefetch);
        }

        @Override // p005io.reactivex.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onNext(JoinInnerSubscriber<T> inner, T value) {
            if (get() != 0 || !compareAndSet(0, 1)) {
                if (!inner.getQueue().offer(value) && inner.cancel()) {
                    this.errors.addThrowable(new MissingBackpressureException("Queue full?!"));
                    this.done.decrementAndGet();
                }
                if (getAndIncrement() != 0) {
                    return;
                }
            } else {
                if (this.requested.get() != 0) {
                    this.downstream.onNext(value);
                    if (this.requested.get() != Long.MAX_VALUE) {
                        this.requested.decrementAndGet();
                    }
                    inner.request(1);
                } else if (!inner.getQueue().offer(value)) {
                    inner.cancel();
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
        void onError(Throwable e) {
            this.errors.addThrowable(e);
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

        /* JADX WARN: Code restructure failed: missing block: B:23:0x004d, code lost:
            if (r11 == false) goto L_0x0069;
         */
        /* JADX WARN: Code restructure failed: missing block: B:24:0x004f, code lost:
            if (r12 == false) goto L_0x0069;
         */
        /* JADX WARN: Code restructure failed: missing block: B:26:0x0059, code lost:
            if (r20.errors.get() == null) goto L_0x0065;
         */
        /* JADX WARN: Code restructure failed: missing block: B:27:0x005b, code lost:
            r4.onError(r20.errors.terminate());
         */
        /* JADX WARN: Code restructure failed: missing block: B:28:0x0065, code lost:
            r4.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:29:0x0068, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:30:0x0069, code lost:
            if (r12 == false) goto L_0x0010;
         */
        /* JADX WARN: Code restructure failed: missing block: B:90:?, code lost:
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

        JoinInnerSubscriber(JoinSubscriptionBase<T> parent, int prefetch) {
            this.parent = parent;
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, (long) this.prefetch);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.parent.onNext(this, t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.parent.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.parent.onComplete();
        }

        public void requestOne() {
            long p = this.produced + 1;
            if (p == ((long) this.limit)) {
                this.produced = 0;
                get().request(p);
                return;
            }
            this.produced = p;
        }

        public void request(long n) {
            long p = this.produced + n;
            if (p >= ((long) this.limit)) {
                this.produced = 0;
                get().request(p);
                return;
            }
            this.produced = p;
        }

        public boolean cancel() {
            return SubscriptionHelper.cancel(this);
        }

        SimplePlainQueue<T> getQueue() {
            SimplePlainQueue<T> q = this.queue;
            if (q != null) {
                return q;
            }
            SimplePlainQueue<T> q2 = new SpscArrayQueue<>(this.prefetch);
            this.queue = q2;
            return q2;
        }
    }
}
