package p005io.reactivex.internal.operators.completable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.completable.CompletableMergeDelayErrorArray;
import p005io.reactivex.internal.util.AtomicThrowable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableMergeDelayErrorIterable */
/* loaded from: classes.dex */
public final class CompletableMergeDelayErrorIterable extends Completable {
    final Iterable<? extends CompletableSource> sources;

    public CompletableMergeDelayErrorIterable(Iterable<? extends CompletableSource> iterable) {
        this.sources = iterable;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        completableObserver.onSubscribe(compositeDisposable);
        try {
            Iterator it = (Iterator) ObjectHelper.requireNonNull(this.sources.iterator(), "The source iterator returned is null");
            AtomicInteger atomicInteger = new AtomicInteger(1);
            AtomicThrowable atomicThrowable = new AtomicThrowable();
            while (!compositeDisposable.isDisposed()) {
                try {
                    if (it.hasNext()) {
                        if (!compositeDisposable.isDisposed()) {
                            try {
                                CompletableSource completableSource = (CompletableSource) ObjectHelper.requireNonNull(it.next(), "The iterator returned a null CompletableSource");
                                if (!compositeDisposable.isDisposed()) {
                                    atomicInteger.getAndIncrement();
                                    completableSource.subscribe(new CompletableMergeDelayErrorArray.MergeInnerCompletableObserver(completableObserver, compositeDisposable, atomicThrowable, atomicInteger));
                                } else {
                                    return;
                                }
                            } catch (Throwable th) {
                                Exceptions.throwIfFatal(th);
                                atomicThrowable.addThrowable(th);
                            }
                        } else {
                            return;
                        }
                    }
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    atomicThrowable.addThrowable(th2);
                }
                if (atomicInteger.decrementAndGet() == 0) {
                    Throwable terminate = atomicThrowable.terminate();
                    if (terminate == null) {
                        completableObserver.onComplete();
                        return;
                    } else {
                        completableObserver.onError(terminate);
                        return;
                    }
                } else {
                    return;
                }
            }
        } catch (Throwable th3) {
            Exceptions.throwIfFatal(th3);
            completableObserver.onError(th3);
        }
    }
}
