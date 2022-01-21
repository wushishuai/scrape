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

    public CompletableTimeout(CompletableSource source, long timeout, TimeUnit unit, Scheduler scheduler, CompletableSource other) {
        this.source = source;
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
        this.other = other;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver observer) {
        CompositeDisposable set = new CompositeDisposable();
        observer.onSubscribe(set);
        AtomicBoolean once = new AtomicBoolean();
        set.add(this.scheduler.scheduleDirect(new DisposeTask(once, set, observer), this.timeout, this.unit));
        this.source.subscribe(new TimeOutObserver(set, once, observer));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableTimeout$TimeOutObserver */
    /* loaded from: classes.dex */
    static final class TimeOutObserver implements CompletableObserver {
        private final CompletableObserver downstream;
        private final AtomicBoolean once;
        private final CompositeDisposable set;

        TimeOutObserver(CompositeDisposable set, AtomicBoolean once, CompletableObserver observer) {
            this.set = set;
            this.once = once;
            this.downstream = observer;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            if (this.once.compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onError(e);
                return;
            }
            RxJavaPlugins.onError(e);
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

        DisposeTask(AtomicBoolean once, CompositeDisposable set, CompletableObserver observer) {
            this.once = once;
            this.set = set;
            this.downstream = observer;
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
            public void onSubscribe(Disposable d) {
                DisposeTask.this.set.add(d);
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onError(Throwable e) {
                DisposeTask.this.set.dispose();
                DisposeTask.this.downstream.onError(e);
            }

            @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
            public void onComplete() {
                DisposeTask.this.set.dispose();
                DisposeTask.this.downstream.onComplete();
            }
        }
    }
}
