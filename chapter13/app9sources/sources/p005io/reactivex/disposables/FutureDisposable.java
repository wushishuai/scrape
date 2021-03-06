package p005io.reactivex.disposables;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/* renamed from: io.reactivex.disposables.FutureDisposable */
/* loaded from: classes.dex */
final class FutureDisposable extends AtomicReference<Future<?>> implements Disposable {
    private static final long serialVersionUID = 6545242830671168775L;
    private final boolean allowInterrupt;

    /* JADX INFO: Access modifiers changed from: package-private */
    public FutureDisposable(Future<?> future, boolean z) {
        super(future);
        this.allowInterrupt = z;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        Future<?> future = get();
        return future == null || future.isDone();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        Future<?> andSet = getAndSet(null);
        if (andSet != null) {
            andSet.cancel(this.allowInterrupt);
        }
    }
}
