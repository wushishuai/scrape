package p005io.reactivex.functions;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.functions.Function */
/* loaded from: classes.dex */
public interface Function<T, R> {
    R apply(@NonNull T t) throws Exception;
}
