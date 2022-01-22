package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Callable;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.EmptySubscription;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableError */
/* loaded from: classes.dex */
public final class FlowableError<T> extends Flowable<T> {
    final Callable<? extends Throwable> errorSupplier;

    public FlowableError(Callable<? extends Throwable> callable) {
        this.errorSupplier = callable;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        Throwable th;
        try {
            th = (Throwable) ObjectHelper.requireNonNull(this.errorSupplier.call(), "Callable returned null throwable. Null values are generally not allowed in 2.x operators and sources.");
        } catch (Throwable th2) {
            th = th2;
            Exceptions.throwIfFatal(th);
        }
        EmptySubscription.error(th, subscriber);
    }
}
