package p005io.reactivex.internal.operators.mixed;

import java.util.concurrent.Callable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.Observer;
import p005io.reactivex.SingleSource;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.maybe.MaybeToObservable;
import p005io.reactivex.internal.operators.single.SingleToObservable;

/* renamed from: io.reactivex.internal.operators.mixed.ScalarXMapZHelper */
/* loaded from: classes.dex */
final class ScalarXMapZHelper {
    private ScalarXMapZHelper() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> boolean tryAsCompletable(Object source, Function<? super T, ? extends CompletableSource> mapper, CompletableObserver observer) {
        if (!(source instanceof Callable)) {
            return false;
        }
        CompletableSource cs = null;
        try {
            Object obj = (Object) ((Callable) source).call();
            if (obj != 0) {
                cs = (CompletableSource) ObjectHelper.requireNonNull(mapper.apply(obj), "The mapper returned a null CompletableSource");
            }
            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(observer);
            }
            return true;
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return true;
        }
    }

    public static <T, R> boolean tryAsMaybe(Object source, Function<? super T, ? extends MaybeSource<? extends R>> mapper, Observer<? super R> observer) {
        if (!(source instanceof Callable)) {
            return false;
        }
        MaybeSource<? extends R> cs = null;
        try {
            Object obj = (Object) ((Callable) source).call();
            if (obj != 0) {
                cs = (MaybeSource) ObjectHelper.requireNonNull(mapper.apply(obj), "The mapper returned a null MaybeSource");
            }
            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(MaybeToObservable.create(observer));
            }
            return true;
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return true;
        }
    }

    public static <T, R> boolean tryAsSingle(Object source, Function<? super T, ? extends SingleSource<? extends R>> mapper, Observer<? super R> observer) {
        if (!(source instanceof Callable)) {
            return false;
        }
        SingleSource<? extends R> cs = null;
        try {
            Object obj = (Object) ((Callable) source).call();
            if (obj != 0) {
                cs = (SingleSource) ObjectHelper.requireNonNull(mapper.apply(obj), "The mapper returned a null SingleSource");
            }
            if (cs == null) {
                EmptyDisposable.complete(observer);
            } else {
                cs.subscribe(SingleToObservable.create(observer));
            }
            return true;
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
            return true;
        }
    }
}
