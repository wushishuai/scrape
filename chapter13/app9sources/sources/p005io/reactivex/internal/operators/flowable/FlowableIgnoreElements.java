package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableIgnoreElements */
/* loaded from: classes.dex */
public final class FlowableIgnoreElements<T> extends AbstractFlowableWithUpstream<T, T> {
    public FlowableIgnoreElements(Flowable<T> flowable) {
        super(flowable);
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((FlowableSubscriber) new IgnoreElementsSubscriber(subscriber));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableIgnoreElements$IgnoreElementsSubscriber */
    /* loaded from: classes.dex */
    static final class IgnoreElementsSubscriber<T> implements FlowableSubscriber<T>, QueueSubscription<T> {
        final Subscriber<? super T> downstream;
        Subscription upstream;

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return true;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() {
            return null;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            return i & 2;
        }

        IgnoreElementsSubscriber(Subscriber<? super T> subscriber) {
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean offer(T t) {
            throw new UnsupportedOperationException("Should not be called!");
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean offer(T t, T t2) {
            throw new UnsupportedOperationException("Should not be called!");
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }
    }
}
