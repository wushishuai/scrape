package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.observers.ResumeSingleObserver;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleDelayWithObservable */
/* loaded from: classes.dex */
public final class SingleDelayWithObservable<T, U> extends Single<T> {
    final ObservableSource<U> other;
    final SingleSource<T> source;

    public SingleDelayWithObservable(SingleSource<T> singleSource, ObservableSource<U> observableSource) {
        this.source = singleSource;
        this.other = observableSource;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.other.subscribe(new OtherSubscriber(singleObserver, this.source));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDelayWithObservable$OtherSubscriber */
    /* loaded from: classes.dex */
    static final class OtherSubscriber<T, U> extends AtomicReference<Disposable> implements Observer<U>, Disposable {
        private static final long serialVersionUID = -8565274649390031272L;
        boolean done;
        final SingleObserver<? super T> downstream;
        final SingleSource<T> source;

        OtherSubscriber(SingleObserver<? super T> singleObserver, SingleSource<T> singleSource) {
            this.downstream = singleObserver;
            this.source = singleSource;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.set(this, disposable)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.Observer
        public void onNext(U u) {
            get().dispose();
            onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.source.subscribe(new ResumeSingleObserver(this, this.downstream));
            }
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
