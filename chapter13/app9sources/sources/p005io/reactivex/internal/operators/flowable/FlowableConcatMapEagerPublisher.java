package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.operators.flowable.FlowableConcatMapEager;
import p005io.reactivex.internal.util.ErrorMode;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableConcatMapEagerPublisher */
/* loaded from: classes.dex */
public final class FlowableConcatMapEagerPublisher<T, R> extends Flowable<R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int maxConcurrency;
    final int prefetch;
    final Publisher<T> source;

    public FlowableConcatMapEagerPublisher(Publisher<T> publisher, Function<? super T, ? extends Publisher<? extends R>> function, int i, int i2, ErrorMode errorMode) {
        this.source = publisher;
        this.mapper = function;
        this.maxConcurrency = i;
        this.prefetch = i2;
        this.errorMode = errorMode;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> subscriber) {
        this.source.subscribe(new FlowableConcatMapEager.ConcatMapEagerDelayErrorSubscriber(subscriber, this.mapper, this.maxConcurrency, this.prefetch, this.errorMode));
    }
}
