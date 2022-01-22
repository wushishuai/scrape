package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.internal.disposables.EmptyDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableNever */
/* loaded from: classes.dex */
public final class ObservableNever extends Observable<Object> {
    public static final Observable<Object> INSTANCE = new ObservableNever();

    private ObservableNever() {
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super Object> observer) {
        observer.onSubscribe(EmptyDisposable.NEVER);
    }
}
