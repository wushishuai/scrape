package p005io.reactivex.internal.observers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.util.BlockingHelper;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.internal.observers.BlockingMultiObserver */
/* loaded from: classes.dex */
public final class BlockingMultiObserver<T> extends CountDownLatch implements SingleObserver<T>, CompletableObserver, MaybeObserver<T> {
    volatile boolean cancelled;
    Throwable error;
    Disposable upstream;
    T value;

    public BlockingMultiObserver() {
        super(1);
    }

    void dispose() {
        this.cancelled = true;
        Disposable d = this.upstream;
        if (d != null) {
            d.dispose();
        }
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSubscribe(Disposable d) {
        this.upstream = d;
        if (this.cancelled) {
            d.dispose();
        }
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSuccess(T value) {
        this.value = value;
        countDown();
    }

    @Override // p005io.reactivex.SingleObserver
    public void onError(Throwable e) {
        this.error = e;
        countDown();
    }

    @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
    public void onComplete() {
        countDown();
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.Throwable: [D('ex' java.lang.InterruptedException), D('ex' java.lang.Throwable)] */
    public T blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable ex2 = this.error;
        if (ex2 == null) {
            return this.value;
        }
        throw ExceptionHelper.wrapOrThrow(ex2);
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.Throwable: [D('ex' java.lang.InterruptedException), D('ex' java.lang.Throwable)] */
    public T blockingGet(T defaultValue) {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable ex2 = this.error;
        if (ex2 == null) {
            T v = this.value;
            return v != null ? v : defaultValue;
        }
        throw ExceptionHelper.wrapOrThrow(ex2);
    }

    public Throwable blockingGetError() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                return ex;
            }
        }
        return this.error;
    }

    public Throwable blockingGetError(long timeout, TimeUnit unit) {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                if (!await(timeout, unit)) {
                    dispose();
                    throw ExceptionHelper.wrapOrThrow(new TimeoutException(ExceptionHelper.timeoutMessage(timeout, unit)));
                }
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        return this.error;
    }

    /* JADX INFO: Multiple debug info for r0v4 java.lang.Throwable: [D('ex' java.lang.InterruptedException), D('ex' java.lang.Throwable)] */
    public boolean blockingAwait(long timeout, TimeUnit unit) {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                if (!await(timeout, unit)) {
                    dispose();
                    return false;
                }
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }
        Throwable ex2 = this.error;
        if (ex2 == null) {
            return true;
        }
        throw ExceptionHelper.wrapOrThrow(ex2);
    }
}
