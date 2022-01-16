package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.subscriptions.DeferredScalarSubscription;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFromFuture */
/* loaded from: classes.dex */
public final class FlowableFromFuture<T> extends Flowable<T> {
    final Future<? extends T> future;
    final long timeout;
    final TimeUnit unit;

    public FlowableFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {
        this.future = future;
        this.timeout = timeout;
        this.unit = unit;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> s) {
        DeferredScalarSubscription deferredScalarSubscription = new DeferredScalarSubscription(s);
        s.onSubscribe(deferredScalarSubscription);
        try {
            Object obj = this.unit != null ? this.future.get(this.timeout, this.unit) : this.future.get();
            if (obj == null) {
                s.onError(new NullPointerException("The future returned null"));
            } else {
                deferredScalarSubscription.complete(obj);
            }
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            if (!deferredScalarSubscription.isCancelled()) {
                s.onError(ex);
            }
        }
    }
}
