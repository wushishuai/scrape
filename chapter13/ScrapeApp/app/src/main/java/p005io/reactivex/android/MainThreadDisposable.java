package p005io.reactivex.android;

import android.os.Looper;
import java.util.concurrent.atomic.AtomicBoolean;
import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.android.MainThreadDisposable */
/* loaded from: classes.dex */
public abstract class MainThreadDisposable implements Disposable {
    private final AtomicBoolean unsubscribed = new AtomicBoolean();

    protected abstract void onDispose();

    public static void verifyMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().getName());
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return this.unsubscribed.get();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        if (!this.unsubscribed.compareAndSet(false, true)) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            onDispose();
        } else {
            AndroidSchedulers.mainThread().scheduleDirect(new Runnable() { // from class: io.reactivex.android.MainThreadDisposable.1
                @Override // java.lang.Runnable
                public void run() {
                    MainThreadDisposable.this.onDispose();
                }
            });
        }
    }
}
