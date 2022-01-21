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

    public CompletableMergeArray(CompletableSource[] sources) {
        this.sources = sources;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver observer) {
        CompositeDisposable set = new CompositeDisposable();
        InnerCompletableObserver shared = new InnerCompletableObserver(observer, new AtomicBoolean(), set, this.sources.length + 1);
        observer.onSubscribe(set);
        CompletableSource[] completableSourceArr = this.sources;
        for (CompletableSource c : completableSourceArr) {
            if (!set.isDisposed()) {
                if (c == null) {
                    set.dispose();
                    shared.onError(new NullPointerException("A completable source is null"));
                    return;
                }
                c.subscribe(shared);
            } else {
                return;
            }
        }
        shared.onComplete();
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableMergeArray$InnerCompletableObserver */
    /* loaded from: classes.dex */
    static final class InnerCompletableObserver extends AtomicInteger implements CompletableObserver {
        private static final long serialVersionUID = -8360547806504310570L;
        final CompletableObserver downstream;
        final AtomicBoolean once;
        final CompositeDisposable set;

        InnerCompletableObserver(CompletableObserver actual, AtomicBoolean once, CompositeDisposable set, int n) {
            this.downstream = actual;
            this.once = once;
            this.set = set;
            lazySet(n);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            this.set.dispose();
            if (this.once.compareAndSet(false, true)) {
                this.downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
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
