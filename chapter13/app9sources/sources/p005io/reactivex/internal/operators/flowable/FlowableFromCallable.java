package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Callable;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.DeferredScalarSubscription;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFromCallable */
/* loaded from: classes.dex */
public final class FlowableFromCallable<T> extends Flowable<T> implements Callable<T> {
    final Callable<? extends T> callable;

    public FlowableFromCallable(Callable<? extends T> callable) {
        this.callable = callable;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        DeferredScalarSubscription deferredScalarSubscription = new DeferredScalarSubscription(subscriber);
        subscriber.onSubscribe(deferredScalarSubscription);
        try {
            deferredScalarSubscription.complete(ObjectHelper.requireNonNull(this.callable.call(), "The callable returned a null value"));
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            if (deferredScalarSubscription.isCancelled()) {
                RxJavaPlugins.onError(th);
            } else {
                subscriber.onError(th);
            }
        }
    }

    @Override // java.util.concurrent.Callable
    public T call() throws Exception {
        return (T) ObjectHelper.requireNonNull(this.callable.call(), "The callable returned a null value");
    }
}
