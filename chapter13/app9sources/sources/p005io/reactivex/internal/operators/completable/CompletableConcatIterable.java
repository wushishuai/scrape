package p005io.reactivex.internal.operators.completable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.completable.CompletableConcatIterable */
/* loaded from: classes.dex */
public final class CompletableConcatIterable extends Completable {
    final Iterable<? extends CompletableSource> sources;

    public CompletableConcatIterable(Iterable<? extends CompletableSource> iterable) {
        this.sources = iterable;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        try {
            ConcatInnerObserver concatInnerObserver = new ConcatInnerObserver(completableObserver, (Iterator) ObjectHelper.requireNonNull(this.sources.iterator(), "The iterator returned is null"));
            completableObserver.onSubscribe(concatInnerObserver.f110sd);
            concatInnerObserver.next();
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, completableObserver);
        }
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableConcatIterable$ConcatInnerObserver */
    /* loaded from: classes.dex */
    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {
        private static final long serialVersionUID = -7965400327305809232L;
        final CompletableObserver downstream;

        /* renamed from: sd */
        final SequentialDisposable f110sd = new SequentialDisposable();
        final Iterator<? extends CompletableSource> sources;

        ConcatInnerObserver(CompletableObserver completableObserver, Iterator<? extends CompletableSource> it) {
            this.downstream = completableObserver;
            this.sources = it;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.f110sd.replace(disposable);
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
            if (!this.f110sd.isDisposed() && getAndIncrement() == 0) {
                Iterator<? extends CompletableSource> it = this.sources;
                while (!this.f110sd.isDisposed()) {
                    try {
                        if (!it.hasNext()) {
                            this.downstream.onComplete();
                            return;
                        }
                        try {
                            ((CompletableSource) ObjectHelper.requireNonNull(it.next(), "The CompletableSource returned is null")).subscribe(this);
                            if (decrementAndGet() == 0) {
                                return;
                            }
                        } catch (Throwable th) {
                            Exceptions.throwIfFatal(th);
                            this.downstream.onError(th);
                            return;
                        }
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        this.downstream.onError(th2);
                        return;
                    }
                }
            }
        }
    }
}
