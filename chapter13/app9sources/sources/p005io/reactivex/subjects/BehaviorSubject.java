package p005io.reactivex.subjects;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.subjects.BehaviorSubject */
/* loaded from: classes.dex */
public final class BehaviorSubject<T> extends Subject<T> {
    long index;
    final ReadWriteLock lock;
    final Lock readLock;
    final AtomicReference<BehaviorDisposable<T>[]> subscribers;
    final AtomicReference<Throwable> terminalEvent;
    final AtomicReference<Object> value;
    final Lock writeLock;
    private static final Object[] EMPTY_ARRAY = new Object[0];
    static final BehaviorDisposable[] EMPTY = new BehaviorDisposable[0];
    static final BehaviorDisposable[] TERMINATED = new BehaviorDisposable[0];

    @CheckReturnValue
    @NonNull
    public static <T> BehaviorSubject<T> create() {
        return new BehaviorSubject<>();
    }

    @CheckReturnValue
    @NonNull
    public static <T> BehaviorSubject<T> createDefault(T t) {
        return new BehaviorSubject<>(t);
    }

    BehaviorSubject() {
        this.lock = new ReentrantReadWriteLock();
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
        this.subscribers = new AtomicReference<>(EMPTY);
        this.value = new AtomicReference<>();
        this.terminalEvent = new AtomicReference<>();
    }

