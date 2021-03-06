package p005io.reactivex.internal.operators.observable;

import java.util.Collection;
import java.util.concurrent.Callable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.BasicFuseableObserver;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableDistinct */
/* loaded from: classes.dex */
public final class ObservableDistinct<T, K> extends AbstractObservableWithUpstream<T, T> {
    final Callable<? extends Collection<? super K>> collectionSupplier;
    final Function<? super T, K> keySelector;

    public ObservableDistinct(ObservableSource<T> source, Function<? super T, K> keySelector, Callable<? extends Collection<? super K>> collectionSupplier) {
        super(source);
        this.keySelector = keySelector;
        this.collectionSupplier = collectionSupplier;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        try {
            this.source.subscribe(new DistinctObserver(observer, this.keySelector, (Collection) ObjectHelper.requireNonNull(this.collectionSupplier.call(), "The collectionSupplier returned a null collection. Null values are generally not allowed in 2.x operators and sources.")));
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableDistinct$DistinctObserver */
    /* loaded from: classes.dex */
    static final class DistinctObserver<T, K> extends BasicFuseableObserver<T, T> {
        final Collection<? super K> collection;
        final Function<? super T, K> keySelector;

        DistinctObserver(Observer<? super T> actual, Function<? super T, K> keySelector, Collection<? super K> collection) {
            super(actual);
            this.keySelector = keySelector;
            this.collection = collection;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T value) {
            if (!this.done) {
                if (this.sourceMode == 0) {
                    try {
                        if (this.collection.add(ObjectHelper.requireNonNull(this.keySelector.apply(value), "The keySelector returned a null key"))) {
                            this.downstream.onNext(value);
                        }
                    } catch (Throwable ex) {
                        fail(ex);
                    }
                } else {
                    this.downstream.onNext(null);
                }
            }
        }

        @Override // p005io.reactivex.internal.observers.BasicFuseableObserver, p005io.reactivex.Observer
        public void onError(Throwable e) {
            if (this.done) {
                RxJavaPlugins.onError(e);
                return;
            }
            this.done = true;
            this.collection.clear();
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.internal.observers.BasicFuseableObserver, p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.collection.clear();
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T v;
            do {
                v = this.f99qd.poll();
                if (v == null) {
                    break;
                }
            } while (!this.collection.add((Object) ObjectHelper.requireNonNull(this.keySelector.apply(v), "The keySelector returned a null key")));
            return v;
        }

        @Override // p005io.reactivex.internal.observers.BasicFuseableObserver, p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.collection.clear();
            super.clear();
        }
    }
}
