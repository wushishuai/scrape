package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.DeferredScalarDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableFromFuture */
/* loaded from: classes.dex */
public final class ObservableFromFuture<T> extends Observable<T> {
    final Future<? extends T> future;
    final long timeout;
    final TimeUnit unit;

    public ObservableFromFuture(Future<? extends T> future, long j, TimeUnit timeUnit) {
        this.future = future;
        this.timeout = j;
        this.unit = timeUnit;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        DeferredScalarDisposable deferredScalarDisposable = new DeferredScalarDisposable(observer);
        observer.onSubscribe(deferredScalarDisposable);
        if (!deferredScalarDisposable.isDisposed()) {
            try {
                deferredScalarDisposable.complete(ObjectHelper.requireNonNull(this.unit != null ? this.future.get(this.timeout, this.unit) : this.future.get(), "Future returned null"));
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                if (!deferredScalarDisposable.isDisposed()) {
                    observer.onError(th);
                }
            }
        }
    }
}
