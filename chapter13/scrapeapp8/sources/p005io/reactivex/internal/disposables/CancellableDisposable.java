package p005io.reactivex.internal.disposables;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Cancellable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.disposables.CancellableDisposable */
/* loaded from: classes.dex */
public final class CancellableDisposable extends AtomicReference<Cancellable> implements Disposable {
    private static final long serialVersionUID = 5718521705281392066L;

    public CancellableDisposable(Cancellable cancellable) {
        super(cancellable);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return get() == null;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        Cancellable c;
        if (get() != null && (c = getAndSet(null)) != null) {
            try {
                c.cancel();
            } catch (Exception ex) {
                Exceptions.throwIfFatal(ex);
                RxJavaPlugins.onError(ex);
            }
        }
    }
}
