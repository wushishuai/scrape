package p005io.reactivex.internal.fuseable;

import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;

/* renamed from: io.reactivex.internal.fuseable.SimpleQueue */
/* loaded from: classes.dex */
public interface SimpleQueue<T> {
    void clear();

    boolean isEmpty();

    boolean offer(@NonNull T t);

    boolean offer(@NonNull T t, @NonNull T t2);

    @Nullable
    T poll() throws Exception;
}
