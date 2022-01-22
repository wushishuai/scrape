package p005io.reactivex.subscribers;

import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.EndConsumerHelper;

/* renamed from: io.reactivex.subscribers.DisposableSubscriber */
/* loaded from: classes.dex */
public abstract class DisposableSubscriber<T> implements FlowableSubscriber<T>, Disposable {
    final AtomicReference<Subscription> upstream = new AtomicReference<>();

    @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
    public final void onSubscribe(Subscription subscription) {
        if (EndConsumerHelper.setOnce(this.upstream, subscription, getClass())) {
            onStart();
        }
    }

    protected void onStart() {
        this.upstream.get().request(Long.MAX_VALUE);
    }

    protected final void request(long j) {
        this.upstream.get().request(j);
    }

    protected final void cancel() {
        dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return this.upstream.get() == SubscriptionHelper.CANCELLED;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        SubscriptionHelper.cancel(this.upstream);
    }
}
