package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableDisposeOn */
/* loaded from: classes.dex */
public final class CompletableDisposeOn extends Completable {
    final Scheduler scheduler;
    final CompletableSource source;

    public CompletableDisposeOn(CompletableSource source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        this.source.subscribe(new DisposeOnObserver(observer, this.scheduler));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableDisposeOn$DisposeOnObserver */
    /* loaded from: classes.dex */
    static final class DisposeOnObserver implements CompletableObserver, Disposable, Runnable {
        volatile boolean disposed;
        final CompletableObserver downstream;
        final Scheduler scheduler;
        Disposable upstream;

        DisposeOnObserver(CompletableObserver observer, Scheduler scheduler) {
            this.downstream = observer;
            this.scheduler = scheduler;
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            if (!this.disposed) {
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            if (this.disposed) {
                RxJavaPlugins.onError(e);
            } else {
                this.downstream.onError(e);
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
            this.scheduler.scheduleDirect(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.upstream.dispose();
            this.upstream = DisposableHelper.DISPOSED;
        }
    }
}
