package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.functions.BiPredicate;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.observers.BasicFuseableObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableDistinctUntilChanged */
/* loaded from: classes.dex */
public final class ObservableDistinctUntilChanged<T, K> extends AbstractObservableWithUpstream<T, T> {
    final BiPredicate<? super K, ? super K> comparer;
    final Function<? super T, K> keySelector;

    public ObservableDistinctUntilChanged(ObservableSource<T> source, Function<? super T, K> keySelector, BiPredicate<? super K, ? super K> comparer) {
        super(source);
        this.keySelector = keySelector;
        this.comparer = comparer;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new DistinctUntilChangedObserver(observer, this.keySelector, this.comparer));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableDistinctUntilChanged$DistinctUntilChangedObserver */
    /* loaded from: classes.dex */
    static final class DistinctUntilChangedObserver<T, K> extends BasicFuseableObserver<T, T> {
        final BiPredicate<? super K, ? super K> comparer;
        boolean hasValue;
        final Function<? super T, K> keySelector;
        K last;

        DistinctUntilChangedObserver(Observer<? super T> actual, Function<? super T, K> keySelector, BiPredicate<? super K, ? super K> comparer) {
            super(actual);
            this.keySelector = keySelector;
            this.comparer = comparer;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (!this.done) {
                if (this.sourceMode != 0) {
                    this.downstream.onNext(t);
                    return;
                }
                try {
                    K key = this.keySelector.apply(t);
                    if (this.hasValue) {
                        boolean equal = this.comparer.test((K) this.last, key);
                        this.last = key;
                        if (equal) {
                            return;
                        }
                    } else {
                        this.hasValue = true;
                        this.last = key;
                    }
                    this.downstream.onNext(t);
                } catch (Throwable ex) {
                    fail(ex);
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            while (true) {
                T v = this.f99qd.poll();
                if (v == null) {
                    return null;
                }
                K key = this.keySelector.apply(v);
                if (!this.hasValue) {
                    this.hasValue = true;
                    this.last = key;
                    return v;
                } else if (!this.comparer.test((K) this.last, key)) {
                    this.last = key;
                    return v;
                } else {
                    this.last = key;
                }
            }
        }
    }
}
