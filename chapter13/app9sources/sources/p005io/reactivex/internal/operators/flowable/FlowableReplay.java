package p005io.reactivex.internal.operators.flowable;

import android.support.p003v7.widget.ActivityChooserView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.flowables.ConnectableFlowable;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.ResettableConnectable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.HasUpstreamPublisher;
import p005io.reactivex.internal.subscribers.SubscriberResourceWrapper;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.Timed;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay */
/* loaded from: classes.dex */
public final class FlowableReplay<T> extends ConnectableFlowable<T> implements HasUpstreamPublisher<T>, ResettableConnectable {
    static final Callable DEFAULT_UNBOUNDED_FACTORY = new DefaultUnboundedFactory();
    final Callable<? extends ReplayBuffer<T>> bufferFactory;
    final AtomicReference<ReplaySubscriber<T>> current;
    final Publisher<T> onSubscribe;
    final Flowable<T> source;

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$ReplayBuffer */
    /* loaded from: classes.dex */
    interface ReplayBuffer<T> {
        void complete();

        void error(Throwable th);

        void next(T t);

        void replay(InnerSubscription<T> innerSubscription);
    }

    public static <U, R> Flowable<R> multicastSelector(Callable<? extends ConnectableFlowable<U>> callable, Function<? super Flowable<U>, ? extends Publisher<R>> function) {
        return new MulticastFlowable(callable, function);
    }

    public static <T> ConnectableFlowable<T> observeOn(ConnectableFlowable<T> connectableFlowable, Scheduler scheduler) {
        return RxJavaPlugins.onAssembly((ConnectableFlowable) new ConnectableFlowableReplay(connectableFlowable, connectableFlowable.observeOn(scheduler)));
    }

    public static <T> ConnectableFlowable<T> createFrom(Flowable<? extends T> flowable) {
        return create(flowable, DEFAULT_UNBOUNDED_FACTORY);
    }

    public static <T> ConnectableFlowable<T> create(Flowable<T> flowable, int i) {
        if (i == Integer.MAX_VALUE) {
            return createFrom(flowable);
        }
        return create(flowable, new ReplayBufferTask(i));
    }

