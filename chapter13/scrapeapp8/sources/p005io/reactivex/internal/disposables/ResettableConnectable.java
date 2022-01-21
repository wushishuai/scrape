package p005io.reactivex.internal.disposables;

import p005io.reactivex.annotations.Experimental;
import p005io.reactivex.disposables.Disposable;

@Experimental
/* renamed from: io.reactivex.internal.disposables.ResettableConnectable */
/* loaded from: classes.dex */
public interface ResettableConnectable {
    void resetIf(Disposable disposable);
}
