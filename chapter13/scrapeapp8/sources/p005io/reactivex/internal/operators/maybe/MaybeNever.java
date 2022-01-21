package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeNever */
/* loaded from: classes.dex */
public final class MaybeNever extends Maybe<Object> {
    public static final MaybeNever INSTANCE = new MaybeNever();

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super Object> observer) {
        observer.onSubscribe(EmptyDisposable.NEVER);
    }
}
