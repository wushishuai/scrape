package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableDelaySubscriptionOther */
/* loaded from: classes.dex */
public final class FlowableDelaySubscriptionOther<T, U> extends Flowable<T> {
    final Publisher<? extends T> main;
    final Publisher<U> other;

    public FlowableDelaySubscriptionOther(Publisher<? extends T> publisher, Publisher<U> publisher2) {
        this.main = publisher;
        this.other = publisher2;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        SubscriptionArbiter subscriptionArbiter = new SubscriptionArbiter();
        subscriber.onSubscribe(subscriptionArbiter);
        this.other.subscribe(new DelaySubscriber(subscriptionArbiter, subscriber));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelaySubscriptionOther$DelaySubscriber */
    /* loaded from: classes.dex */
    final class DelaySubscriber implements FlowableSubscriber<U> {
        final Subscriber<? super T> child;
        boolean done;
        final SubscriptionArbiter serial;

        DelaySubscriber(SubscriptionArbiter subscriptionArbiter, Subscriber<? super T> subscriber) {
            this.serial = subscriptionArbiter;
            this.child = subscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            this.serial.setSubscription(new DelaySubscription(subscription));
            subscription.request(Long.MAX_VALUE);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(U u) {
            onComplete();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.child.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                FlowableDelaySubscriptionOther.this.main.subscribe(new OnCompleteSubscriber());
            }
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelaySubscriptionOther$DelaySubscriber$DelaySubscription */
        /* loaded from: classes.dex */
        final class DelaySubscription implements Subscription {
            final Subscription upstream;

            @Override // org.reactivestreams.Subscription
            public void request(long j) {
            }

            DelaySubscription(Subscription subscription) {
                this.upstream = subscription;
            }

            @Override // org.reactivestreams.Subscription
            public void cancel() {
                this.upstream.cancel();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.flowable.FlowableDelaySubscriptionOther$DelaySubscriber$OnCompleteSubscriber */
        /* loaded from: classes.dex */
        public final class OnCompleteSubscriber implements FlowableSubscriber<T> {
            OnCompleteSubscriber() {
            }

            @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
            public void onSubscribe(Subscription subscription) {
                DelaySubscriber.this.serial.setSubscription(subscription);
            }

            @Override // org.reactivestreams.Subscriber
            public void onNext(T t) {
                DelaySubscriber.this.child.onNext(t);
            }

            @Override // org.reactivestreams.Subscriber
            public void onError(Throwable th) {
                DelaySubscriber.this.child.onError(th);
            }

            @Override // org.reactivestreams.Subscriber
            public void onComplete() {
                DelaySubscriber.this.child.onComplete();
            }
        }
    }
}
