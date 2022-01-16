package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableTakeUntilCompletable */
/* loaded from: classes.dex */
public final class CompletableTakeUntilCompletable extends Completable {
    final CompletableSource other;
    final Completable source;

    public CompletableTakeUntilCompletable(Completable source, CompletableSource other) {
        this.source = source;
        this.other = other;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        TakeUntilMainObserver parent = new TakeUntilMainObserver(observer);
        observer.onSubscribe(parent);
        this.other.subscribe(parent.other);
        this.source.subscribe(parent);
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableTakeUntilCompletable$TakeUntilMainObserver */
    /* loaded from: classes.dex */
    static final class TakeUntilMainObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
        private static final long serialVersionUID = 3533011714830024923L;
        final CompletableObserver downstream;
        final OtherObserver other = new OtherObserver(this);
        final AtomicBoolean once = new AtomicBoolean();

        TakeUntilMainObserver(CompletableObserver downstream) {
            this.downstream = downstream;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (this.once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this);
                DisposableHelper.dispose(this.other);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.once.get();
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            if (this.once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this.other);
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable e) {
            if (this.once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this.other);
                this.downstream.onError(e);
                return;
            }
            RxJavaPlugins.onError(e);
        }

        void innerComplete() {
            if (this.once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this);
                this.downstream.onComplete();
            }
        }

        void innerError(Throwable e) {
            if (this.once.compareAndSet(false, true)) {
                DisposableHelper.dispose(this);
                this.downstream.onError(e);
                return;
            }
            RxJavaPlugins.onError(e);
        }

        /* renamed from: io.reactivex.internal.operators.completable.CompletableTakeUntilCompletable$TakeUntilMainObserver$OtherObserver */
        /* loaded from: classes.dex */
        static final class OtherObserver extends AtomicReference<Disposable> implements CompletableObserver {
            private static final long serialVersionUID = 5176264485428790318L;
            final TakeUntilMainObserver parent;

            OtherObserver(TakeUntilMainObserver parent) {
                this.parent = parent;
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.parent.innerComplete();
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onError(Throwable e) {
                this.parent.innerError(e);
            }
        }
    }
}
