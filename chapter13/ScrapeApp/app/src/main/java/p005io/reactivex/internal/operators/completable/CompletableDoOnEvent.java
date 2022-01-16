package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;

/* renamed from: io.reactivex.internal.operators.completable.CompletableDoOnEvent */
/* loaded from: classes.dex */
public final class CompletableDoOnEvent extends Completable {
    final Consumer<? super Throwable> onEvent;
    final CompletableSource source;

    public CompletableDoOnEvent(CompletableSource source, Consumer<? super Throwable> onEvent) {
        this.source = source;
        this.onEvent = onEvent;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        this.source.subscribe(new DoOnEvent(observer));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableDoOnEvent$DoOnEvent */
    /* loaded from: classes.dex */
    final class DoOnEvent implements CompletableObserver {
        private final CompletableObserver observer;

        DoOnEvent(CompletableObserver observer) {
            this.observer = observer;
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            try {
                CompletableDoOnEvent.this.onEvent.accept(null);
                this.observer.onComplete();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.observer.onError(e);
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            try {
                CompletableDoOnEvent.this.onEvent.accept(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }
            this.observer.onError(e);
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            this.observer.onSubscribe(d);
        }
    }
}
