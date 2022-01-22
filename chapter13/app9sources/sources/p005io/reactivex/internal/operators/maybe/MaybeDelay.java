package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeDelay */
/* loaded from: classes.dex */
public final class MaybeDelay<T> extends AbstractMaybeWithUpstream<T, T> {
    final long delay;
    final Scheduler scheduler;
    final TimeUnit unit;

    public MaybeDelay(MaybeSource<T> maybeSource, long j, TimeUnit timeUnit, Scheduler scheduler) {
        super(maybeSource);
        this.delay = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new DelayMaybeObserver(maybeObserver, this.delay, this.unit, this.scheduler));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeDelay$DelayMaybeObserver */
    /* loaded from: classes.dex */
    static final class DelayMaybeObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable, Runnable {
        private static final long serialVersionUID = 5566860102500855068L;
        final long delay;
        final MaybeObserver<? super T> downstream;
        Throwable error;
        final Scheduler scheduler;
        final TimeUnit unit;
        T value;

        DelayMaybeObserver(MaybeObserver<? super T> maybeObserver, long j, TimeUnit timeUnit, Scheduler scheduler) {
            this.downstream = maybeObserver;
            this.delay = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
        }

        @Override // java.lang.Runnable
        public void run() {
            Throwable th = this.error;
            if (th != null) {
                this.downstream.onError(th);
                return;
            }
            T t = this.value;
            if (t != null) {
                this.downstream.onSuccess(t);
            } else {
                this.downstream.onComplete();
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

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.setOnce(this, disposable)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            this.value = t;
            schedule();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.error = th;
            schedule();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            schedule();
        }

        void schedule() {
            DisposableHelper.replace(this, this.scheduler.scheduleDirect(this, this.delay, this.unit));
        }
    }
}
