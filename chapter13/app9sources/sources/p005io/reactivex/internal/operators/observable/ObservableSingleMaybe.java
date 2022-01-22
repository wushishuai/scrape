package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableSingleMaybe */
/* loaded from: classes.dex */
public final class ObservableSingleMaybe<T> extends Maybe<T> {
    final ObservableSource<T> source;

    public ObservableSingleMaybe(ObservableSource<T> observableSource) {
        this.source = observableSource;
    }

    @Override // p005io.reactivex.Maybe
    public void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new SingleElementObserver(maybeObserver));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSingleMaybe$SingleElementObserver */
    /* loaded from: classes.dex */
    static final class SingleElementObserver<T> implements Observer<T>, Disposable {
        boolean done;
        final MaybeObserver<? super T> downstream;
        Disposable upstream;
        T value;

        SingleElementObserver(MaybeObserver<? super T> maybeObserver) {
            this.downstream = maybeObserver;
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
                    this.downstream.onComplete();
                } else {
                    this.downstream.onSuccess(t);
                }
            }
        }
    }
}
