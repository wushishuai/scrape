package p005io.reactivex.internal.operators.single;

import java.util.Arrays;
import java.util.NoSuchElementException;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.single.SingleMap;
import p005io.reactivex.internal.operators.single.SingleZipArray;

/* renamed from: io.reactivex.internal.operators.single.SingleZipIterable */
/* loaded from: classes.dex */
public final class SingleZipIterable<T, R> extends Single<R> {
    final Iterable<? extends SingleSource<? extends T>> sources;
    final Function<? super Object[], ? extends R> zipper;

    public SingleZipIterable(Iterable<? extends SingleSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {
        this.sources = sources;
        this.zipper = zipper;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super R> observer) {
        Throwable ex;
        SingleSource<? extends T>[] a = new SingleSource[8];
        int n = 0;
        try {
            for (SingleSource<? extends T> source : this.sources) {
                if (source == null) {
                    EmptyDisposable.error(new NullPointerException("One of the sources is null"), observer);
                    return;
                }
                if (n == a.length) {
                    a = (SingleSource[]) Arrays.copyOf(a, (n >> 2) + n);
                }
                int n2 = n + 1;
                try {
                    a[n] = source;
                    n = n2;
                } catch (Throwable th) {
                    ex = th;
                    Exceptions.throwIfFatal(ex);
                    EmptyDisposable.error(ex, observer);
                    return;
                }
            }
            if (n == 0) {
                EmptyDisposable.error(new NoSuchElementException(), observer);
            } else if (n == 1) {
                a[0].subscribe(new SingleMap.MapSingleObserver<>(observer, new SingletonArrayFunc()));
            } else {
                SingleZipArray.ZipCoordinator<T, R> parent = new SingleZipArray.ZipCoordinator<>(observer, n, this.zipper);
                observer.onSubscribe(parent);
                for (int i = 0; i < n && !parent.isDisposed(); i++) {
                    a[i].subscribe(parent.observers[i]);
                }
            }
        } catch (Throwable th2) {
            ex = th2;
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleZipIterable$SingletonArrayFunc */
    /* loaded from: classes.dex */
    final class SingletonArrayFunc implements Function<T, R> {
        SingletonArrayFunc() {
        }

        /* JADX WARN: Type inference failed for: r1v1, types: [java.lang.Object[], java.lang.Object] */
        @Override // p005io.reactivex.functions.Function
        public R apply(T t) throws Exception {
            return (R) ObjectHelper.requireNonNull(SingleZipIterable.this.zipper.apply(new Object[]{t}), "The zipper returned a null value");
        }
    }
}
