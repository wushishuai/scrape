package p005io.reactivex.parallel;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.parallel.ParallelFlowableConverter */
/* loaded from: classes.dex */
public interface ParallelFlowableConverter<T, R> {
    @NonNull
    R apply(@NonNull ParallelFlowable<T> parallelFlowable);
}