    public static <T> ConnectableFlowable<T> create(Flowable<T> flowable, long j, TimeUnit timeUnit, Scheduler scheduler) {
        return create(flowable, j, timeUnit, scheduler, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
    }

    public static <T> ConnectableFlowable<T> create(Flowable<T> flowable, long j, TimeUnit timeUnit, Scheduler scheduler, int i) {
        return create(flowable, new ScheduledReplayBufferTask(i, j, timeUnit, scheduler));
    }

    static <T> ConnectableFlowable<T> create(Flowable<T> flowable, Callable<? extends ReplayBuffer<T>> callable) {
        AtomicReference atomicReference = new AtomicReference();
        return RxJavaPlugins.onAssembly((ConnectableFlowable) new FlowableReplay(new ReplayPublisher(atomicReference, callable), flowable, atomicReference, callable));
    }

    private FlowableReplay(Publisher<T> publisher, Flowable<T> flowable, AtomicReference<ReplaySubscriber<T>> atomicReference, Callable<? extends ReplayBuffer<T>> callable) {
        this.onSubscribe = publisher;
        this.source = flowable;
        this.current = atomicReference;
        this.bufferFactory = callable;
    }

    @Override // p005io.reactivex.internal.fuseable.HasUpstreamPublisher
    public Publisher<T> source() {
        return this.source;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.onSubscribe.subscribe(subscriber);
    }

    @Override // p005io.reactivex.internal.disposables.ResettableConnectable
    public void resetIf(Disposable disposable) {
        this.current.compareAndSet((ReplaySubscriber) disposable, null);
    }

    @Override // p005io.reactivex.flowables.ConnectableFlowable
    public void connect(Consumer<? super Disposable> consumer) {
        ReplaySubscriber<T> replaySubscriber;
        while (true) {
            replaySubscriber = this.current.get();
            if (replaySubscriber != null && !replaySubscriber.isDisposed()) {
                break;
            }
            try {
                ReplaySubscriber<T> replaySubscriber2 = new ReplaySubscriber<>((ReplayBuffer) this.bufferFactory.call());
                if (this.current.compareAndSet(replaySubscriber, replaySubscriber2)) {
                    replaySubscriber = replaySubscriber2;
                    break;
                }
            } finally {
                Exceptions.throwIfFatal(th);
                RuntimeException wrapOrThrow = ExceptionHelper.wrapOrThrow(th);
            }
        }
        boolean z = !replaySubscriber.shouldConnect.get() && replaySubscriber.shouldConnect.compareAndSet(false, true);
        try {
            consumer.accept(replaySubscriber);
            if (z) {
                this.source.subscribe((FlowableSubscriber) replaySubscriber);
            }
        } catch (Throwable th) {
            if (z) {
                replaySubscriber.shouldConnect.compareAndSet(true, false);
            }
            throw ExceptionHelper.wrapOrThrow(th);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$ReplaySubscriber */
    /* loaded from: classes.dex */
    public static final class ReplaySubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T>, Disposable {
        static final InnerSubscription[] EMPTY = new InnerSubscription[0];
        static final InnerSubscription[] TERMINATED = new InnerSubscription[0];
        private static final long serialVersionUID = 7224554242710036740L;
        final ReplayBuffer<T> buffer;
        boolean done;
        long maxChildRequested;
        long maxUpstreamRequested;
        final AtomicInteger management = new AtomicInteger();
        final AtomicReference<InnerSubscription<T>[]> subscribers = new AtomicReference<>(EMPTY);
        final AtomicBoolean shouldConnect = new AtomicBoolean();

        ReplaySubscriber(ReplayBuffer<T> replayBuffer) {
            this.buffer = replayBuffer;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.subscribers.get() == TERMINATED;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.subscribers.set(TERMINATED);
            SubscriptionHelper.cancel(this);
        }

        boolean add(InnerSubscription<T> innerSubscription) {
            InnerSubscription<T>[] innerSubscriptionArr;
            InnerSubscription<T>[] innerSubscriptionArr2;
            if (innerSubscription != null) {
                do {
                    innerSubscriptionArr = this.subscribers.get();
                    if (innerSubscriptionArr == TERMINATED) {
                        return false;
                    }
                    int length = innerSubscriptionArr.length;
                    innerSubscriptionArr2 = new InnerSubscription[length + 1];
                    System.arraycopy(innerSubscriptionArr, 0, innerSubscriptionArr2, 0, length);
                    innerSubscriptionArr2[length] = innerSubscription;
                } while (!this.subscribers.compareAndSet(innerSubscriptionArr, innerSubscriptionArr2));
                return true;
            }
            throw new NullPointerException();
        }

        void remove(InnerSubscription<T> innerSubscription) {
            InnerSubscription<T>[] innerSubscriptionArr;
            InnerSubscription<T>[] innerSubscriptionArr2;
            do {
                innerSubscriptionArr = this.subscribers.get();
                int length = innerSubscriptionArr.length;
                if (length != 0) {
                    int i = -1;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length) {
                            break;
                        } else if (innerSubscriptionArr[i2].equals(innerSubscription)) {
                            i = i2;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (i >= 0) {
                        if (length == 1) {
                            innerSubscriptionArr2 = EMPTY;
                        } else {
                            InnerSubscription<T>[] innerSubscriptionArr3 = new InnerSubscription[length - 1];
                            System.arraycopy(innerSubscriptionArr, 0, innerSubscriptionArr3, 0, i);
                            System.arraycopy(innerSubscriptionArr, i + 1, innerSubscriptionArr3, i, (length - i) - 1);
                            innerSubscriptionArr2 = innerSubscriptionArr3;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } while (!this.subscribers.compareAndSet(innerSubscriptionArr, innerSubscriptionArr2));
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.setOnce(this, subscription)) {
                manageRequests();
                for (InnerSubscription<T> innerSubscription : this.subscribers.get()) {
                    this.buffer.replay(innerSubscription);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                this.buffer.next(t);
                for (InnerSubscription<T> innerSubscription : this.subscribers.get()) {
                    this.buffer.replay(innerSubscription);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (!this.done) {
                this.done = true;
                this.buffer.error(th);
                for (InnerSubscription<T> innerSubscription : this.subscribers.getAndSet(TERMINATED)) {
                    this.buffer.replay(innerSubscription);
                }
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.buffer.complete();
                for (InnerSubscription<T> innerSubscription : this.subscribers.getAndSet(TERMINATED)) {
                    this.buffer.replay(innerSubscription);
                }
            }
        }

        void manageRequests() {
            if (this.management.getAndIncrement() == 0) {
                int i = 1;
                while (!isDisposed()) {
                    InnerSubscription<T>[] innerSubscriptionArr = this.subscribers.get();
                    long j = this.maxChildRequested;
                    long j2 = j;
                    for (InnerSubscription<T> innerSubscription : innerSubscriptionArr) {
                        j2 = Math.max(j2, innerSubscription.totalRequested.get());
                    }
                    long j3 = this.maxUpstreamRequested;
                    Subscription subscription = get();
                    long j4 = j2 - j;
                    if (j4 != 0) {
                        this.maxChildRequested = j2;
                        if (subscription == null) {
                            long j5 = j3 + j4;
                            if (j5 < 0) {
                                j5 = Long.MAX_VALUE;
                            }
                            this.maxUpstreamRequested = j5;
                        } else if (j3 != 0) {
                            this.maxUpstreamRequested = 0;
                            subscription.request(j3 + j4);
                        } else {
                            subscription.request(j4);
                        }
                    } else if (!(j3 == 0 || subscription == null)) {
                        this.maxUpstreamRequested = 0;
                        subscription.request(j3);
                    }
                    i = this.management.addAndGet(-i);
                    if (i == 0) {
                        return;
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$InnerSubscription */
    /* loaded from: classes.dex */
    public static final class InnerSubscription<T> extends AtomicLong implements Subscription, Disposable {
        static final long CANCELLED = Long.MIN_VALUE;
        private static final long serialVersionUID = -4453897557930727610L;
        final Subscriber<? super T> child;
        boolean emitting;
        Object index;
        boolean missed;
        final ReplaySubscriber<T> parent;
        final AtomicLong totalRequested = new AtomicLong();

        InnerSubscription(ReplaySubscriber<T> replaySubscriber, Subscriber<? super T> subscriber) {
            this.parent = replaySubscriber;
            this.child = subscriber;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j) && BackpressureHelper.addCancel(this, j) != CANCELLED) {
                BackpressureHelper.add(this.totalRequested, j);
                this.parent.manageRequests();
                this.parent.buffer.replay(this);
            }
        }

        public long produced(long j) {
            return BackpressureHelper.producedCancel(this, j);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() == CANCELLED;
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (getAndSet(CANCELLED) != CANCELLED) {
                this.parent.remove(this);
                this.parent.manageRequests();
            }
        }

        <U> U index() {
            return (U) this.index;
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$UnboundedReplayBuffer */
    /* loaded from: classes.dex */
    static final class UnboundedReplayBuffer<T> extends ArrayList<Object> implements ReplayBuffer<T> {
        private static final long serialVersionUID = 7063189396499112664L;
        volatile int size;

        UnboundedReplayBuffer(int i) {
            super(i);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public void next(T t) {
            add(NotificationLite.next(t));
            this.size++;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public void error(Throwable th) {
            add(NotificationLite.error(th));
            this.size++;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public void complete() {
            add(NotificationLite.complete());
            this.size++;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public void replay(InnerSubscription<T> innerSubscription) {
            synchronized (innerSubscription) {
                if (innerSubscription.emitting) {
                    innerSubscription.missed = true;
                    return;
                }
                innerSubscription.emitting = true;
                Subscriber<? super T> subscriber = innerSubscription.child;
                while (!innerSubscription.isDisposed()) {
                    int i = this.size;
                    Integer num = (Integer) innerSubscription.index();
                    int intValue = num != null ? num.intValue() : 0;
                    long j = innerSubscription.get();
                    long j2 = j;
                    long j3 = 0;
                    while (j2 != 0 && intValue < i) {
                        Object obj = get(intValue);
                        try {
                            if (!NotificationLite.accept(obj, subscriber) && !innerSubscription.isDisposed()) {
                                intValue++;
                                j2--;
                                j3++;
                            } else {
                                return;
                            }
                        } catch (Throwable th) {
                            Exceptions.throwIfFatal(th);
                            innerSubscription.dispose();
                            if (!NotificationLite.isError(obj) && !NotificationLite.isComplete(obj)) {
                                subscriber.onError(th);
                                return;
                            }
                            return;
                        }
                    }
                    if (j3 != 0) {
                        innerSubscription.index = Integer.valueOf(intValue);
                        if (j != Long.MAX_VALUE) {
                            innerSubscription.produced(j3);
                        }
                    }
                    synchronized (innerSubscription) {
                        if (!innerSubscription.missed) {
                            innerSubscription.emitting = false;
                            return;
                        }
                        innerSubscription.missed = false;
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$Node */
    /* loaded from: classes.dex */
    public static final class Node extends AtomicReference<Node> {
        private static final long serialVersionUID = 245354315435971818L;
        final long index;
        final Object value;

        Node(Object obj, long j) {
            this.value = obj;
            this.index = j;
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$BoundedReplayBuffer */
    /* loaded from: classes.dex */
    static class BoundedReplayBuffer<T> extends AtomicReference<Node> implements ReplayBuffer<T> {
        private static final long serialVersionUID = 2346567790059478686L;
        long index;
        int size;
        Node tail;

        Object enterTransform(Object obj) {
            return obj;
        }

        Object leaveTransform(Object obj) {
            return obj;
        }

        void truncate() {
        }

        BoundedReplayBuffer() {
            Node node = new Node(null, 0);
            this.tail = node;
            set(node);
        }

        final void addLast(Node node) {
            this.tail.set(node);
            this.tail = node;
            this.size++;
        }

        final void removeFirst() {
            Node node = get().get();
            if (node != null) {
                this.size--;
                setFirst(node);
                return;
            }
            throw new IllegalStateException("Empty list!");
        }

        final void removeSome(int i) {
            Node node = get();
            while (i > 0) {
                node = node.get();
                i--;
                this.size--;
            }
            setFirst(node);
        }

        final void setFirst(Node node) {
            set(node);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public final void next(T t) {
            Object enterTransform = enterTransform(NotificationLite.next(t));
            long j = this.index + 1;
            this.index = j;
            addLast(new Node(enterTransform, j));
            truncate();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public final void error(Throwable th) {
            Object enterTransform = enterTransform(NotificationLite.error(th));
            long j = this.index + 1;
            this.index = j;
            addLast(new Node(enterTransform, j));
            truncateFinal();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public final void complete() {
            Object enterTransform = enterTransform(NotificationLite.complete());
            long j = this.index + 1;
            this.index = j;
            addLast(new Node(enterTransform, j));
            truncateFinal();
        }

        final void trimHead() {
            Node node = get();
            if (node.value != null) {
                Node node2 = new Node(null, 0);
                node2.lazySet(node.get());
                set(node2);
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.ReplayBuffer
        public final void replay(InnerSubscription<T> innerSubscription) {
            long j;
            Node node;
            synchronized (innerSubscription) {
                if (innerSubscription.emitting) {
                    innerSubscription.missed = true;
                    return;
                }
                innerSubscription.emitting = true;
                while (!innerSubscription.isDisposed()) {
                    long j2 = innerSubscription.get();
                    boolean z = j2 == Long.MAX_VALUE;
                    Node node2 = (Node) innerSubscription.index();
                    if (node2 == null) {
                        node2 = getHead();
                        innerSubscription.index = node2;
                        BackpressureHelper.add(innerSubscription.totalRequested, node2.index);
                        j = 0;
                    } else {
                        j = 0;
                    }
                    while (j2 != 0 && (node = node2.get()) != null) {
                        Object leaveTransform = leaveTransform(node.value);
                        try {
                            if (NotificationLite.accept(leaveTransform, innerSubscription.child)) {
                                innerSubscription.index = null;
                                return;
                            }
                            j++;
                            j2--;
                            if (!innerSubscription.isDisposed()) {
                                node2 = node;
                            } else {
                                return;
                            }
                        } catch (Throwable th) {
                            Exceptions.throwIfFatal(th);
                            innerSubscription.index = null;
                            innerSubscription.dispose();
                            if (!NotificationLite.isError(leaveTransform) && !NotificationLite.isComplete(leaveTransform)) {
                                innerSubscription.child.onError(th);
                                return;
                            }
                            return;
                        }
                    }
                    if (j != 0) {
                        innerSubscription.index = node2;
                        if (!z) {
                            innerSubscription.produced(j);
                        }
                    }
                    synchronized (innerSubscription) {
                        if (!innerSubscription.missed) {
                            innerSubscription.emitting = false;
                            return;
                        }
                        innerSubscription.missed = false;
                    }
                }
            }
        }

        void truncateFinal() {
            trimHead();
        }

        final void collect(Collection<? super T> collection) {
            Node head = getHead();
            while (true) {
                head = head.get();
                if (head != null) {
                    Object leaveTransform = leaveTransform(head.value);
                    if (!NotificationLite.isComplete(leaveTransform) && !NotificationLite.isError(leaveTransform)) {
                        collection.add((Object) NotificationLite.getValue(leaveTransform));
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        boolean hasError() {
            return this.tail.value != null && NotificationLite.isError(leaveTransform(this.tail.value));
        }

        boolean hasCompleted() {
            return this.tail.value != null && NotificationLite.isComplete(leaveTransform(this.tail.value));
        }

        Node getHead() {
            return get();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$SizeBoundReplayBuffer */
    /* loaded from: classes.dex */
    public static final class SizeBoundReplayBuffer<T> extends BoundedReplayBuffer<T> {
        private static final long serialVersionUID = -5898283885385201806L;
        final int limit;

        SizeBoundReplayBuffer(int i) {
            this.limit = i;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.BoundedReplayBuffer
        void truncate() {
            if (this.size > this.limit) {
                removeFirst();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$SizeAndTimeBoundReplayBuffer */
    /* loaded from: classes.dex */
    public static final class SizeAndTimeBoundReplayBuffer<T> extends BoundedReplayBuffer<T> {
        private static final long serialVersionUID = 3457957419649567404L;
        final int limit;
        final long maxAge;
        final Scheduler scheduler;
        final TimeUnit unit;

        SizeAndTimeBoundReplayBuffer(int i, long j, TimeUnit timeUnit, Scheduler scheduler) {
            this.scheduler = scheduler;
            this.limit = i;
            this.maxAge = j;
            this.unit = timeUnit;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.BoundedReplayBuffer
        Object enterTransform(Object obj) {
            return new Timed(obj, this.scheduler.now(this.unit), this.unit);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.BoundedReplayBuffer
        Object leaveTransform(Object obj) {
            return ((Timed) obj).value();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.BoundedReplayBuffer
        void truncate() {
            long now = this.scheduler.now(this.unit) - this.maxAge;
            Node node = (Node) get();
            Node node2 = node.get();
            int i = 0;
            Node node3 = node;
            while (node2 != null) {
                if (this.size <= this.limit) {
                    if (((Timed) node2.value).time() > now) {
                        break;
                    }
                    i++;
                    this.size--;
                    node2 = node2.get();
                    node3 = node2;
                } else {
                    i++;
                    this.size--;
                    node2 = node2.get();
                    node3 = node2;
                }
            }
            if (i != 0) {
                setFirst(node3);
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:10:0x0041, code lost:
            setFirst(r3);
         */
        /* JADX WARN: Code restructure failed: missing block: B:11:0x0044, code lost:
            return;
         */
        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.BoundedReplayBuffer
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void truncateFinal() {
            /*
                r10 = this;
                io.reactivex.Scheduler r0 = r10.scheduler
                java.util.concurrent.TimeUnit r1 = r10.unit
                long r0 = r0.now(r1)
                long r2 = r10.maxAge
                long r0 = r0 - r2
                java.lang.Object r2 = r10.get()
                io.reactivex.internal.operators.flowable.FlowableReplay$Node r2 = (p005io.reactivex.internal.operators.flowable.FlowableReplay.Node) r2
                java.lang.Object r3 = r2.get()
                io.reactivex.internal.operators.flowable.FlowableReplay$Node r3 = (p005io.reactivex.internal.operators.flowable.FlowableReplay.Node) r3
                r4 = 0
                r9 = r3
                r3 = r2
                r2 = r9
            L_0x001b:
                if (r2 == 0) goto L_0x003f
                int r5 = r10.size
                r6 = 1
                if (r5 <= r6) goto L_0x003f
                java.lang.Object r5 = r2.value
                io.reactivex.schedulers.Timed r5 = (p005io.reactivex.schedulers.Timed) r5
                long r7 = r5.time()
                int r5 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
                if (r5 > 0) goto L_0x003f
                int r4 = r4 + 1
                int r3 = r10.size
                int r3 = r3 - r6
                r10.size = r3
                java.lang.Object r3 = r2.get()
                io.reactivex.internal.operators.flowable.FlowableReplay$Node r3 = (p005io.reactivex.internal.operators.flowable.FlowableReplay.Node) r3
                r9 = r3
                r3 = r2
                r2 = r9
                goto L_0x001b
            L_0x003f:
                if (r4 == 0) goto L_0x0044
                r10.setFirst(r3)
            L_0x0044:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.flowable.FlowableReplay.SizeAndTimeBoundReplayBuffer.truncateFinal():void");
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableReplay.BoundedReplayBuffer
        Node getHead() {
            long now = this.scheduler.now(this.unit) - this.maxAge;
            Node node = (Node) get();
            Node node2 = node.get();
            Node node3 = node;
            while (node2 != null) {
                Timed timed = (Timed) node2.value;
                if (NotificationLite.isComplete(timed.value()) || NotificationLite.isError(timed.value()) || timed.time() > now) {
                    break;
                }
                node2 = node2.get();
                node3 = node2;
            }
            return node3;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$MulticastFlowable */
    /* loaded from: classes.dex */
    public static final class MulticastFlowable<R, U> extends Flowable<R> {
        private final Callable<? extends ConnectableFlowable<U>> connectableFactory;
        private final Function<? super Flowable<U>, ? extends Publisher<R>> selector;

        MulticastFlowable(Callable<? extends ConnectableFlowable<U>> callable, Function<? super Flowable<U>, ? extends Publisher<R>> function) {
            this.connectableFactory = callable;
            this.selector = function;
        }

        @Override // p005io.reactivex.Flowable
        protected void subscribeActual(Subscriber<? super R> subscriber) {
            try {
                ConnectableFlowable connectableFlowable = (ConnectableFlowable) ObjectHelper.requireNonNull(this.connectableFactory.call(), "The connectableFactory returned null");
                try {
                    Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.selector.apply(connectableFlowable), "The selector returned a null Publisher");
                    SubscriberResourceWrapper subscriberResourceWrapper = new SubscriberResourceWrapper(subscriber);
                    publisher.subscribe(subscriberResourceWrapper);
                    connectableFlowable.connect(new DisposableConsumer(subscriberResourceWrapper));
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    EmptySubscription.error(th, subscriber);
                }
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                EmptySubscription.error(th2, subscriber);
            }
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$MulticastFlowable$DisposableConsumer */
        /* loaded from: classes.dex */
        final class DisposableConsumer implements Consumer<Disposable> {
            private final SubscriberResourceWrapper<R> srw;

            DisposableConsumer(SubscriberResourceWrapper<R> subscriberResourceWrapper) {
                this.srw = subscriberResourceWrapper;
            }

            public void accept(Disposable disposable) {
                this.srw.setResource(disposable);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$ConnectableFlowableReplay */
    /* loaded from: classes.dex */
    public static final class ConnectableFlowableReplay<T> extends ConnectableFlowable<T> {

        /* renamed from: cf */
        private final ConnectableFlowable<T> f129cf;
        private final Flowable<T> flowable;

        ConnectableFlowableReplay(ConnectableFlowable<T> connectableFlowable, Flowable<T> flowable) {
            this.f129cf = connectableFlowable;
            this.flowable = flowable;
        }

        @Override // p005io.reactivex.flowables.ConnectableFlowable
        public void connect(Consumer<? super Disposable> consumer) {
            this.f129cf.connect(consumer);
        }

        @Override // p005io.reactivex.Flowable
        protected void subscribeActual(Subscriber<? super T> subscriber) {
            this.flowable.subscribe(subscriber);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$ReplayBufferTask */
    /* loaded from: classes.dex */
    public static final class ReplayBufferTask<T> implements Callable<ReplayBuffer<T>> {
        private final int bufferSize;

        ReplayBufferTask(int i) {
            this.bufferSize = i;
        }

        @Override // java.util.concurrent.Callable
        public ReplayBuffer<T> call() {
            return new SizeBoundReplayBuffer(this.bufferSize);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$ScheduledReplayBufferTask */
    /* loaded from: classes.dex */
    public static final class ScheduledReplayBufferTask<T> implements Callable<ReplayBuffer<T>> {
        private final int bufferSize;
        private final long maxAge;
        private final Scheduler scheduler;
        private final TimeUnit unit;

        ScheduledReplayBufferTask(int i, long j, TimeUnit timeUnit, Scheduler scheduler) {
            this.bufferSize = i;
            this.maxAge = j;
            this.unit = timeUnit;
            this.scheduler = scheduler;
        }

        @Override // java.util.concurrent.Callable
        public ReplayBuffer<T> call() {
            return new SizeAndTimeBoundReplayBuffer(this.bufferSize, this.maxAge, this.unit, this.scheduler);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$ReplayPublisher */
    /* loaded from: classes.dex */
    public static final class ReplayPublisher<T> implements Publisher<T> {
        private final Callable<? extends ReplayBuffer<T>> bufferFactory;
        private final AtomicReference<ReplaySubscriber<T>> curr;

        ReplayPublisher(AtomicReference<ReplaySubscriber<T>> atomicReference, Callable<? extends ReplayBuffer<T>> callable) {
            this.curr = atomicReference;
            this.bufferFactory = callable;
        }

        @Override // org.reactivestreams.Publisher
        public void subscribe(Subscriber<? super T> subscriber) {
            ReplaySubscriber<T> replaySubscriber;
            while (true) {
                replaySubscriber = this.curr.get();
                if (replaySubscriber != null) {
                    break;
                }
                try {
                    ReplaySubscriber<T> replaySubscriber2 = new ReplaySubscriber<>((ReplayBuffer) this.bufferFactory.call());
                    if (this.curr.compareAndSet(null, replaySubscriber2)) {
                        replaySubscriber = replaySubscriber2;
                        break;
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    EmptySubscription.error(th, subscriber);
                    return;
                }
            }
            InnerSubscription<T> innerSubscription = new InnerSubscription<>(replaySubscriber, subscriber);
            subscriber.onSubscribe(innerSubscription);
            replaySubscriber.add(innerSubscription);
            if (innerSubscription.isDisposed()) {
                replaySubscriber.remove(innerSubscription);
                return;
            }
            replaySubscriber.manageRequests();
            replaySubscriber.buffer.replay(innerSubscription);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableReplay$DefaultUnboundedFactory */
    /* loaded from: classes.dex */
    static final class DefaultUnboundedFactory implements Callable<Object> {
        DefaultUnboundedFactory() {
        }

        @Override // java.util.concurrent.Callable
        public Object call() {
            return new UnboundedReplayBuffer(16);
        }
    }
}
