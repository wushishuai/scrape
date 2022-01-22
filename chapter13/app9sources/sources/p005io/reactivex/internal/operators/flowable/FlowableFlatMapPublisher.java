package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.functions.Function;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFlatMapPublisher */
/* loaded from: classes.dex */
public final class FlowableFlatMapPublisher<T, U> extends Flowable<U> {
    final int bufferSize;
    final boolean delayErrors;
    final Function<? super T, ? extends Publisher<? extends U>> mapper;
    final int maxConcurrency;
    final Publisher<T> source;

    public FlowableFlatMapPublisher(Publisher<T> publisher, Function<? super T, ? extends Publisher<? extends U>> function, boolean z, int i, int i2) {
        this.source = publisher;
        this.mapper = function;
        this.delayErrors = z;
        this.maxConcurrency = i;
        this.bufferSize = i2;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super U> subscriber) {
        if (!FlowableScalarXMap.tryScalarXMapSubscribe(this.source, subscriber, this.mapper)) {
            this.source.subscribe(FlowableFlatMap.subscribe(subscriber, this.mapper, this.delayErrors, this.maxConcurrency, this.bufferSize));
        }
    }
}
