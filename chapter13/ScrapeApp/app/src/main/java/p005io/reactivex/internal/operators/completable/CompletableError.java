package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableError */
/* loaded from: classes.dex */
public final class CompletableError extends Completable {
    final Throwable error;

    public CompletableError(Throwable error) {
        this.error = error;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        EmptyDisposable.error(this.error, observer);
    }
}
