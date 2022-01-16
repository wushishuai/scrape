package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableOnErrorNext */
/* loaded from: classes.dex */
public final class ObservableOnErrorNext<T> extends AbstractObservableWithUpstream<T, T> {
    final boolean allowFatal;
    final Function<? super Throwable, ? extends ObservableSource<? extends T>> nextSupplier;

    public ObservableOnErrorNext(ObservableSource<T> source, Function<? super Throwable, ? extends ObservableSource<? extends T>> nextSupplier, boolean allowFatal) {
        super(source);
        this.nextSupplier = nextSupplier;
        this.allowFatal = allowFatal;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> t) {
        OnErrorNextObserver<T> parent = new OnErrorNextObserver<>(t, this.nextSupplier, this.allowFatal);
        t.onSubscribe(parent.arbiter);
        this.source.subscribe(parent);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableOnErrorNext$OnErrorNextObserver */
    /* loaded from: classes.dex */
    static final class OnErrorNextObserver<T> implements Observer<T> {
        final boolean allowFatal;
        final SequentialDisposable arbiter = new SequentialDisposable();
        boolean done;
        final Observer<? super T> downstream;
        final Function<? super Throwable, ? extends ObservableSource<? extends T>> nextSupplier;
        boolean once;

        OnErrorNextObserver(Observer<? super T> actual, Function<? super Throwable, ? extends ObservableSource<? extends T>> nextSupplier, boolean allowFatal) {
            this.downstream = actual;
            this.nextSupplier = nextSupplier;
            this.allowFatal = allowFatal;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.arbiter.replace(d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (!this.done) {
                this.downstream.onNext(t);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            if (!this.once) {
                this.once = true;
                if (!this.allowFatal || (t instanceof Exception)) {
                    try {
                        ObservableSource<? extends T> p = (ObservableSource) this.nextSupplier.apply(t);
                        if (p == null) {
                            NullPointerException npe = new NullPointerException("Observable is null");
                            npe.initCause(t);
                            this.downstream.onError(npe);
                            return;
                        }
                        p.subscribe(this);
                    } catch (Throwable e) {
                        Exceptions.throwIfFatal(e);
                        this.downstream.onError(new CompositeException(t, e));
                    }
                } else {
                    this.downstream.onError(t);
                }
            } else if (this.done) {
                RxJavaPlugins.onError(t);
            } else {
                this.downstream.onError(t);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.once = true;
                this.downstream.onComplete();
            }
        }
    }
}
