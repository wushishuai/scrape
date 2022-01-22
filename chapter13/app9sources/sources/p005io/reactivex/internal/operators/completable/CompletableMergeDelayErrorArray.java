package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableMergeDelayErrorArray */
/* loaded from: classes.dex */
public final class CompletableMergeDelayErrorArray extends Completable {
    final CompletableSource[] sources;

    public CompletableMergeDelayErrorArray(CompletableSource[] completableSourceArr) {
        this.sources = completableSourceArr;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        AtomicInteger atomicInteger = new AtomicInteger(this.sources.length + 1);
        AtomicThrowable atomicThrowable = new AtomicThrowable();
        completableObserver.onSubscribe(compositeDisposable);
        CompletableSource[] completableSourceArr = this.sources;
        for (CompletableSource completableSource : completableSourceArr) {
            if (!compositeDisposable.isDisposed()) {
                if (completableSource == null) {
                    atomicThrowable.addThrowable(new NullPointerException("A completable source is null"));
                    atomicInteger.decrementAndGet();
                } else {
                    completableSource.subscribe(new MergeInnerCompletableObserver(completableObserver, compositeDisposable, atomicThrowable, atomicInteger));
                }
            } else {
                return;
            }
        }
        if (atomicInteger.decrementAndGet() == 0) {
            Throwable terminate = atomicThrowable.terminate();
            if (terminate == null) {
                completableObserver.onComplete();
            } else {
                completableObserver.onError(terminate);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableMergeDelayErrorArray$MergeInnerCompletableObserver */
    /* loaded from: classes.dex */
    static final class MergeInnerCompletableObserver implements CompletableObserver {
        final CompletableObserver downstream;
        final AtomicThrowable error;
        final CompositeDisposable set;
        final AtomicInteger wip;

        /* JADX INFO: Access modifiers changed from: package-private */
        public MergeInnerCompletableObserver(CompletableObserver completableObserver, CompositeDisposable compositeDisposable, AtomicThrowable atomicThrowable, AtomicInteger atomicInteger) {
            this.downstream = completableObserver;
            this.set = compositeDisposable;
            this.error = atomicThrowable;
            this.wip = atomicInteger;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.set.add(disposable);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            if (this.error.addThrowable(th)) {
                tryTerminate();
            } else {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            tryTerminate();
        }

        void tryTerminate() {
            if (this.wip.decrementAndGet() == 0) {
                Throwable terminate = this.error.terminate();
                if (terminate == null) {
                    this.downstream.onComplete();
                } else {
                    this.downstream.onError(terminate);
                }
            }
        }
    }
}
