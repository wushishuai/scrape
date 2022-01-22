package p005io.reactivex.internal.observers;

/* renamed from: io.reactivex.internal.observers.BlockingFirstObserver */
/* loaded from: classes.dex */
public final class BlockingFirstObserver<T> extends BlockingBaseObserver<T> {
    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        if (this.value == null) {
            this.value = t;
            this.upstream.dispose();
            countDown();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        if (this.value == null) {
            this.error = th;
        }
        countDown();
    }
}
