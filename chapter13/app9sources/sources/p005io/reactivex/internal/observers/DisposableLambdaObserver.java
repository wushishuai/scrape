package p005io.reactivex.internal.observers;

import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.observers.DisposableLambdaObserver */
/* loaded from: classes.dex */
public final class DisposableLambdaObserver<T> implements Observer<T>, Disposable {
    final Observer<? super T> downstream;
    final Action onDispose;
    final Consumer<? super Disposable> onSubscribe;
    Disposable upstream;

    public DisposableLambdaObserver(Observer<? super T> observer, Consumer<? super Disposable> consumer, Action action) {
        this.downstream = observer;
        this.onSubscribe = consumer;
        this.onDispose = action;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        try {
            this.onSubscribe.accept(disposable);
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            disposable.dispose();
            this.upstream = DisposableHelper.DISPOSED;
            EmptyDisposable.error(th, this.downstream);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        this.downstream.onNext(t);
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        if (this.upstream != DisposableHelper.DISPOSED) {
            this.downstream.onError(th);
        } else {
            RxJavaPlugins.onError(th);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (this.upstream != DisposableHelper.DISPOSED) {
            this.downstream.onComplete();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        try {
            this.onDispose.run();
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            RxJavaPlugins.onError(th);
        }
        this.upstream.dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.upstream.isDisposed();
    }
}
