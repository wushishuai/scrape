package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableMergeArray */
/* loaded from: classes.dex */
public final class CompletableMergeArray extends Completable {
    final CompletableSource[] sources;

    public CompletableMergeArray(CompletableSource[] completableSourceArr) {
        this.sources = completableSourceArr;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        InnerCompletableObserver innerCompletableObserver = new InnerCompletableObserver(completableObserver, new AtomicBoolean(), compositeDisposable, this.sources.length + 1);
        completableObserver.onSubscribe(compositeDisposable);
        CompletableSource[] completableSourceArr = this.sources;
        for (CompletableSource completableSource : completableSourceArr) {
            if (!compositeDisposable.isDisposed()) {
                if (completableSource == null) {
                    compositeDisposable.dispose();
                    innerCompletableObserver.onError(new NullPointerException("A completable source is null"));
                    return;
                }
                completableSource.subscribe(innerCompletableObserver);
            } else {
                return;
            }
        }
        innerCompletableObserver.onComplete();
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableMergeArray$InnerCompletableObserver */
    /* loaded from: classes.dex */
    static final class InnerCompletableObserver extends AtomicInteger implements CompletableObserver {
        private static final long serialVersionUID = -8360547806504310570L;
        final CompletableObserver downstream;
        final AtomicBoolean once;
        final CompositeDisposable set;

        InnerCompletableObserver(CompletableObserver completableObserver, AtomicBoolean atomicBoolean, CompositeDisposable compositeDisposable, int i) {
            this.downstream = completableObserver;
            this.once = atomicBoolean;
            this.set = compositeDisposable;
            lazySet(i);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.set.add(disposable);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            this.set.dispose();
            if (this.once.compareAndSet(false, true)) {
                this.downstream.onError(th);
            } else {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            if (decrementAndGet() == 0 && this.once.compareAndSet(false, true)) {
                this.downstream.onComplete();
            }
        }
    }
}
