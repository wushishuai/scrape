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
    public final void onSubscribe(Disposable disposable) {
        this.upstream = disposable;
        if (this.cancelled) {
            disposable.dispose();
        }
    }

    @Override // p005io.reactivex.Observer
    public final void onComplete() {
        countDown();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        this.cancelled = true;
        Disposable disposable = this.upstream;
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return this.cancelled;
    }

    public final T blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException e) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(e);
            }
        }
        Throwable th = this.error;
        if (th == null) {
            return this.value;
        }
        throw ExceptionHelper.wrapOrThrow(th);
    }
}
