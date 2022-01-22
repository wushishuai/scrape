package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;

/* renamed from: io.reactivex.internal.operators.single.SingleFromUnsafeSource */
/* loaded from: classes.dex */
public final class SingleFromUnsafeSource<T> extends Single<T> {
    final SingleSource<T> source;

    public SingleFromUnsafeSource(SingleSource<T> singleSource) {
        this.source = singleSource;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe(singleObserver);
    }
}
