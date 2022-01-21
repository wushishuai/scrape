package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.ArrayCompositeDisposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.observers.SerializedObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableSkipUntil */
/* loaded from: classes.dex */
public final class ObservableSkipUntil<T, U> extends AbstractObservableWithUpstream<T, T> {
    final ObservableSource<U> other;

    public ObservableSkipUntil(ObservableSource<T> source, ObservableSource<U> other) {
        super(source);
        this.other = other;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> child) {
        SerializedObserver<T> serial = new SerializedObserver<>(child);
        ArrayCompositeDisposable frc = new ArrayCompositeDisposable(2);
        serial.onSubscribe(frc);
        SkipUntilObserver<T> sus = new SkipUntilObserver<>(serial, frc);
        this.other.subscribe(new SkipUntil(frc, sus, serial));
        this.source.subscribe(sus);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSkipUntil$SkipUntilObserver */
    /* loaded from: classes.dex */
    static final class SkipUntilObserver<T> implements Observer<T> {
        final Observer<? super T> downstream;
        final ArrayCompositeDisposable frc;
        volatile boolean notSkipping;
        boolean notSkippingLocal;
        Disposable upstream;

        SkipUntilObserver(Observer<? super T> actual, ArrayCompositeDisposable frc) {
            this.downstream = actual;
            this.frc = frc;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.frc.setResource(0, d);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (this.notSkippingLocal) {
                this.downstream.onNext(t);
            } else if (this.notSkipping) {
                this.notSkippingLocal = true;
                this.downstream.onNext(t);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.frc.dispose();
            this.downstream.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.frc.dispose();
            this.downstream.onComplete();
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSkipUntil$SkipUntil */
    /* loaded from: classes.dex */
    final class SkipUntil implements Observer<U> {
        final ArrayCompositeDisposable frc;
        final SerializedObserver<T> serial;
        final SkipUntilObserver<T> sus;
        Disposable upstream;

        SkipUntil(ArrayCompositeDisposable frc, SkipUntilObserver<T> sus, SerializedObserver<T> serial) {
            this.frc = frc;
            this.sus = sus;
            this.serial = serial;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.frc.setResource(1, d);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(U t) {
            this.upstream.dispose();
            this.sus.notSkipping = true;
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.frc.dispose();
            this.serial.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.sus.notSkipping = true;
        }
    }
}
