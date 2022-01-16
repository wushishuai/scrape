package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableHide */
/* loaded from: classes.dex */
public final class ObservableHide<T> extends AbstractObservableWithUpstream<T, T> {
    public ObservableHide(ObservableSource<T> source) {
        super(source);
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> o) {
        this.source.subscribe(new HideDisposable(o));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableHide$HideDisposable */
    /* loaded from: classes.dex */
    static final class HideDisposable<T> implements Observer<T>, Disposable {
        final Observer<? super T> downstream;
        Disposable upstream;

        HideDisposable(Observer<? super T> downstream) {
            this.downstream = downstream;
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
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
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
            this.downstream.onComplete();
        }
    }
}
