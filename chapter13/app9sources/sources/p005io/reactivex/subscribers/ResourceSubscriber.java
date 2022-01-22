package p005io.reactivex.subscribers;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.ListCompositeDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.EndConsumerHelper;

/* renamed from: io.reactivex.subscribers.ResourceSubscriber */
/* loaded from: classes.dex */
public abstract class ResourceSubscriber<T> implements FlowableSubscriber<T>, Disposable {
    private final AtomicReference<Subscription> upstream = new AtomicReference<>();
    private final ListCompositeDisposable resources = new ListCompositeDisposable();
    private final AtomicLong missedRequested = new AtomicLong();

    public final void add(Disposable disposable) {
        ObjectHelper.requireNonNull(disposable, "resource is null");
        this.resources.add(disposable);
    }

    @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
    public final void onSubscribe(Subscription subscription) {
        if (EndConsumerHelper.setOnce(this.upstream, subscription, getClass())) {
            long andSet = this.missedRequested.getAndSet(0);
            if (andSet != 0) {
                subscription.request(andSet);
            }
            onStart();
        }
    }

    protected void onStart() {
        request(Long.MAX_VALUE);
    }

    protected final void request(long j) {
        SubscriptionHelper.deferredRequest(this.upstream, this.missedRequested, j);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        if (SubscriptionHelper.cancel(this.upstream)) {
            this.resources.dispose();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return SubscriptionHelper.isCancelled(this.upstream.get());
    }
}
