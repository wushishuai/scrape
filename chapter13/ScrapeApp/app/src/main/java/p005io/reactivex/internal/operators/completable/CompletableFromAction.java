package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.disposables.Disposables;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;

/* renamed from: io.reactivex.internal.operators.completable.CompletableFromAction */
/* loaded from: classes.dex */
public final class CompletableFromAction extends Completable {
    final Action run;

    public CompletableFromAction(Action run) {
        this.run = run;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        Disposable d = Disposables.empty();
        observer.onSubscribe(d);
        try {
            this.run.run();
            if (!d.isDisposed()) {
                observer.onComplete();
            }
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            }
        }
    }
}
