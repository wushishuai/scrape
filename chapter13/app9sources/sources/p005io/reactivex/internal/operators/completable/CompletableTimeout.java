package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableTimeout */
/* loaded from: classes.dex */
public final class CompletableTimeout extends Completable {
    final CompletableSource other;
    final Scheduler scheduler;
    final CompletableSource source;
    final long timeout;
    final TimeUnit unit;

    public CompletableTimeout(CompletableSource completableSource, long j, TimeUnit timeUnit, Scheduler scheduler, CompletableSource completableSource2) {
        this.source = completableSource;
        this.timeout = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.other = completableSource2;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        completableObserver.onSubscribe(compositeDisposable);
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        compositeDisposable.add(this.scheduler.scheduleDirect(new DisposeTask(atomicBoolean, compositeDisposable, completableObserver), this.timeout, this.unit));
        this.source.subscribe(new TimeOutObserver(compositeDisposable, atomicBoolean, completableObserver));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableTimeout$TimeOutObserver */
    /* loaded from: classes.dex */
    static final class TimeOutObserver implements CompletableObserver {
        private final CompletableObserver downstream;
        private final AtomicBoolean once;
        private final CompositeDisposable set;

        TimeOutObserver(CompositeDisposable compositeDisposable, AtomicBoolean atomicBoolean, CompletableObserver completableObserver) {
            this.set = compositeDisposable;
            this.once = atomicBoolean;
            this.downstream = completableObserver;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.set.add(disposable);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            if (this.once.compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            if (this.once.compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onComplete();
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableTimeout$DisposeTask */
    /* loaded from: classes.dex */
    final class DisposeTask implements Runnable {
        final CompletableObserver downstream;
        private final AtomicBoolean once;
        final CompositeDisposable set;

        DisposeTask(AtomicBoolean atomicBoolean, CompositeDisposable compositeDisposable, CompletableObserver completableObserver) {
            this.once = atomicBoolean;
            this.set = compositeDisposable;
            this.downstream = completableObserver;
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.once.compareAndSet(false, true)) {
                this.set.clear();
                if (CompletableTimeout.this.other == null) {
                    this.downstream.onError(new TimeoutException(ExceptionHelper.timeoutMessage(CompletableTimeout.this.timeout, CompletableTimeout.this.unit)));
                } else {
                    CompletableTimeout.this.other.subscribe(new DisposeObserver());
                }
            }
        }

        /* renamed from: io.reactivex.internal.operators.completable.CompletableTimeout$DisposeTask$DisposeObserver */
        /* loaded from: classes.dex */
        final class DisposeObserver implements CompletableObserver {
            DisposeObserver() {
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onSubscribe(Disposable disposable) {
                DisposeTask.this.set.add(disposable);
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onError(Throwable th) {
                DisposeTask.this.set.dispose();
                DisposeTask.this.downstream.onError(th);
            }

            @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
            public void onComplete() {
                DisposeTask.this.set.dispose();
                DisposeTask.this.downstream.onComplete();
            }
        }
    }
}
