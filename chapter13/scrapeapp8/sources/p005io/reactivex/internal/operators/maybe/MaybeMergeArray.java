package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.subscriptions.BasicIntQueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeMergeArray */
/* loaded from: classes.dex */
public final class MaybeMergeArray<T> extends Flowable<T> {
    final MaybeSource<? extends T>[] sources;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.maybe.MaybeMergeArray$SimpleQueueWithConsumerIndex */
    /* loaded from: classes.dex */
    public interface SimpleQueueWithConsumerIndex<T> extends SimpleQueue<T> {
        int consumerIndex();

        void drop();

        T peek();

        @Override // java.util.Queue, p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex, p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        T poll();

        int producerIndex();
    }

    public MaybeMergeArray(MaybeSource<? extends T>[] sources) {
        this.sources = sources;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        SimpleQueueWithConsumerIndex<Object> queue;
        MaybeSource<? extends T>[] maybes = this.sources;
        int n = maybes.length;
        if (n <= bufferSize()) {
            queue = new MpscFillOnceSimpleQueue<>(n);
        } else {
            queue = new ClqSimpleQueue<>();
        }
        MergeMaybeObserver<T> parent = new MergeMaybeObserver<>(s, n, queue);
        s.onSubscribe(parent);
        AtomicThrowable e = parent.error;
        for (MaybeSource<? extends T> source : maybes) {
            if (!parent.isCancelled() && e.get() == null) {
                source.subscribe(parent);
            } else {
                return;
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeMergeArray$MergeMaybeObserver */
    /* loaded from: classes.dex */
    static final class MergeMaybeObserver<T> extends BasicIntQueueSubscription<T> implements MaybeObserver<T> {
        private static final long serialVersionUID = -660395290758764731L;
        volatile boolean cancelled;
        long consumed;
        final Subscriber<? super T> downstream;
        boolean outputFused;
        final SimpleQueueWithConsumerIndex<Object> queue;
        final int sourceCount;
        final CompositeDisposable set = new CompositeDisposable();
        final AtomicLong requested = new AtomicLong();
        final AtomicThrowable error = new AtomicThrowable();

        MergeMaybeObserver(Subscriber<? super T> actual, int sourceCount, SimpleQueueWithConsumerIndex<Object> queue) {
            this.downstream = actual;
            this.sourceCount = sourceCount;
            this.queue = queue;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            if ((mode & 2) == 0) {
                return 0;
            }
            this.outputFused = true;
            return 2;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T t;
            do {
                t = (T) this.queue.poll();
            } while (t == NotificationLite.COMPLETE);
            return t;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.queue.clear();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.set.dispose();
                if (getAndIncrement() == 0) {
                    this.queue.clear();
                }
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T value) {
            this.queue.offer(value);
            drain();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable e) {
            if (this.error.addThrowable(e)) {
                this.set.dispose();
                this.queue.offer(NotificationLite.COMPLETE);
                drain();
                return;
            }
            RxJavaPlugins.onError(e);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.queue.offer(NotificationLite.COMPLETE);
            drain();
        }

        boolean isCancelled() {
            return this.cancelled;
        }

        void drainNormal() {
            int missed = 1;
            Subscriber<? super T> a = this.downstream;
            SimpleQueueWithConsumerIndex<Object> q = this.queue;
            long e = this.consumed;
            do {
                long r = this.requested.get();
                while (e != r) {
                    if (this.cancelled) {
                        q.clear();
                        return;
                    } else if (this.error.get() != null) {
                        q.clear();
                        a.onError(this.error.terminate());
                        return;
                    } else if (q.consumerIndex() == this.sourceCount) {
                        a.onComplete();
                        return;
                    } else {
                        Object v = q.poll();
                        if (v == null) {
                            break;
                        } else if (v != NotificationLite.COMPLETE) {
                            a.onNext(v);
                            e++;
                        }
                    }
                }
                if (e == r) {
                    if (this.error.get() != null) {
                        q.clear();
                        a.onError(this.error.terminate());
                        return;
                    }
                    while (q.peek() == NotificationLite.COMPLETE) {
                        q.drop();
                    }
                    if (q.consumerIndex() == this.sourceCount) {
                        a.onComplete();
                        return;
                    }
                }
                this.consumed = e;
                missed = addAndGet(-missed);
            } while (missed != 0);
        }

        void drainFused() {
            int missed = 1;
            Subscriber<? super T> a = this.downstream;
            SimpleQueueWithConsumerIndex<Object> q = this.queue;
            while (!this.cancelled) {
                Throwable ex = this.error.get();
                if (ex != null) {
                    q.clear();
                    a.onError(ex);
                    return;
                }
                boolean d = q.producerIndex() == this.sourceCount;
                if (!q.isEmpty()) {
                    a.onNext(null);
                }
                if (d) {
                    a.onComplete();
                    return;
                }
                missed = addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            }
            q.clear();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                if (this.outputFused) {
                    drainFused();
                } else {
                    drainNormal();
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeMergeArray$MpscFillOnceSimpleQueue */
    /* loaded from: classes.dex */
    static final class MpscFillOnceSimpleQueue<T> extends AtomicReferenceArray<T> implements SimpleQueueWithConsumerIndex<T> {
        private static final long serialVersionUID = -7969063454040569579L;
        int consumerIndex;
        final AtomicInteger producerIndex = new AtomicInteger();

        MpscFillOnceSimpleQueue(int length) {
            super(length);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean offer(T value) {
            ObjectHelper.requireNonNull(value, "value is null");
            int idx = this.producerIndex.getAndIncrement();
            if (idx >= length()) {
                return false;
            }
            lazySet(idx, value);
            return true;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean offer(T v1, T v2) {
            throw new UnsupportedOperationException();
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex, java.util.Queue, p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() {
            int ci = this.consumerIndex;
            if (ci == length()) {
                return null;
            }
            AtomicInteger pi = this.producerIndex;
            do {
                T v = get(ci);
                if (v != null) {
                    this.consumerIndex = ci + 1;
                    lazySet(ci, null);
                    return v;
                }
            } while (pi.get() != ci);
            return null;
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public T peek() {
            int ci = this.consumerIndex;
            if (ci == length()) {
                return null;
            }
            return get(ci);
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public void drop() {
            int ci = this.consumerIndex;
            lazySet(ci, null);
            this.consumerIndex = ci + 1;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.consumerIndex == producerIndex();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            while (poll() != null && !isEmpty()) {
            }
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public int consumerIndex() {
            return this.consumerIndex;
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public int producerIndex() {
            return this.producerIndex.get();
        }
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeMergeArray$ClqSimpleQueue */
    /* loaded from: classes.dex */
    static final class ClqSimpleQueue<T> extends ConcurrentLinkedQueue<T> implements SimpleQueueWithConsumerIndex<T> {
        private static final long serialVersionUID = -4025173261791142821L;
        int consumerIndex;
        final AtomicInteger producerIndex = new AtomicInteger();

        ClqSimpleQueue() {
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean offer(T v1, T v2) {
            throw new UnsupportedOperationException();
        }

        @Override // java.util.concurrent.ConcurrentLinkedQueue, java.util.Queue, p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean offer(T e) {
            this.producerIndex.getAndIncrement();
            return super.offer(e);
        }

        @Override // java.util.concurrent.ConcurrentLinkedQueue, java.util.Queue, p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex, p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() {
            T v = (T) super.poll();
            if (v != null) {
                this.consumerIndex++;
            }
            return v;
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public int consumerIndex() {
            return this.consumerIndex;
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public int producerIndex() {
            return this.producerIndex.get();
        }

        @Override // p005io.reactivex.internal.operators.maybe.MaybeMergeArray.SimpleQueueWithConsumerIndex
        public void drop() {
            poll();
        }
    }
}
