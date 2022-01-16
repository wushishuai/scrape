package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableConcatWithCompletable */
/* loaded from: classes.dex */
public final class ObservableConcatWithCompletable<T> extends AbstractObservableWithUpstream<T, T> {
    final CompletableSource other;

    public ObservableConcatWithCompletable(Observable<T> source, CompletableSource other) {
        super(source);
        this.other = other;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(new ConcatWithObserver(observer, this.other));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableConcatWithCompletable$ConcatWithObserver */
    /* loaded from: classes.dex */
    static final class ConcatWithObserver<T> extends AtomicReference<Disposable> implements Observer<T>, CompletableObserver, Disposable {
        private static final long serialVersionUID = -1953724749712440952L;
        final Observer<? super T> downstream;
        boolean inCompletable;
        CompletableSource other;

        ConcatWithObserver(Observer<? super T> actual, CompletableSource other) {
            this.downstream = actual;
            this.other = other;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d) && !this.inCompletable) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (this.inCompletable) {
                this.downstream.onComplete();
                return;
            }
            this.inCompletable = true;
            DisposableHelper.replace(this, null);
            CompletableSource cs = this.other;
            this.other = null;
            cs.subscribe(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
