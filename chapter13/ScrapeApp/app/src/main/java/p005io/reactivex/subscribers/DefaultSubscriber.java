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
    public final void onSubscribe(Subscription s) {
        if (EndConsumerHelper.validate(this.upstream, s, getClass())) {
            this.upstream = s;
            onStart();
        }
    }

    protected final void request(long n) {
        Subscription s = this.upstream;
        if (s != null) {
            s.request(n);
        }
    }

    protected final void cancel() {
        Subscription s = this.upstream;
        this.upstream = SubscriptionHelper.CANCELLED;
        s.cancel();
    }

    protected void onStart() {
        request(Long.MAX_VALUE);
    }
}
