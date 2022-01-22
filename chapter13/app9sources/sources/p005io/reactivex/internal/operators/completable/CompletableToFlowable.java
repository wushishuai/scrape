package p005io.reactivex.internal.operators.completable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Flowable;
import p005io.reactivex.internal.observers.SubscriberCompletableObserver;

/* renamed from: io.reactivex.internal.operators.completable.CompletableToFlowable */
/* loaded from: classes.dex */
public final class CompletableToFlowable<T> extends Flowable<T> {
    final CompletableSource source;

    public CompletableToFlowable(CompletableSource completableSource) {
        this.source = completableSource;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe(new SubscriberCompletableObserver(subscriber));
    }
}
