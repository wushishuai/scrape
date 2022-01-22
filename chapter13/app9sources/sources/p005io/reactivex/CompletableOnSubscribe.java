package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.CompletableOnSubscribe */
/* loaded from: classes.dex */
public interface CompletableOnSubscribe {
    void subscribe(@NonNull CompletableEmitter completableEmitter) throws Exception;
}
