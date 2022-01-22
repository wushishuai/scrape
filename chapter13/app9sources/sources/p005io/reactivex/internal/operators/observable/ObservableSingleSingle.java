package p005io.reactivex.internal.operators.observable;

import java.util.NoSuchElementException;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableSingleSingle */
/* loaded from: classes.dex */
public final class ObservableSingleSingle<T> extends Single<T> {
    final T defaultValue;
    final ObservableSource<? extends T> source;

    public ObservableSingleSingle(ObservableSource<? extends T> observableSource, T t) {
        this.source = observableSource;
        this.defaultValue = t;
    }

    @Override // p005io.reactivex.Single
    public void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(new SingleElementObserver(singleObserver, this.defaultValue));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSingleSingle$SingleElementObserver */
    /* loaded from: classes.dex */
    static final class SingleElementObserver<T> implements Observer<T>, Disposable {
        final T defaultValue;
        boolean done;
        final SingleObserver<? super T> downstream;
        Disposable upstream;
        T value;

        SingleElementObserver(SingleObserver<? super T> singleObserver, T t) {
            this.downstream = singleObserver;
            this.defaultValue = t;
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
        public void onNext(T t) {
            if (!this.done) {
                if (this.value != null) {
                    this.done = true;
                    this.upstream.dispose();
                    this.downstream.onError(new IllegalArgumentException("Sequence contains more than one element!"));
                    return;
                }
                this.value = t;
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
                T t = this.value;
                this.value = null;
                if (t == null) {
                    t = this.defaultValue;
                }
                if (t != null) {
                    this.downstream.onSuccess(t);
                } else {
                    this.downstream.onError(new NoSuchElementException());
                }
            }
        }
    }
}
