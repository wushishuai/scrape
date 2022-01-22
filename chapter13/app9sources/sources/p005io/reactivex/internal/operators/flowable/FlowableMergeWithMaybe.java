package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableMergeWithMaybe */
/* loaded from: classes.dex */
public final class FlowableMergeWithMaybe<T> extends AbstractFlowableWithUpstream<T, T> {
    final MaybeSource<? extends T> other;

    public FlowableMergeWithMaybe(Flowable<T> flowable, MaybeSource<? extends T> maybeSource) {
        super(flowable);
        this.other = maybeSource;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        MergeWithObserver mergeWithObserver = new MergeWithObserver(subscriber);
        subscriber.onSubscribe(mergeWithObserver);
        this.source.subscribe((FlowableSubscriber) mergeWithObserver);
        this.other.subscribe(mergeWithObserver.otherObserver);
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableMergeWithMaybe$MergeWithObserver */
    /* loaded from: classes.dex */
    static final class MergeWithObserver<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {
        static final int OTHER_STATE_CONSUMED_OR_EMPTY = 2;
        static final int OTHER_STATE_HAS_VALUE = 1;
        private static final long serialVersionUID = -4592979584110982903L;
        volatile boolean cancelled;
        int consumed;
        final Subscriber<? super T> downstream;
        long emitted;
        final int limit;
        volatile boolean mainDone;
        volatile int otherState;
        volatile SimplePlainQueue<T> queue;
        T singleItem;
        final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();
        final OtherObserver<T> otherObserver = new OtherObserver<>(this);
        final AtomicThrowable error = new AtomicThrowable();
        final AtomicLong requested = new AtomicLong();
        final int prefetch = Flowable.bufferSize();

        MergeWithObserver(Subscriber<? super T> subscriber) {
            this.downstream = subscriber;
            int i = this.prefetch;
            this.limit = i - (i >> 2);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this.mainSubscription, subscription, (long) this.prefetch);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (compareAndSet(0, 1)) {
                long j = this.emitted;
                if (this.requested.get() != j) {
                    SimplePlainQueue<T> simplePlainQueue = this.queue;
                    if (simplePlainQueue == null || simplePlainQueue.isEmpty()) {
                        this.emitted = j + 1;
                        this.downstream.onNext(t);
                        int i = this.consumed + 1;
                        if (i == this.limit) {
                            this.consumed = 0;
                            this.mainSubscription.get().request((long) i);
                        } else {
                            this.consumed = i;
                        }
                    } else {
                        simplePlainQueue.offer(t);
                    }
                } else {
                    getOrCreateQueue().offer(t);
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                getOrCreateQueue().offer(t);
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.error.addThrowable(th)) {
                SubscriptionHelper.cancel(this.mainSubscription);
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.mainDone = true;
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            BackpressureHelper.add(this.requested, j);
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
            SubscriptionHelper.cancel(this.mainSubscription);
            DisposableHelper.dispose(this.otherObserver);
            if (getAndIncrement() == 0) {
                this.queue = null;
                this.singleItem = null;
            }
        }

        void otherSuccess(T t) {
            if (compareAndSet(0, 1)) {
                long j = this.emitted;
                if (this.requested.get() != j) {
                    this.emitted = j + 1;
                    this.downstream.onNext(t);
                    this.otherState = 2;
                } else {
                    this.singleItem = t;
                    this.otherState = 1;
                    if (decrementAndGet() == 0) {
                        return;
                    }
                }
            } else {
                this.singleItem = t;
                this.otherState = 1;
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        void otherError(Throwable th) {
            if (this.error.addThrowable(th)) {
                SubscriptionHelper.cancel(this.mainSubscription);
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        void otherComplete() {
            this.otherState = 2;
            drain();
        }

        SimplePlainQueue<T> getOrCreateQueue() {
            SimplePlainQueue<T> simplePlainQueue = this.queue;
            if (simplePlainQueue != null) {
                return simplePlainQueue;
            }
            SpscArrayQueue spscArrayQueue = new SpscArrayQueue(Flowable.bufferSize());
            this.queue = spscArrayQueue;
            return spscArrayQueue;
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void drainLoop() {
            Subscriber<? super T> subscriber = this.downstream;
            long j = this.emitted;
            int i = this.consumed;
            int i2 = this.limit;
            int i3 = 1;
            long j2 = j;
            int i4 = 1;
            while (true) {
                long j3 = this.requested.get();
                while (j2 != j3) {
                    if (this.cancelled) {
                        this.singleItem = null;
                        this.queue = null;
                        return;
                    } else if (this.error.get() != null) {
                        this.singleItem = null;
                        this.queue = null;
                        subscriber.onError(this.error.terminate());
                        return;
                    } else {
                        int i5 = this.otherState;
                        if (i5 == i3) {
                            this.singleItem = null;
                            this.otherState = 2;
                            subscriber.onNext((T) this.singleItem);
                            j2++;
                        } else {
                            boolean z = this.mainDone;
                            SimplePlainQueue<T> simplePlainQueue = this.queue;
                            T poll = simplePlainQueue != null ? simplePlainQueue.poll() : (Object) null;
                            boolean z2 = poll == null;
                            if (z && z2 && i5 == 2) {
                                this.queue = null;
                                subscriber.onComplete();
                                return;
                            } else if (z2) {
                                break;
                            } else {
                                subscriber.onNext(poll);
                                j2++;
                                i++;
                                if (i == i2) {
                                    this.mainSubscription.get().request((long) i2);
                                    i = 0;
                                }
                                i3 = 1;
                            }
                        }
                    }
                }
                if (j2 == j3) {
                    if (this.cancelled) {
                        this.singleItem = null;
                        this.queue = null;
                        return;
                    } else if (this.error.get() != null) {
                        this.singleItem = null;
                        this.queue = null;
                        subscriber.onError(this.error.terminate());
                        return;
                    } else {
                        boolean z3 = this.mainDone;
                        SimplePlainQueue<T> simplePlainQueue2 = this.queue;
                        boolean z4 = simplePlainQueue2 == null || simplePlainQueue2.isEmpty();
                        if (z3 && z4 && this.otherState == 2) {
                            this.queue = null;
                            subscriber.onComplete();
                            return;
                        }
                    }
                }
                this.emitted = j2;
                this.consumed = i;
                i4 = addAndGet(-i4);
                if (i4 != 0) {
                    i3 = 1;
                } else {
                    return;
                }
            }
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableMergeWithMaybe$MergeWithObserver$OtherObserver */
        /* loaded from: classes.dex */
        static final class OtherObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T> {
            private static final long serialVersionUID = -2935427570954647017L;
            final MergeWithObserver<T> parent;

            OtherObserver(MergeWithObserver<T> mergeWithObserver) {
                this.parent = mergeWithObserver;
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(this, disposable);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSuccess(T t) {
                this.parent.otherSuccess(t);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onError(Throwable th) {
                this.parent.otherError(th);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.parent.otherComplete();
            }
        }
    }
}
