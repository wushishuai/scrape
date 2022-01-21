package p005io.reactivex.disposables;

import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.disposables.RunnableDisposable */
/* loaded from: classes.dex */
final class RunnableDisposable extends ReferenceDisposable<Runnable> {
    private static final long serialVersionUID = -8219729196779211169L;

    /* JADX INFO: Access modifiers changed from: package-private */
    public RunnableDisposable(Runnable value) {
        super(value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDisposed(@NonNull Runnable value) {
        value.run();
    }

    @Override // java.util.concurrent.atomic.AtomicReference, java.lang.Object
    public String toString() {
        return "RunnableDisposable(disposed=" + isDisposed() + ", " + get() + ")";
    }
}
