package p005io.reactivex.internal.subscribers;

/* renamed from: io.reactivex.internal.subscribers.InnerQueuedSubscriberSupport */
/* loaded from: classes.dex */
public interface InnerQueuedSubscriberSupport<T> {
    void drain();

    void innerComplete(InnerQueuedSubscriber<T> innerQueuedSubscriber);

    void innerError(InnerQueuedSubscriber<T> innerQueuedSubscriber, Throwable th);

    void innerNext(InnerQueuedSubscriber<T> innerQueuedSubscriber, T t);
}
