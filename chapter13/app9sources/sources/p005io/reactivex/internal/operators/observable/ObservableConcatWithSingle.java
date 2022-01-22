package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableConcatWithSingle */
/* loaded from: classes.dex */
public final class ObservableConcatWithSingle<T> extends AbstractObservableWithUpstream<T, T> {
    final SingleSource<? extends T> other;

    public ObservableConcatWithSingle(Observable<T> observable, SingleSource<? extends T> singleSource) {
        super(observable);
        this.other = singleSource;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new ConcatWithObserver(observer, this.other));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableConcatWithSingle$ConcatWithObserver */
    /* loaded from: classes.dex */
    static final class ConcatWithObserver<T> extends AtomicReference<Disposable> implements Observer<T>, SingleObserver<T>, Disposable {
        private static final long serialVersionUID = -1953724749712440952L;
        final Observer<? super T> downstream;
        boolean inSingle;
        SingleSource<? extends T> other;

        ConcatWithObserver(Observer<? super T> observer, SingleSource<? extends T> singleSource) {
            this.downstream = observer;
            this.other = singleSource;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.setOnce(this, disposable) && !this.inSingle) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.downstream.onNext(t);
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.inSingle = true;
            DisposableHelper.replace(this, null);
            SingleSource<? extends T> singleSource = this.other;
            this.other = null;
            singleSource.subscribe(this);
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
}
