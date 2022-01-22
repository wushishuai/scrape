package p005io.reactivex.internal.observers;

/* renamed from: io.reactivex.internal.observers.InnerQueuedObserverSupport */
/* loaded from: classes.dex */
public interface InnerQueuedObserverSupport<T> {
    void drain();

    void innerComplete(InnerQueuedObserver<T> innerQueuedObserver);

    void innerError(InnerQueuedObserver<T> innerQueuedObserver, Throwable th);

    void innerNext(InnerQueuedObserver<T> innerQueuedObserver, T t);
}
