package p005io.reactivex.internal.subscriptions;

import org.reactivestreams.Subscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.QueueSubscription;

/* renamed from: io.reactivex.internal.subscriptions.EmptySubscription */
/* loaded from: classes.dex */
public enum EmptySubscription implements QueueSubscription<Object> {
    INSTANCE;

    @Override // org.reactivestreams.Subscription
    public void request(long n) {
        SubscriptionHelper.validate(n);
    }

    @Override // org.reactivestreams.Subscription
    public void cancel() {
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "EmptySubscription";
    }

    public static void error(Throwable e, Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
        s.onError(e);
    }

    public static void complete(Subscriber<?> s) {
        s.onSubscribe(INSTANCE);
        s.onComplete();
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public Object poll() {
        return null;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return true;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
    }

    @Override // p005io.reactivex.internal.fuseable.QueueFuseable
    public int requestFusion(int mode) {
        return mode & 2;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object v1, Object v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
