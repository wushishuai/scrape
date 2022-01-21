package p005io.reactivex.disposables;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.disposables.SerialDisposable */
/* loaded from: classes.dex */
public final class SerialDisposable implements Disposable {
    final AtomicReference<Disposable> resource;

    public SerialDisposable() {
        this.resource = new AtomicReference<>();
    }

    public SerialDisposable(@Nullable Disposable initialDisposable) {
        this.resource = new AtomicReference<>(initialDisposable);
    }

    public boolean set(@Nullable Disposable next) {
        return DisposableHelper.set(this.resource, next);
    }

    public boolean replace(@Nullable Disposable next) {
        return DisposableHelper.replace(this.resource, next);
    }

    @Nullable
    public Disposable get() {
        Disposable d = this.resource.get();
        if (d == DisposableHelper.DISPOSED) {
            return Disposables.disposed();
        }
        return d;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        DisposableHelper.dispose(this.resource);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(this.resource.get());
    }
}
