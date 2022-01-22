package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFromPublisher */
/* loaded from: classes.dex */
public final class FlowableFromPublisher<T> extends Flowable<T> {
    final Publisher<? extends T> publisher;

    public FlowableFromPublisher(Publisher<? extends T> publisher) {
        this.publisher = publisher;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.publisher.subscribe(subscriber);
    }
}
