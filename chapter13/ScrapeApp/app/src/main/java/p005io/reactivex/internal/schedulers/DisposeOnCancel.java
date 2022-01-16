package p005io.reactivex.internal.schedulers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.schedulers.DisposeOnCancel */
/* loaded from: classes.dex */
final class DisposeOnCancel implements Future<Object> {
    final Disposable upstream;

    /* JADX INFO: Access modifiers changed from: package-private */
    public DisposeOnCancel(Disposable d) {
        this.upstream = d;
    }

    @Override // java.util.concurrent.Future
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.upstream.dispose();
        return false;
    }

    @Override // java.util.concurrent.Future
    public boolean isCancelled() {
        return false;
    }

    @Override // java.util.concurrent.Future
    public boolean isDone() {
        return false;
    }

    @Override // java.util.concurrent.Future
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override // java.util.concurrent.Future
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
