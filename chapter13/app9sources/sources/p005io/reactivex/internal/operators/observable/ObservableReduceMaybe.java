package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableReduceMaybe */
/* loaded from: classes.dex */
public final class ObservableReduceMaybe<T> extends Maybe<T> {
    final BiFunction<T, T, T> reducer;
    final ObservableSource<T> source;

    public ObservableReduceMaybe(ObservableSource<T> observableSource, BiFunction<T, T, T> biFunction) {
        this.source = observableSource;
        this.reducer = biFunction;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new ReduceObserver(maybeObserver, this.reducer));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableReduceMaybe$ReduceObserver */
    /* loaded from: classes.dex */
    static final class ReduceObserver<T> implements Observer<T>, Disposable {
        boolean done;
        final MaybeObserver<? super T> downstream;
        final BiFunction<T, T, T> reducer;
        Disposable upstream;
        T value;

        ReduceObserver(MaybeObserver<? super T> maybeObserver, BiFunction<T, T, T> biFunction) {
            this.downstream = maybeObserver;
            this.reducer = biFunction;
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
            if (!this.done) {
                T t2 = this.value;
                if (t2 == null) {
                    this.value = t;
                    return;
                }
                try {
                    this.value = (T) ObjectHelper.requireNonNull(this.reducer.apply(t2, t), "The reducer returned a null value");
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
            this.value = null;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                T t = this.value;
                this.value = null;
                if (t != null) {
                    this.downstream.onSuccess(t);
                } else {
                    this.downstream.onComplete();
                }
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
    }
}
