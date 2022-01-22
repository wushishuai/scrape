package p005io.reactivex;

import org.reactivestreams.Subscriber;
import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.FlowableOperator */
/* loaded from: classes.dex */
public interface FlowableOperator<Downstream, Upstream> {
    @NonNull
    Subscriber<? super Upstream> apply(@NonNull Subscriber<? super Downstream> subscriber) throws Exception;
}
