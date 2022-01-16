package p005io.reactivex.internal.observers;

import p005io.reactivex.internal.fuseable.QueueDisposable;

/* renamed from: io.reactivex.internal.observers.BasicQueueDisposable */
/* loaded from: classes.dex */
public abstract class BasicQueueDisposable<T> implements QueueDisposable<T> {
    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(T e) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called");
    }
}
