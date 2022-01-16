package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.single.SingleMap;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleZipArray */
/* loaded from: classes.dex */
public final class SingleZipArray<T, R> extends Single<R> {
    final SingleSource<? extends T>[] sources;
    final Function<? super Object[], ? extends R> zipper;

    public SingleZipArray(SingleSource<? extends T>[] sources, Function<? super Object[], ? extends R> zipper) {
        this.sources = sources;
        this.zipper = zipper;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super R> observer) {
        SingleSource<? extends T>[] sources = this.sources;
        int n = sources.length;
        if (n == 1) {
            sources[0].subscribe(new SingleMap.MapSingleObserver(observer, new SingletonArrayFunc()));
            return;
        }
        ZipCoordinator<T, R> parent = new ZipCoordinator<>(observer, n, this.zipper);
        observer.onSubscribe(parent);
        for (int i = 0; i < n && !parent.isDisposed(); i++) {
            SingleSource<? extends T> source = sources[i];
            if (source == null) {
                parent.innerError(new NullPointerException("One of the sources is null"), i);
                return;
            } else {
                source.subscribe(parent.observers[i]);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.single.SingleZipArray$ZipCoordinator */
    /* loaded from: classes.dex */
    public static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {
        private static final long serialVersionUID = -5556924161382950569L;
        final SingleObserver<? super R> downstream;
        final ZipSingleObserver<T>[] observers;
        final Object[] values;
        final Function<? super Object[], ? extends R> zipper;

        public ZipCoordinator(SingleObserver<? super R> observer, int n, Function<? super Object[], ? extends R> zipper) {
            super(n);
            this.downstream = observer;
            this.zipper = zipper;
            ZipSingleObserver<T>[] o = new ZipSingleObserver[n];
            for (int i = 0; i < n; i++) {
                o[i] = new ZipSingleObserver<>(this, i);
            }
            this.observers = o;
            this.values = new Object[n];
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() <= 0;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (getAndSet(0) > 0) {
                for (ZipSingleObserver<T> zipSingleObserver : this.observers) {
                    zipSingleObserver.dispose();
                }
            }
        }

        void innerSuccess(T value, int index) {
            this.values[index] = value;
            if (decrementAndGet() == 0) {
                try {
                    this.downstream.onSuccess(ObjectHelper.requireNonNull(this.zipper.apply(this.values), "The zipper returned a null value"));
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    this.downstream.onError(ex);
                }
            }
        }

        void disposeExcept(int index) {
            ZipSingleObserver<T>[] observers = this.observers;
            int n = observers.length;
            for (int i = 0; i < index; i++) {
                observers[i].dispose();
            }
            for (int i2 = index + 1; i2 < n; i2++) {
                observers[i2].dispose();
            }
        }

        void innerError(Throwable ex, int index) {
            if (getAndSet(0) > 0) {
                disposeExcept(index);
                this.downstream.onError(ex);
                return;
            }
            RxJavaPlugins.onError(ex);
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleZipArray$ZipSingleObserver */
    /* loaded from: classes.dex */
    public static final class ZipSingleObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T> {
        private static final long serialVersionUID = 3323743579927613702L;
        final int index;
        final ZipCoordinator<T, ?> parent;

        ZipSingleObserver(ZipCoordinator<T, ?> parent, int index) {
            this.parent = parent;
            this.index = index;
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.parent.innerSuccess(value, this.index);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.parent.innerError(e, this.index);
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleZipArray$SingletonArrayFunc */
    /* loaded from: classes.dex */
    final class SingletonArrayFunc implements Function<T, R> {
        SingletonArrayFunc() {
            SingleZipArray.this = this$0;
        }

        /* JADX WARN: Type inference failed for: r1v1, types: [java.lang.Object[], java.lang.Object] */
        @Override // p005io.reactivex.functions.Function
        public R apply(T t) throws Exception {
            return (R) ObjectHelper.requireNonNull(SingleZipArray.this.zipper.apply(new Object[]{t}), "The zipper returned a null value");
        }
    }
}
