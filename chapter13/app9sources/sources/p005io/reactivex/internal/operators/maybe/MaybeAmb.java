package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeAmb */
/* loaded from: classes.dex */
public final class MaybeAmb<T> extends Maybe<T> {
    private final MaybeSource<? extends T>[] sources;
    private final Iterable<? extends MaybeSource<? extends T>> sourcesIterable;

    public MaybeAmb(MaybeSource<? extends T>[] maybeSourceArr, Iterable<? extends MaybeSource<? extends T>> iterable) {
        this.sources = maybeSourceArr;
        this.sourcesIterable = iterable;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        int i;
        MaybeSource<? extends T>[] maybeSourceArr = this.sources;
        if (maybeSourceArr == null) {
            maybeSourceArr = new MaybeSource[8];
            try {
                i = 0;
                for (MaybeSource<? extends T> maybeSource : this.sourcesIterable) {
                    if (maybeSource == null) {
                        EmptyDisposable.error(new NullPointerException("One of the sources is null"), maybeObserver);
                        return;
                    }
                    if (i == maybeSourceArr.length) {
                        MaybeSource<? extends T>[] maybeSourceArr2 = new MaybeSource[(i >> 2) + i];
                        System.arraycopy(maybeSourceArr, 0, maybeSourceArr2, 0, i);
                        maybeSourceArr = maybeSourceArr2;
                    }
                    int i2 = i + 1;
                    maybeSourceArr[i] = maybeSource;
                    i = i2;
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                EmptyDisposable.error(th, maybeObserver);
                return;
            }
        } else {
            i = maybeSourceArr.length;
        }
        AmbMaybeObserver ambMaybeObserver = new AmbMaybeObserver(maybeObserver);
        maybeObserver.onSubscribe(ambMaybeObserver);
        for (int i3 = 0; i3 < i; i3++) {
            MaybeSource<? extends T> maybeSource2 = maybeSourceArr[i3];
            if (ambMaybeObserver.isDisposed()) {
                return;
            }
            if (maybeSource2 == null) {
                ambMaybeObserver.onError(new NullPointerException("One of the MaybeSources is null"));
                return;
            } else {
                maybeSource2.subscribe(ambMaybeObserver);
            }
        }
        if (i == 0) {
            maybeObserver.onComplete();
        }
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeAmb$AmbMaybeObserver */
    /* loaded from: classes.dex */
    static final class AmbMaybeObserver<T> extends AtomicBoolean implements MaybeObserver<T>, Disposable {
        private static final long serialVersionUID = -7044685185359438206L;
        final MaybeObserver<? super T> downstream;
        final CompositeDisposable set = new CompositeDisposable();

        AmbMaybeObserver(MaybeObserver<? super T> maybeObserver) {
            this.downstream = maybeObserver;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (compareAndSet(false, true)) {
                this.set.dispose();
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            this.set.add(disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            if (compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onSuccess(t);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            if (compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            if (compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onComplete();
            }
        }
    }
}
