package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableOperator;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableLift */
/* loaded from: classes.dex */
public final class CompletableLift extends Completable {
    final CompletableOperator onLift;
    final CompletableSource source;

    public CompletableLift(CompletableSource completableSource, CompletableOperator completableOperator) {
        this.source = completableSource;
        this.onLift = completableOperator;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        try {
            this.source.subscribe(this.onLift.apply(completableObserver));
        } catch (NullPointerException e) {
            throw e;
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            RxJavaPlugins.onError(th);
        }
    }
}
