package p005io.reactivex.internal.subscriptions;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Subscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.QueueSubscription;

/* renamed from: io.reactivex.internal.subscriptions.ScalarSubscription */
/* loaded from: classes.dex */
public final class ScalarSubscription<T> extends AtomicInteger implements QueueSubscription<T> {
    static final int CANCELLED = 2;
    static final int NO_REQUEST = 0;
    static final int REQUESTED = 1;
    private static final long serialVersionUID = -3830916580126663321L;
    final Subscriber<? super T> subscriber;
    final T value;

    @Override // p005io.reactivex.internal.fuseable.QueueFuseable
    public int requestFusion(int i) {
        return i & 1;
    }

    public ScalarSubscription(Subscriber<? super T> subscriber, T t) {
        this.subscriber = subscriber;
        this.value = t;
    }

    @Override // org.reactivestreams.Subscription
    public void request(long j) {
        if (SubscriptionHelper.validate(j) && compareAndSet(0, 1)) {
            Subscriber<? super T> subscriber = this.subscriber;
            subscriber.onNext((T) this.value);
            if (get() != 2) {
                subscriber.onComplete();
            }
        }
    }

    @Override // org.reactivestreams.Subscription
    public void cancel() {
        lazySet(2);
    }

    public boolean isCancelled() {
        return get() == 2;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(T t) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(T t, T t2) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public T poll() {
        if (get() != 0) {
            return null;
        }
        lazySet(1);
        return this.value;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return get() != 0;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
        lazySet(1);
    }
}
