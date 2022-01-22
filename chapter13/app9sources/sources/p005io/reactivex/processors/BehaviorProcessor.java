package p005io.reactivex.processors;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.processors.BehaviorProcessor */
/* loaded from: classes.dex */
public final class BehaviorProcessor<T> extends FlowableProcessor<T> {
    long index;
    final ReadWriteLock lock;
    final Lock readLock;
    final AtomicReference<BehaviorSubscription<T>[]> subscribers;
    final AtomicReference<Throwable> terminalEvent;
    final AtomicReference<Object> value;
    final Lock writeLock;
    static final Object[] EMPTY_ARRAY = new Object[0];
    static final BehaviorSubscription[] EMPTY = new BehaviorSubscription[0];
    static final BehaviorSubscription[] TERMINATED = new BehaviorSubscription[0];

    @CheckReturnValue
    @NonNull
    public static <T> BehaviorProcessor<T> create() {
        return new BehaviorProcessor<>();
    }

    @CheckReturnValue
    @NonNull
    public static <T> BehaviorProcessor<T> createDefault(T t) {
        ObjectHelper.requireNonNull(t, "defaultValue is null");
        return new BehaviorProcessor<>(t);
    }

    BehaviorProcessor() {
        this.value = new AtomicReference<>();
        this.lock = new ReentrantReadWriteLock();
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
        this.subscribers = new AtomicReference<>(EMPTY);
        this.terminalEvent = new AtomicReference<>();
    }

