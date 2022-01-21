package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Notification;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.observable.ObservableMaterialize */
/* loaded from: classes.dex */
public final class ObservableMaterialize<T> extends AbstractObservableWithUpstream<T, Notification<T>> {
    public ObservableMaterialize(ObservableSource<T> source) {
        super(source);
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super Notification<T>> t) {
        this.source.subscribe(new MaterializeObserver(t));
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableMaterialize$MaterializeObserver */
    /* loaded from: classes.dex */
    static final class MaterializeObserver<T> implements Observer<T>, Disposable {
        final Observer<? super Notification<T>> downstream;
        Disposable upstream;

        MaterializeObserver(Observer<? super Notification<T>> downstream) {
            this.downstream = downstream;
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
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
            this.downstream.onNext(Notification.createOnNext(t));
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
            this.downstream.onNext(Notification.createOnError(t));
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.downstream.onNext(Notification.createOnComplete());
            this.downstream.onComplete();
        }
    }
}
