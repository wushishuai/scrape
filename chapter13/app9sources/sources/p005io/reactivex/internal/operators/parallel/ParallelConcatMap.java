package p005io.reactivex.internal.operators.parallel;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.flowable.FlowableConcatMap;
import p005io.reactivex.internal.util.ErrorMode;
import p005io.reactivex.parallel.ParallelFlowable;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelConcatMap */
/* loaded from: classes.dex */
public final class ParallelConcatMap<T, R> extends ParallelFlowable<R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int prefetch;
    final ParallelFlowable<T> source;

    public ParallelConcatMap(ParallelFlowable<T> parallelFlowable, Function<? super T, ? extends Publisher<? extends R>> function, int i, ErrorMode errorMode) {
        this.source = parallelFlowable;
        this.mapper = (Function) ObjectHelper.requireNonNull(function, "mapper");
        this.prefetch = i;
        this.errorMode = (ErrorMode) ObjectHelper.requireNonNull(errorMode, "errorMode");
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public int parallelism() {
        return this.source.parallelism();
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public void subscribe(Subscriber<? super R>[] subscriberArr) {
        if (validate(subscriberArr)) {
            int length = subscriberArr.length;
            Subscriber<? super T>[] subscriberArr2 = new Subscriber[length];
            for (int i = 0; i < length; i++) {
                subscriberArr2[i] = FlowableConcatMap.subscribe(subscriberArr[i], this.mapper, this.prefetch, this.errorMode);
            }
            this.source.subscribe(subscriberArr2);
        }
    }
}
