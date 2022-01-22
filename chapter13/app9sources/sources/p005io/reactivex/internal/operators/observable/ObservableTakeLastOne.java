package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableTakeLastOne */
/* loaded from: classes.dex */
public final class ObservableTakeLastOne<T> extends AbstractObservableWithUpstream<T, T> {
    public ObservableTakeLastOne(ObservableSource<T> observableSource) {
        super(observableSource);
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new TakeLastOneObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTakeLastOne$TakeLastOneObserver */
    /* loaded from: classes.dex */
    static final class TakeLastOneObserver<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        Disposable upstream;
        T value;

        TakeLastOneObserver(Observer<? super T> observer) {
            this.downstream = observer;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.value = t;
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.value = null;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            emit();
        }

        void emit() {
            T t = this.value;
            if (t != null) {
                this.value = null;
                this.downstream.onNext(t);
            }
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.value = null;
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }
    }
}
