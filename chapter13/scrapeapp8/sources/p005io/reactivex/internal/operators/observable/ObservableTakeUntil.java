package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.HalfSerializer;

/* renamed from: io.reactivex.internal.operators.observable.ObservableTakeUntil */
/* loaded from: classes.dex */
public final class ObservableTakeUntil<T, U> extends AbstractObservableWithUpstream<T, T> {
    final ObservableSource<? extends U> other;

    public ObservableTakeUntil(ObservableSource<T> source, ObservableSource<? extends U> other) {
        super(source);
        this.other = other;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> child) {
        TakeUntilMainObserver<T, U> parent = new TakeUntilMainObserver<>(child);
        child.onSubscribe(parent);
        this.other.subscribe(parent.otherObserver);
        this.source.subscribe(parent);
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableTakeUntil$TakeUntilMainObserver */
    /* loaded from: classes.dex */
    static final class TakeUntilMainObserver<T, U> extends AtomicInteger implements Observer<T>, Disposable {
        private static final long serialVersionUID = 1418547743690811973L;
        final Observer<? super T> downstream;
        final AtomicReference<Disposable> upstream = new AtomicReference<>();
        final TakeUntilMainObserver<T, U>.OtherObserver otherObserver = new OtherObserver();
        final AtomicThrowable error = new AtomicThrowable();

        TakeUntilMainObserver(Observer<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this.upstream);
            DisposableHelper.dispose(this.otherObserver);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(this.upstream.get());
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this.upstream, d);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            HalfSerializer.onNext(this.downstream, t, this, this.error);
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            DisposableHelper.dispose(this.otherObserver);
            HalfSerializer.onError(this.downstream, e, this, this.error);
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            DisposableHelper.dispose(this.otherObserver);
            HalfSerializer.onComplete(this.downstream, this, this.error);
        }

        void otherError(Throwable e) {
            DisposableHelper.dispose(this.upstream);
            HalfSerializer.onError(this.downstream, e, this, this.error);
        }

        void otherComplete() {
            DisposableHelper.dispose(this.upstream);
            HalfSerializer.onComplete(this.downstream, this, this.error);
        }

        /* renamed from: io.reactivex.internal.operators.observable.ObservableTakeUntil$TakeUntilMainObserver$OtherObserver */
        /* loaded from: classes.dex */
        final class OtherObserver extends AtomicReference<Disposable> implements Observer<U> {
            private static final long serialVersionUID = -8693423678067375039L;

            OtherObserver() {
            }

            @Override // p005io.reactivex.Observer
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override // p005io.reactivex.Observer
            public void onNext(U t) {
                DisposableHelper.dispose(this);
                TakeUntilMainObserver.this.otherComplete();
            }

            @Override // p005io.reactivex.Observer
            public void onError(Throwable e) {
                TakeUntilMainObserver.this.otherError(e);
            }

            @Override // p005io.reactivex.Observer
            public void onComplete() {
                TakeUntilMainObserver.this.otherComplete();
            }
        }
    }
}
