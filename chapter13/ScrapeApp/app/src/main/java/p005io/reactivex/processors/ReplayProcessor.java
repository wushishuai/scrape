package p005io.reactivex.processors;

import android.support.p003v7.widget.ActivityChooserView;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Scheduler;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.processors.ReplayProcessor */
/* loaded from: classes.dex */
public final class ReplayProcessor<T> extends FlowableProcessor<T> {
    final ReplayBuffer<T> buffer;
    boolean done;
    final AtomicReference<ReplaySubscription<T>[]> subscribers = new AtomicReference<>(EMPTY);
    private static final Object[] EMPTY_ARRAY = new Object[0];
    static final ReplaySubscription[] EMPTY = new ReplaySubscription[0];
    static final ReplaySubscription[] TERMINATED = new ReplaySubscription[0];

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.processors.ReplayProcessor$ReplayBuffer */
    /* loaded from: classes.dex */
    public interface ReplayBuffer<T> {
        void complete();

        void error(Throwable th);

        Throwable getError();

        @Nullable
        T getValue();

        T[] getValues(T[] tArr);

        boolean isDone();

        void next(T t);

        void replay(ReplaySubscription<T> replaySubscription);

        int size();

        void trimHead();
    }

    @CheckReturnValue
    @NonNull
    public static <T> ReplayProcessor<T> create() {
        return new ReplayProcessor<>(new UnboundedReplayBuffer(16));
    }

    @CheckReturnValue
    @NonNull
    public static <T> ReplayProcessor<T> create(int capacityHint) {
        return new ReplayProcessor<>(new UnboundedReplayBuffer(capacityHint));
    }

    @CheckReturnValue
    @NonNull
    public static <T> ReplayProcessor<T> createWithSize(int maxSize) {
        return new ReplayProcessor<>(new SizeBoundReplayBuffer(maxSize));
    }

    static <T> ReplayProcessor<T> createUnbounded() {
        return new ReplayProcessor<>(new SizeBoundReplayBuffer(ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED));
    }

    @CheckReturnValue
    @NonNull
    public static <T> ReplayProcessor<T> createWithTime(long maxAge, TimeUnit unit, Scheduler scheduler) {
        return new ReplayProcessor<>(new SizeAndTimeBoundReplayBuffer(ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, maxAge, unit, scheduler));
    }

    @CheckReturnValue
    @NonNull
    public static <T> ReplayProcessor<T> createWithTimeAndSize(long maxAge, TimeUnit unit, Scheduler scheduler, int maxSize) {
        return new ReplayProcessor<>(new SizeAndTimeBoundReplayBuffer(maxSize, maxAge, unit, scheduler));
    }

