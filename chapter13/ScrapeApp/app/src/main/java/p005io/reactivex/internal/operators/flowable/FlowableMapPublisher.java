package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.operators.flowable.FlowableMap;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableMapPublisher */
/* loaded from: classes.dex */
public final class FlowableMapPublisher<T, U> extends Flowable<U> {
    final Function<? super T, ? extends U> mapper;
    final Publisher<T> source;

    public FlowableMapPublisher(Publisher<T> source, Function<? super T, ? extends U> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super U> s) {
        this.source.subscribe(new FlowableMap.MapSubscriber(s, this.mapper));
    }
}
