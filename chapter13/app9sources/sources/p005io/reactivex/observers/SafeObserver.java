package p005io.reactivex.observers;

import p005io.reactivex.Observer;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.observers.SafeObserver */
/* loaded from: classes.dex */
public final class SafeObserver<T> implements Observer<T>, Disposable {
    boolean done;
    final Observer<? super T> downstream;
    Disposable upstream;

    public SafeObserver(@NonNull Observer<? super T> observer) {
        this.downstream = observer;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(@NonNull Disposable disposable) {
        if (DisposableHelper.validate(this.upstream, disposable)) {
            this.upstream = disposable;
            try {
                this.downstream.onSubscribe(this);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.done = true;
                try {
                    disposable.dispose();
                    RxJavaPlugins.onError(th);
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    RxJavaPlugins.onError(new CompositeException(th, th2));
                }
            }
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        this.upstream.dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.upstream.isDisposed();
    }

    @Override // p005io.reactivex.Observer
    public void onNext(@NonNull T t) {
        if (!this.done) {
            if (this.upstream == null) {
                onNextNoSubscription();
            } else if (t == null) {
                NullPointerException nullPointerException = new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
                try {
                    this.upstream.dispose();
                    onError(nullPointerException);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    onError(new CompositeException(nullPointerException, th));
                }
            } else {
                try {
                    this.downstream.onNext(t);
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    try {
                        this.upstream.dispose();
                        onError(th2);
                    } catch (Throwable th3) {
                        Exceptions.throwIfFatal(th3);
                        onError(new CompositeException(th2, th3));
                    }
                }
            }
        }
    }

    void onNextNoSubscription() {
        this.done = true;
        NullPointerException nullPointerException = new NullPointerException("Subscription not set!");
        try {
            this.downstream.onSubscribe(EmptyDisposable.INSTANCE);
            try {
                this.downstream.onError(nullPointerException);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(new CompositeException(nullPointerException, th));
            }
        } catch (Throwable th2) {
            Exceptions.throwIfFatal(th2);
            RxJavaPlugins.onError(new CompositeException(nullPointerException, th2));
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(@NonNull Throwable th) {
        if (this.done) {
            RxJavaPlugins.onError(th);
            return;
        }
        this.done = true;
        if (this.upstream == null) {
            NullPointerException nullPointerException = new NullPointerException("Subscription not set!");
            try {
                this.downstream.onSubscribe(EmptyDisposable.INSTANCE);
                try {
                    this.downstream.onError(new CompositeException(th, nullPointerException));
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    RxJavaPlugins.onError(new CompositeException(th, nullPointerException, th2));
                }
            } catch (Throwable th3) {
                Exceptions.throwIfFatal(th3);
                RxJavaPlugins.onError(new CompositeException(th, nullPointerException, th3));
            }
        } else {
            if (th == null) {
                th = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            try {
                this.downstream.onError(th);
            } catch (Throwable th4) {
                Exceptions.throwIfFatal(th4);
                RxJavaPlugins.onError(new CompositeException(th, th4));
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.done) {
            this.done = true;
            if (this.upstream == null) {
                onCompleteNoSubscription();
                return;
            }
            try {
                this.downstream.onComplete();
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(th);
            }
        }
    }

    void onCompleteNoSubscription() {
        NullPointerException nullPointerException = new NullPointerException("Subscription not set!");
        try {
            this.downstream.onSubscribe(EmptyDisposable.INSTANCE);
            try {
                this.downstream.onError(nullPointerException);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                RxJavaPlugins.onError(new CompositeException(nullPointerException, th));
            }
        } catch (Throwable th2) {
            Exceptions.throwIfFatal(th2);
            RxJavaPlugins.onError(new CompositeException(nullPointerException, th2));
        }
    }
}
