package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.schedulers.TrampolineScheduler;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableIntervalRange */
/* loaded from: classes.dex */
public final class FlowableIntervalRange extends Flowable<Long> {
    final long end;
    final long initialDelay;
    final long period;
    final Scheduler scheduler;
    final long start;
    final TimeUnit unit;

    public FlowableIntervalRange(long j, long j2, long j3, long j4, TimeUnit timeUnit, Scheduler scheduler) {
        this.initialDelay = j3;
        this.period = j4;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.start = j;
        this.end = j2;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super Long> subscriber) {
        IntervalRangeSubscriber intervalRangeSubscriber = new IntervalRangeSubscriber(subscriber, this.start, this.end);
        subscriber.onSubscribe(intervalRangeSubscriber);
        Scheduler scheduler = this.scheduler;
        if (scheduler instanceof TrampolineScheduler) {
            Scheduler.Worker createWorker = scheduler.createWorker();
            intervalRangeSubscriber.setResource(createWorker);
            createWorker.schedulePeriodically(intervalRangeSubscriber, this.initialDelay, this.period, this.unit);
            return;
        }
        intervalRangeSubscriber.setResource(scheduler.schedulePeriodicallyDirect(intervalRangeSubscriber, this.initialDelay, this.period, this.unit));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableIntervalRange$IntervalRangeSubscriber */
    /* loaded from: classes.dex */
    static final class IntervalRangeSubscriber extends AtomicLong implements Subscription, Runnable {
        private static final long serialVersionUID = -2809475196591179431L;
        long count;
        final Subscriber<? super Long> downstream;
        final long end;
        final AtomicReference<Disposable> resource = new AtomicReference<>();

        IntervalRangeSubscriber(Subscriber<? super Long> subscriber, long j, long j2) {
            this.downstream = subscriber;
            this.count = j;
            this.end = j2;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this, j);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            DisposableHelper.dispose(this.resource);
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.resource.get() != DisposableHelper.DISPOSED) {
                long j = get();
                if (j != 0) {
                    long j2 = this.count;
                    this.downstream.onNext(Long.valueOf(j2));
                    if (j2 == this.end) {
                        if (this.resource.get() != DisposableHelper.DISPOSED) {
                            this.downstream.onComplete();
                        }
                        DisposableHelper.dispose(this.resource);
                        return;
                    }
                    this.count = j2 + 1;
                    if (j != Long.MAX_VALUE) {
                        decrementAndGet();
                        return;
                    }
                    return;
                }
                Subscriber<? super Long> subscriber = this.downstream;
                subscriber.onError(new MissingBackpressureException("Can't deliver value " + this.count + " due to lack of requests"));
                DisposableHelper.dispose(this.resource);
            }
        }

        public void setResource(Disposable disposable) {
            DisposableHelper.setOnce(this.resource, disposable);
        }
    }
}
