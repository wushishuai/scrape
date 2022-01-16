package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Scheduler;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleObserveOn */
/* loaded from: classes.dex */
public final class SingleObserveOn<T> extends Single<T> {
    final Scheduler scheduler;
    final SingleSource<T> source;

    public SingleObserveOn(SingleSource<T> source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        this.source.subscribe(new ObserveOnSingleObserver(observer, this.scheduler));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleObserveOn$ObserveOnSingleObserver */
    /* loaded from: classes.dex */
    static final class ObserveOnSingleObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T>, Disposable, Runnable {
        private static final long serialVersionUID = 3528003840217436037L;
        final SingleObserver<? super T> downstream;
        Throwable error;
        final Scheduler scheduler;
        T value;

        ObserveOnSingleObserver(SingleObserver<? super T> actual, Scheduler scheduler) {
            this.downstream = actual;
            this.scheduler = scheduler;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.value = value;
            DisposableHelper.replace(this, this.scheduler.scheduleDirect(this));
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.error = e;
            DisposableHelper.replace(this, this.scheduler.scheduleDirect(this));
        }

        @Override // java.lang.Runnable
        public void run() {
            Throwable ex = this.error;
            if (ex != null) {
                this.downstream.onError(ex);
            } else {
                this.downstream.onSuccess((T) this.value);
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
