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

/* renamed from: io.reactivex.internal.operators.flowable.FlowableInterval */
/* loaded from: classes.dex */
public final class FlowableInterval extends Flowable<Long> {
    final long initialDelay;
    final long period;
    final Scheduler scheduler;
    final TimeUnit unit;

    public FlowableInterval(long j, long j2, TimeUnit timeUnit, Scheduler scheduler) {
        this.initialDelay = j;
        this.period = j2;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super Long> subscriber) {
        IntervalSubscriber intervalSubscriber = new IntervalSubscriber(subscriber);
        subscriber.onSubscribe(intervalSubscriber);
        Scheduler scheduler = this.scheduler;
        if (scheduler instanceof TrampolineScheduler) {
            Scheduler.Worker createWorker = scheduler.createWorker();
            intervalSubscriber.setResource(createWorker);
            createWorker.schedulePeriodically(intervalSubscriber, this.initialDelay, this.period, this.unit);
            return;
        }
        intervalSubscriber.setResource(scheduler.schedulePeriodicallyDirect(intervalSubscriber, this.initialDelay, this.period, this.unit));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableInterval$IntervalSubscriber */
    /* loaded from: classes.dex */
    static final class IntervalSubscriber extends AtomicLong implements Subscription, Runnable {
        private static final long serialVersionUID = -2809475196591179431L;
        long count;
        final Subscriber<? super Long> downstream;
        final AtomicReference<Disposable> resource = new AtomicReference<>();

        IntervalSubscriber(Subscriber<? super Long> subscriber) {
            this.downstream = subscriber;
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
            if (this.resource.get() == DisposableHelper.DISPOSED) {
                return;
            }
            if (get() != 0) {
                Subscriber<? super Long> subscriber = this.downstream;
                long j = this.count;
                this.count = j + 1;
                subscriber.onNext(Long.valueOf(j));
                BackpressureHelper.produced(this, 1);
                return;
            }
            Subscriber<? super Long> subscriber2 = this.downstream;
            subscriber2.onError(new MissingBackpressureException("Can't deliver value " + this.count + " due to lack of requests"));
            DisposableHelper.dispose(this.resource);
        }

        public void setResource(Disposable disposable) {
            DisposableHelper.setOnce(this.resource, disposable);
        }
    }
}
