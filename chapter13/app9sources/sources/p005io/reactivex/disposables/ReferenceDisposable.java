package p005io.reactivex.disposables;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.internal.functions.ObjectHelper;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: io.reactivex.disposables.ReferenceDisposable */
/* loaded from: classes.dex */
public abstract class ReferenceDisposable<T> extends AtomicReference<T> implements Disposable {
    private static final long serialVersionUID = 6537757548749041217L;

    protected abstract void onDisposed(@NonNull T t);

    public ReferenceDisposable(T t) {
        super(ObjectHelper.requireNonNull(t, "value is null"));
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        T andSet;
        if (get() != null && (andSet = getAndSet(null)) != null) {
            onDisposed(andSet);
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return get() == null;
    }
}
