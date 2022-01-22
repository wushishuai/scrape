package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.processors.FlowableProcessor;
import p005io.reactivex.processors.UnicastProcessor;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableRepeatWhen */
/* loaded from: classes.dex */
public final class FlowableRepeatWhen<T> extends AbstractFlowableWithUpstream<T, T> {
    final Function<? super Flowable<Object>, ? extends Publisher<?>> handler;

    public FlowableRepeatWhen(Flowable<T> flowable, Function<? super Flowable<Object>, ? extends Publisher<?>> function) {
        super(flowable);
        this.handler = function;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        SerializedSubscriber serializedSubscriber = new SerializedSubscriber(subscriber);
        FlowableProcessor<T> serialized = UnicastProcessor.create(8).toSerialized();
        try {
            Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.handler.apply(serialized), "handler returned a null Publisher");
            WhenReceiver whenReceiver = new WhenReceiver(this.source);
            RepeatWhenSubscriber repeatWhenSubscriber = new RepeatWhenSubscriber(serializedSubscriber, serialized, whenReceiver);
            whenReceiver.subscriber = repeatWhenSubscriber;
            subscriber.onSubscribe(repeatWhenSubscriber);
            publisher.subscribe(whenReceiver);
            whenReceiver.onNext(0);
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptySubscription.error(th, subscriber);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRepeatWhen$WhenReceiver */
    /* loaded from: classes.dex */
    static final class WhenReceiver<T, U> extends AtomicInteger implements FlowableSubscriber<Object>, Subscription {
        private static final long serialVersionUID = 2827772011130406689L;
        final Publisher<T> source;
        WhenSourceSubscriber<T, U> subscriber;
        final AtomicReference<Subscription> upstream = new AtomicReference<>();
        final AtomicLong requested = new AtomicLong();

        public WhenReceiver(Publisher<T> publisher) {
            this.source = publisher;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.deferredSetOnce(this.upstream, this.requested, subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object obj) {
            if (getAndIncrement() == 0) {
                while (!SubscriptionHelper.isCancelled(this.upstream.get())) {
                    this.source.subscribe(this.subscriber);
                    if (decrementAndGet() == 0) {
                        return;
                    }
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.subscriber.cancel();
            this.subscriber.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.subscriber.cancel();
            this.subscriber.downstream.onComplete();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            SubscriptionHelper.deferredRequest(this.upstream, this.requested, j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            SubscriptionHelper.cancel(this.upstream);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRepeatWhen$WhenSourceSubscriber */
    /* loaded from: classes.dex */
    public static abstract class WhenSourceSubscriber<T, U> extends SubscriptionArbiter implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -5604623027276966720L;
        protected final Subscriber<? super T> downstream;
        protected final FlowableProcessor<U> processor;
        private long produced;
        protected final Subscription receiver;

        public WhenSourceSubscriber(Subscriber<? super T> subscriber, FlowableProcessor<U> flowableProcessor, Subscription subscription) {
            this.downstream = subscriber;
            this.processor = flowableProcessor;
            this.receiver = subscription;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public final void onSubscribe(Subscription subscription) {
            setSubscription(subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public final void onNext(T t) {
            this.produced++;
            this.downstream.onNext(t);
        }

        protected final void again(U u) {
            long j = this.produced;
            if (j != 0) {
                this.produced = 0;
                produced(j);
            }
            this.receiver.request(1);
            this.processor.onNext(u);
        }

        @Override // p005io.reactivex.internal.subscriptions.SubscriptionArbiter, org.reactivestreams.Subscription
        public final void cancel() {
            super.cancel();
            this.receiver.cancel();
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRepeatWhen$RepeatWhenSubscriber */
    /* loaded from: classes.dex */
    static final class RepeatWhenSubscriber<T> extends WhenSourceSubscriber<T, Object> {
        private static final long serialVersionUID = -2680129890138081029L;

        RepeatWhenSubscriber(Subscriber<? super T> subscriber, FlowableProcessor<Object> flowableProcessor, Subscription subscription) {
            super(subscriber, flowableProcessor, subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.receiver.cancel();
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            again(0);
        }
    }
}