    BehaviorSubject(T t) {
        this();
        this.value.lazySet(ObjectHelper.requireNonNull(t, "defaultValue is null"));
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        BehaviorDisposable<T> behaviorDisposable = new BehaviorDisposable<>(observer, this);
        observer.onSubscribe(behaviorDisposable);
        if (!add(behaviorDisposable)) {
            Throwable th = this.terminalEvent.get();
            if (th == ExceptionHelper.TERMINATED) {
                observer.onComplete();
            } else {
                observer.onError(th);
            }
        } else if (behaviorDisposable.cancelled) {
            remove(behaviorDisposable);
        } else {
            behaviorDisposable.emitFirst();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        if (this.terminalEvent.get() != null) {
            disposable.dispose();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        ObjectHelper.requireNonNull(t, "onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.terminalEvent.get() == null) {
            Object next = NotificationLite.next(t);
            setCurrent(next);
            for (BehaviorDisposable<T> behaviorDisposable : this.subscribers.get()) {
                behaviorDisposable.emitNext(next, this.index);
            }
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        ObjectHelper.requireNonNull(th, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (!this.terminalEvent.compareAndSet(null, th)) {
            RxJavaPlugins.onError(th);
            return;
        }
        Object error = NotificationLite.error(th);
        for (BehaviorDisposable<T> behaviorDisposable : terminate(error)) {
            behaviorDisposable.emitNext(error, this.index);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (this.terminalEvent.compareAndSet(null, ExceptionHelper.TERMINATED)) {
            Object complete = NotificationLite.complete();
            for (BehaviorDisposable<T> behaviorDisposable : terminate(complete)) {
                behaviorDisposable.emitNext(complete, this.index);
            }
        }
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasObservers() {
        return this.subscribers.get().length != 0;
    }

    int subscriberCount() {
        return this.subscribers.get().length;
    }

    @Override // p005io.reactivex.subjects.Subject
    @Nullable
    public Throwable getThrowable() {
        Object obj = this.value.get();
        if (NotificationLite.isError(obj)) {
            return NotificationLite.getError(obj);
        }
        return null;
    }

    @Nullable
    public T getValue() {
        Object obj = this.value.get();
        if (NotificationLite.isComplete(obj) || NotificationLite.isError(obj)) {
            return null;
        }
        return (T) NotificationLite.getValue(obj);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Deprecated
    public Object[] getValues() {
        Object[] values = getValues(EMPTY_ARRAY);
        return values == EMPTY_ARRAY ? new Object[0] : values;
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Deprecated
    public T[] getValues(T[] tArr) {
        Object obj = this.value.get();
        if (obj == null || NotificationLite.isComplete(obj) || NotificationLite.isError(obj)) {
            if (tArr.length != 0) {
                tArr[0] = 0;
            }
            return tArr;
        }
        Object value = NotificationLite.getValue(obj);
        if (tArr.length != 0) {
            tArr[0] = value;
            if (tArr.length == 1) {
                return tArr;
            }
            tArr[1] = 0;
            return tArr;
        }
        T[] tArr2 = (T[]) ((Object[]) Array.newInstance(tArr.getClass().getComponentType(), 1));
        tArr2[0] = value;
        return tArr2;
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasComplete() {
        return NotificationLite.isComplete(this.value.get());
    }

    @Override // p005io.reactivex.subjects.Subject
    public boolean hasThrowable() {
        return NotificationLite.isError(this.value.get());
    }

    public boolean hasValue() {
        Object obj = this.value.get();
        return obj != null && !NotificationLite.isComplete(obj) && !NotificationLite.isError(obj);
    }

    boolean add(BehaviorDisposable<T> behaviorDisposable) {
        BehaviorDisposable<T>[] behaviorDisposableArr;
        BehaviorDisposable<T>[] behaviorDisposableArr2;
        do {
            behaviorDisposableArr = this.subscribers.get();
            if (behaviorDisposableArr == TERMINATED) {
                return false;
            }
            int length = behaviorDisposableArr.length;
            behaviorDisposableArr2 = new BehaviorDisposable[length + 1];
            System.arraycopy(behaviorDisposableArr, 0, behaviorDisposableArr2, 0, length);
            behaviorDisposableArr2[length] = behaviorDisposable;
        } while (!this.subscribers.compareAndSet(behaviorDisposableArr, behaviorDisposableArr2));
        return true;
    }

    void remove(BehaviorDisposable<T> behaviorDisposable) {
        BehaviorDisposable<T>[] behaviorDisposableArr;
        BehaviorDisposable<T>[] behaviorDisposableArr2;
        do {
            behaviorDisposableArr = this.subscribers.get();
            int length = behaviorDisposableArr.length;
            if (length != 0) {
                int i = -1;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (behaviorDisposableArr[i2] == behaviorDisposable) {
                        i = i2;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (i >= 0) {
                    if (length == 1) {
                        behaviorDisposableArr2 = EMPTY;
                    } else {
                        BehaviorDisposable<T>[] behaviorDisposableArr3 = new BehaviorDisposable[length - 1];
                        System.arraycopy(behaviorDisposableArr, 0, behaviorDisposableArr3, 0, i);
                        System.arraycopy(behaviorDisposableArr, i + 1, behaviorDisposableArr3, i, (length - i) - 1);
                        behaviorDisposableArr2 = behaviorDisposableArr3;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.subscribers.compareAndSet(behaviorDisposableArr, behaviorDisposableArr2));
    }

    BehaviorDisposable<T>[] terminate(Object obj) {
        BehaviorDisposable<T>[] andSet = this.subscribers.getAndSet(TERMINATED);
        if (andSet != TERMINATED) {
            setCurrent(obj);
        }
        return andSet;
    }

    void setCurrent(Object obj) {
        this.writeLock.lock();
        this.index++;
        this.value.lazySet(obj);
        this.writeLock.unlock();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.subjects.BehaviorSubject$BehaviorDisposable */
    /* loaded from: classes.dex */
    public static final class BehaviorDisposable<T> implements Disposable, AppendOnlyLinkedArrayList.NonThrowingPredicate<Object> {
        volatile boolean cancelled;
        final Observer<? super T> downstream;
        boolean emitting;
        boolean fastPath;
        long index;
        boolean next;
        AppendOnlyLinkedArrayList<Object> queue;
        final BehaviorSubject<T> state;

        BehaviorDisposable(Observer<? super T> observer, BehaviorSubject<T> behaviorSubject) {
            this.downstream = observer;
            this.state = behaviorSubject;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.state.remove(this);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void emitFirst() {
            if (!this.cancelled) {
                synchronized (this) {
                    if (!this.cancelled) {
                        if (!this.next) {
                            BehaviorSubject<T> behaviorSubject = this.state;
                            Lock lock = behaviorSubject.readLock;
                            lock.lock();
                            this.index = behaviorSubject.index;
                            Object obj = behaviorSubject.value.get();
                            lock.unlock();
                            this.emitting = obj != null;
                            this.next = true;
                            if (obj != null && !test(obj)) {
                                emitLoop();
                            }
                        }
                    }
                }
            }
        }

        void emitNext(Object obj, long j) {
            if (!this.cancelled) {
                if (!this.fastPath) {
                    synchronized (this) {
                        if (!this.cancelled) {
                            if (this.index != j) {
                                if (this.emitting) {
                                    AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList = this.queue;
                                    if (appendOnlyLinkedArrayList == null) {
                                        appendOnlyLinkedArrayList = new AppendOnlyLinkedArrayList<>(4);
                                        this.queue = appendOnlyLinkedArrayList;
                                    }
                                    appendOnlyLinkedArrayList.add(obj);
                                    return;
                                }
                                this.next = true;
                                this.fastPath = true;
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    }
                }
                test(obj);
            }
        }

        @Override // p005io.reactivex.internal.util.AppendOnlyLinkedArrayList.NonThrowingPredicate, p005io.reactivex.functions.Predicate
        public boolean test(Object obj) {
            return this.cancelled || NotificationLite.accept(obj, this.downstream);
        }

        void emitLoop() {
            AppendOnlyLinkedArrayList<Object> appendOnlyLinkedArrayList;
            while (!this.cancelled) {
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
    }
}
