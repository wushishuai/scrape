package p005io.reactivex.internal.operators.mixed;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.mixed.CompletableAndThenObservable */
/* loaded from: classes.dex */
public final class CompletableAndThenObservable<R> extends Observable<R> {
    final ObservableSource<? extends R> other;
    final CompletableSource source;

    public CompletableAndThenObservable(CompletableSource source, ObservableSource<? extends R> other) {
        this.source = source;
        this.other = other;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super R> observer) {
        AndThenObservableObserver<R> parent = new AndThenObservableObserver<>(observer, this.other);
        observer.onSubscribe(parent);
        this.source.subscribe(parent);
    }

    /* renamed from: io.reactivex.internal.operators.mixed.CompletableAndThenObservable$AndThenObservableObserver */
    /* loaded from: classes.dex */
    static final class AndThenObservableObserver<R> extends AtomicReference<Disposable> implements Observer<R>, CompletableObserver, Disposable {
        private static final long serialVersionUID = -8948264376121066672L;
        final Observer<? super R> downstream;
        ObservableSource<? extends R> other;

        AndThenObservableObserver(Observer<? super R> downstream, ObservableSource<? extends R> other) {
            this.other = other;
            this.downstream = downstream;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(R t) {
            this.downstream.onNext(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.downstream.onError(t);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            ObservableSource<? extends R> o = this.other;
            if (o == null) {
                this.downstream.onComplete();
                return;
            }
            this.other = null;
            o.subscribe(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            DisposableHelper.replace(this, d);
        }
    }
}
