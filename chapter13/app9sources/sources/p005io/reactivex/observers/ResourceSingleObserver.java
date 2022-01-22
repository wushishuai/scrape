package p005io.reactivex.observers;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.ListCompositeDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.EndConsumerHelper;

/* renamed from: io.reactivex.observers.ResourceSingleObserver */
/* loaded from: classes.dex */
public abstract class ResourceSingleObserver<T> implements SingleObserver<T>, Disposable {
    private final AtomicReference<Disposable> upstream = new AtomicReference<>();
    private final ListCompositeDisposable resources = new ListCompositeDisposable();

    protected void onStart() {
    }

    public final void add(@NonNull Disposable disposable) {
        ObjectHelper.requireNonNull(disposable, "resource is null");
        this.resources.add(disposable);
    }

    @Override // p005io.reactivex.SingleObserver
    public final void onSubscribe(@NonNull Disposable disposable) {
        if (EndConsumerHelper.setOnce(this.upstream, disposable, getClass())) {
            onStart();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        if (DisposableHelper.dispose(this.upstream)) {
            this.resources.dispose();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(this.upstream.get());
    }
}
