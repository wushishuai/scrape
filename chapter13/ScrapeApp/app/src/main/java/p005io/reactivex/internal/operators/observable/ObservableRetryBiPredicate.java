package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiPredicate;
import p005io.reactivex.internal.disposables.SequentialDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableRetryBiPredicate */
/* loaded from: classes.dex */
public final class ObservableRetryBiPredicate<T> extends AbstractObservableWithUpstream<T, T> {
    final BiPredicate<? super Integer, ? super Throwable> predicate;

    public ObservableRetryBiPredicate(Observable<T> source, BiPredicate<? super Integer, ? super Throwable> predicate) {
        super(source);
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        SequentialDisposable sa = new SequentialDisposable();
        observer.onSubscribe(sa);
        new RetryBiObserver<>(observer, this.predicate, sa, this.source).subscribeNext();
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableRetryBiPredicate$RetryBiObserver */
    /* loaded from: classes.dex */
    static final class RetryBiObserver<T> extends AtomicInteger implements Observer<T> {
        private static final long serialVersionUID = -7098360935104053232L;
        final Observer<? super T> downstream;
        final BiPredicate<? super Integer, ? super Throwable> predicate;
        int retries;
        final ObservableSource<? extends T> source;
        final SequentialDisposable upstream;

        RetryBiObserver(Observer<? super T> actual, BiPredicate<? super Integer, ? super Throwable> predicate, SequentialDisposable sa, ObservableSource<? extends T> source) {
            this.downstream = actual;
            this.upstream = sa;
            this.source = source;
            this.predicate = predicate;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.upstream.update(d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            try {
                BiPredicate<? super Integer, ? super Throwable> biPredicate = this.predicate;
                int i = this.retries + 1;
                this.retries = i;
                if (!biPredicate.test(Integer.valueOf(i), t)) {
                    this.downstream.onError(t);
                } else {
                    subscribeNext();
                }
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(new CompositeException(t, e));
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.downstream.onComplete();
        }

        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                while (!this.upstream.isDisposed()) {
                    this.source.subscribe(this);
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        return;
                    }
                }
            }
        }
    }
}
