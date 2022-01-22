package p005io.reactivex.internal.operators.flowable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.subscribers.QueueDrainSubscriber;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.processors.UnicastProcessor;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed */
/* loaded from: classes.dex */
public final class FlowableWindowTimed<T> extends AbstractFlowableWithUpstream<T, Flowable<T>> {
    final int bufferSize;
    final long maxSize;
    final boolean restartTimerOnMaxSize;
    final Scheduler scheduler;
    final long timeskip;
    final long timespan;
    final TimeUnit unit;

    public FlowableWindowTimed(Flowable<T> flowable, long j, long j2, TimeUnit timeUnit, Scheduler scheduler, long j3, int i, boolean z) {
        super(flowable);
        this.timespan = j;
        this.timeskip = j2;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.maxSize = j3;
        this.bufferSize = i;
        this.restartTimerOnMaxSize = z;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super Flowable<T>> subscriber) {
        SerializedSubscriber serializedSubscriber = new SerializedSubscriber(subscriber);
        if (this.timespan != this.timeskip) {
            this.source.subscribe((FlowableSubscriber) new WindowSkipSubscriber(serializedSubscriber, this.timespan, this.timeskip, this.unit, this.scheduler.createWorker(), this.bufferSize));
        } else if (this.maxSize == Long.MAX_VALUE) {
            this.source.subscribe((FlowableSubscriber) new WindowExactUnboundedSubscriber(serializedSubscriber, this.timespan, this.unit, this.scheduler, this.bufferSize));
        } else {
            this.source.subscribe((FlowableSubscriber) new WindowExactBoundedSubscriber(serializedSubscriber, this.timespan, this.unit, this.scheduler, this.bufferSize, this.maxSize, this.restartTimerOnMaxSize));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed$WindowExactUnboundedSubscriber */
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

        WindowExactUnboundedSubscriber(Subscriber<? super Flowable<T>> subscriber, long j, TimeUnit timeUnit, Scheduler scheduler, int i) {
            super(subscriber, new MpscLinkedQueue());
            this.timespan = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
            this.bufferSize = i;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.window = UnicastProcessor.create(this.bufferSize);
                Subscriber subscriber = this.downstream;
                subscriber.onSubscribe(this);
                long requested = requested();
                if (requested != 0) {
                    subscriber.onNext(this.window);
                    if (requested != Long.MAX_VALUE) {
                        produced(1);
                    }
                    if (!this.cancelled) {
                        SequentialDisposable sequentialDisposable = this.timer;
                        Scheduler scheduler = this.scheduler;
                        long j = this.timespan;
                        if (sequentialDisposable.replace(scheduler.schedulePeriodicallyDirect(this, j, j, this.unit))) {
                            subscription.request(Long.MAX_VALUE);
                            return;
                        }
                        return;
                    }
                    return;
                }
                this.cancelled = true;
                subscription.cancel();
                subscriber.onError(new MissingBackpressureException("Could not deliver first window due to lack of requests."));
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
        public void onError(Throwable th) {
            this.error = th;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onError(th);
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
        public void request(long j) {
            requested(j);
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
            r2.onError(r0);
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
            r10.window = null;
            r0.clear();
            dispose();
            r0 = r10.error;
         */
        /* JADX WARN: Code restructure failed: missing block: B:9:0x0022, code lost:
            if (r0 == null) goto L_0x0028;
         */
        /* JADX WARN: Multi-variable type inference failed */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                r10 = this;
                io.reactivex.internal.fuseable.SimplePlainQueue r0 = r10.queue
                org.reactivestreams.Subscriber r1 = r10.downstream
                io.reactivex.processors.UnicastProcessor<T> r2 = r10.window
                r3 = 1
            L_0x0007:
                boolean r4 = r10.terminated
                boolean r5 = r10.done
                java.lang.Object r6 = r0.poll()
                r7 = 0
                if (r5 == 0) goto L_0x002c
                if (r6 == 0) goto L_0x0018
                java.lang.Object r5 = p005io.reactivex.internal.operators.flowable.FlowableWindowTimed.WindowExactUnboundedSubscriber.NEXT
                if (r6 != r5) goto L_0x002c
            L_0x0018:
                r10.window = r7
                r0.clear()
                r10.dispose()
                java.lang.Throwable r0 = r10.error
                if (r0 == 0) goto L_0x0028
                r2.onError(r0)
                goto L_0x002b
            L_0x0028:
                r2.onComplete()
            L_0x002b:
                return
            L_0x002c:
                if (r6 != 0) goto L_0x0036
                int r3 = -r3
                int r3 = r10.leave(r3)
                if (r3 != 0) goto L_0x0007
                return
            L_0x0036:
                java.lang.Object r5 = p005io.reactivex.internal.operators.flowable.FlowableWindowTimed.WindowExactUnboundedSubscriber.NEXT
                if (r6 != r5) goto L_0x0083
                r2.onComplete()
                if (r4 != 0) goto L_0x007d
                int r2 = r10.bufferSize
                io.reactivex.processors.UnicastProcessor r2 = p005io.reactivex.processors.UnicastProcessor.create(r2)
                r10.window = r2
                long r4 = r10.requested()
                r8 = 0
                int r6 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
                if (r6 == 0) goto L_0x0063
                r1.onNext(r2)
                r6 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
                int r8 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
                if (r8 == 0) goto L_0x0007
                r4 = 1
                r10.produced(r4)
                goto L_0x0007
            L_0x0063:
                r10.window = r7
                io.reactivex.internal.fuseable.SimplePlainQueue r0 = r10.queue
                r0.clear()
                org.reactivestreams.Subscription r0 = r10.upstream
                r0.cancel()
                r10.dispose()
                io.reactivex.exceptions.MissingBackpressureException r0 = new io.reactivex.exceptions.MissingBackpressureException
                java.lang.String r2 = "Could not deliver first window due to lack of requests."
                r0.<init>(r2)
                r1.onError(r0)
                return
            L_0x007d:
                org.reactivestreams.Subscription r4 = r10.upstream
                r4.cancel()
                goto L_0x0007
            L_0x0083:
                java.lang.Object r4 = p005io.reactivex.internal.util.NotificationLite.getValue(r6)
                r2.onNext(r4)
                goto L_0x0007
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.flowable.FlowableWindowTimed.WindowExactUnboundedSubscriber.drainLoop():void");
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed$WindowExactBoundedSubscriber */
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

        WindowExactBoundedSubscriber(Subscriber<? super Flowable<T>> subscriber, long j, TimeUnit timeUnit, Scheduler scheduler, int i, long j2, boolean z) {
            super(subscriber, new MpscLinkedQueue());
            this.timespan = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
            this.bufferSize = i;
            this.maxSize = j2;
            this.restartTimerOnMaxSize = z;
            if (z) {
                this.worker = scheduler.createWorker();
            } else {
                this.worker = null;
            }
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            Disposable disposable;
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                Subscriber subscriber = this.downstream;
                subscriber.onSubscribe(this);
                if (!this.cancelled) {
                    UnicastProcessor<T> create = UnicastProcessor.create(this.bufferSize);
                    this.window = create;
                    long requested = requested();
                    if (requested != 0) {
                        subscriber.onNext(create);
                        if (requested != Long.MAX_VALUE) {
                            produced(1);
                        }
                        ConsumerIndexHolder consumerIndexHolder = new ConsumerIndexHolder(this.producerIndex, this);
                        if (this.restartTimerOnMaxSize) {
                            Scheduler.Worker worker = this.worker;
                            long j = this.timespan;
                            disposable = worker.schedulePeriodically(consumerIndexHolder, j, j, this.unit);
                        } else {
                            Scheduler scheduler = this.scheduler;
                            long j2 = this.timespan;
                            disposable = scheduler.schedulePeriodicallyDirect(consumerIndexHolder, j2, j2, this.unit);
                        }
                        if (this.timer.replace(disposable)) {
                            subscription.request(Long.MAX_VALUE);
                            return;
                        }
                        return;
                    }
                    this.cancelled = true;
                    subscription.cancel();
                    subscriber.onError(new MissingBackpressureException("Could not deliver initial window due to lack of requests."));
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.terminated) {
                if (fastEnter()) {
                    UnicastProcessor<T> unicastProcessor = this.window;
                    unicastProcessor.onNext(t);
                    long j = this.count + 1;
                    if (j >= this.maxSize) {
                        this.producerIndex++;
                        this.count = 0;
                        unicastProcessor.onComplete();
                        long requested = requested();
                        if (requested != 0) {
                            UnicastProcessor<T> create = UnicastProcessor.create(this.bufferSize);
                            this.window = create;
                            this.downstream.onNext(create);
                            if (requested != Long.MAX_VALUE) {
                                produced(1);
                            }
                            if (this.restartTimerOnMaxSize) {
                                this.timer.get().dispose();
                                Scheduler.Worker worker = this.worker;
                                ConsumerIndexHolder consumerIndexHolder = new ConsumerIndexHolder(this.producerIndex, this);
                                long j2 = this.timespan;
                                this.timer.replace(worker.schedulePeriodically(consumerIndexHolder, j2, j2, this.unit));
                            }
                        } else {
                            this.window = null;
                            this.upstream.cancel();
                            this.downstream.onError(new MissingBackpressureException("Could not deliver window due to lack of requests"));
                            dispose();
                            return;
                        }
                    } else {
                        this.count = j;
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
        public void onError(Throwable th) {
            this.error = th;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onError(th);
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
        public void request(long j) {
            requested(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
        }

        public void dispose() {
            DisposableHelper.dispose(this.timer);
            Scheduler.Worker worker = this.worker;
            if (worker != null) {
                worker.dispose();
            }
        }

        void drainLoop() {
            SimplePlainQueue simplePlainQueue = this.queue;
            Subscriber subscriber = this.downstream;
            UnicastProcessor<T> unicastProcessor = this.window;
            int i = 1;
            while (!this.terminated) {
                boolean z = this.done;
                Object poll = simplePlainQueue.poll();
                boolean z2 = poll == null;
                boolean z3 = poll instanceof ConsumerIndexHolder;
                if (z && (z2 || z3)) {
                    this.window = null;
                    simplePlainQueue.clear();
                    Throwable th = this.error;
                    if (th != null) {
                        unicastProcessor.onError(th);
                    } else {
                        unicastProcessor.onComplete();
                    }
                    dispose();
                    return;
                } else if (z2) {
                    i = leave(-i);
                    if (i == 0) {
                        return;
                    }
                } else if (z3) {
                    ConsumerIndexHolder consumerIndexHolder = (ConsumerIndexHolder) poll;
                    if (this.restartTimerOnMaxSize || this.producerIndex == consumerIndexHolder.index) {
                        unicastProcessor.onComplete();
                        this.count = 0;
                        unicastProcessor = UnicastProcessor.create(this.bufferSize);
                        this.window = unicastProcessor;
                        long requested = requested();
                        if (requested != 0) {
                            subscriber.onNext(unicastProcessor);
                            if (requested != Long.MAX_VALUE) {
                                produced(1);
                            }
                            i = i;
                        } else {
                            this.window = null;
                            this.queue.clear();
                            this.upstream.cancel();
                            subscriber.onError(new MissingBackpressureException("Could not deliver first window due to lack of requests."));
                            dispose();
                            return;
                        }
                    } else {
                        i = i;
                    }
                } else {
                    unicastProcessor.onNext(NotificationLite.getValue(poll));
                    long j = this.count + 1;
                    if (j >= this.maxSize) {
                        this.producerIndex++;
                        this.count = 0;
                        unicastProcessor.onComplete();
                        long requested2 = requested();
                        if (requested2 != 0) {
                            UnicastProcessor<T> create = UnicastProcessor.create(this.bufferSize);
                            this.window = create;
                            this.downstream.onNext(create);
                            if (requested2 != Long.MAX_VALUE) {
                                produced(1);
                            }
                            if (this.restartTimerOnMaxSize) {
                                this.timer.get().dispose();
                                Scheduler.Worker worker = this.worker;
                                ConsumerIndexHolder consumerIndexHolder2 = new ConsumerIndexHolder(this.producerIndex, this);
                                long j2 = this.timespan;
                                this.timer.replace(worker.schedulePeriodically(consumerIndexHolder2, j2, j2, this.unit));
                            }
                            unicastProcessor = create;
                        } else {
                            this.window = null;
                            this.upstream.cancel();
                            this.downstream.onError(new MissingBackpressureException("Could not deliver window due to lack of requests"));
                            dispose();
                            return;
                        }
                    } else {
                        this.count = j;
                    }
                    i = i;
                }
            }
            this.upstream.cancel();
            simplePlainQueue.clear();
            dispose();
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed$WindowExactBoundedSubscriber$ConsumerIndexHolder */
        /* loaded from: classes.dex */
        public static final class ConsumerIndexHolder implements Runnable {
            final long index;
            final WindowExactBoundedSubscriber<?> parent;

            ConsumerIndexHolder(long j, WindowExactBoundedSubscriber<?> windowExactBoundedSubscriber) {
                this.index = j;
                this.parent = windowExactBoundedSubscriber;
            }

            @Override // java.lang.Runnable
            public void run() {
                WindowExactBoundedSubscriber<?> windowExactBoundedSubscriber = this.parent;
                if (!windowExactBoundedSubscriber.cancelled) {
                    windowExactBoundedSubscriber.queue.offer(this);
                } else {
                    windowExactBoundedSubscriber.terminated = true;
                    windowExactBoundedSubscriber.dispose();
                }
                if (windowExactBoundedSubscriber.enter()) {
                    windowExactBoundedSubscriber.drainLoop();
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed$WindowSkipSubscriber */
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

        WindowSkipSubscriber(Subscriber<? super Flowable<T>> subscriber, long j, long j2, TimeUnit timeUnit, Scheduler.Worker worker, int i) {
            super(subscriber, new MpscLinkedQueue());
            this.timespan = j;
            this.timeskip = j2;
            this.unit = timeUnit;
            this.worker = worker;
            this.bufferSize = i;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                if (!this.cancelled) {
                    long requested = requested();
                    if (requested != 0) {
                        UnicastProcessor<T> create = UnicastProcessor.create(this.bufferSize);
                        this.windows.add(create);
                        this.downstream.onNext(create);
                        if (requested != Long.MAX_VALUE) {
                            produced(1);
                        }
                        this.worker.schedule(new Completion(create), this.timespan, this.unit);
                        Scheduler.Worker worker = this.worker;
                        long j = this.timeskip;
                        worker.schedulePeriodically(this, j, j, this.unit);
                        subscription.request(Long.MAX_VALUE);
                        return;
                    }
                    subscription.cancel();
                    this.downstream.onError(new MissingBackpressureException("Could not emit the first window due to lack of requests"));
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (fastEnter()) {
                for (UnicastProcessor<T> unicastProcessor : this.windows) {
                    unicastProcessor.onNext(t);
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
        public void onError(Throwable th) {
            this.error = th;
            this.done = true;
            if (enter()) {
                drainLoop();
            }
            this.downstream.onError(th);
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
        public void request(long j) {
            requested(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
        }

        public void dispose() {
            this.worker.dispose();
        }

        void complete(UnicastProcessor<T> unicastProcessor) {
            this.queue.offer(new SubjectWork(unicastProcessor, false));
            if (enter()) {
                drainLoop();
            }
        }

        void drainLoop() {
            SimplePlainQueue simplePlainQueue = this.queue;
            Subscriber subscriber = this.downstream;
            List<UnicastProcessor<T>> list = this.windows;
            int i = 1;
            while (!this.terminated) {
                boolean z = this.done;
                T t = (T) simplePlainQueue.poll();
                boolean z2 = t == null;
                boolean z3 = t instanceof SubjectWork;
                if (z && (z2 || z3)) {
                    simplePlainQueue.clear();
                    Throwable th = this.error;
                    if (th != null) {
                        for (UnicastProcessor<T> unicastProcessor : list) {
                            unicastProcessor.onError(th);
                        }
                    } else {
                        for (UnicastProcessor<T> unicastProcessor2 : list) {
                            unicastProcessor2.onComplete();
                        }
                    }
                    list.clear();
                    dispose();
                    return;
                } else if (z2) {
                    i = leave(-i);
                    if (i == 0) {
                        return;
                    }
                } else if (z3) {
                    SubjectWork subjectWork = (SubjectWork) t;
                    if (!subjectWork.open) {
                        list.remove(subjectWork.f141w);
                        subjectWork.f141w.onComplete();
                        if (list.isEmpty() && this.cancelled) {
                            this.terminated = true;
                        }
                    } else if (!this.cancelled) {
                        long requested = requested();
                        if (requested != 0) {
                            UnicastProcessor<T> create = UnicastProcessor.create(this.bufferSize);
                            list.add(create);
                            subscriber.onNext(create);
                            if (requested != Long.MAX_VALUE) {
                                produced(1);
                            }
                            this.worker.schedule(new Completion(create), this.timespan, this.unit);
                        } else {
                            subscriber.onError(new MissingBackpressureException("Can't emit window due to lack of requests"));
                        }
                    }
                } else {
                    for (UnicastProcessor<T> unicastProcessor3 : list) {
                        unicastProcessor3.onNext(t);
                    }
                }
            }
            this.upstream.cancel();
            dispose();
            simplePlainQueue.clear();
            list.clear();
        }

        @Override // java.lang.Runnable
        public void run() {
            SubjectWork subjectWork = new SubjectWork(UnicastProcessor.create(this.bufferSize), true);
            if (!this.cancelled) {
                this.queue.offer(subjectWork);
            }
            if (enter()) {
                drainLoop();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed$WindowSkipSubscriber$SubjectWork */
        /* loaded from: classes.dex */
        public static final class SubjectWork<T> {
            final boolean open;

            /* renamed from: w */
            final UnicastProcessor<T> f141w;

            SubjectWork(UnicastProcessor<T> unicastProcessor, boolean z) {
                this.f141w = unicastProcessor;
                this.open = z;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowTimed$WindowSkipSubscriber$Completion */
        /* loaded from: classes.dex */
        public final class Completion implements Runnable {
            private final UnicastProcessor<T> processor;

            Completion(UnicastProcessor<T> unicastProcessor) {
                this.processor = unicastProcessor;
            }

            @Override // java.lang.Runnable
            public void run() {
                WindowSkipSubscriber.this.complete(this.processor);
            }
        }
    }
}
