package p005io.reactivex.internal.observers;

/* renamed from: io.reactivex.internal.observers.BlockingLastObserver */
/* loaded from: classes.dex */
public final class BlockingLastObserver<T> extends BlockingBaseObserver<T> {
    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        this.value = t;
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        this.value = null;
        this.error = th;
        countDown();
    }
}
