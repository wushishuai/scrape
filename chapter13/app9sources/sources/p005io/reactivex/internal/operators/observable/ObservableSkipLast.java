package p005io.reactivex.internal.operators.observable;

import java.util.ArrayDeque;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableSkipLast */
/* loaded from: classes.dex */
public final class ObservableSkipLast<T> extends AbstractObservableWithUpstream<T, T> {
    final int skip;

    public ObservableSkipLast(ObservableSource<T> observableSource, int i) {
        super(observableSource);
        this.skip = i;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new SkipLastObserver(observer, this.skip));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableSkipLast$SkipLastObserver */
    /* loaded from: classes.dex */
    static final class SkipLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {
        private static final long serialVersionUID = -3807491841935125653L;
        final Observer<? super T> downstream;
        final int skip;
        Disposable upstream;

        SkipLastObserver(Observer<? super T> observer, int i) {
            super(i);
            this.downstream = observer;
            this.skip = i;
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
            if (this.skip == size()) {
                this.downstream.onNext((T) poll());
            }
            offer(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.downstream.onComplete();
        }
    }
}
