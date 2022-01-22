package p005io.reactivex.subscribers;

import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.EndConsumerHelper;

/* renamed from: io.reactivex.subscribers.DefaultSubscriber */
/* loaded from: classes.dex */
public abstract class DefaultSubscriber<T> implements FlowableSubscriber<T> {
    Subscription upstream;

    @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
    public final void onSubscribe(Subscription subscription) {
        if (EndConsumerHelper.validate(this.upstream, subscription, getClass())) {
            this.upstream = subscription;
            onStart();
        }
    }

    protected final void request(long j) {
        Subscription subscription = this.upstream;
        if (subscription != null) {
            subscription.request(j);
        }
    }

    protected final void cancel() {
        Subscription subscription = this.upstream;
        this.upstream = SubscriptionHelper.CANCELLED;
        subscription.cancel();
    }

    protected void onStart() {
        request(Long.MAX_VALUE);
    }
}
