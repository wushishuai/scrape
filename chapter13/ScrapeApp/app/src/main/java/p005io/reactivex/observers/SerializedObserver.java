package p005io.reactivex.observers;

import p005io.reactivex.Observer;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.observers.SerializedObserver */
/* loaded from: classes.dex */
public final class SerializedObserver<T> implements Observer<T>, Disposable {
    static final int QUEUE_LINK_SIZE = 4;
    final boolean delayError;
    volatile boolean done;
    final Observer<? super T> downstream;
    boolean emitting;
    AppendOnlyLinkedArrayList<Object> queue;
    Disposable upstream;

    public SerializedObserver(@NonNull Observer<? super T> downstream) {
        this(downstream, false);
    }

    public SerializedObserver(@NonNull Observer<? super T> actual, boolean delayError) {
        this.downstream = actual;
        this.delayError = delayError;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(@NonNull Disposable d) {
        if (DisposableHelper.validate(this.upstream, d)) {
            this.upstream = d;
            this.downstream.onSubscribe(this);
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        this.upstream.dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.upstream.isDisposed();
    }

    @Override // p005io.reactivex.Observer
    public void onNext(@NonNull T t) {
        if (!this.done) {
            if (t == null) {
                this.upstream.dispose();
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            synchronized (this) {
                if (!this.done) {
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> q = this.queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = q;
                        }
                        q.add(NotificationLite.next(t));
                        return;
                    }
                    this.emitting = true;
                    this.downstream.onNext(t);
                    emitLoop();
                }
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(@NonNull Throwable t) {
        boolean reportError;
        if (this.done) {
            RxJavaPlugins.onError(t);
            return;
        }
        synchronized (this) {
            if (this.done) {
                reportError = true;
            } else if (this.emitting) {
                this.done = true;
                AppendOnlyLinkedArrayList<Object> q = this.queue;
                if (q == null) {
                    q = new AppendOnlyLinkedArrayList<>(4);
                    this.queue = q;
                }
                Object err = NotificationLite.error(t);
                if (this.delayError) {
                    q.add(err);
                } else {
                    q.setFirst(err);
                }
                return;
            } else {
                this.done = true;
                this.emitting = true;
                reportError = false;
            }
            if (reportError) {
                RxJavaPlugins.onError(t);
            } else {
                this.downstream.onError(t);
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.done) {
            synchronized (this) {
                if (!this.done) {
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> q = this.queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = q;
                        }
                        q.add(NotificationLite.complete());
                        return;
                    }
                    this.done = true;
                    this.emitting = true;
                    this.downstream.onComplete();
                }
            }
        }
    }

    void emitLoop() {
        AppendOnlyLinkedArrayList<Object> q;
        do {
            synchronized (this) {
                q = this.queue;
                if (q == null) {
                    this.emitting = false;
                    return;
                }
                this.queue = null;
            }
        } while (!q.accept((Observer<? super T>) this.downstream));
    }
}
