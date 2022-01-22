package p005io.reactivex.internal.operators.parallel;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.parallel.ParallelFlowable;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelFromArray */
/* loaded from: classes.dex */
public final class ParallelFromArray<T> extends ParallelFlowable<T> {
    final Publisher<T>[] sources;

    public ParallelFromArray(Publisher<T>[] publisherArr) {
        this.sources = publisherArr;
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public int parallelism() {
        return this.sources.length;
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public void subscribe(Subscriber<? super T>[] subscriberArr) {
        if (validate(subscriberArr)) {
            int length = subscriberArr.length;
            for (int i = 0; i < length; i++) {
                this.sources[i].subscribe(subscriberArr[i]);
            }
        }
    }
}
