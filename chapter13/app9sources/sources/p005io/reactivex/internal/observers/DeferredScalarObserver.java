package p005io.reactivex.internal.observers;

import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.observers.DeferredScalarObserver */
/* loaded from: classes.dex */
public abstract class DeferredScalarObserver<T, R> extends DeferredScalarDisposable<R> implements Observer<T> {
    private static final long serialVersionUID = -266195175408988651L;
    protected Disposable upstream;

    public DeferredScalarObserver(Observer<? super R> observer) {
        super(observer);
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        if (DisposableHelper.validate(this.upstream, disposable)) {
            this.upstream = disposable;
            this.downstream.onSubscribe(this);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        this.value = null;
        error(th);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // p005io.reactivex.Observer
    public void onComplete() {
        Object obj = this.value;
        if (obj != null) {
            this.value = null;
            complete(obj);
            return;
        }
        complete();
    }

    @Override // p005io.reactivex.internal.observers.DeferredScalarDisposable, p005io.reactivex.disposables.Disposable
    public void dispose() {
        super.dispose();
        this.upstream.dispose();
    }
}
