package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposables;

/* renamed from: io.reactivex.internal.operators.single.SingleJust */
/* loaded from: classes.dex */
public final class SingleJust<T> extends Single<T> {
    final T value;

    public SingleJust(T t) {
        this.value = t;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        singleObserver.onSubscribe(Disposables.disposed());
        singleObserver.onSuccess((T) this.value);
    }
}
