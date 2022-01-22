package p005io.reactivex.internal.observers;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.observers.ForEachWhileObserver */
/* loaded from: classes.dex */
public final class ForEachWhileObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {
    private static final long serialVersionUID = -4403180040475402120L;
    boolean done;
    final Action onComplete;
    final Consumer<? super Throwable> onError;
    final Predicate<? super T> onNext;

    public ForEachWhileObserver(Predicate<? super T> predicate, Consumer<? super Throwable> consumer, Action action) {
        this.onNext = predicate;
        this.onError = consumer;
        this.onComplete = action;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        DisposableHelper.setOnce(this, disposable);
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        if (!this.done) {
            try {
                if (!this.onNext.test(t)) {
                    dispose();
                    onComplete();
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                dispose();
                onError(th);
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        if (this.done) {
            RxJavaPlugins.onError(th);
            return;
        }
        this.done = true;
        try {
            this.onError.accept(th);
        } catch (Throwable th2) {
            Exceptions.throwIfFatal(th2);
            RxJavaPlugins.onError(new CompositeException(th, th2));
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.done) {
            this.done = true;
            try {
                this.onComplete.run();
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(th);
            }
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(get());
    }
}
