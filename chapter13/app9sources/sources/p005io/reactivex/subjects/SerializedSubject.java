package p005io.reactivex.subjects;

import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.subjects.SerializedSubject */
/* loaded from: classes.dex */
final class SerializedSubject<T> extends Subject<T> implements AppendOnlyLinkedArrayList.NonThrowingPredicate<Object> {
    final Subject<T> actual;
    volatile boolean done;
    boolean emitting;
    AppendOnlyLinkedArrayList<Object> queue;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SerializedSubject(Subject<T> subject) {
        this.actual = subject;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.actual.subscribe(observer);
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        boolean z = true;
        if (!this.done) {
            synchronized (this) {
                if (!this.done) {
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList = this.queue;
                        if (appendOnlyLinkedArrayList == null) {
                            appendOnlyLinkedArrayList = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = appendOnlyLinkedArrayList;
                        }
                        appendOnlyLinkedArrayList.add(NotificationLite.disposable(disposable));
                        return;
                    }
                    this.emitting = true;
                    z = false;
                }
            }
        }
        if (z) {
            disposable.dispose();
            return;
        }
        this.actual.onSubscribe(disposable);
        emitLoop();
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        if (!this.done) {
            synchronized (this) {
                if (!this.done) {
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList = this.queue;
                        if (appendOnlyLinkedArrayList == null) {
                            appendOnlyLinkedArrayList = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = appendOnlyLinkedArrayList;
                        }
                        appendOnlyLinkedArrayList.add(NotificationLite.next(t));
                        return;
                    }
                    this.emitting = true;
                    this.actual.onNext(t);
                    emitLoop();
                }
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        boolean z;
        if (this.done) {
            RxJavaPlugins.onError(th);
            return;
        }
        synchronized (this) {
            if (this.done) {
                z = true;
            } else {
                this.done = true;
                if (this.emitting) {
                    AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList = this.queue;
                    if (appendOnlyLinkedArrayList == null) {
                        appendOnlyLinkedArrayList = new AppendOnlyLinkedArrayList<>(4);
                        this.queue = appendOnlyLinkedArrayList;
                    }
                    appendOnlyLinkedArrayList.setFirst(NotificationLite.error(th));
                    return;
                }
                z = false;
                this.emitting = true;
            }
            if (z) {
                RxJavaPlugins.onError(th);
            } else {
                this.actual.onError(th);
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.done) {
            synchronized (this) {
                if (!this.done) {
                    this.done = true;
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList = this.queue;
                        if (appendOnlyLinkedArrayList == null) {
                            appendOnlyLinkedArrayList = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = appendOnlyLinkedArrayList;
                        }
                        appendOnlyLinkedArrayList.add(NotificationLite.complete());
                        return;
                    }
                    this.emitting = true;
                    this.actual.onComplete();
                }
            }
        }
    }

    void emitLoop() {
        AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList;
        while (true) {
            synchronized (this) {
                appendOnlyLinkedArrayList = this.queue;
                if (appendOnlyLinkedArrayList == null) {
                    this.emitting = false;
                    return;
                }
                this.queue = null;
            }
            appendOnlyLinkedArrayList.forEachWhile(this);
        }
    }

    @Override // p005io.reactivex.internal.util.AppendOnlyLinkedArrayList.NonThrowingPredicate, p005io.reactivex.functions.Predicate
    public boolean test(Object obj) {
        return NotificationLite.acceptFull(obj, this.actual);
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasObservers() {
        return this.actual.hasObservers();
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasThrowable() {
        return this.actual.hasThrowable();
    }

    @Override // p005io.reactivex.subjects.Subject
    @Nullable
    public Throwable getThrowable() {
        return this.actual.getThrowable();
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasComplete() {
        return this.actual.hasComplete();
    }
}
