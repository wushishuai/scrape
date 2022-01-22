package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.disposables.Disposables;
import p005io.reactivex.internal.fuseable.ScalarCallable;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeJust */
/* loaded from: classes.dex */
public final class MaybeJust<T> extends Maybe<T> implements ScalarCallable<T> {
    final T value;

    public MaybeJust(T t) {
        this.value = t;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        maybeObserver.onSubscribe(Disposables.disposed());
        maybeObserver.onSuccess((T) this.value);
    }

    @Override // p005io.reactivex.internal.fuseable.ScalarCallable, java.util.concurrent.Callable
    public T call() {
        return this.value;
    }
}
