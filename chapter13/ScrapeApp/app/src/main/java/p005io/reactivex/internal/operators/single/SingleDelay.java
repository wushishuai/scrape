package p005io.reactivex.internal.operators.single;

import java.util.concurrent.TimeUnit;
import p005io.reactivex.Scheduler;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;

/* renamed from: io.reactivex.internal.operators.single.SingleDelay */
/* loaded from: classes.dex */
public final class SingleDelay<T> extends Single<T> {
    final boolean delayError;
    final Scheduler scheduler;
    final SingleSource<? extends T> source;
    final long time;
    final TimeUnit unit;

    public SingleDelay(SingleSource<? extends T> source, long time, TimeUnit unit, Scheduler scheduler, boolean delayError) {
        this.source = source;
        this.time = time;
        this.unit = unit;
        this.scheduler = scheduler;
        this.delayError = delayError;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        SequentialDisposable sd = new SequentialDisposable();
        observer.onSubscribe(sd);
        this.source.subscribe(new Delay(sd, observer));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDelay$Delay */
    /* loaded from: classes.dex */
    final class Delay implements SingleObserver<T> {
        final SingleObserver<? super T> downstream;

        /* renamed from: sd */
        private final SequentialDisposable f171sd;

        Delay(SequentialDisposable sd, SingleObserver<? super T> observer) {
            this.f171sd = sd;
            this.downstream = observer;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.f171sd.replace(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.f171sd.replace(SingleDelay.this.scheduler.scheduleDirect(new OnSuccess(value), SingleDelay.this.time, SingleDelay.this.unit));
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.f171sd.replace(SingleDelay.this.scheduler.scheduleDirect(new OnError(e), SingleDelay.this.delayError ? SingleDelay.this.time : 0, SingleDelay.this.unit));
        }

        /* renamed from: io.reactivex.internal.operators.single.SingleDelay$Delay$OnSuccess */
        /* loaded from: classes.dex */
        final class OnSuccess implements Runnable {
            private final T value;

            OnSuccess(T value) {
                this.value = value;
            }

            @Override // java.lang.Runnable
            public void run() {
                Delay.this.downstream.onSuccess((T) this.value);
            }
        }

        /* renamed from: io.reactivex.internal.operators.single.SingleDelay$Delay$OnError */
        /* loaded from: classes.dex */
        final class OnError implements Runnable {

            /* renamed from: e */
            private final Throwable f172e;

            OnError(Throwable e) {
                this.f172e = e;
            }

            @Override // java.lang.Runnable
            public void run() {
                Delay.this.downstream.onError(this.f172e);
            }
        }
    }
}
