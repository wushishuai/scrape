package p005io.reactivex.internal.observers;

import java.util.concurrent.atomic.AtomicInteger;
import p005io.reactivex.internal.fuseable.QueueDisposable;

/* renamed from: io.reactivex.internal.observers.BasicIntQueueDisposable */
/* loaded from: classes.dex */
public abstract class BasicIntQueueDisposable<T> extends AtomicInteger implements QueueDisposable<T> {
    private static final long serialVersionUID = -1001730202384742097L;

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(T e) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called");
    }
}
