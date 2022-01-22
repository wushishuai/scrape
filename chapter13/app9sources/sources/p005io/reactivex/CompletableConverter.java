package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.CompletableConverter */
/* loaded from: classes.dex */
public interface CompletableConverter<R> {
    @NonNull
    R apply(@NonNull Completable completable);
}
