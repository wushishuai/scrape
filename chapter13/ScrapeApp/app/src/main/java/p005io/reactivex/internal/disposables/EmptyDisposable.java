package p005io.reactivex.internal.disposables;

import p005io.reactivex.CompletableObserver;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.Observer;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.QueueDisposable;

/* renamed from: io.reactivex.internal.disposables.EmptyDisposable */
/* loaded from: classes.dex */
public enum EmptyDisposable implements QueueDisposable<Object> {
    INSTANCE,
    NEVER;

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this == INSTANCE;
    }

    public static void complete(Observer<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    public static void complete(MaybeObserver<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    public static void error(Throwable e, Observer<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    public static void complete(CompletableObserver observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    public static void error(Throwable e, CompletableObserver observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    public static void error(Throwable e, SingleObserver<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    public static void error(Throwable e, MaybeObserver<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(e);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object value) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object v1, Object v2) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public Object poll() throws Exception {
        return null;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return true;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
    }

    @Override // p005io.reactivex.internal.fuseable.QueueFuseable
    public int requestFusion(int mode) {
        return mode & 2;
    }
}
