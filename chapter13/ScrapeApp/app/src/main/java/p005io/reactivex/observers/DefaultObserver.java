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

    @Override // p005io.reactivex.Observer
    public final void onSubscribe(@NonNull Disposable d) {
        if (EndConsumerHelper.validate(this.upstream, d, getClass())) {
            this.upstream = d;
            onStart();
        }
    }

    protected final void cancel() {
        Disposable upstream = this.upstream;
        this.upstream = DisposableHelper.DISPOSED;
        upstream.dispose();
    }

    protected void onStart() {
    }
}
