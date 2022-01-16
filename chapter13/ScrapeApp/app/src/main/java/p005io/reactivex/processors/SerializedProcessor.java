package p005io.reactivex.processors;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.processors.SerializedProcessor */
/* loaded from: classes.dex */
final class SerializedProcessor<T> extends FlowableProcessor<T> {
    final FlowableProcessor<T> actual;
    volatile boolean done;
    boolean emitting;
    AppendOnlyLinkedArrayList<Object> queue;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SerializedProcessor(FlowableProcessor<T> actual) {
        this.actual = actual;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        this.actual.subscribe(s);
    }

    @Override // org.reactivestreams.Subscriber
    public void onSubscribe(Subscription s) {
        boolean cancel;
        if (!this.done) {
            synchronized (this) {
                if (this.done) {
                    cancel = true;
                } else if (this.emitting) {
                    AppendOnlyLinkedArrayList<Object> q = this.queue;
                    if (q == null) {
                        q = new AppendOnlyLinkedArrayList<>(4);
                        this.queue = q;
                    }
                    q.add(NotificationLite.subscription(s));
                    return;
                } else {
                    this.emitting = true;
                    cancel = false;
                }
            }
        } else {
            cancel = true;
        }
        if (cancel) {
            s.cancel();
            return;
        }
        this.actual.onSubscribe(s);
        emitLoop();
    }

    @Override // org.reactivestreams.Subscriber
    public void onNext(T t) {
        if (!this.done) {
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
                    this.actual.onNext(t);
                    emitLoop();
                }
            }
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable t) {
        boolean reportError;
        if (this.done) {
            RxJavaPlugins.onError(t);
            return;
        }
        synchronized (this) {
            if (this.done) {
                reportError = true;
            } else {
                this.done = true;
                if (this.emitting) {
                    AppendOnlyLinkedArrayList<Object> q = this.queue;
                    if (q == null) {
                        q = new AppendOnlyLinkedArrayList<>(4);
                        this.queue = q;
                    }
                    q.setFirst(NotificationLite.error(t));
                    return;
                }
                this.emitting = true;
                reportError = false;
            }
            if (reportError) {
                RxJavaPlugins.onError(t);
            } else {
                this.actual.onError(t);
            }
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onComplete() {
        if (!this.done) {
            synchronized (this) {
                if (!this.done) {
                    this.done = true;
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> q = this.queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = q;
                        }
                        q.add(NotificationLite.complete());
                        return;
                    }
                    this.emitting = true;
                    this.actual.onComplete();
                }
            }
        }
    }

    void emitLoop() {
        AppendOnlyLinkedArrayList<Object> q;
        while (true) {
            synchronized (this) {
                q = this.queue;
                if (q == null) {
                    this.emitting = false;
                    return;
                }
                this.queue = null;
            }
            q.accept(this.actual);
        }
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasSubscribers() {
        return this.actual.hasSubscribers();
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasThrowable() {
        return this.actual.hasThrowable();
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    @Nullable
    public Throwable getThrowable() {
        return this.actual.getThrowable();
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasComplete() {
        return this.actual.hasComplete();
    }
}
