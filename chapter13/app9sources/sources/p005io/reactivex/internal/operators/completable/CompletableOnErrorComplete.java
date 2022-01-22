package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Predicate;

/* renamed from: io.reactivex.internal.operators.completable.CompletableOnErrorComplete */
/* loaded from: classes.dex */
public final class CompletableOnErrorComplete extends Completable {
    final Predicate<? super Throwable> predicate;
    final CompletableSource source;

    public CompletableOnErrorComplete(CompletableSource completableSource, Predicate<? super Throwable> predicate) {
        this.source = completableSource;
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        this.source.subscribe(new OnError(completableObserver));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableOnErrorComplete$OnError */
    /* loaded from: classes.dex */
    final class OnError implements CompletableObserver {
        private final CompletableObserver downstream;

        OnError(CompletableObserver completableObserver) {
            this.downstream = completableObserver;
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            try {
                if (CompletableOnErrorComplete.this.predicate.test(th)) {
                    this.downstream.onComplete();
                } else {
                    this.downstream.onError(th);
                }
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                this.downstream.onError(new CompositeException(th, th2));
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            this.downstream.onSubscribe(disposable);
        }
    }
}
