package io.reactivex.internal.operators.flowable;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.disposables.SequentialDisposable;
import io.reactivex.internal.fuseable.SimplePlainQueue;
import io.reactivex.internal.queue.MpscLinkedQueue;
import io.reactivex.internal.subscribers.QueueDrainSubscriber;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.NotificationLite;
import io.reactivex.processors.UnicastProcessor;
import io.reactivex.subscribers.SerializedSubscriber;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
/* loaded from: classes.dex */
public final class FlowableWindowTimed<T> extends AbstractFlowableWithUpstream<T, Flowable<T>> {
    final int bufferSize;
    final long maxSize;
    final boolean restartTimerOnMaxSize;
    final Scheduler scheduler;
    final long timeskip;
    final long timespan;
    final TimeUnit unit;

    public FlowableWindowTimed(Flowable<T> source, long timespan, long timeskip, TimeUnit unit, Scheduler scheduler, long maxSize, int bufferSize, boolean restartTimerOnMaxSize) {
        super(source);
        this.timespan = timespan;
        this.timeskip = timeskip;
        this.unit = unit;
        this.scheduler = scheduler;
        this.maxSize = maxSize;
        this.bufferSize = bufferSize;
        this.restartTimerOnMaxSize = restartTimerOnMaxSize;
    }

