package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.FlowableConverter */
/* loaded from: classes.dex */
public interface FlowableConverter<T, R> {
    @NonNull
    R apply(@NonNull Flowable<T> flowable);
}
