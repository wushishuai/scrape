package p005io.reactivex.internal.operators.single;

import java.util.concurrent.Callable;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleError */
/* loaded from: classes.dex */
public final class SingleError<T> extends Single<T> {
    final Callable<? extends Throwable> errorSupplier;

    public SingleError(Callable<? extends Throwable> callable) {
        this.errorSupplier = callable;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        Throwable th;
        try {
            th = (Throwable) ObjectHelper.requireNonNull(this.errorSupplier.call(), "Callable returned null throwable. Null values are generally not allowed in 2.x operators and sources.");
        } catch (Throwable th2) {
            th = th2;
            Exceptions.throwIfFatal(th);
        }
        EmptyDisposable.error(th, singleObserver);
    }
}
