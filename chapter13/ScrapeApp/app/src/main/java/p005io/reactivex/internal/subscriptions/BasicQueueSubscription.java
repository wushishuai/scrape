package p005io.reactivex.internal.subscriptions;

import java.util.concurrent.atomic.AtomicLong;
import p005io.reactivex.internal.fuseable.QueueSubscription;

/* renamed from: io.reactivex.internal.subscriptions.BasicQueueSubscription */
/* loaded from: classes.dex */
public abstract class BasicQueueSubscription<T> extends AtomicLong implements QueueSubscription<T> {
    private static final long serialVersionUID = -6671519529404341862L;

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(T e) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public final boolean offer(T v1, T v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
