package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.SequentialDisposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableSubscribeOn */
/* loaded from: classes.dex */
public final class CompletableSubscribeOn extends Completable {
    final Scheduler scheduler;
    final CompletableSource source;

    public CompletableSubscribeOn(CompletableSource source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        SubscribeOnObserver parent = new SubscribeOnObserver(observer, this.source);
        observer.onSubscribe(parent);
        parent.task.replace(this.scheduler.scheduleDirect(parent));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableSubscribeOn$SubscribeOnObserver */
    /* loaded from: classes.dex */
    static final class SubscribeOnObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable, Runnable {
        private static final long serialVersionUID = 7000911171163930287L;
        final CompletableObserver downstream;
        final CompletableSource source;
        final SequentialDisposable task = new SequentialDisposable();

        SubscribeOnObserver(CompletableObserver actual, CompletableSource source) {
            this.downstream = actual;
            this.source = source;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.source.subscribe(this);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
            this.task.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
