package p005io.reactivex.internal.operators.single;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Scheduler;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleTimeout */
/* loaded from: classes.dex */
public final class SingleTimeout<T> extends Single<T> {
    final SingleSource<? extends T> other;
    final Scheduler scheduler;
    final SingleSource<T> source;
    final long timeout;
    final TimeUnit unit;

    public SingleTimeout(SingleSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler, SingleSource<? extends T> other) {
        this.source = source;
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
        this.other = other;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        TimeoutMainObserver<T> parent = new TimeoutMainObserver<>(observer, this.other, this.timeout, this.unit);
        observer.onSubscribe(parent);
        DisposableHelper.replace(parent.task, this.scheduler.scheduleDirect(parent, this.timeout, this.unit));
        this.source.subscribe(parent);
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleTimeout$TimeoutMainObserver */
    /* loaded from: classes.dex */
    static final class TimeoutMainObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T>, Runnable, Disposable {
        private static final long serialVersionUID = 37497744973048446L;
        final SingleObserver<? super T> downstream;
        final TimeoutFallbackObserver<T> fallback;
        SingleSource<? extends T> other;
        final AtomicReference<Disposable> task = new AtomicReference<>();
        final long timeout;
        final TimeUnit unit;

        /* renamed from: io.reactivex.internal.operators.single.SingleTimeout$TimeoutMainObserver$TimeoutFallbackObserver */
        /* loaded from: classes.dex */
        static final class TimeoutFallbackObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T> {
            private static final long serialVersionUID = 2071387740092105509L;
            final SingleObserver<? super T> downstream;

            TimeoutFallbackObserver(SingleObserver<? super T> downstream) {
                this.downstream = downstream;
            }

            @Override // p005io.reactivex.SingleObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override // p005io.reactivex.SingleObserver
            public void onSuccess(T t) {
                this.downstream.onSuccess(t);
            }

            @Override // p005io.reactivex.SingleObserver
            public void onError(Throwable e) {
                this.downstream.onError(e);
            }
        }

        TimeoutMainObserver(SingleObserver<? super T> actual, SingleSource<? extends T> other, long timeout, TimeUnit unit) {
            this.downstream = actual;
            this.other = other;
            this.timeout = timeout;
            this.unit = unit;
            if (other != null) {
                this.fallback = new TimeoutFallbackObserver<>(actual);
            } else {
                this.fallback = null;
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            Disposable d = get();
            if (d != DisposableHelper.DISPOSED && compareAndSet(d, DisposableHelper.DISPOSED)) {
                if (d != null) {
                    d.dispose();
                }
                SingleSource<? extends T> other = this.other;
                if (other == null) {
                    this.downstream.onError(new TimeoutException(ExceptionHelper.timeoutMessage(this.timeout, this.unit)));
                    return;
                }
                this.other = null;
                other.subscribe(this.fallback);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            Disposable d = get();
            if (d != DisposableHelper.DISPOSED && compareAndSet(d, DisposableHelper.DISPOSED)) {
                DisposableHelper.dispose(this.task);
                this.downstream.onSuccess(t);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            Disposable d = get();
            if (d == DisposableHelper.DISPOSED || !compareAndSet(d, DisposableHelper.DISPOSED)) {
                RxJavaPlugins.onError(e);
                return;
            }
            DisposableHelper.dispose(this.task);
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
            DisposableHelper.dispose(this.task);
            TimeoutFallbackObserver<T> timeoutFallbackObserver = this.fallback;
            if (timeoutFallbackObserver != null) {
                DisposableHelper.dispose(timeoutFallbackObserver);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
