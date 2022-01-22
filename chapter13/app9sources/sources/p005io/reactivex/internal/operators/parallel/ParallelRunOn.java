package p005io.reactivex.internal.operators.parallel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.parallel.ParallelFlowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelRunOn */
/* loaded from: classes.dex */
public final class ParallelRunOn<T> extends ParallelFlowable<T> {
    final int prefetch;
    final Scheduler scheduler;
    final ParallelFlowable<? extends T> source;

    public ParallelRunOn(ParallelFlowable<? extends T> parallelFlowable, Scheduler scheduler, int i) {
        this.source = parallelFlowable;
        this.scheduler = scheduler;
        this.prefetch = i;
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public void subscribe(Subscriber<? super T>[] subscriberArr) {
        if (validate(subscriberArr)) {
            int length = subscriberArr.length;
            Subscriber<T>[] subscriberArr2 = new Subscriber[length];
            Scheduler scheduler = this.scheduler;
            if (scheduler instanceof SchedulerMultiWorkerSupport) {
                ((SchedulerMultiWorkerSupport) scheduler).createWorkers(length, new MultiWorkerCallback(subscriberArr, subscriberArr2));
            } else {
                for (int i = 0; i < length; i++) {
                    createSubscriber(i, subscriberArr, subscriberArr2, this.scheduler.createWorker());
                }
            }
            this.source.subscribe(subscriberArr2);
        }
    }

    void createSubscriber(int i, Subscriber<? super T>[] subscriberArr, Subscriber<T>[] subscriberArr2, Scheduler.Worker worker) {
        Subscriber<? super T> subscriber = subscriberArr[i];
        SpscArrayQueue spscArrayQueue = new SpscArrayQueue(this.prefetch);
        if (subscriber instanceof ConditionalSubscriber) {
            subscriberArr2[i] = new RunOnConditionalSubscriber((ConditionalSubscriber) subscriber, this.prefetch, spscArrayQueue, worker);
        } else {
            subscriberArr2[i] = new RunOnSubscriber(subscriber, this.prefetch, spscArrayQueue, worker);
        }
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelRunOn$MultiWorkerCallback */
    /* loaded from: classes.dex */
    final class MultiWorkerCallback implements SchedulerMultiWorkerSupport.WorkerCallback {
        final Subscriber<T>[] parents;
        final Subscriber<? super T>[] subscribers;

        MultiWorkerCallback(Subscriber<? super T>[] subscriberArr, Subscriber<T>[] subscriberArr2) {
            this.subscribers = subscriberArr;
            this.parents = subscriberArr2;
        }

        @Override // p005io.reactivex.internal.schedulers.SchedulerMultiWorkerSupport.WorkerCallback
        public void onWorker(int i, Scheduler.Worker worker) {
            ParallelRunOn.this.createSubscriber(i, this.subscribers, this.parents, worker);
        }
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public int parallelism() {
        return this.source.parallelism();
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelRunOn$BaseRunOnSubscriber */
    /* loaded from: classes.dex */
    static abstract class BaseRunOnSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = 9222303586456402150L;
        volatile boolean cancelled;
        int consumed;
        volatile boolean done;
        Throwable error;
        final int limit;
        final int prefetch;
        final SpscArrayQueue<T> queue;
        final AtomicLong requested = new AtomicLong();
        Subscription upstream;
        final Scheduler.Worker worker;

        BaseRunOnSubscriber(int i, SpscArrayQueue<T> spscArrayQueue, Scheduler.Worker worker) {
            this.prefetch = i;
            this.queue = spscArrayQueue;
            this.limit = i - (i >> 2);
            this.worker = worker;
        }

        @Override // org.reactivestreams.Subscriber
        public final void onNext(T t) {
            if (!this.done) {
                if (!this.queue.offer(t)) {
                    this.upstream.cancel();
                    onError(new MissingBackpressureException("Queue is full?!"));
                    return;
                }
                schedule();
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
            schedule();
        }

        @Override // org.reactivestreams.Subscriber
        public final void onComplete() {
            if (!this.done) {
                this.done = true;
                schedule();
            }
        }

        @Override // org.reactivestreams.Subscription
        public final void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                schedule();
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

        final void schedule() {
            if (getAndIncrement() == 0) {
                this.worker.schedule(this);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelRunOn$RunOnSubscriber */
    /* loaded from: classes.dex */
    public static final class RunOnSubscriber<T> extends BaseRunOnSubscriber<T> {
        private static final long serialVersionUID = 1075119423897941642L;
        final Subscriber<? super T> downstream;

        RunOnSubscriber(Subscriber<? super T> subscriber, int i, SpscArrayQueue<T> spscArrayQueue, Scheduler.Worker worker) {
            super(i, spscArrayQueue, worker);
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                subscription.request((long) this.prefetch);
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:33:0x006f, code lost:
            if (r11 != r7) goto L_0x009c;
         */
        /* JADX WARN: Code restructure failed: missing block: B:35:0x0073, code lost:
            if (r17.cancelled == false) goto L_0x0079;
         */
        /* JADX WARN: Code restructure failed: missing block: B:36:0x0075, code lost:
            r2.clear();
         */
        /* JADX WARN: Code restructure failed: missing block: B:37:0x0078, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:39:0x007b, code lost:
            if (r17.done == false) goto L_0x009c;
         */
        /* JADX WARN: Code restructure failed: missing block: B:40:0x007d, code lost:
            r5 = r17.error;
         */
        /* JADX WARN: Code restructure failed: missing block: B:41:0x007f, code lost:
            if (r5 == null) goto L_0x008d;
         */
        /* JADX WARN: Code restructure failed: missing block: B:42:0x0081, code lost:
            r2.clear();
            r3.onError(r5);
            r17.worker.dispose();
         */
        /* JADX WARN: Code restructure failed: missing block: B:43:0x008c, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:45:0x0091, code lost:
            if (r2.isEmpty() == false) goto L_0x009c;
         */
        /* JADX WARN: Code restructure failed: missing block: B:46:0x0093, code lost:
            r3.onComplete();
            r17.worker.dispose();
         */
        /* JADX WARN: Code restructure failed: missing block: B:47:0x009b, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:49:0x009e, code lost:
            if (r11 == 0) goto L_0x00af;
         */
        /* JADX WARN: Code restructure failed: missing block: B:51:0x00a7, code lost:
            if (r7 == Long.MAX_VALUE) goto L_0x00af;
         */
        /* JADX WARN: Code restructure failed: missing block: B:52:0x00a9, code lost:
            r17.requested.addAndGet(-r11);
         */
        /* JADX WARN: Code restructure failed: missing block: B:53:0x00af, code lost:
            r5 = get();
         */
        /* JADX WARN: Code restructure failed: missing block: B:54:0x00b5, code lost:
            if (r5 != r16) goto L_0x00c4;
         */
        /* JADX WARN: Code restructure failed: missing block: B:55:0x00b7, code lost:
            r17.consumed = r1;
            r5 = addAndGet(-r16);
         */
        /* JADX WARN: Code restructure failed: missing block: B:56:0x00be, code lost:
            if (r5 != 0) goto L_0x00c1;
         */
        /* JADX WARN: Code restructure failed: missing block: B:57:0x00c0, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:58:0x00c1, code lost:
            r6 = r5;
         */
        /* JADX WARN: Code restructure failed: missing block: B:59:0x00c4, code lost:
            r6 = r5;
         */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void run() {
            /*
                r17 = this;
                r0 = r17
                int r1 = r0.consumed
                io.reactivex.internal.queue.SpscArrayQueue r2 = r0.queue
                org.reactivestreams.Subscriber<? super T> r3 = r0.downstream
                int r4 = r0.limit
                r6 = 1
            L_0x000b:
                java.util.concurrent.atomic.AtomicLong r7 = r0.requested
                long r7 = r7.get()
                r9 = 0
                r11 = r9
            L_0x0014:
                int r13 = (r11 > r7 ? 1 : (r11 == r7 ? 0 : -1))
                if (r13 == 0) goto L_0x006b
                boolean r13 = r0.cancelled
                if (r13 == 0) goto L_0x0020
                r2.clear()
                return
            L_0x0020:
                boolean r13 = r0.done
                if (r13 == 0) goto L_0x0034
                java.lang.Throwable r14 = r0.error
                if (r14 == 0) goto L_0x0034
                r2.clear()
                r3.onError(r14)
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x0034:
                java.lang.Object r14 = r2.poll()
                r15 = 0
                if (r14 != 0) goto L_0x003e
                r16 = 1
                goto L_0x0040
            L_0x003e:
                r16 = 0
            L_0x0040:
                if (r13 == 0) goto L_0x004d
                if (r16 == 0) goto L_0x004d
                r3.onComplete()
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x004d:
                if (r16 == 0) goto L_0x0052
                r16 = r6
                goto L_0x006d
            L_0x0052:
                r3.onNext(r14)
                r13 = 1
                long r11 = r11 + r13
                int r1 = r1 + 1
                if (r1 != r4) goto L_0x0066
                org.reactivestreams.Subscription r13 = r0.upstream
                r16 = r6
                long r5 = (long) r1
                r13.request(r5)
                r1 = 0
                goto L_0x0068
            L_0x0066:
                r16 = r6
            L_0x0068:
                r6 = r16
                goto L_0x0014
            L_0x006b:
                r16 = r6
            L_0x006d:
                int r5 = (r11 > r7 ? 1 : (r11 == r7 ? 0 : -1))
                if (r5 != 0) goto L_0x009c
                boolean r5 = r0.cancelled
                if (r5 == 0) goto L_0x0079
                r2.clear()
                return
            L_0x0079:
                boolean r5 = r0.done
                if (r5 == 0) goto L_0x009c
                java.lang.Throwable r5 = r0.error
                if (r5 == 0) goto L_0x008d
                r2.clear()
                r3.onError(r5)
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x008d:
                boolean r5 = r2.isEmpty()
                if (r5 == 0) goto L_0x009c
                r3.onComplete()
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x009c:
                int r5 = (r11 > r9 ? 1 : (r11 == r9 ? 0 : -1))
                if (r5 == 0) goto L_0x00af
                r5 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
                int r9 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
                if (r9 == 0) goto L_0x00af
                java.util.concurrent.atomic.AtomicLong r5 = r0.requested
                long r6 = -r11
                r5.addAndGet(r6)
            L_0x00af:
                int r5 = r17.get()
                r6 = r16
                if (r5 != r6) goto L_0x00c4
                r0.consumed = r1
                int r5 = -r6
                int r5 = r0.addAndGet(r5)
                if (r5 != 0) goto L_0x00c1
                return
            L_0x00c1:
                r6 = r5
                goto L_0x000b
            L_0x00c4:
                r6 = r5
                goto L_0x000b
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.parallel.ParallelRunOn.RunOnSubscriber.run():void");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelRunOn$RunOnConditionalSubscriber */
    /* loaded from: classes.dex */
    public static final class RunOnConditionalSubscriber<T> extends BaseRunOnSubscriber<T> {
        private static final long serialVersionUID = 1075119423897941642L;
        final ConditionalSubscriber<? super T> downstream;

        RunOnConditionalSubscriber(ConditionalSubscriber<? super T> conditionalSubscriber, int i, SpscArrayQueue<T> spscArrayQueue, Scheduler.Worker worker) {
            super(i, spscArrayQueue, worker);
            this.downstream = conditionalSubscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                subscription.request((long) this.prefetch);
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:36:0x0072, code lost:
            if (r11 != r7) goto L_0x009f;
         */
        /* JADX WARN: Code restructure failed: missing block: B:38:0x0076, code lost:
            if (r17.cancelled == false) goto L_0x007c;
         */
        /* JADX WARN: Code restructure failed: missing block: B:39:0x0078, code lost:
            r2.clear();
         */
        /* JADX WARN: Code restructure failed: missing block: B:40:0x007b, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:42:0x007e, code lost:
            if (r17.done == false) goto L_0x009f;
         */
        /* JADX WARN: Code restructure failed: missing block: B:43:0x0080, code lost:
            r5 = r17.error;
         */
        /* JADX WARN: Code restructure failed: missing block: B:44:0x0082, code lost:
            if (r5 == null) goto L_0x0090;
         */
        /* JADX WARN: Code restructure failed: missing block: B:45:0x0084, code lost:
            r2.clear();
            r3.onError(r5);
            r17.worker.dispose();
         */
        /* JADX WARN: Code restructure failed: missing block: B:46:0x008f, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:48:0x0094, code lost:
            if (r2.isEmpty() == false) goto L_0x009f;
         */
        /* JADX WARN: Code restructure failed: missing block: B:49:0x0096, code lost:
            r3.onComplete();
            r17.worker.dispose();
         */
        /* JADX WARN: Code restructure failed: missing block: B:50:0x009e, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:52:0x00a1, code lost:
            if (r11 == 0) goto L_0x00b2;
         */
        /* JADX WARN: Code restructure failed: missing block: B:54:0x00aa, code lost:
            if (r7 == Long.MAX_VALUE) goto L_0x00b2;
         */
        /* JADX WARN: Code restructure failed: missing block: B:55:0x00ac, code lost:
            r17.requested.addAndGet(-r11);
         */
        /* JADX WARN: Code restructure failed: missing block: B:56:0x00b2, code lost:
            r5 = get();
         */
        /* JADX WARN: Code restructure failed: missing block: B:57:0x00b8, code lost:
            if (r5 != r16) goto L_0x00c7;
         */
        /* JADX WARN: Code restructure failed: missing block: B:58:0x00ba, code lost:
            r17.consumed = r1;
            r5 = addAndGet(-r16);
         */
        /* JADX WARN: Code restructure failed: missing block: B:59:0x00c1, code lost:
            if (r5 != 0) goto L_0x00c4;
         */
        /* JADX WARN: Code restructure failed: missing block: B:60:0x00c3, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:61:0x00c4, code lost:
            r6 = r5;
         */
        /* JADX WARN: Code restructure failed: missing block: B:62:0x00c7, code lost:
            r6 = r5;
         */
        @Override // java.lang.Runnable
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void run() {
            /*
                r17 = this;
                r0 = r17
                int r1 = r0.consumed
                io.reactivex.internal.queue.SpscArrayQueue r2 = r0.queue
                io.reactivex.internal.fuseable.ConditionalSubscriber<? super T> r3 = r0.downstream
                int r4 = r0.limit
                r6 = 1
            L_0x000b:
                java.util.concurrent.atomic.AtomicLong r7 = r0.requested
                long r7 = r7.get()
                r9 = 0
                r11 = r9
            L_0x0014:
                int r13 = (r11 > r7 ? 1 : (r11 == r7 ? 0 : -1))
                if (r13 == 0) goto L_0x006e
                boolean r13 = r0.cancelled
                if (r13 == 0) goto L_0x0020
                r2.clear()
                return
            L_0x0020:
                boolean r13 = r0.done
                if (r13 == 0) goto L_0x0034
                java.lang.Throwable r14 = r0.error
                if (r14 == 0) goto L_0x0034
                r2.clear()
                r3.onError(r14)
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x0034:
                java.lang.Object r14 = r2.poll()
                r15 = 0
                if (r14 != 0) goto L_0x003e
                r16 = 1
                goto L_0x0040
            L_0x003e:
                r16 = 0
            L_0x0040:
                if (r13 == 0) goto L_0x004d
                if (r16 == 0) goto L_0x004d
                r3.onComplete()
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x004d:
                if (r16 == 0) goto L_0x0052
                r16 = r6
                goto L_0x0070
            L_0x0052:
                boolean r13 = r3.tryOnNext(r14)
                if (r13 == 0) goto L_0x005b
                r13 = 1
                long r11 = r11 + r13
            L_0x005b:
                int r1 = r1 + 1
                if (r1 != r4) goto L_0x0069
                org.reactivestreams.Subscription r13 = r0.upstream
                r16 = r6
                long r5 = (long) r1
                r13.request(r5)
                r1 = 0
                goto L_0x006b
            L_0x0069:
                r16 = r6
            L_0x006b:
                r6 = r16
                goto L_0x0014
            L_0x006e:
                r16 = r6
            L_0x0070:
                int r5 = (r11 > r7 ? 1 : (r11 == r7 ? 0 : -1))
                if (r5 != 0) goto L_0x009f
                boolean r5 = r0.cancelled
                if (r5 == 0) goto L_0x007c
                r2.clear()
                return
            L_0x007c:
                boolean r5 = r0.done
                if (r5 == 0) goto L_0x009f
                java.lang.Throwable r5 = r0.error
                if (r5 == 0) goto L_0x0090
                r2.clear()
                r3.onError(r5)
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x0090:
                boolean r5 = r2.isEmpty()
                if (r5 == 0) goto L_0x009f
                r3.onComplete()
                io.reactivex.Scheduler$Worker r1 = r0.worker
                r1.dispose()
                return
            L_0x009f:
                int r5 = (r11 > r9 ? 1 : (r11 == r9 ? 0 : -1))
                if (r5 == 0) goto L_0x00b2
                r5 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
                int r9 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
                if (r9 == 0) goto L_0x00b2
                java.util.concurrent.atomic.AtomicLong r5 = r0.requested
                long r6 = -r11
                r5.addAndGet(r6)
            L_0x00b2:
                int r5 = r17.get()
                r6 = r16
                if (r5 != r6) goto L_0x00c7
                r0.consumed = r1
                int r5 = -r6
                int r5 = r0.addAndGet(r5)
                if (r5 != 0) goto L_0x00c4
                return
            L_0x00c4:
                r6 = r5
                goto L_0x000b
            L_0x00c7:
                r6 = r5
                goto L_0x000b
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.parallel.ParallelRunOn.RunOnConditionalSubscriber.run():void");
        }
    }
}
