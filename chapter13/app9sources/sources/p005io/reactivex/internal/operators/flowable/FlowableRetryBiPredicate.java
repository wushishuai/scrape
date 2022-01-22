package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiPredicate;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableRetryBiPredicate */
/* loaded from: classes.dex */
public final class FlowableRetryBiPredicate<T> extends AbstractFlowableWithUpstream<T, T> {
    final BiPredicate<? super Integer, ? super Throwable> predicate;

    public FlowableRetryBiPredicate(Flowable<T> flowable, BiPredicate<? super Integer, ? super Throwable> biPredicate) {
        super(flowable);
        this.predicate = biPredicate;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        SubscriptionArbiter subscriptionArbiter = new SubscriptionArbiter();
        subscriber.onSubscribe(subscriptionArbiter);
        new RetryBiSubscriber(subscriber, this.predicate, subscriptionArbiter, this.source).subscribeNext();
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRetryBiPredicate$RetryBiSubscriber */
    /* loaded from: classes.dex */
    static final class RetryBiSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -7098360935104053232L;
        final Subscriber<? super T> downstream;
        final BiPredicate<? super Integer, ? super Throwable> predicate;
        long produced;
        int retries;

        /* renamed from: sa */
        final SubscriptionArbiter f130sa;
        final Publisher<? extends T> source;

        RetryBiSubscriber(Subscriber<? super T> subscriber, BiPredicate<? super Integer, ? super Throwable> biPredicate, SubscriptionArbiter subscriptionArbiter, Publisher<? extends T> publisher) {
            this.downstream = subscriber;
            this.f130sa = subscriptionArbiter;
            this.source = publisher;
            this.predicate = biPredicate;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            this.f130sa.setSubscription(subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.produced++;
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            try {
                BiPredicate<? super Integer, ? super Throwable> biPredicate = this.predicate;
                int i = this.retries + 1;
                this.retries = i;
                if (!biPredicate.test(Integer.valueOf(i), th)) {
                    this.downstream.onError(th);
                } else {
                    subscribeNext();
                }
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                this.downstream.onError(new CompositeException(th, th2));
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
        }

        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int i = 1;
                while (!this.f130sa.isCancelled()) {
                    long j = this.produced;
                    if (j != 0) {
                        this.produced = 0;
                        this.f130sa.produced(j);
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
