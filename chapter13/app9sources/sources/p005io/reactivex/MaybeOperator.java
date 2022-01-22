package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.MaybeOperator */
/* loaded from: classes.dex */
public interface MaybeOperator<Downstream, Upstream> {
    @NonNull
    MaybeObserver<? super Upstream> apply(@NonNull MaybeObserver<? super Downstream> maybeObserver) throws Exception;
}
