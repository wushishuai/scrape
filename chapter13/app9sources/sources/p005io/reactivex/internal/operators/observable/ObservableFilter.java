package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.observers.BasicFuseableObserver;

/* renamed from: io.reactivex.internal.operators.observable.ObservableFilter */
/* loaded from: classes.dex */
public final class ObservableFilter<T> extends AbstractObservableWithUpstream<T, T> {
    final Predicate<? super T> predicate;

    public ObservableFilter(ObservableSource<T> observableSource, Predicate<? super T> predicate) {
        super(observableSource);
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new FilterObserver(observer, this.predicate));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableFilter$FilterObserver */
    /* loaded from: classes.dex */
    static final class FilterObserver<T> extends BasicFuseableObserver<T, T> {
        final Predicate<? super T> filter;

        FilterObserver(Observer<? super T> observer, Predicate<? super T> predicate) {
            super(observer);
            this.filter = predicate;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (this.sourceMode == 0) {
                try {
                    if (this.filter.test(t)) {
                        this.downstream.onNext(t);
                    }
                } catch (Throwable th) {
                    fail(th);
                }
            } else {
                this.downstream.onNext(null);
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            return transitiveBoundaryFusion(i);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T poll;
            do {
                poll = this.f99qd.poll();
                if (poll == null) {
                    break;
                }
            } while (!this.filter.test(poll));
            return poll;
        }
    }
}