    BehaviorProcessor(T t) {
        this();
        this.value.lazySet(ObjectHelper.requireNonNull(t, "defaultValue is null"));
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        BehaviorSubscription<T> behaviorSubscription = new BehaviorSubscription<>(subscriber, this);
        subscriber.onSubscribe(behaviorSubscription);
        if (!add(behaviorSubscription)) {
            Throwable th = this.terminalEvent.get();
            if (th == ExceptionHelper.TERMINATED) {
                subscriber.onComplete();
            } else {
                subscriber.onError(th);
            }
        } else if (behaviorSubscription.cancelled) {
            remove(behaviorSubscription);
        } else {
            behaviorSubscription.emitFirst();
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onSubscribe(Subscription subscription) {
        if (this.terminalEvent.get() != null) {
            subscription.cancel();
        } else {
            subscription.request(Long.MAX_VALUE);
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onNext(T t) {
        ObjectHelper.requireNonNull(t, "onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.terminalEvent.get() == null) {
            Object next = NotificationLite.next(t);
            setCurrent(next);
            for (BehaviorSubscription<T> behaviorSubscription : this.subscribers.get()) {
                behaviorSubscription.emitNext(next, this.index);
            }
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable th) {
        ObjectHelper.requireNonNull(th, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (!this.terminalEvent.compareAndSet(null, th)) {
            RxJavaPlugins.onError(th);
            return;
        }
        Object error = NotificationLite.error(th);
        for (BehaviorSubscription<T> behaviorSubscription : terminate(error)) {
            behaviorSubscription.emitNext(error, this.index);
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onComplete() {
        if (this.terminalEvent.compareAndSet(null, ExceptionHelper.TERMINATED)) {
            Object complete = NotificationLite.complete();
            for (BehaviorSubscription<T> behaviorSubscription : terminate(complete)) {
                behaviorSubscription.emitNext(complete, this.index);
            }
        }
    }

    public boolean offer(T t) {
        if (t == null) {
            onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
            return true;
        }
        BehaviorSubscription<T>[] behaviorSubscriptionArr = this.subscribers.get();
        for (BehaviorSubscription<T> behaviorSubscription : behaviorSubscriptionArr) {
            if (behaviorSubscription.isFull()) {
                return false;
            }
        }
        Object next = NotificationLite.next(t);
        setCurrent(next);
        for (BehaviorSubscription<T> behaviorSubscription2 : behaviorSubscriptionArr) {
            behaviorSubscription2.emitNext(next, this.index);
        }
        return true;
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasSubscribers() {
        return this.subscribers.get().length != 0;
    }

    int subscriberCount() {
        return this.subscribers.get().length;
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
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

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasComplete() {
        return NotificationLite.isComplete(this.value.get());
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasThrowable() {
        return NotificationLite.isError(this.value.get());
    }

    public boolean hasValue() {
        Object obj = this.value.get();
        return obj != null && !NotificationLite.isComplete(obj) && !NotificationLite.isError(obj);
    }

    boolean add(BehaviorSubscription<T> behaviorSubscription) {
        BehaviorSubscription<T>[] behaviorSubscriptionArr;
        BehaviorSubscription<T>[] behaviorSubscriptionArr2;
        do {
            behaviorSubscriptionArr = this.subscribers.get();
            if (behaviorSubscriptionArr == TERMINATED) {
                return false;
            }
            int length = behaviorSubscriptionArr.length;
            behaviorSubscriptionArr2 = new BehaviorSubscription[length + 1];
            System.arraycopy(behaviorSubscriptionArr, 0, behaviorSubscriptionArr2, 0, length);
            behaviorSubscriptionArr2[length] = behaviorSubscription;
        } while (!this.subscribers.compareAndSet(behaviorSubscriptionArr, behaviorSubscriptionArr2));
        return true;
    }

    void remove(BehaviorSubscription<T> behaviorSubscription) {
        BehaviorSubscription<T>[] behaviorSubscriptionArr;
        BehaviorSubscription<T>[] behaviorSubscriptionArr2;
        do {
            behaviorSubscriptionArr = this.subscribers.get();
            int length = behaviorSubscriptionArr.length;
            if (length != 0) {
                int i = -1;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (behaviorSubscriptionArr[i2] == behaviorSubscription) {
                        i = i2;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (i >= 0) {
                    if (length == 1) {
                        behaviorSubscriptionArr2 = EMPTY;
                    } else {
                        BehaviorSubscription<T>[] behaviorSubscriptionArr3 = new BehaviorSubscription[length - 1];
                        System.arraycopy(behaviorSubscriptionArr, 0, behaviorSubscriptionArr3, 0, i);
                        System.arraycopy(behaviorSubscriptionArr, i + 1, behaviorSubscriptionArr3, i, (length - i) - 1);
                        behaviorSubscriptionArr2 = behaviorSubscriptionArr3;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.subscribers.compareAndSet(behaviorSubscriptionArr, behaviorSubscriptionArr2));
    }

    BehaviorSubscription<T>[] terminate(Object obj) {
        BehaviorSubscription<T>[] behaviorSubscriptionArr = this.subscribers.get();
        BehaviorSubscription<T>[] behaviorSubscriptionArr2 = TERMINATED;
        if (!(behaviorSubscriptionArr == behaviorSubscriptionArr2 || (behaviorSubscriptionArr = this.subscribers.getAndSet(behaviorSubscriptionArr2)) == TERMINATED)) {
            setCurrent(obj);
        }
        return behaviorSubscriptionArr;
    }

    void setCurrent(Object obj) {
        Lock lock = this.writeLock;
        lock.lock();
        this.index++;
        this.value.lazySet(obj);
        lock.unlock();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.processors.BehaviorProcessor$BehaviorSubscription */
    /* loaded from: classes.dex */
    public static final class BehaviorSubscription<T> extends AtomicLong implements Subscription, AppendOnlyLinkedArrayList.NonThrowingPredicate<Object> {
        private static final long serialVersionUID = 3293175281126227086L;
        volatile boolean cancelled;
        final Subscriber<? super T> downstream;
        boolean emitting;
        boolean fastPath;
        long index;
        boolean next;
        AppendOnlyLinkedArrayList<Object> queue;
        final BehaviorProcessor<T> state;

        BehaviorSubscription(Subscriber<? super T> subscriber, BehaviorProcessor<T> behaviorProcessor) {
            this.downstream = subscriber;
            this.state = behaviorProcessor;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this, j);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.state.remove(this);
            }
        }

        void emitFirst() {
            if (!this.cancelled) {
                synchronized (this) {
                    if (!this.cancelled) {
                        if (!this.next) {
                            BehaviorProcessor<T> behaviorProcessor = this.state;
                            Lock lock = behaviorProcessor.readLock;
                            lock.lock();
                            this.index = behaviorProcessor.index;
                            Object obj = behaviorProcessor.value.get();
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
            if (this.cancelled) {
                return true;
            }
            if (NotificationLite.isComplete(obj)) {
                this.downstream.onComplete();
                return true;
            } else if (NotificationLite.isError(obj)) {
                this.downstream.onError(NotificationLite.getError(obj));
                return true;
            } else {
                long j = get();
                if (j != 0) {
                    this.downstream.onNext((Object) NotificationLite.getValue(obj));
                    if (j == Long.MAX_VALUE) {
                        return false;
                    }
                    decrementAndGet();
                    return false;
                }
                cancel();
                this.downstream.onError(new MissingBackpressureException("Could not deliver value due to lack of requests"));
                return true;
            }
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

        public boolean isFull() {
            return get() == 0;
        }
    }
}
