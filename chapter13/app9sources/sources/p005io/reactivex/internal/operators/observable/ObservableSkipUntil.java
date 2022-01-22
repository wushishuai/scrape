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

    public ObservableSkipUntil(ObservableSource<T> observableSource, ObservableSource<U> observableSource2) {
        super(observableSource);
        this.other = observableSource2;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        SerializedObserver serializedObserver = new SerializedObserver(observer);
        ArrayCompositeDisposable arrayCompositeDisposable = new ArrayCompositeDisposable(2);
        serializedObserver.onSubscribe(arrayCompositeDisposable);
        SkipUntilObserver skipUntilObserver = new SkipUntilObserver(serializedObserver, arrayCompositeDisposable);
        this.other.subscribe(new SkipUntil(arrayCompositeDisposable, skipUntilObserver, serializedObserver));
        this.source.subscribe(skipUntilObserver);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSkipUntil$SkipUntilObserver */
    /* loaded from: classes.dex */
    static final class SkipUntilObserver<T> implements Observer<T> {
        final Observer<? super T> downstream;
        final ArrayCompositeDisposable frc;
        volatile boolean notSkipping;
        boolean notSkippingLocal;
        Disposable upstream;

        SkipUntilObserver(Observer<? super T> observer, ArrayCompositeDisposable arrayCompositeDisposable) {
            this.downstream = observer;
            this.frc = arrayCompositeDisposable;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.frc.setResource(0, disposable);
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
        public void onError(Throwable th) {
            this.frc.dispose();
            this.downstream.onError(th);
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

        SkipUntil(ArrayCompositeDisposable arrayCompositeDisposable, SkipUntilObserver<T> skipUntilObserver, SerializedObserver<T> serializedObserver) {
            this.frc = arrayCompositeDisposable;
            this.sus = skipUntilObserver;
            this.serial = serializedObserver;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.frc.setResource(1, disposable);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(U u) {
            this.upstream.dispose();
            this.sus.notSkipping = true;
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.frc.dispose();
            this.serial.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.sus.notSkipping = true;
        }
    }
}
