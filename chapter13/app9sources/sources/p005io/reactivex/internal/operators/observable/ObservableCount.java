package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableCount */
/* loaded from: classes.dex */
public final class ObservableCount<T> extends AbstractObservableWithUpstream<T, Long> {
    public ObservableCount(ObservableSource<T> observableSource) {
        super(observableSource);
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Long> observer) {
        this.source.subscribe(new CountObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableCount$CountObserver */
    /* loaded from: classes.dex */
    static final class CountObserver implements Observer<Object>, Disposable {
        long count;
        final Observer<? super Long> downstream;
        Disposable upstream;

        CountObserver(Observer<? super Long> observer) {
            this.downstream = observer;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
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
        public void onNext(Object obj) {
            this.count++;
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.downstream.onNext(Long.valueOf(this.count));
            this.downstream.onComplete();
        }
    }
}
