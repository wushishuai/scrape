package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.LinkedArrayList;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableCache */
/* loaded from: classes.dex */
public final class FlowableCache<T> extends AbstractFlowableWithUpstream<T, T> {
    final AtomicBoolean once = new AtomicBoolean();
    final CacheState<T> state;

    public FlowableCache(Flowable<T> flowable, int i) {
        super(flowable);
        this.state = new CacheState<>(flowable, i);
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        boolean z;
        ReplaySubscription<T> replaySubscription = new ReplaySubscription<>(subscriber, this.state);
        subscriber.onSubscribe(replaySubscription);
        if (!this.state.addChild(replaySubscription) || replaySubscription.requested.get() != Long.MIN_VALUE) {
            z = true;
        } else {
            this.state.removeChild(replaySubscription);
            z = false;
        }
        if (!this.once.get() && this.once.compareAndSet(false, true)) {
            this.state.connect();
        }
        if (z) {
            replaySubscription.replay();
        }
    }

    boolean isConnected() {
        return this.state.isConnected;
    }

    boolean hasSubscribers() {
        return this.state.subscribers.get().length != 0;
    }

    int cachedEventCount() {
        return this.state.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCache$CacheState */
    /* loaded from: classes.dex */
    public static final class CacheState<T> extends LinkedArrayList implements FlowableSubscriber<T> {
        static final ReplaySubscription[] EMPTY = new ReplaySubscription[0];
        static final ReplaySubscription[] TERMINATED = new ReplaySubscription[0];
        volatile boolean isConnected;
        final Flowable<T> source;
        boolean sourceDone;
        final AtomicReference<Subscription> connection = new AtomicReference<>();
        final AtomicReference<ReplaySubscription<T>[]> subscribers = new AtomicReference<>(EMPTY);

        CacheState(Flowable<T> flowable, int i) {
            super(i);
            this.source = flowable;
        }

        public boolean addChild(ReplaySubscription<T> replaySubscription) {
            ReplaySubscription<T>[] replaySubscriptionArr;
            ReplaySubscription<T>[] replaySubscriptionArr2;
            do {
                replaySubscriptionArr = this.subscribers.get();
                if (replaySubscriptionArr == TERMINATED) {
                    return false;
                }
                int length = replaySubscriptionArr.length;
                replaySubscriptionArr2 = new ReplaySubscription[length + 1];
                System.arraycopy(replaySubscriptionArr, 0, replaySubscriptionArr2, 0, length);
                replaySubscriptionArr2[length] = replaySubscription;
            } while (!this.subscribers.compareAndSet(replaySubscriptionArr, replaySubscriptionArr2));
            return true;
        }

        public void removeChild(ReplaySubscription<T> replaySubscription) {
            ReplaySubscription<T>[] replaySubscriptionArr;
            ReplaySubscription<T>[] replaySubscriptionArr2;
            do {
                replaySubscriptionArr = this.subscribers.get();
                int length = replaySubscriptionArr.length;
                if (length != 0) {
                    int i = -1;
                    int i2 = 0;
                    while (true) {
                        if (i2 >= length) {
                            break;
                        } else if (replaySubscriptionArr[i2].equals(replaySubscription)) {
                            i = i2;
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (i >= 0) {
                        if (length == 1) {
                            replaySubscriptionArr2 = EMPTY;
                        } else {
                            ReplaySubscription<T>[] replaySubscriptionArr3 = new ReplaySubscription[length - 1];
                            System.arraycopy(replaySubscriptionArr, 0, replaySubscriptionArr3, 0, i);
                            System.arraycopy(replaySubscriptionArr, i + 1, replaySubscriptionArr3, i, (length - i) - 1);
                            replaySubscriptionArr2 = replaySubscriptionArr3;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } while (!this.subscribers.compareAndSet(replaySubscriptionArr, replaySubscriptionArr2));
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this.connection, subscription, Long.MAX_VALUE);
        }

        public void connect() {
            this.source.subscribe((FlowableSubscriber) this);
            this.isConnected = true;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.sourceDone) {
                add(NotificationLite.next(t));
                for (ReplaySubscription<T> replaySubscription : this.subscribers.get()) {
                    replaySubscription.replay();
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (!this.sourceDone) {
                this.sourceDone = true;
                add(NotificationLite.error(th));
                SubscriptionHelper.cancel(this.connection);
                for (ReplaySubscription<T> replaySubscription : this.subscribers.getAndSet(TERMINATED)) {
                    replaySubscription.replay();
                }
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.sourceDone) {
                this.sourceDone = true;
                add(NotificationLite.complete());
                SubscriptionHelper.cancel(this.connection);
                for (ReplaySubscription<T> replaySubscription : this.subscribers.getAndSet(TERMINATED)) {
                    replaySubscription.replay();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCache$ReplaySubscription */
    /* loaded from: classes.dex */
    public static final class ReplaySubscription<T> extends AtomicInteger implements Subscription {
        private static final long CANCELLED = Long.MIN_VALUE;
        private static final long serialVersionUID = -2557562030197141021L;
        final Subscriber<? super T> child;
        Object[] currentBuffer;
        int currentIndexInBuffer;
        long emitted;
        int index;
        final AtomicLong requested = new AtomicLong();
        final CacheState<T> state;

        ReplaySubscription(Subscriber<? super T> subscriber, CacheState<T> cacheState) {
            this.child = subscriber;
            this.state = cacheState;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.addCancel(this.requested, j);
                replay();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (this.requested.getAndSet(CANCELLED) != CANCELLED) {
                this.state.removeChild(this);
            }
        }

        public void replay() {
            if (getAndIncrement() == 0) {
                Subscriber<? super T> subscriber = this.child;
                AtomicLong atomicLong = this.requested;
                long j = this.emitted;
                int i = 1;
                int i2 = 1;
                while (true) {
                    long j2 = atomicLong.get();
                    if (j2 != CANCELLED) {
                        int size = this.state.size();
                        if (size != 0) {
                            Object[] objArr = this.currentBuffer;
                            if (objArr == null) {
                                objArr = this.state.head();
                                this.currentBuffer = objArr;
                            }
                            int length = objArr.length - i;
                            int i3 = this.index;
                            int i4 = this.currentIndexInBuffer;
                            while (i3 < size && j != j2) {
                                if (atomicLong.get() != CANCELLED) {
                                    if (i4 == length) {
                                        objArr = (Object[]) objArr[length];
                                        i4 = 0;
                                    }
                                    if (!NotificationLite.accept(objArr[i4], subscriber)) {
                                        i4++;
                                        i3++;
                                        j++;
                                    } else {
                                        return;
                                    }
                                } else {
                                    return;
                                }
                            }
                            if (atomicLong.get() != CANCELLED) {
                                if (j2 == j) {
                                    Object obj = objArr[i4];
                                    if (NotificationLite.isComplete(obj)) {
                                        subscriber.onComplete();
                                        return;
                                    } else if (NotificationLite.isError(obj)) {
                                        subscriber.onError(NotificationLite.getError(obj));
                                        return;
                                    }
                                }
                                this.index = i3;
                                this.currentIndexInBuffer = i4;
                                this.currentBuffer = objArr;
                            } else {
                                return;
                            }
                        }
                        this.emitted = j;
                        i2 = addAndGet(-i2);
                        if (i2 != 0) {
                            i = 1;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }
    }
}
