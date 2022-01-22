package p005io.reactivex.observers;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.util.EndConsumerHelper;

/* renamed from: io.reactivex.observers.DisposableSingleObserver */
/* loaded from: classes.dex */
public abstract class DisposableSingleObserver<T> implements SingleObserver<T>, Disposable {
    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    protected void onStart() {
    }

    @Override // p005io.reactivex.SingleObserver
    public final void onSubscribe(@NonNull Disposable disposable) {
        if (EndConsumerHelper.setOnce(this.upstream, disposable, getClass())) {
            onStart();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return this.upstream.get() == DisposableHelper.DISPOSED;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        DisposableHelper.dispose(this.upstream);
    }
}
