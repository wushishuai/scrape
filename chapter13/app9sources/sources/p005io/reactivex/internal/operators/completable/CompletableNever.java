package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableNever */
/* loaded from: classes.dex */
public final class CompletableNever extends Completable {
    public static final Completable INSTANCE = new CompletableNever();

    private CompletableNever() {
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        completableObserver.onSubscribe(EmptyDisposable.NEVER);
    }
}
