package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableDelay */
/* loaded from: classes.dex */
public final class FlowableDelay<T> extends AbstractFlowableWithUpstream<T, T> {
    final long delay;
    final boolean delayError;
    final Scheduler scheduler;
    final TimeUnit unit;

    public FlowableDelay(Flowable<T> flowable, long j, TimeUnit timeUnit, Scheduler scheduler, boolean z) {
        super(flowable);
        this.delay = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.delayError = z;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        Subscriber<? super T> subscriber2;
        if (this.delayError) {
            subscriber2 = subscriber;
        } else {
            subscriber2 = new SerializedSubscriber<>(subscriber);
        }
        this.source.subscribe((FlowableSubscriber) new DelaySubscriber(subscriber2, this.delay, this.unit, this.scheduler.createWorker(), this.delayError));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelay$DelaySubscriber */
    /* loaded from: classes.dex */
    static final class DelaySubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final long delay;
        final boolean delayError;
        final Subscriber<? super T> downstream;
        final TimeUnit unit;
        Subscription upstream;

        /* renamed from: w */
        final Scheduler.Worker f117w;

        DelaySubscriber(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler.Worker worker, boolean z) {
            this.downstream = subscriber;
            this.delay = j;
            this.unit = timeUnit;
            this.f117w = worker;
            this.delayError = z;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.f117w.schedule(new OnNext(t), this.delay, this.unit);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.f117w.schedule(new OnError(th), this.delayError ? this.delay : 0, this.unit);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.f117w.schedule(new OnComplete(), this.delay, this.unit);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
            this.f117w.dispose();
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelay$DelaySubscriber$OnNext */
        /* loaded from: classes.dex */
        final class OnNext implements Runnable {

            /* renamed from: t */
            private final T f119t;

            OnNext(T t) {
                this.f119t = t;
            }

            @Override // java.lang.Runnable
            public void run() {
                DelaySubscriber.this.downstream.onNext((T) this.f119t);
            }
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelay$DelaySubscriber$OnError */
        /* loaded from: classes.dex */
        final class OnError implements Runnable {

            /* renamed from: t */
            private final Throwable f118t;

            OnError(Throwable th) {
                this.f118t = th;
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    DelaySubscriber.this.downstream.onError(this.f118t);
                } finally {
                    DelaySubscriber.this.f117w.dispose();
                }
            }
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelay$DelaySubscriber$OnComplete */
        /* loaded from: classes.dex */
        final class OnComplete implements Runnable {
            OnComplete() {
            }

            @Override // java.lang.Runnable
            public void run() {
                try {
                    DelaySubscriber.this.downstream.onComplete();
                } finally {
                    DelaySubscriber.this.f117w.dispose();
                }
            }
        }
    }
}
