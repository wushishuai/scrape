package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.Callable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableScanSeed */
/* loaded from: classes.dex */
public final class ObservableScanSeed<T, R> extends AbstractObservableWithUpstream<T, R> {
    final BiFunction<R, ? super T, R> accumulator;
    final Callable<R> seedSupplier;

    public ObservableScanSeed(ObservableSource<T> observableSource, Callable<R> callable, BiFunction<R, ? super T, R> biFunction) {
        super(observableSource);
        this.accumulator = biFunction;
        this.seedSupplier = callable;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super R> observer) {
        try {
            this.source.subscribe(new ScanSeedObserver(observer, this.accumulator, ObjectHelper.requireNonNull(this.seedSupplier.call(), "The seed supplied is null")));
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, observer);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableScanSeed$ScanSeedObserver */
    /* loaded from: classes.dex */
    static final class ScanSeedObserver<T, R> implements Observer<T>, Disposable {
        final BiFunction<R, ? super T, R> accumulator;
        boolean done;
        final Observer<? super R> downstream;
        Disposable upstream;
        R value;

        ScanSeedObserver(Observer<? super R> observer, BiFunction<R, ? super T, R> biFunction, R r) {
            this.downstream = observer;
            this.accumulator = biFunction;
            this.value = r;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
                this.downstream.onNext((R) this.value);
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
        public void onNext(T t) {
            if (!this.done) {
                try {
                    R r = (R) ObjectHelper.requireNonNull(this.accumulator.apply(this.value, t), "The accumulator returned a null value");
                    this.value = r;
                    this.downstream.onNext(r);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.upstream.dispose();
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
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
            }
        }
    }
}
