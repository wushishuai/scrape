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

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return true;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public Object poll() throws Exception {
        return null;
    }

    @Override // p005io.reactivex.internal.fuseable.QueueFuseable
    public int requestFusion(int i) {
        return i & 2;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this == INSTANCE;
    }

    public static void complete(Observer<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onComplete();
    }

    public static void complete(MaybeObserver<?> maybeObserver) {
        maybeObserver.onSubscribe(INSTANCE);
        maybeObserver.onComplete();
    }

    public static void error(Throwable th, Observer<?> observer) {
        observer.onSubscribe(INSTANCE);
        observer.onError(th);
    }

    public static void complete(CompletableObserver completableObserver) {
        completableObserver.onSubscribe(INSTANCE);
        completableObserver.onComplete();
    }

    public static void error(Throwable th, CompletableObserver completableObserver) {
        completableObserver.onSubscribe(INSTANCE);
        completableObserver.onError(th);
    }

    public static void error(Throwable th, SingleObserver<?> singleObserver) {
        singleObserver.onSubscribe(INSTANCE);
        singleObserver.onError(th);
    }

    public static void error(Throwable th, MaybeObserver<?> maybeObserver) {
        maybeObserver.onSubscribe(INSTANCE);
        maybeObserver.onError(th);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object obj) {
        throw new UnsupportedOperationException("Should not be called!");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(Object obj, Object obj2) {
        throw new UnsupportedOperationException("Should not be called!");
    }
}
