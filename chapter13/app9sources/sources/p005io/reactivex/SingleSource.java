package p005io.reactivex;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.SingleSource */
/* loaded from: classes.dex */
public interface SingleSource<T> {
    void subscribe(@NonNull SingleObserver<? super T> singleObserver);
}
