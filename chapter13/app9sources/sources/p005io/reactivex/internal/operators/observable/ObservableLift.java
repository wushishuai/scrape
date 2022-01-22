package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.ObservableOperator;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableLift */
/* loaded from: classes.dex */
public final class ObservableLift<R, T> extends AbstractObservableWithUpstream<T, R> {
    final ObservableOperator<? extends R, ? super T> operator;

    public ObservableLift(ObservableSource<T> observableSource, ObservableOperator<? extends R, ? super T> observableOperator) {
        super(observableSource);
        this.operator = observableOperator;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super R> observer) {
        try {
            Observer<? super Object> apply = this.operator.apply(observer);
            this.source.subscribe((Observer) ObjectHelper.requireNonNull(apply, "Operator " + this.operator + " returned a null Observer"));
        } catch (NullPointerException e) {
            throw e;
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            RxJavaPlugins.onError(th);
            NullPointerException nullPointerException = new NullPointerException("Actually not, but can't throw other exceptions due to RS");
            nullPointerException.initCause(th);
            throw nullPointerException;
        }
    }
}
