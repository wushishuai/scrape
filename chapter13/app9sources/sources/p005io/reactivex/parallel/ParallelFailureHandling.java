package p005io.reactivex.parallel;

import p005io.reactivex.functions.BiFunction;

/* renamed from: io.reactivex.parallel.ParallelFailureHandling */
/* loaded from: classes.dex */
public enum ParallelFailureHandling implements BiFunction<Long, Throwable, ParallelFailureHandling> {
    STOP,
    ERROR,
    SKIP,
    RETRY;

    public ParallelFailureHandling apply(Long l, Throwable th) {
        return this;
    }
}
