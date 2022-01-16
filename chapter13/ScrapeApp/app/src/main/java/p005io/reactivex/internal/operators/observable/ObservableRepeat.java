package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableRepeat */
/* loaded from: classes.dex */
public final class ObservableRepeat<T> extends AbstractObservableWithUpstream<T, T> {
    final long count;

    public ObservableRepeat(Observable<T> source, long count) {
        super(source);
        this.count = count;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        SequentialDisposable sd = new SequentialDisposable();
        observer.onSubscribe(sd);
        long j = this.count;
        long j2 = Long.MAX_VALUE;
        if (j != Long.MAX_VALUE) {
            j2 = j - 1;
        }
        new RepeatObserver<>(observer, j2, sd, this.source).subscribeNext();
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableRepeat$RepeatObserver */
    /* loaded from: classes.dex */
    static final class RepeatObserver<T> extends AtomicInteger implements Observer<T> {
        private static final long serialVersionUID = -7098360935104053232L;
        final Observer<? super T> downstream;
        long remaining;

        /* renamed from: sd */
        final SequentialDisposable f157sd;
        final ObservableSource<? extends T> source;

        RepeatObserver(Observer<? super T> actual, long count, SequentialDisposable sd, ObservableSource<? extends T> source) {
            this.downstream = actual;
            this.f157sd = sd;
            this.source = source;
            this.remaining = count;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.f157sd.replace(d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.downstream.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            long r = this.remaining;
            if (r != Long.MAX_VALUE) {
                this.remaining = r - 1;
            }
            if (r != 0) {
                subscribeNext();
            } else {
                this.downstream.onComplete();
            }
        }

        void subscribeNext() {
            if (getAndIncrement() == 0) {
                int missed = 1;
                while (!this.f157sd.isDisposed()) {
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
