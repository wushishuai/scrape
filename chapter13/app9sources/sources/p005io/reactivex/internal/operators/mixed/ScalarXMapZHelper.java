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

    public static <T> boolean tryAsCompletable(Object obj, Function<? super T, ? extends CompletableSource> function, CompletableObserver completableObserver) {
        if (!(obj instanceof Callable)) {
            return false;
        }
        CompletableSource completableSource = null;
        try {
            Object obj2 = (Object) ((Callable) obj).call();
            if (obj2 != 0) {
                completableSource = (CompletableSource) ObjectHelper.requireNonNull(function.apply(obj2), "The mapper returned a null CompletableSource");
            }
            if (completableSource == null) {
                EmptyDisposable.complete(completableObserver);
            } else {
                completableSource.subscribe(completableObserver);
            }
            return true;
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, completableObserver);
            return true;
        }
    }

    public static <T, R> boolean tryAsMaybe(Object obj, Function<? super T, ? extends MaybeSource<? extends R>> function, Observer<? super R> observer) {
        if (!(obj instanceof Callable)) {
            return false;
        }
        MaybeSource maybeSource = null;
        try {
            Object obj2 = (Object) ((Callable) obj).call();
            if (obj2 != 0) {
                maybeSource = (MaybeSource) ObjectHelper.requireNonNull(function.apply(obj2), "The mapper returned a null MaybeSource");
            }
            if (maybeSource == null) {
                EmptyDisposable.complete(observer);
            } else {
                maybeSource.subscribe(MaybeToObservable.create(observer));
            }
            return true;
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, observer);
            return true;
        }
    }

    public static <T, R> boolean tryAsSingle(Object obj, Function<? super T, ? extends SingleSource<? extends R>> function, Observer<? super R> observer) {
        if (!(obj instanceof Callable)) {
            return false;
        }
        SingleSource singleSource = null;
        try {
            Object obj2 = (Object) ((Callable) obj).call();
            if (obj2 != 0) {
                singleSource = (SingleSource) ObjectHelper.requireNonNull(function.apply(obj2), "The mapper returned a null SingleSource");
            }
            if (singleSource == null) {
                EmptyDisposable.complete(observer);
            } else {
                singleSource.subscribe(SingleToObservable.create(observer));
            }
            return true;
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptyDisposable.error(th, observer);
            return true;
        }
    }
}