    @Override // io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super Flowable<T>> s) {
        SerializedSubscriber<Flowable<T>> actual = new SerializedSubscriber<>(s);
        if (this.timespan != this.timeskip) {
            this.source.subscribe((FlowableSubscriber) new WindowSkipSubscriber(actual, this.timespan, this.timeskip, this.unit, this.scheduler.createWorker(), this.bufferSize));
        } else if (this.maxSize == Long.MAX_VALUE) {
            this.source.subscribe((FlowableSubscriber) new WindowExactUnboundedSubscriber(actual, this.timespan, this.unit, this.scheduler, this.bufferSize));
        } else {
            this.source.subscribe((FlowableSubscriber) new WindowExactBoundedSubscriber(actual, this.timespan, this.unit, this.scheduler, this.bufferSize, this.maxSize, this.restartTimerOnMaxSize));
        }
    }

    /* loaded from: classes.dex */
    static final class WindowExactUnboundedSubscriber<T> extends QueueDrainSubscriber<T, Object, Flowable<T>> implements FlowableSubscriber<T>, Subscription, Runnable {
        static final Object NEXT = new Object();
        final int bufferSize;
        final Scheduler scheduler;
        volatile boolean terminated;
        final SequentialDisposable timer = new SequentialDisposable();
        final long timespan;
        final TimeUnit unit;
        Subscription upstream;
        UnicastProcessor<T> window;

        WindowExactUnboundedSubscriber(Subscriber<? super Flowable<T>> actual, long timespan, TimeUnit unit, Scheduler scheduler, int bufferSize) {
            super(actual, new MpscLinkedQueue());
            this.timespan = timespan;
            this.unit = unit;
            this.scheduler = scheduler;
            this.bufferSize = bufferSize;
        }

        @Override // io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.window = UnicastProcessor.create(this.bufferSize);
                Subscriber<? super Flowable<T>> a = this.downstream;
                a.onSubscribe(this);
                long r = requested();
                if (r != 0) {
                    a.onNext(this.window);
                    if (r != Long.MAX_VALUE) {
                        produced(1);
                    }
                    if (!this.cancelled) {
                        SequentialDisposable sequentialDisposable = this.timer;
                        Scheduler scheduler = this.scheduler;
                        long j = this.timespan;
                        if (sequentialDisposable.replace(scheduler.schedulePeriodicallyDirect(this, j, j, this.unit))) {
                            s.request(Long.MAX_VALUE);
                            return;
                        }
                        return;
                    }
                    return;
                }
                this.cancelled = true;
                s.cancel();
                a.onError(new MissingBackpressureException("Could not deliver first window due to lack of requests."));
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.terminated) {
                if (fastEnter()) {
                    this.window.onNext(t);
                    if (leave(-1) == 0) {
                        return;
                    }
                } else {
                    this.queue.offer(NotificationLite.next(t));
                    if (!enter()) {
                        return;
                    }
                }
                drainLoop();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.error = t;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onError(t);
            dispose();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onComplete();
            dispose();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            requested(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
        }

        public void dispose() {
            DisposableHelper.dispose(this.timer);
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.lang.Runnable
        public void run() {
            if (this.cancelled) {
                this.terminated = true;
                dispose();
            }
            this.queue.offer(NEXT);
            if (enter()) {
                drainLoop();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:10:0x0024, code lost:
            r2.onError(r7);
         */
        /* JADX WARN: Code restructure failed: missing block: B:11:0x0028, code lost:
            r2.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:12:0x002b, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:47:?, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:8:0x0018, code lost:
            r13.window = null;
            r0.clear();
            dispose();
            r7 = r13.error;
         */
        /* JADX WARN: Code restructure failed: missing block: B:9:0x0022, code lost:
            if (r7 == null) goto L_0x0028;
         */
        /* JADX WARN: Multi-variable type inference failed */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        void drainLoop() {
            /*
                r13 = this;
                io.reactivex.internal.fuseable.SimplePlainQueue r0 = r13.queue
                org.reactivestreams.Subscriber r1 = r13.downstream
                io.reactivex.processors.UnicastProcessor<T> r2 = r13.window
                r3 = 1
            L_0x0007:
                boolean r4 = r13.terminated
                boolean r5 = r13.done
                java.lang.Object r6 = r0.poll()
                r7 = 0
                if (r5 == 0) goto L_0x002c
                if (r6 == 0) goto L_0x0018
                java.lang.Object r8 = io.reactivex.internal.operators.flowable.FlowableWindowTimed.WindowExactUnboundedSubscriber.NEXT
                if (r6 != r8) goto L_0x002c
            L_0x0018:
                r13.window = r7
                r0.clear()
                r13.dispose()
                java.lang.Throwable r7 = r13.error
                if (r7 == 0) goto L_0x0028
                r2.onError(r7)
                goto L_0x002b
            L_0x0028:
                r2.onComplete()
            L_0x002b:
                return
            L_0x002c:
                if (r6 != 0) goto L_0x0038
                int r4 = -r3
                int r3 = r13.leave(r4)
                if (r3 != 0) goto L_0x0007
                return
            L_0x0038:
                java.lang.Object r8 = io.reactivex.internal.operators.flowable.FlowableWindowTimed.WindowExactUnboundedSubscriber.NEXT
                if (r6 != r8) goto L_0x0085
                r2.onComplete()
                if (r4 != 0) goto L_0x007f
                int r8 = r13.bufferSize
                io.reactivex.processors.UnicastProcessor r2 = io.reactivex.processors.UnicastProcessor.create(r8)
                r13.window = r2
                long r8 = r13.requested()
                r10 = 0
                int r12 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
                if (r12 == 0) goto L_0x0065
                r1.onNext(r2)
                r10 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
                int r7 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1))
                if (r7 == 0) goto L_0x0064
                r10 = 1
                r13.produced(r10)
            L_0x0064:
                goto L_0x0007
            L_0x0065:
                r13.window = r7
                io.reactivex.internal.fuseable.SimplePlainQueue r7 = r13.queue
                r7.clear()
                org.reactivestreams.Subscription r7 = r13.upstream
                r7.cancel()
                r13.dispose()
                io.reactivex.exceptions.MissingBackpressureException r7 = new io.reactivex.exceptions.MissingBackpressureException
                java.lang.String r10 = "Could not deliver first window due to lack of requests."
                r7.<init>(r10)
                r1.onError(r7)
                return
            L_0x007f:
                org.reactivestreams.Subscription r7 = r13.upstream
                r7.cancel()
                goto L_0x0007
            L_0x0085:
                java.lang.Object r7 = io.reactivex.internal.util.NotificationLite.getValue(r6)
                r2.onNext(r7)
                goto L_0x0007
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.operators.flowable.FlowableWindowTimed.WindowExactUnboundedSubscriber.drainLoop():void");
        }
    }

    /* loaded from: classes.dex */
    static final class WindowExactBoundedSubscriber<T> extends QueueDrainSubscriber<T, Object, Flowable<T>> implements Subscription {
        final int bufferSize;
        long count;
        final long maxSize;
        long producerIndex;
        final boolean restartTimerOnMaxSize;
        final Scheduler scheduler;
        volatile boolean terminated;
        final SequentialDisposable timer = new SequentialDisposable();
        final long timespan;
        final TimeUnit unit;
        Subscription upstream;
        UnicastProcessor<T> window;
        final Scheduler.Worker worker;

        WindowExactBoundedSubscriber(Subscriber<? super Flowable<T>> actual, long timespan, TimeUnit unit, Scheduler scheduler, int bufferSize, long maxSize, boolean restartTimerOnMaxSize) {
            super(actual, new MpscLinkedQueue());
            this.timespan = timespan;
            this.unit = unit;
            this.scheduler = scheduler;
            this.bufferSize = bufferSize;
            this.maxSize = maxSize;
            this.restartTimerOnMaxSize = restartTimerOnMaxSize;
            if (restartTimerOnMaxSize) {
                this.worker = scheduler.createWorker();
            } else {
                this.worker = null;
            }
        }

        @Override // io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            Disposable task;
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                Subscriber<? super Flowable<T>> a = this.downstream;
                a.onSubscribe(this);
                if (!this.cancelled) {
                    UnicastProcessor<T> w = UnicastProcessor.create(this.bufferSize);
                    this.window = w;
                    long r = requested();
                    if (r != 0) {
                        a.onNext(w);
                        if (r != Long.MAX_VALUE) {
                            produced(1);
                        }
                        ConsumerIndexHolder consumerIndexHolder = new ConsumerIndexHolder(this.producerIndex, this);
                        if (this.restartTimerOnMaxSize) {
                            Scheduler.Worker worker = this.worker;
                            long j = this.timespan;
                            task = worker.schedulePeriodically(consumerIndexHolder, j, j, this.unit);
                        } else {
                            Scheduler scheduler = this.scheduler;
                            long j2 = this.timespan;
                            task = scheduler.schedulePeriodicallyDirect(consumerIndexHolder, j2, j2, this.unit);
                        }
                        if (this.timer.replace(task)) {
                            s.request(Long.MAX_VALUE);
                            return;
                        }
                        return;
                    }
                    this.cancelled = true;
                    s.cancel();
                    a.onError(new MissingBackpressureException("Could not deliver initial window due to lack of requests."));
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.terminated) {
                if (fastEnter()) {
                    UnicastProcessor<T> w = this.window;
                    w.onNext(t);
                    long c = this.count + 1;
                    if (c >= this.maxSize) {
                        this.producerIndex++;
                        this.count = 0;
                        w.onComplete();
                        long r = requested();
                        if (r != 0) {
                            UnicastProcessor<T> w2 = UnicastProcessor.create(this.bufferSize);
                            this.window = w2;
                            this.downstream.onNext(w2);
                            if (r != Long.MAX_VALUE) {
                                produced(1);
                            }
                            if (this.restartTimerOnMaxSize) {
                                this.timer.get().dispose();
                                Scheduler.Worker worker = this.worker;
                                ConsumerIndexHolder consumerIndexHolder = new ConsumerIndexHolder(this.producerIndex, this);
                                long j = this.timespan;
                                this.timer.replace(worker.schedulePeriodically(consumerIndexHolder, j, j, this.unit));
                            }
                        } else {
                            this.window = null;
                            this.upstream.cancel();
                            this.downstream.onError(new MissingBackpressureException("Could not deliver window due to lack of requests"));
                            dispose();
                            return;
                        }
                    } else {
                        this.count = c;
                    }
                    if (leave(-1) == 0) {
                        return;
                    }
                } else {
                    this.queue.offer(NotificationLite.next(t));
                    if (!enter()) {
                        return;
                    }
                }
                drainLoop();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.error = t;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onError(t);
            dispose();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onComplete();
            dispose();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            requested(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
        }

        public void dispose() {
            DisposableHelper.dispose(this.timer);
            Scheduler.Worker w = this.worker;
            if (w != null) {
                w.dispose();
            }
        }

        void drainLoop() {
            Subscriber<? super Flowable<T>> a;
            SimplePlainQueue<Object> q;
            UnicastProcessor<T> w;
            SimplePlainQueue<Object> q2 = this.queue;
            Subscriber<? super Flowable<T>> a2 = this.downstream;
            UnicastProcessor<T> w2 = this.window;
            int missed = 1;
            while (!this.terminated) {
                boolean d = this.done;
                Object o = q2.poll();
                boolean empty = o == null;
                boolean isHolder = o instanceof ConsumerIndexHolder;
                if (d && (empty || isHolder)) {
                    this.window = null;
                    q2.clear();
                    Throwable err = this.error;
                    if (err != null) {
                        w2.onError(err);
                    } else {
                        w2.onComplete();
                    }
                    dispose();
                    return;
                } else if (empty) {
                    missed = leave(-missed);
                    if (missed == 0) {
                        return;
                    }
                } else if (isHolder) {
                    ConsumerIndexHolder consumerIndexHolder = (ConsumerIndexHolder) o;
                    if (this.restartTimerOnMaxSize || this.producerIndex == consumerIndexHolder.index) {
                        w2.onComplete();
                        this.count = 0;
                        w2 = UnicastProcessor.create(this.bufferSize);
                        this.window = w2;
                        long r = requested();
                        if (r != 0) {
                            a2.onNext(w2);
                            if (r != Long.MAX_VALUE) {
                                produced(1);
                            }
                        } else {
                            this.window = null;
                            this.queue.clear();
                            this.upstream.cancel();
                            a2.onError(new MissingBackpressureException("Could not deliver first window due to lack of requests."));
                            dispose();
                            return;
                        }
                    }
                } else {
                    w2.onNext(NotificationLite.getValue(o));
                    long c = this.count + 1;
                    if (c >= this.maxSize) {
                        this.producerIndex++;
                        this.count = 0;
                        w2.onComplete();
                        long r2 = requested();
                        if (r2 != 0) {
                            UnicastProcessor<T> w3 = UnicastProcessor.create(this.bufferSize);
                            this.window = w3;
                            this.downstream.onNext(w3);
                            if (r2 != Long.MAX_VALUE) {
                                produced(1);
                            }
                            if (this.restartTimerOnMaxSize) {
                                this.timer.get().dispose();
                                Scheduler.Worker worker = this.worker;
                                q = q2;
                                a = a2;
                                ConsumerIndexHolder consumerIndexHolder2 = new ConsumerIndexHolder(this.producerIndex, this);
                                long j = this.timespan;
                                w = w3;
                                this.timer.replace(worker.schedulePeriodically(consumerIndexHolder2, j, j, this.unit));
                            } else {
                                q = q2;
                                a = a2;
                                w = w3;
                            }
                            w2 = w;
                        } else {
                            this.window = null;
                            this.upstream.cancel();
                            this.downstream.onError(new MissingBackpressureException("Could not deliver window due to lack of requests"));
                            dispose();
                            return;
                        }
                    } else {
                        q = q2;
                        a = a2;
                        this.count = c;
                    }
                    q2 = q;
                    a2 = a;
                }
            }
            this.upstream.cancel();
            q2.clear();
            dispose();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public static final class ConsumerIndexHolder implements Runnable {
            final long index;
            final WindowExactBoundedSubscriber<?> parent;

            ConsumerIndexHolder(long index, WindowExactBoundedSubscriber<?> parent) {
                this.index = index;
                this.parent = parent;
            }

            @Override // java.lang.Runnable
            public void run() {
                WindowExactBoundedSubscriber<?> p = this.parent;
                if (!p.cancelled) {
                    p.queue.offer(this);
                } else {
                    p.terminated = true;
                    p.dispose();
                }
                if (p.enter()) {
                    p.drainLoop();
                }
            }
        }
    }

    /* loaded from: classes.dex */
    static final class WindowSkipSubscriber<T> extends QueueDrainSubscriber<T, Object, Flowable<T>> implements Subscription, Runnable {
        final int bufferSize;
        volatile boolean terminated;
        final long timeskip;
        final long timespan;
        final TimeUnit unit;
        Subscription upstream;
        final List<UnicastProcessor<T>> windows = new LinkedList();
        final Scheduler.Worker worker;

        WindowSkipSubscriber(Subscriber<? super Flowable<T>> actual, long timespan, long timeskip, TimeUnit unit, Scheduler.Worker worker, int bufferSize) {
            super(actual, new MpscLinkedQueue());
            this.timespan = timespan;
            this.timeskip = timeskip;
            this.unit = unit;
            this.worker = worker;
            this.bufferSize = bufferSize;
        }

        @Override // io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
                if (!this.cancelled) {
                    long r = requested();
                    if (r != 0) {
                        UnicastProcessor<T> w = UnicastProcessor.create(this.bufferSize);
                        this.windows.add(w);
                        this.downstream.onNext(w);
                        if (r != Long.MAX_VALUE) {
                            produced(1);
                        }
                        this.worker.schedule(new Completion(w), this.timespan, this.unit);
                        Scheduler.Worker worker = this.worker;
                        long j = this.timeskip;
                        worker.schedulePeriodically(this, j, j, this.unit);
                        s.request(Long.MAX_VALUE);
                        return;
                    }
                    s.cancel();
                    this.downstream.onError(new MissingBackpressureException("Could not emit the first window due to lack of requests"));
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (fastEnter()) {
                for (UnicastProcessor<T> w : this.windows) {
                    w.onNext(t);
                }
                if (leave(-1) == 0) {
                    return;
                }
            } else {
                this.queue.offer(t);
                if (!enter()) {
                    return;
                }
            }
            drainLoop();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.error = t;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onError(t);
            dispose();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onComplete();
            dispose();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            requested(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
        }

        public void dispose() {
            this.worker.dispose();
        }

        void complete(UnicastProcessor<T> w) {
            this.queue.offer(new SubjectWork(w, false));
            if (enter()) {
                drainLoop();
            }
        }

        void drainLoop() {
            SimplePlainQueue<Object> q;
            int missed;
            SimplePlainQueue<Object> q2 = this.queue;
            Subscriber<? super Flowable<T>> a = this.downstream;
            List<UnicastProcessor<T>> ws = this.windows;
            int missed2 = 1;
            while (!this.terminated) {
                boolean d = this.done;
                T t = (T) q2.poll();
                boolean empty = t == null;
                boolean sw = t instanceof SubjectWork;
                if (d && (empty || sw)) {
                    q2.clear();
                    Throwable e = this.error;
                    if (e != null) {
                        for (UnicastProcessor<T> w : ws) {
                            w.onError(e);
                        }
                    } else {
                        for (UnicastProcessor<T> w2 : ws) {
                            w2.onComplete();
                        }
                    }
                    ws.clear();
                    dispose();
                    return;
                } else if (empty) {
                    missed2 = leave(-missed2);
                    if (missed2 == 0) {
                        return;
                    }
                } else {
                    if (sw) {
                        SubjectWork<T> work = (SubjectWork) t;
                        if (!work.open) {
                            q = q2;
                            missed = missed2;
                            ws.remove(work.w);
                            work.w.onComplete();
                            if (ws.isEmpty() && this.cancelled) {
                                this.terminated = true;
                            }
                        } else if (this.cancelled) {
                            q = q2;
                            missed = missed2;
                        } else {
                            long r = requested();
                            if (r != 0) {
                                UnicastProcessor<T> w3 = UnicastProcessor.create(this.bufferSize);
                                ws.add(w3);
                                a.onNext(w3);
                                if (r != Long.MAX_VALUE) {
                                    produced(1);
                                }
                                missed = missed2;
                                q = q2;
                                this.worker.schedule(new Completion(w3), this.timespan, this.unit);
                            } else {
                                q = q2;
                                missed = missed2;
                                a.onError(new MissingBackpressureException("Can't emit window due to lack of requests"));
                            }
                        }
                    } else {
                        q = q2;
                        missed = missed2;
                        for (UnicastProcessor<T> w4 : ws) {
                            w4.onNext(t);
                        }
                    }
                    missed2 = missed;
                    q2 = q;
                }
            }
            this.upstream.cancel();
            dispose();
            q2.clear();
            ws.clear();
        }

        @Override // java.lang.Runnable
        public void run() {
            SubjectWork<T> sw = new SubjectWork<>(UnicastProcessor.create(this.bufferSize), true);
            if (!this.cancelled) {
                this.queue.offer(sw);
            }
            if (enter()) {
                drainLoop();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public static final class SubjectWork<T> {
            final boolean open;
            final UnicastProcessor<T> w;

            SubjectWork(UnicastProcessor<T> w, boolean open) {
                this.w = w;
                this.open = open;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public final class Completion implements Runnable {
            private final UnicastProcessor<T> processor;

            Completion(UnicastProcessor<T> processor) {
                this.processor = processor;
            }

            @Override // java.lang.Runnable
            public void run() {
                WindowSkipSubscriber.this.complete(this.processor);
            }
        }
    }
}
