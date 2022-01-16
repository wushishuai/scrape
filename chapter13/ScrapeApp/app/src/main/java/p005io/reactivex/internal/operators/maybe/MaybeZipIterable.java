package p005io.reactivex.internal.operators.maybe;

import java.util.Arrays;
import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.maybe.MaybeMap;
import p005io.reactivex.internal.operators.maybe.MaybeZipArray;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeZipIterable */
/* loaded from: classes.dex */
public final class MaybeZipIterable<T, R> extends Maybe<R> {
    final Iterable<? extends MaybeSource<? extends T>> sources;
    final Function<? super Object[], ? extends R> zipper;

    public MaybeZipIterable(Iterable<? extends MaybeSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {
        this.sources = sources;
        this.zipper = zipper;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super R> observer) {
        Throwable ex;
        MaybeSource<? extends T>[] a = new MaybeSource[8];
        int n = 0;
        try {
            for (MaybeSource<? extends T> source : this.sources) {
                if (source == null) {
                    EmptyDisposable.error(new NullPointerException("One of the sources is null"), observer);
                    return;
                }
                if (n == a.length) {
                    a = (MaybeSource[]) Arrays.copyOf(a, (n >> 2) + n);
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
                EmptyDisposable.complete(observer);
            } else if (n == 1) {
                a[0].subscribe(new MaybeMap.MapMaybeObserver<>(observer, new SingletonArrayFunc()));
            } else {
                MaybeZipArray.ZipCoordinator<T, R> parent = new MaybeZipArray.ZipCoordinator<>(observer, n, this.zipper);
                observer.onSubscribe(parent);
                for (int i = 0; i < n && !parent.isDisposed(); i++) {
                    a[i].subscribe(parent.observers[i]);
                }
            }
        } catch (Throwable th2) {
            ex = th2;
        }
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeZipIterable$SingletonArrayFunc */
    /* loaded from: classes.dex */
    final class SingletonArrayFunc implements Function<T, R> {
        SingletonArrayFunc() {
        }

        /* JADX WARN: Type inference failed for: r1v1, types: [java.lang.Object[], java.lang.Object] */
        @Override // p005io.reactivex.functions.Function
        public R apply(T t) throws Exception {
            return (R) ObjectHelper.requireNonNull(MaybeZipIterable.this.zipper.apply(new Object[]{t}), "The zipper returned a null value");
        }
    }
}
