package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;

/* renamed from: io.reactivex.internal.operators.observable.ObservableFromUnsafeSource */
/* loaded from: classes.dex */
public final class ObservableFromUnsafeSource<T> extends Observable<T> {
    final ObservableSource<T> source;

    public ObservableFromUnsafeSource(ObservableSource<T> observableSource) {
        this.source = observableSource;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(observer);
    }
}
