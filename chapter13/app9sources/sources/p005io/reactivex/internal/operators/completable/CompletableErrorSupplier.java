package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.Callable;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.completable.CompletableErrorSupplier */
/* loaded from: classes.dex */
public final class CompletableErrorSupplier extends Completable {
    final Callable<? extends Throwable> errorSupplier;

    public CompletableErrorSupplier(Callable<? extends Throwable> callable) {
        this.errorSupplier = callable;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        Throwable th;
        try {
            th = (Throwable) ObjectHelper.requireNonNull(this.errorSupplier.call(), "The error returned is null");
        } catch (Throwable th2) {
            th = th2;
            Exceptions.throwIfFatal(th);
        }
        EmptyDisposable.error(th, completableObserver);
    }
}
