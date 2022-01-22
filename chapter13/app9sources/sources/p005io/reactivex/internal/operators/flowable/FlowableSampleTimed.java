package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableSampleTimed */
/* loaded from: classes.dex */
public final class FlowableSampleTimed<T> extends AbstractFlowableWithUpstream<T, T> {
    final boolean emitLast;
    final long period;
    final Scheduler scheduler;
    final TimeUnit unit;

    public FlowableSampleTimed(Flowable<T> flowable, long j, TimeUnit timeUnit, Scheduler scheduler, boolean z) {
        super(flowable);
        this.period = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.emitLast = z;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        SerializedSubscriber serializedSubscriber = new SerializedSubscriber(subscriber);
        if (this.emitLast) {
            this.source.subscribe((FlowableSubscriber) new SampleTimedEmitLast(serializedSubscriber, this.period, this.unit, this.scheduler));
        } else {
            this.source.subscribe((FlowableSubscriber) new SampleTimedNoLast(serializedSubscriber, this.period, this.unit, this.scheduler));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableSampleTimed$SampleTimedSubscriber */
    /* loaded from: classes.dex */
    static abstract class SampleTimedSubscriber<T> extends AtomicReference<T> implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = -3517602651313910099L;
        final Subscriber<? super T> downstream;
        final long period;
        final Scheduler scheduler;
        final TimeUnit unit;
        Subscription upstream;
        final AtomicLong requested = new AtomicLong();
        final SequentialDisposable timer = new SequentialDisposable();

        abstract void complete();

        SampleTimedSubscriber(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler scheduler) {
            this.downstream = subscriber;
            this.period = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                SequentialDisposable sequentialDisposable = this.timer;
                Scheduler scheduler = this.scheduler;
                long j = this.period;
                sequentialDisposable.replace(scheduler.schedulePeriodicallyDirect(this, j, j, this.unit));
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            lazySet(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            cancelTimer();
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            cancelTimer();
            complete();
        }

        void cancelTimer() {
            DisposableHelper.dispose(this.timer);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            cancelTimer();
            this.upstream.cancel();
        }

        void emit() {
            T andSet = getAndSet(null);
            if (andSet == null) {
                return;
            }
            if (this.requested.get() != 0) {
                this.downstream.onNext(andSet);
                BackpressureHelper.produced(this.requested, 1);
                return;
            }
            cancel();
            this.downstream.onError(new MissingBackpressureException("Couldn't emit value due to lack of requests!"));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableSampleTimed$SampleTimedNoLast */
    /* loaded from: classes.dex */
    static final class SampleTimedNoLast<T> extends SampleTimedSubscriber<T> {
        private static final long serialVersionUID = -7139995637533111443L;

        SampleTimedNoLast(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler scheduler) {
            super(subscriber, j, timeUnit, scheduler);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableSampleTimed.SampleTimedSubscriber
        void complete() {
            this.downstream.onComplete();
        }

        @Override // java.lang.Runnable
        public void run() {
            emit();
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableSampleTimed$SampleTimedEmitLast */
    /* loaded from: classes.dex */
    static final class SampleTimedEmitLast<T> extends SampleTimedSubscriber<T> {
        private static final long serialVersionUID = -7139995637533111443L;
        final AtomicInteger wip = new AtomicInteger(1);

        SampleTimedEmitLast(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler scheduler) {
            super(subscriber, j, timeUnit, scheduler);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableSampleTimed.SampleTimedSubscriber
        void complete() {
            emit();
            if (this.wip.decrementAndGet() == 0) {
                this.downstream.onComplete();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.wip.incrementAndGet() == 2) {
                emit();
                if (this.wip.decrementAndGet() == 0) {
                    this.downstream.onComplete();
                }
            }
        }
    }
}
