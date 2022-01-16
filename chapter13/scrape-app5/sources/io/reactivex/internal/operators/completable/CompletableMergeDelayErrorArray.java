package io.reactivex.internal.operators.completable;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.AtomicThrowable;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicInteger;
/* loaded from: classes.dex */
public final class CompletableMergeDelayErrorArray extends Completable {
    final CompletableSource[] sources;

    public CompletableMergeDelayErrorArray(CompletableSource[] sources) {
        this.sources = sources;
    }

    @Override // io.reactivex.Completable
    public void subscribeActual(CompletableObserver observer) {
        CompositeDisposable set = new CompositeDisposable();
        AtomicInteger wip = new AtomicInteger(this.sources.length + 1);
        AtomicThrowable error = new AtomicThrowable();
        observer.onSubscribe(set);
        CompletableSource[] completableSourceArr = this.sources;
        for (CompletableSource c : completableSourceArr) {
            if (!set.isDisposed()) {
                if (c == null) {
                    error.addThrowable(new NullPointerException("A completable source is null"));
                    wip.decrementAndGet();
                } else {
                    c.subscribe(new MergeInnerCompletableObserver(observer, set, error, wip));
                }
            } else {
                return;
            }
        }
        if (wip.decrementAndGet() == 0) {
            Throwable ex = error.terminate();
            if (ex == null) {
                observer.onComplete();
            } else {
                observer.onError(ex);
            }
        }
    }

    /* loaded from: classes.dex */
    static final class MergeInnerCompletableObserver implements CompletableObserver {
        final CompletableObserver downstream;
        final AtomicThrowable error;
        final CompositeDisposable set;
        final AtomicInteger wip;

        /* JADX INFO: Access modifiers changed from: package-private */
        public MergeInnerCompletableObserver(CompletableObserver observer, CompositeDisposable set, AtomicThrowable error, AtomicInteger wip) {
            this.downstream = observer;
            this.set = set;
            this.error = error;
            this.wip = wip;
        }

        @Override // io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            if (this.error.addThrowable(e)) {
                tryTerminate();
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override // io.reactivex.CompletableObserver, io.reactivex.MaybeObserver
        public void onComplete() {
            tryTerminate();
        }

        void tryTerminate() {
            if (this.wip.decrementAndGet() == 0) {
                Throwable ex = this.error.terminate();
                if (ex == null) {
                    this.downstream.onComplete();
                } else {
                    this.downstream.onError(ex);
                }
            }
        }
    }
}
