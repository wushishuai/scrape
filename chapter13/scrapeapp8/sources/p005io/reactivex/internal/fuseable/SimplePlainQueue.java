package p005io.reactivex.internal.fuseable;

import p005io.reactivex.annotations.Nullable;

/* renamed from: io.reactivex.internal.fuseable.SimplePlainQueue */
/* loaded from: classes.dex */
public interface SimplePlainQueue<T> extends SimpleQueue<T> {
    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    T poll();
}
