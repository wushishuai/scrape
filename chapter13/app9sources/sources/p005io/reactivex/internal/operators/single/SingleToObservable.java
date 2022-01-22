package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.observers.DeferredScalarDisposable;

/* renamed from: io.reactivex.internal.operators.single.SingleToObservable */
/* loaded from: classes.dex */
public final class SingleToObservable<T> extends Observable<T> {
    final SingleSource<? extends T> source;

    public SingleToObservable(SingleSource<? extends T> singleSource) {
        this.source = singleSource;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(create(observer));
    }

    public static <T> SingleObserver<T> create(Observer<? super T> observer) {
        return new SingleToObservableObserver(observer);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.single.SingleToObservable$SingleToObservableObserver */
    /* loaded from: classes.dex */
    public static final class SingleToObservableObserver<T> extends DeferredScalarDisposable<T> implements SingleObserver<T> {
        private static final long serialVersionUID = 3786543492451018833L;
        Disposable upstream;

        SingleToObservableObserver(Observer<? super T> observer) {
            super(observer);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            complete(t);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            error(th);
        }

        @Override // p005io.reactivex.internal.observers.DeferredScalarDisposable, p005io.reactivex.disposables.Disposable
        public void dispose() {
            super.dispose();
            this.upstream.dispose();
        }
    }
}
