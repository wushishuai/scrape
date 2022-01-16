package p005io.reactivex.internal.observers;

import java.util.concurrent.CountDownLatch;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.util.BlockingHelper;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.internal.observers.BlockingBaseObserver */
/* loaded from: classes.dex */
public abstract class BlockingBaseObserver<T> extends CountDownLatch implements Observer<T>, Disposable {
    volatile boolean cancelled;
    Throwable error;
    Disposable upstream;
    T value;

    public BlockingBaseObserver() {
        super(1);
    }

    @Override // p005io.reactivex.Observer
    public final void onSubscribe(Disposable d) {
        this.upstream = d;
        if (this.cancelled) {
            d.dispose();
        }
    }

    @Override // p005io.reactivex.Observer
    public final void onComplete() {
        countDown();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        this.cancelled = true;
        Disposable d = this.upstream;
        if (d != null) {
            d.dispose();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return this.cancelled;
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.Throwable: [D('ex' java.lang.InterruptedException), D('e' java.lang.Throwable)] */
    public final T blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable e = this.error;
        if (e == null) {
            return this.value;
        }
        throw ExceptionHelper.wrapOrThrow(e);
    }
}
