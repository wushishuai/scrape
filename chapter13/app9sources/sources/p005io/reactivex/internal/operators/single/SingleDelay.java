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

    public SingleDelay(SingleSource<? extends T> singleSource, long j, TimeUnit timeUnit, Scheduler scheduler, boolean z) {
        this.source = singleSource;
        this.time = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.delayError = z;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        SequentialDisposable sequentialDisposable = new SequentialDisposable();
        singleObserver.onSubscribe(sequentialDisposable);
        this.source.subscribe(new Delay(sequentialDisposable, singleObserver));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDelay$Delay */
    /* loaded from: classes.dex */
    final class Delay implements SingleObserver<T> {
        final SingleObserver<? super T> downstream;

        /* renamed from: sd */
        private final SequentialDisposable f171sd;

        Delay(SequentialDisposable sequentialDisposable, SingleObserver<? super T> singleObserver) {
            this.f171sd = sequentialDisposable;
            this.downstream = singleObserver;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            this.f171sd.replace(disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.f171sd.replace(SingleDelay.this.scheduler.scheduleDirect(new OnSuccess(t), SingleDelay.this.time, SingleDelay.this.unit));
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.f171sd.replace(SingleDelay.this.scheduler.scheduleDirect(new OnError(th), SingleDelay.this.delayError ? SingleDelay.this.time : 0, SingleDelay.this.unit));
        }

        /* renamed from: io.reactivex.internal.operators.single.SingleDelay$Delay$OnSuccess */
        /* loaded from: classes.dex */
        final class OnSuccess implements Runnable {
            private final T value;

            OnSuccess(T t) {
                this.value = t;
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

            OnError(Throwable th) {
                this.f172e = th;
            }

            @Override // java.lang.Runnable
            public void run() {
                Delay.this.downstream.onError(this.f172e);
            }
        }
    }
}
