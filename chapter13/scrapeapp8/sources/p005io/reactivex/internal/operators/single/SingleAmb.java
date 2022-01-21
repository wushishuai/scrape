package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleAmb */
/* loaded from: classes.dex */
public final class SingleAmb<T> extends Single<T> {
    private final SingleSource<? extends T>[] sources;
    private final Iterable<? extends SingleSource<? extends T>> sourcesIterable;

    public SingleAmb(SingleSource<? extends T>[] sources, Iterable<? extends SingleSource<? extends T>> sourcesIterable) {
        this.sources = sources;
        this.sourcesIterable = sourcesIterable;
    }

    /* JADX INFO: Multiple debug info for r5v4 int: [D('count' int), D('b' io.reactivex.SingleSource<? extends T>[])] */
    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        Throwable e;
        SingleSource<? extends T>[] sources = this.sources;
        int count = 0;
        if (sources == null) {
            sources = new SingleSource[8];
            try {
                for (SingleSource<? extends T> element : this.sourcesIterable) {
                    if (element == null) {
                        EmptyDisposable.error(new NullPointerException("One of the sources is null"), observer);
                        return;
                    }
                    if (count == sources.length) {
                        SingleSource<? extends T>[] b = new SingleSource[(count >> 2) + count];
                        System.arraycopy(sources, 0, b, 0, count);
                        sources = b;
                    }
                    int count2 = count + 1;
                    try {
                        sources[count] = element;
                        count = count2;
                    } catch (Throwable th) {
                        e = th;
                        Exceptions.throwIfFatal(e);
                        EmptyDisposable.error(e, observer);
                        return;
                    }
                }
            } catch (Throwable th2) {
                e = th2;
            }
        } else {
            count = sources.length;
        }
        CompositeDisposable set = new CompositeDisposable();
        AmbSingleObserver<T> shared = new AmbSingleObserver<>(observer, set);
        observer.onSubscribe(set);
        for (int i = 0; i < count; i++) {
            SingleSource<? extends T> s1 = sources[i];
            if (shared.get()) {
                return;
            }
            if (s1 == null) {
                set.dispose();
                Throwable e2 = new NullPointerException("One of the sources is null");
                if (shared.compareAndSet(false, true)) {
                    observer.onError(e2);
                    return;
                } else {
                    RxJavaPlugins.onError(e2);
                    return;
                }
            } else {
                s1.subscribe(shared);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleAmb$AmbSingleObserver */
    /* loaded from: classes.dex */
    static final class AmbSingleObserver<T> extends AtomicBoolean implements SingleObserver<T> {
        private static final long serialVersionUID = -1944085461036028108L;
        final SingleObserver<? super T> downstream;
        final CompositeDisposable set;

        AmbSingleObserver(SingleObserver<? super T> observer, CompositeDisposable set) {
            this.downstream = observer;
            this.set = set;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            if (compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onSuccess(value);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            if (compareAndSet(false, true)) {
                this.set.dispose();
                this.downstream.onError(e);
                return;
            }
            RxJavaPlugins.onError(e);
        }
    }
}
