package p006me.goldze.mvvmhabit.bus;

import p005io.reactivex.observers.DisposableObserver;

/* renamed from: me.goldze.mvvmhabit.bus.RxBusSubscriber */
/* loaded from: classes.dex */
public abstract class RxBusSubscriber<T> extends DisposableObserver<T> {
    @Override // p005io.reactivex.Observer
    public void onComplete() {
    }

    protected abstract void onEvent(T t);

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        try {
            onEvent(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        th.printStackTrace();
    }
}
