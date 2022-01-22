package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.internal.fuseable.ScalarCallable;
import p005io.reactivex.internal.subscriptions.ScalarSubscription;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableJust */
/* loaded from: classes.dex */
public final class FlowableJust<T> extends Flowable<T> implements ScalarCallable<T> {
    private final T value;

    public FlowableJust(T t) {
        this.value = t;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new ScalarSubscription(subscriber, this.value));
    }

    @Override // p005io.reactivex.internal.fuseable.ScalarCallable, java.util.concurrent.Callable
    public T call() {
        return this.value;
    }
}
