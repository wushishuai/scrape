package p005io.reactivex.internal.fuseable;

import java.util.concurrent.Callable;

/* renamed from: io.reactivex.internal.fuseable.ScalarCallable */
/* loaded from: classes.dex */
public interface ScalarCallable<T> extends Callable<T> {
    @Override // java.util.concurrent.Callable
    T call();
}
