package p005io.reactivex.internal.subscribers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.subscribers.BasicFuseableSubscriber */
/* loaded from: classes.dex */
public abstract class BasicFuseableSubscriber<T, R> implements FlowableSubscriber<T>, QueueSubscription<R> {
    protected boolean done;
    protected final Subscriber<? super R> downstream;

    /* renamed from: qs */
    protected QueueSubscription<T> f180qs;
    protected int sourceMode;
    protected Subscription upstream;

    public BasicFuseableSubscriber(Subscriber<? super R> downstream) {
        this.downstream = downstream;
    }

    @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
    public final void onSubscribe(Subscription s) {
        if (SubscriptionHelper.validate(this.upstream, s)) {
            this.upstream = s;
            if (s instanceof QueueSubscription) {
                this.f180qs = (QueueSubscription) s;
            }
            if (beforeDownstream()) {
                this.downstream.onSubscribe(this);
                afterDownstream();
            }
        }
    }

    protected boolean beforeDownstream() {
        return true;
    }

    protected void afterDownstream() {
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable t) {
        if (this.done) {
            RxJavaPlugins.onError(t);
            return;
        }
        this.done = true;
        this.downstream.onError(t);
    }

    protected final void fail(Throwable t) {
        Exceptions.throwIfFatal(t);
        this.upstream.cancel();
        onError(t);
    }

    @Override // org.reactivestreams.Subscriber
    public void onComplete() {
        if (!this.done) {
            this.done = true;
            this.downstream.onComplete();
        }
    }

    protected final int transitiveBoundaryFusion(int mode) {
        QueueSubscription<T> qs = this.f180qs;
        if (qs == null || (mode & 4) != 0) {
            return 0;
        }
        int m = qs.requestFusion(mode);
        if (m != 0) {
            this.sourceMode = m;
        }
        return m;
    }

    @Override // org.reactivestreams.Subscription
    public void request(long n) {
        this.upstream.request(n);
    }

    @Override // org.reactivestreams.Subscription
    public void cancel() {
        this.upstream.cancel();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return this.f180qs.isEmpty();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
        this.f180qs.clear();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(R e) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(R v1, R v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
