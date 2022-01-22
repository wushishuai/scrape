package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableConcatArray */
/* loaded from: classes.dex */
public final class CompletableConcatArray extends Completable {
    final CompletableSource[] sources;

    public CompletableConcatArray(CompletableSource[] completableSourceArr) {
        this.sources = completableSourceArr;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        ConcatInnerObserver concatInnerObserver = new ConcatInnerObserver(completableObserver, this.sources);
        completableObserver.onSubscribe(concatInnerObserver.f109sd);
        concatInnerObserver.next();
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableConcatArray$ConcatInnerObserver */
    /* loaded from: classes.dex */
    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {
        private static final long serialVersionUID = -7965400327305809232L;
        final CompletableObserver downstream;
        int index;

        /* renamed from: sd */
        final SequentialDisposable f109sd = new SequentialDisposable();
        final CompletableSource[] sources;

        ConcatInnerObserver(CompletableObserver completableObserver, CompletableSource[] completableSourceArr) {
            this.downstream = completableObserver;
            this.sources = completableSourceArr;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.f109sd.replace(disposable);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            next();
        }

        void next() {
            if (!this.f109sd.isDisposed() && getAndIncrement() == 0) {
                CompletableSource[] completableSourceArr = this.sources;
                while (!this.f109sd.isDisposed()) {
                    int i = this.index;
                    this.index = i + 1;
                    if (i == completableSourceArr.length) {
                        this.downstream.onComplete();
                        return;
                    }
                    completableSourceArr[i].subscribe(this);
                    if (decrementAndGet() == 0) {
                        return;
                    }
                }
            }
        }
    }
}
