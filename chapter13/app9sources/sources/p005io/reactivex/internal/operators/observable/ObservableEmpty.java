package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.fuseable.ScalarCallable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableEmpty */
/* loaded from: classes.dex */
public final class ObservableEmpty extends Observable<Object> implements ScalarCallable<Object> {
    public static final Observable<Object> INSTANCE = new ObservableEmpty();

    @Override // p005io.reactivex.internal.fuseable.ScalarCallable, java.util.concurrent.Callable
    public Object call() {
        return null;
    }

    private ObservableEmpty() {
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Object> observer) {
        EmptyDisposable.complete(observer);
    }
}
