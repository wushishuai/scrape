package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.SingleTransformer */
/* loaded from: classes.dex */
public interface SingleTransformer<Upstream, Downstream> {
    @NonNull
    SingleSource<Downstream> apply(@NonNull Single<Upstream> single);
}
