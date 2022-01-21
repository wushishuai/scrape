package p005io.reactivex.parallel;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.parallel.ParallelTransformer */
/* loaded from: classes.dex */
public interface ParallelTransformer<Upstream, Downstream> {
    @NonNull
    ParallelFlowable<Downstream> apply(@NonNull ParallelFlowable<Upstream> parallelFlowable);
}
