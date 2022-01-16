package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.internal.subscriptions.DeferredScalarSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableCount */
/* loaded from: classes.dex */
public final class FlowableCount<T> extends AbstractFlowableWithUpstream<T, Long> {
    public FlowableCount(Flowable<T> source) {
        super(source);
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super Long> s) {
        this.source.subscribe((FlowableSubscriber) new CountSubscriber(s));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCount$CountSubscriber */
    /* loaded from: classes.dex */
    static final class CountSubscriber extends DeferredScalarSubscription<Long> implements FlowableSubscriber<Object> {
        private static final long serialVersionUID = 4973004223787171406L;
        long count;
        Subscription upstream;

        CountSubscriber(Subscriber<? super Long> downstream) {
            super(downstream);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
                s.request(Long.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object t) {
            this.count++;
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            complete(Long.valueOf(this.count));
        }

        @Override // p005io.reactivex.internal.subscriptions.DeferredScalarSubscription, org.reactivestreams.Subscription
        public void cancel() {
            super.cancel();
            this.upstream.cancel();
        }
    }
}
