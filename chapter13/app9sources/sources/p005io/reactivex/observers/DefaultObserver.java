package p005io.reactivex.observers;

import p005io.reactivex.Observer;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.util.EndConsumerHelper;

/* renamed from: io.reactivex.observers.DefaultObserver */
/* loaded from: classes.dex */
public abstract class DefaultObserver<T> implements Observer<T> {
    private Disposable upstream;

    protected void onStart() {
    }

    @Override // p005io.reactivex.Observer
    public final void onSubscribe(@NonNull Disposable disposable) {
        if (EndConsumerHelper.validate(this.upstream, disposable, getClass())) {
            this.upstream = disposable;
            onStart();
        }
    }

    protected final void cancel() {
        Disposable disposable = this.upstream;
        this.upstream = DisposableHelper.DISPOSED;
        disposable.dispose();
    }
}
