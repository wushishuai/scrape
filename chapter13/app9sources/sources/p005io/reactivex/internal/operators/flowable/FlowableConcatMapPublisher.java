package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.util.ErrorMode;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableConcatMapPublisher */
/* loaded from: classes.dex */
public final class FlowableConcatMapPublisher<T, R> extends Flowable<R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int prefetch;
    final Publisher<T> source;

    public FlowableConcatMapPublisher(Publisher<T> publisher, Function<? super T, ? extends Publisher<? extends R>> function, int i, ErrorMode errorMode) {
        this.source = publisher;
        this.mapper = function;
        this.prefetch = i;
        this.errorMode = errorMode;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> subscriber) {
        if (!FlowableScalarXMap.tryScalarXMapSubscribe(this.source, subscriber, this.mapper)) {
            this.source.subscribe(FlowableConcatMap.subscribe(subscriber, this.mapper, this.prefetch, this.errorMode));
        }
    }
}