    ReplayProcessor(ReplayBuffer<T> buffer) {
        this.buffer = buffer;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        ReplaySubscription<T> rs = new ReplaySubscription<>(s, this);
        s.onSubscribe(rs);
        if (!add(rs) || !rs.cancelled) {
            this.buffer.replay(rs);
        } else {
            remove(rs);
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onSubscribe(Subscription s) {
        if (this.done) {
            s.cancel();
        } else {
            s.request(Long.MAX_VALUE);
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onNext(T t) {
        ObjectHelper.requireNonNull(t, "onNext called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (!this.done) {
            ReplayBuffer<T> b = this.buffer;
            b.next(t);
            for (ReplaySubscription<T> rs : this.subscribers.get()) {
                b.replay(rs);
            }
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable t) {
        ObjectHelper.requireNonNull(t, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.done) {
            RxJavaPlugins.onError(t);
            return;
        }
        this.done = true;
        ReplayBuffer<T> b = this.buffer;
        b.error(t);
        for (ReplaySubscription<T> rs : this.subscribers.getAndSet(TERMINATED)) {
            b.replay(rs);
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onComplete() {
        if (!this.done) {
            this.done = true;
            ReplayBuffer<T> b = this.buffer;
            b.complete();
            for (ReplaySubscription<T> rs : this.subscribers.getAndSet(TERMINATED)) {
                b.replay(rs);
            }
        }
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
        ReplayBuffer<T> b = this.buffer;
        if (b.isDone()) {
            return b.getError();
        }
        return null;
    }

    public void cleanupBuffer() {
        this.buffer.trimHead();
    }

    public T getValue() {
        return this.buffer.getValue();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public Object[] getValues() {
        T[] b = getValues(EMPTY_ARRAY);
        if (b == EMPTY_ARRAY) {
            return new Object[0];
        }
        return b;
    }

    public T[] getValues(T[] array) {
        return this.buffer.getValues(array);
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasComplete() {
        ReplayBuffer<T> b = this.buffer;
        return b.isDone() && b.getError() == null;
    }

    @Override // p005io.reactivex.processors.FlowableProcessor
    public boolean hasThrowable() {
        ReplayBuffer<T> b = this.buffer;
        return b.isDone() && b.getError() != null;
    }

    public boolean hasValue() {
        return this.buffer.size() != 0;
    }

    int size() {
        return this.buffer.size();
    }

    boolean add(ReplaySubscription<T> rs) {
        ReplaySubscription<T>[] a;
        ReplaySubscription<T>[] b;
        do {
            a = this.subscribers.get();
            if (a == TERMINATED) {
                return false;
            }
            int len = a.length;
            b = new ReplaySubscription[len + 1];
            System.arraycopy(a, 0, b, 0, len);
            b[len] = rs;
        } while (!this.subscribers.compareAndSet(a, b));
        return true;
    }

    void remove(ReplaySubscription<T> rs) {
        ReplaySubscription<T>[] a;
        ReplaySubscription<T>[] b;
        do {
            a = this.subscribers.get();
            if (a != TERMINATED && a != EMPTY) {
                int len = a.length;
                int j = -1;
                int i = 0;
                while (true) {
                    if (i >= len) {
                        break;
                    } else if (a[i] == rs) {
                        j = i;
                        break;
                    } else {
                        i++;
                    }
                }
                if (j >= 0) {
                    if (len == 1) {
                        b = EMPTY;
                    } else {
                        ReplaySubscription<T>[] b2 = new ReplaySubscription[len - 1];
                        System.arraycopy(a, 0, b2, 0, j);
                        System.arraycopy(a, j + 1, b2, j, (len - j) - 1);
                        b = b2;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.subscribers.compareAndSet(a, b));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.processors.ReplayProcessor$ReplaySubscription */
    /* loaded from: classes.dex */
    public static final class ReplaySubscription<T> extends AtomicInteger implements Subscription {
        private static final long serialVersionUID = 466549804534799122L;
        volatile boolean cancelled;
        final Subscriber<? super T> downstream;
        long emitted;
        Object index;
        final AtomicLong requested = new AtomicLong();
        final ReplayProcessor<T> state;

        ReplaySubscription(Subscriber<? super T> actual, ReplayProcessor<T> state) {
            this.downstream = actual;
            this.state = state;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
                this.state.buffer.replay(this);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.state.remove(this);
            }
        }
    }

    /* renamed from: io.reactivex.processors.ReplayProcessor$UnboundedReplayBuffer */
    /* loaded from: classes.dex */
    static final class UnboundedReplayBuffer<T> implements ReplayBuffer<T> {
        final List<T> buffer;
        volatile boolean done;
        Throwable error;
        volatile int size;

        UnboundedReplayBuffer(int capacityHint) {
            this.buffer = new ArrayList(ObjectHelper.verifyPositive(capacityHint, "capacityHint"));
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void next(T value) {
            this.buffer.add(value);
            this.size++;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void error(Throwable ex) {
            this.error = ex;
            this.done = true;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void complete() {
            this.done = true;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void trimHead() {
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        @Nullable
        public T getValue() {
            int s = this.size;
            if (s == 0) {
                return null;
            }
            return this.buffer.get(s - 1);
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public T[] getValues(T[] array) {
            int s = this.size;
            if (s == 0) {
                if (array.length != 0) {
                    array[0] = null;
                }
                return array;
            }
            List<T> b = this.buffer;
            if (array.length < s) {
                array = (T[]) ((Object[]) Array.newInstance(array.getClass().getComponentType(), s));
            }
            for (int i = 0; i < s; i++) {
                array[i] = b.get(i);
            }
            if (array.length > s) {
                array[s] = null;
            }
            return array;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void replay(ReplaySubscription<T> rs) {
            int index;
            if (rs.getAndIncrement() == 0) {
                int missed = 1;
                List<T> b = this.buffer;
                Subscriber<? super T> a = rs.downstream;
                Integer indexObject = (Integer) rs.index;
                if (indexObject != null) {
                    index = indexObject.intValue();
                } else {
                    index = 0;
                    rs.index = 0;
                }
                long e = rs.emitted;
                do {
                    long r = rs.requested.get();
                    while (e != r) {
                        if (rs.cancelled) {
                            rs.index = null;
                            return;
                        }
                        boolean d = this.done;
                        int s = this.size;
                        if (d && index == s) {
                            rs.index = null;
                            rs.cancelled = true;
                            Throwable ex = this.error;
                            if (ex == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex);
                                return;
                            }
                        } else if (index == s) {
                            break;
                        } else {
                            a.onNext(b.get(index));
                            index++;
                            e++;
                        }
                    }
                    if (e == r) {
                        if (rs.cancelled) {
                            rs.index = null;
                            return;
                        }
                        boolean d2 = this.done;
                        int s2 = this.size;
                        if (d2 && index == s2) {
                            rs.index = null;
                            rs.cancelled = true;
                            Throwable ex2 = this.error;
                            if (ex2 == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex2);
                                return;
                            }
                        }
                    }
                    rs.index = Integer.valueOf(index);
                    rs.emitted = e;
                    missed = rs.addAndGet(-missed);
                } while (missed != 0);
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public int size() {
            return this.size;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public boolean isDone() {
            return this.done;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public Throwable getError() {
            return this.error;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.processors.ReplayProcessor$Node */
    /* loaded from: classes.dex */
    public static final class Node<T> extends AtomicReference<Node<T>> {
        private static final long serialVersionUID = 6404226426336033100L;
        final T value;

        Node(T value) {
            this.value = value;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.processors.ReplayProcessor$TimedNode */
    /* loaded from: classes.dex */
    public static final class TimedNode<T> extends AtomicReference<TimedNode<T>> {
        private static final long serialVersionUID = 6404226426336033100L;
        final long time;
        final T value;

        TimedNode(T value, long time) {
            this.value = value;
            this.time = time;
        }
    }

    /* renamed from: io.reactivex.processors.ReplayProcessor$SizeBoundReplayBuffer */
    /* loaded from: classes.dex */
    static final class SizeBoundReplayBuffer<T> implements ReplayBuffer<T> {
        volatile boolean done;
        Throwable error;
        volatile Node<T> head;
        final int maxSize;
        int size;
        Node<T> tail;

        SizeBoundReplayBuffer(int maxSize) {
            this.maxSize = ObjectHelper.verifyPositive(maxSize, "maxSize");
            Node<T> h = new Node<>(null);
            this.tail = h;
            this.head = h;
        }

        void trim() {
            int i = this.size;
            if (i > this.maxSize) {
                this.size = i - 1;
                this.head = this.head.get();
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void next(T value) {
            Node<T> n = new Node<>(value);
            Node<T> t = this.tail;
            this.tail = n;
            this.size++;
            t.set(n);
            trim();
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void error(Throwable ex) {
            this.error = ex;
            trimHead();
            this.done = true;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void complete() {
            trimHead();
            this.done = true;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void trimHead() {
            if (this.head.value != null) {
                Node<T> n = new Node<>(null);
                n.lazySet(this.head.get());
                this.head = n;
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public boolean isDone() {
            return this.done;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public Throwable getError() {
            return this.error;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public T getValue() {
            Node<T> h = this.head;
            while (true) {
                Node<T> n = h.get();
                if (n == null) {
                    return h.value;
                }
                h = n;
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public T[] getValues(T[] array) {
            int s = 0;
            Node<T> h = this.head;
            Node<T> h0 = h;
            while (true) {
                Node<T> next = h0.get();
                if (next == null) {
                    break;
                }
                s++;
                h0 = next;
            }
            if (array.length < s) {
                array = (T[]) ((Object[]) Array.newInstance(array.getClass().getComponentType(), s));
            }
            for (int j = 0; j < s; j++) {
                h = h.get();
                array[j] = h.value;
            }
            if (array.length > s) {
                array[s] = null;
            }
            return array;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void replay(ReplaySubscription<T> rs) {
            if (rs.getAndIncrement() == 0) {
                int missed = 1;
                Subscriber<? super T> a = rs.downstream;
                Node<T> index = (Node) rs.index;
                if (index == null) {
                    index = this.head;
                }
                long e = rs.emitted;
                do {
                    long r = rs.requested.get();
                    while (e != r) {
                        if (rs.cancelled) {
                            rs.index = null;
                            return;
                        }
                        boolean d = this.done;
                        Node<T> next = index.get();
                        boolean empty = next == null;
                        if (d && empty) {
                            rs.index = null;
                            rs.cancelled = true;
                            Throwable ex = this.error;
                            if (ex == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex);
                                return;
                            }
                        } else if (empty) {
                            break;
                        } else {
                            a.onNext((T) next.value);
                            e++;
                            index = next;
                        }
                    }
                    if (e == r) {
                        if (rs.cancelled) {
                            rs.index = null;
                            return;
                        } else if (this.done && index.get() == null) {
                            rs.index = null;
                            rs.cancelled = true;
                            Throwable ex2 = this.error;
                            if (ex2 == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex2);
                                return;
                            }
                        }
                    }
                    rs.index = index;
                    rs.emitted = e;
                    missed = rs.addAndGet(-missed);
                } while (missed != 0);
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public int size() {
            Node<T> next;
            int s = 0;
            Node<T> h = this.head;
            while (s != Integer.MAX_VALUE && (next = h.get()) != null) {
                s++;
                h = next;
            }
            return s;
        }
    }

    /* renamed from: io.reactivex.processors.ReplayProcessor$SizeAndTimeBoundReplayBuffer */
    /* loaded from: classes.dex */
    static final class SizeAndTimeBoundReplayBuffer<T> implements ReplayBuffer<T> {
        volatile boolean done;
        Throwable error;
        volatile TimedNode<T> head;
        final long maxAge;
        final int maxSize;
        final Scheduler scheduler;
        int size;
        TimedNode<T> tail;
        final TimeUnit unit;

        SizeAndTimeBoundReplayBuffer(int maxSize, long maxAge, TimeUnit unit, Scheduler scheduler) {
            this.maxSize = ObjectHelper.verifyPositive(maxSize, "maxSize");
            this.maxAge = ObjectHelper.verifyPositive(maxAge, "maxAge");
            this.unit = (TimeUnit) ObjectHelper.requireNonNull(unit, "unit is null");
            this.scheduler = (Scheduler) ObjectHelper.requireNonNull(scheduler, "scheduler is null");
            TimedNode<T> h = new TimedNode<>(null, 0);
            this.tail = h;
            this.head = h;
        }

        void trim() {
            int i = this.size;
            if (i > this.maxSize) {
                this.size = i - 1;
                this.head = this.head.get();
            }
            long limit = this.scheduler.now(this.unit) - this.maxAge;
            TimedNode<T> h = this.head;
            while (true) {
                TimedNode<T> next = h.get();
                if (next == null) {
                    this.head = h;
                    return;
                } else if (next.time > limit) {
                    this.head = h;
                    return;
                } else {
                    h = next;
                }
            }
        }

        void trimFinal() {
            long limit = this.scheduler.now(this.unit) - this.maxAge;
            TimedNode<T> h = this.head;
            while (true) {
                TimedNode<T> next = h.get();
                if (next == null) {
                    if (h.value != null) {
                        this.head = new TimedNode<>(null, 0);
                        return;
                    } else {
                        this.head = h;
                        return;
                    }
                } else if (next.time <= limit) {
                    h = next;
                } else if (h.value != null) {
                    TimedNode<T> n = new TimedNode<>(null, 0);
                    n.lazySet(h.get());
                    this.head = n;
                    return;
                } else {
                    this.head = h;
                    return;
                }
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void trimHead() {
            if (this.head.value != null) {
                TimedNode<T> n = new TimedNode<>(null, 0);
                n.lazySet(this.head.get());
                this.head = n;
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void next(T value) {
            TimedNode<T> n = new TimedNode<>(value, this.scheduler.now(this.unit));
            TimedNode<T> t = this.tail;
            this.tail = n;
            this.size++;
            t.set(n);
            trim();
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void error(Throwable ex) {
            trimFinal();
            this.error = ex;
            this.done = true;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void complete() {
            trimFinal();
            this.done = true;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        @Nullable
        public T getValue() {
            TimedNode<T> h = this.head;
            while (true) {
                TimedNode<T> next = h.get();
                if (next == null) {
                    break;
                }
                h = next;
            }
            if (h.time < this.scheduler.now(this.unit) - this.maxAge) {
                return null;
            }
            return h.value;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public T[] getValues(T[] array) {
            TimedNode<T> h = getHead();
            int s = size(h);
            if (s != 0) {
                if (array.length < s) {
                    array = (T[]) ((Object[]) Array.newInstance(array.getClass().getComponentType(), s));
                }
                int i = 0;
                while (i != s) {
                    TimedNode<T> next = h.get();
                    array[i] = next.value;
                    i++;
                    h = next;
                }
                if (array.length > s) {
                    array[s] = null;
                }
            } else if (array.length != 0) {
                array[0] = null;
            }
            return array;
        }

        TimedNode<T> getHead() {
            TimedNode<T> index = this.head;
            long limit = this.scheduler.now(this.unit) - this.maxAge;
            TimedNode<T> next = index.get();
            while (next != null && next.time <= limit) {
                index = next;
                next = index.get();
            }
            return index;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public void replay(ReplaySubscription<T> rs) {
            if (rs.getAndIncrement() == 0) {
                int missed = 1;
                Subscriber<? super T> a = rs.downstream;
                TimedNode<T> index = (TimedNode) rs.index;
                if (index == null) {
                    index = getHead();
                }
                long e = rs.emitted;
                do {
                    long r = rs.requested.get();
                    while (e != r) {
                        if (rs.cancelled) {
                            rs.index = null;
                            return;
                        }
                        boolean d = this.done;
                        TimedNode<T> next = index.get();
                        boolean empty = next == null;
                        if (d && empty) {
                            rs.index = null;
                            rs.cancelled = true;
                            Throwable ex = this.error;
                            if (ex == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex);
                                return;
                            }
                        } else if (empty) {
                            break;
                        } else {
                            a.onNext((T) next.value);
                            e++;
                            index = next;
                        }
                    }
                    if (e == r) {
                        if (rs.cancelled) {
                            rs.index = null;
                            return;
                        } else if (this.done && index.get() == null) {
                            rs.index = null;
                            rs.cancelled = true;
                            Throwable ex2 = this.error;
                            if (ex2 == null) {
                                a.onComplete();
                                return;
                            } else {
                                a.onError(ex2);
                                return;
                            }
                        }
                    }
                    rs.index = index;
                    rs.emitted = e;
                    missed = rs.addAndGet(-missed);
                } while (missed != 0);
            }
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public int size() {
            return size(getHead());
        }

        int size(TimedNode<T> h) {
            TimedNode<T> next;
            int s = 0;
            while (s != Integer.MAX_VALUE && (next = h.get()) != null) {
                s++;
                h = next;
            }
            return s;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public Throwable getError() {
            return this.error;
        }

        @Override // p005io.reactivex.processors.ReplayProcessor.ReplayBuffer
        public boolean isDone() {
            return this.done;
        }
    }
}
