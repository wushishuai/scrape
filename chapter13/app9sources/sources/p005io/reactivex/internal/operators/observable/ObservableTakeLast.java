package p005io.reactivex.internal.operators.observable;

import java.util.ArrayDeque;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableTakeLast */
/* loaded from: classes.dex */
public final class ObservableTakeLast<T> extends AbstractObservableWithUpstream<T, T> {
    final int count;

    public ObservableTakeLast(ObservableSource<T> observableSource, int i) {
        super(observableSource);
        this.count = i;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new TakeLastObserver(observer, this.count));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTakeLast$TakeLastObserver */
    /* loaded from: classes.dex */
    static final class TakeLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {
        private static final long serialVersionUID = 7240042530241604978L;
        volatile boolean cancelled;
        final int count;
        final Observer<? super T> downstream;
        Disposable upstream;

        TakeLastObserver(Observer<? super T> observer, int i) {
            this.downstream = observer;
            this.count = i;
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
            if (this.count == size()) {
                poll();
            }
            offer(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            Observer<? super T> observer = this.downstream;
            while (!this.cancelled) {
                Object obj = (T) poll();
                if (obj != null) {
                    observer.onNext(obj);
                } else if (!this.cancelled) {
                    observer.onComplete();
                    return;
                } else {
                    return;
                }
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }
    }
}
