package p005io.reactivex.internal.subscriptions;

import org.reactivestreams.Subscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.QueueSubscription;

/* renamed from: io.reactivex.internal.subscriptions.EmptySubscription */
/* loaded from: classes.dex */
public enum EmptySubscription implements QueueSubscription<Object> {
    INSTANCE;

    @Override // org.reactivestreams.Subscription
    public void cancel() {
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return true;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public Object poll() {
        return null;
    }

    @Override // p005io.reactivex.internal.fuseable.QueueFuseable
    public int requestFusion(int i) {
        return i & 2;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "EmptySubscription";
    }

    @Override // org.reactivestreams.Subscription
    public void request(long j) {
        SubscriptionHelper.validate(j);
    }

    public static void error(Throwable th, Subscriber<?> subscriber) {
        subscriber.onSubscribe(INSTANCE);
        subscriber.onError(th);
    }

    public static void complete(Subscriber<?> subscriber) {
        subscriber.onSubscribe(INSTANCE);
        subscriber.onComplete();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object obj) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object obj, Object obj2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
