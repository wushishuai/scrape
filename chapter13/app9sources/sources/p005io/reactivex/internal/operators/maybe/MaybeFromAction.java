package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.Callable;
import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.disposables.Disposables;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeFromAction */
/* loaded from: classes.dex */
public final class MaybeFromAction<T> extends Maybe<T> implements Callable<T> {
    final Action action;

    public MaybeFromAction(Action action) {
        this.action = action;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        Disposable empty = Disposables.empty();
        maybeObserver.onSubscribe(empty);
        if (!empty.isDisposed()) {
            try {
                this.action.run();
                if (!empty.isDisposed()) {
                    maybeObserver.onComplete();
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                if (!empty.isDisposed()) {
                    maybeObserver.onError(th);
                } else {
                    RxJavaPlugins.onError(th);
                }
            }
        }
    }

    @Override // java.util.concurrent.Callable
    public T call() throws Exception {
        this.action.run();
        return null;
    }
}
