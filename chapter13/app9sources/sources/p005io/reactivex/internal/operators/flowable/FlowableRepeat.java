package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableRepeat */
/* loaded from: classes.dex */
public final class FlowableRepeat<T> extends AbstractFlowableWithUpstream<T, T> {
    final long count;

    public FlowableRepeat(Flowable<T> flowable, long j) {
        super(flowable);
        this.count = j;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        SubscriptionArbiter subscriptionArbiter = new SubscriptionArbiter();
        subscriber.onSubscribe(subscriptionArbiter);
        long j = this.count;
        long j2 = Long.MAX_VALUE;
        if (j != Long.MAX_VALUE) {
            j2 = j - 1;
        }
        new RepeatSubscriber(subscriber, j2, subscriptionArbiter, this.source).subscribeNext();
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRepeat$RepeatSubscriber */
    /* loaded from: classes.dex */
    static final class RepeatSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -7098360935104053232L;
        final Subscriber<? super T> downstream;
        long produced;
        long remaining;

        /* renamed from: sa */
        final SubscriptionArbiter f127sa;
        final Publisher<? extends T> source;

        RepeatSubscriber(Subscriber<? super T> subscriber, long j, SubscriptionArbiter subscriptionArbiter, Publisher<? extends T> publisher) {
            this.downstream = subscriber;
            this.f127sa = subscriptionArbiter;
            this.source = publisher;
            this.remaining = j;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            this.f127sa.setSubscription(subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.produced++;
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            long j = this.remaining;
            if (j != Long.MAX_VALUE) {
                this.remaining = j - 1;
            }
            if (j != 0) {
                subscribeNext();
            } else {
                this.downstream.onComplete();
            }
        }

        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int i = 1;
                while (!this.f127sa.isCancelled()) {
                    long j = this.produced;
                    if (j != 0) {
                        this.produced = 0;
                        this.f127sa.produced(j);
                    }
                    this.source.subscribe(this);
                    i = addAndGet(-i);
                    if (i == 0) {
                        return;
                    }
                }
            }
        }
    }
}
