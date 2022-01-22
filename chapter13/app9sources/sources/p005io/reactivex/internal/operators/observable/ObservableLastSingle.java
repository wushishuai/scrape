package p005io.reactivex.internal.operators.observable;

import java.util.NoSuchElementException;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableLastSingle */
/* loaded from: classes.dex */
public final class ObservableLastSingle<T> extends Single<T> {
    final T defaultItem;
    final ObservableSource<T> source;

    public ObservableLastSingle(ObservableSource<T> observableSource, T t) {
        this.source = observableSource;
        this.defaultItem = t;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(new LastObserver(singleObserver, this.defaultItem));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableLastSingle$LastObserver */
    /* loaded from: classes.dex */
    static final class LastObserver<T> implements Observer<T>, Disposable {
        final T defaultItem;
        final SingleObserver<? super T> downstream;
        T item;
        Disposable upstream;

        LastObserver(SingleObserver<? super T> singleObserver, T t) {
            this.downstream = singleObserver;
            this.defaultItem = t;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            this.upstream = DisposableHelper.DISPOSED;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream == DisposableHelper.DISPOSED;
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
            this.item = t;
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.upstream = DisposableHelper.DISPOSED;
            this.item = null;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.upstream = DisposableHelper.DISPOSED;
            T t = this.item;
            if (t != null) {
                this.item = null;
                this.downstream.onSuccess(t);
                return;
            }
            T t2 = this.defaultItem;
            if (t2 != null) {
                this.downstream.onSuccess(t2);
            } else {
                this.downstream.onError(new NoSuchElementException());
            }
        }
    }
}
