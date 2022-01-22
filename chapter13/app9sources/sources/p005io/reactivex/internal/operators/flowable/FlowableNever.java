package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.internal.subscriptions.EmptySubscription;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableNever */
/* loaded from: classes.dex */
public final class FlowableNever extends Flowable<Object> {
    public static final Flowable<Object> INSTANCE = new FlowableNever();

    private FlowableNever() {
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super Object> subscriber) {
        subscriber.onSubscribe(EmptySubscription.INSTANCE);
    }
}
