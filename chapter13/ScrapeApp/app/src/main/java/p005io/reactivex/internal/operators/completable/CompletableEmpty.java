package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableEmpty */
/* loaded from: classes.dex */
public final class CompletableEmpty extends Completable {
    public static final Completable INSTANCE = new CompletableEmpty();

    private CompletableEmpty() {
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver observer) {
        EmptyDisposable.complete(observer);
    }
}
