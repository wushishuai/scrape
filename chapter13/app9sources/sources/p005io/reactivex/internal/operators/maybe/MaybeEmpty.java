package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.fuseable.ScalarCallable;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeEmpty */
/* loaded from: classes.dex */
public final class MaybeEmpty extends Maybe<Object> implements ScalarCallable<Object> {
    public static final MaybeEmpty INSTANCE = new MaybeEmpty();

    @Override // p005io.reactivex.internal.fuseable.ScalarCallable, java.util.concurrent.Callable
    public Object call() {
        return null;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super Object> maybeObserver) {
        EmptyDisposable.complete(maybeObserver);
    }
}
