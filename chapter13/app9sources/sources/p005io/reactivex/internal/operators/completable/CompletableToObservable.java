package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.observers.BasicQueueDisposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableToObservable */
/* loaded from: classes.dex */
public final class CompletableToObservable<T> extends Observable<T> {
    final CompletableSource source;

    public CompletableToObservable(CompletableSource completableSource) {
        this.source = completableSource;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new ObserverCompletableObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableToObservable$ObserverCompletableObserver */
    /* loaded from: classes.dex */
    static final class ObserverCompletableObserver extends BasicQueueDisposable<Void> implements CompletableObserver {
        final Observer<?> observer;
        Disposable upstream;

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return true;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public Void poll() throws Exception {
            return null;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            return i & 2;
        }

        ObserverCompletableObserver(Observer<?> observer) {
            this.observer = observer;
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.observer.onComplete();
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            this.observer.onError(th);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.observer.onSubscribe(this);
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
