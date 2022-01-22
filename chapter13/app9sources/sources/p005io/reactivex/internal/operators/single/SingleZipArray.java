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

    public SingleZipArray(SingleSource<? extends T>[] singleSourceArr, Function<? super Object[], ? extends R> function) {
        this.sources = singleSourceArr;
        this.zipper = function;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super R> singleObserver) {
        SingleSource<? extends T>[] singleSourceArr = this.sources;
        int length = singleSourceArr.length;
        if (length == 1) {
            singleSourceArr[0].subscribe(new SingleMap.MapSingleObserver(singleObserver, new SingletonArrayFunc()));
            return;
        }
        ZipCoordinator zipCoordinator = new ZipCoordinator(singleObserver, length, this.zipper);
        singleObserver.onSubscribe(zipCoordinator);
        for (int i = 0; i < length && !zipCoordinator.isDisposed(); i++) {
            SingleSource<? extends T> singleSource = singleSourceArr[i];
            if (singleSource == null) {
                zipCoordinator.innerError(new NullPointerException("One of the sources is null"), i);
                return;
            } else {
                singleSource.subscribe(zipCoordinator.observers[i]);
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

        public ZipCoordinator(SingleObserver<? super R> singleObserver, int i, Function<? super Object[], ? extends R> function) {
            super(i);
            this.downstream = singleObserver;
            this.zipper = function;
            ZipSingleObserver<T>[] zipSingleObserverArr = new ZipSingleObserver[i];
            for (int i2 = 0; i2 < i; i2++) {
                zipSingleObserverArr[i2] = new ZipSingleObserver<>(this, i2);
            }
            this.observers = zipSingleObserverArr;
            this.values = new Object[i];
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

        void innerSuccess(T t, int i) {
            this.values[i] = t;
            if (decrementAndGet() == 0) {
                try {
                    this.downstream.onSuccess(ObjectHelper.requireNonNull(this.zipper.apply(this.values), "The zipper returned a null value"));
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.downstream.onError(th);
                }
            }
        }

        void disposeExcept(int i) {
            ZipSingleObserver<T>[] zipSingleObserverArr = this.observers;
            int length = zipSingleObserverArr.length;
            for (int i2 = 0; i2 < i; i2++) {
                zipSingleObserverArr[i2].dispose();
            }
            while (true) {
                i++;
                if (i < length) {
                    zipSingleObserverArr[i].dispose();
                } else {
                    return;
                }
            }
        }

        void innerError(Throwable th, int i) {
            if (getAndSet(0) > 0) {
                disposeExcept(i);
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleZipArray$ZipSingleObserver */
    /* loaded from: classes.dex */
    public static final class ZipSingleObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T> {
        private static final long serialVersionUID = 3323743579927613702L;
        final int index;
        final ZipCoordinator<T, ?> parent;

        ZipSingleObserver(ZipCoordinator<T, ?> zipCoordinator, int i) {
            this.parent = zipCoordinator;
            this.index = i;
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.parent.innerSuccess(t, this.index);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.parent.innerError(th, this.index);
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleZipArray$SingletonArrayFunc */
    /* loaded from: classes.dex */
    final class SingletonArrayFunc implements Function<T, R> {
        SingletonArrayFunc() {
            SingleZipArray.this = r1;
        }

        /* JADX WARN: Type inference failed for: r1v1, types: [java.lang.Object[], java.lang.Object] */
        @Override // p005io.reactivex.functions.Function
        public R apply(T t) throws Exception {
            return (R) ObjectHelper.requireNonNull(SingleZipArray.this.zipper.apply(new Object[]{t}), "The zipper returned a null value");
        }
    }
}
