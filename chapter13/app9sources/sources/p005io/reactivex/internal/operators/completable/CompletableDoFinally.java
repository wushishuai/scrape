package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableDoFinally */
/* loaded from: classes.dex */
public final class CompletableDoFinally extends Completable {
    final Action onFinally;
    final CompletableSource source;

    public CompletableDoFinally(CompletableSource completableSource, Action action) {
        this.source = completableSource;
        this.onFinally = action;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        this.source.subscribe(new DoFinallyObserver(completableObserver, this.onFinally));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableDoFinally$DoFinallyObserver */
    /* loaded from: classes.dex */
    static final class DoFinallyObserver extends AtomicInteger implements CompletableObserver, Disposable {
        private static final long serialVersionUID = 4109457741734051389L;
        final CompletableObserver downstream;
        final Action onFinally;
        Disposable upstream;

        DoFinallyObserver(CompletableObserver completableObserver, Action action) {
            this.downstream = completableObserver;
            this.onFinally = action;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
            runFinally();
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
            runFinally();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
            runFinally();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }

        void runFinally() {
            if (compareAndSet(0, 1)) {
                try {
                    this.onFinally.run();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    RxJavaPlugins.onError(th);
                }
            }
        }
    }
}
