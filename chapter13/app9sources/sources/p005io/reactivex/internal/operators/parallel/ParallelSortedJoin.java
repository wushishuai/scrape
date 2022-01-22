package p005io.reactivex.internal.operators.parallel;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.parallel.ParallelFlowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelSortedJoin */
/* loaded from: classes.dex */
public final class ParallelSortedJoin<T> extends Flowable<T> {
    final Comparator<? super T> comparator;
    final ParallelFlowable<List<T>> source;

    public ParallelSortedJoin(ParallelFlowable<List<T>> parallelFlowable, Comparator<? super T> comparator) {
        this.source = parallelFlowable;
        this.comparator = comparator;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        SortedJoinSubscription sortedJoinSubscription = new SortedJoinSubscription(subscriber, this.source.parallelism(), this.comparator);
        subscriber.onSubscribe(sortedJoinSubscription);
        this.source.subscribe(sortedJoinSubscription.subscribers);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelSortedJoin$SortedJoinSubscription */
    /* loaded from: classes.dex */
    public static final class SortedJoinSubscription<T> extends AtomicInteger implements Subscription {
        private static final long serialVersionUID = 3481980673745556697L;
        volatile boolean cancelled;
        final Comparator<? super T> comparator;
        final Subscriber<? super T> downstream;
        final int[] indexes;
        final List<T>[] lists;
        final SortedJoinInnerSubscriber<T>[] subscribers;
        final AtomicLong requested = new AtomicLong();
        final AtomicInteger remaining = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        SortedJoinSubscription(Subscriber<? super T> subscriber, int i, Comparator<? super T> comparator) {
            this.downstream = subscriber;
            this.comparator = comparator;
            SortedJoinInnerSubscriber<T>[] sortedJoinInnerSubscriberArr = new SortedJoinInnerSubscriber[i];
            for (int i2 = 0; i2 < i; i2++) {
                sortedJoinInnerSubscriberArr[i2] = new SortedJoinInnerSubscriber<>(this, i2);
            }
            this.subscribers = sortedJoinInnerSubscriberArr;
            this.lists = new List[i];
            this.indexes = new int[i];
            this.remaining.lazySet(i);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                if (this.remaining.get() == 0) {
                    drain();
                }
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelAll();
                if (getAndIncrement() == 0) {
                    Arrays.fill(this.lists, (Object) null);
                }
            }
        }

        void cancelAll() {
            for (SortedJoinInnerSubscriber<T> sortedJoinInnerSubscriber : this.subscribers) {
                sortedJoinInnerSubscriber.cancel();
            }
        }

        void innerNext(List<T> list, int i) {
            this.lists[i] = list;
            if (this.remaining.decrementAndGet() == 0) {
                drain();
            }
        }

        void innerError(Throwable th) {
            if (this.error.compareAndSet(null, th)) {
                drain();
            } else if (th != this.error.get()) {
                RxJavaPlugins.onError(th);
            }
        }

        void drain() {
            long j;
            boolean z;
            if (getAndIncrement() == 0) {
                Subscriber<? super T> subscriber = this.downstream;
                List<T>[] listArr = this.lists;
                int[] iArr = this.indexes;
                int length = iArr.length;
                int i = 1;
                while (true) {
                    long j2 = this.requested.get();
                    long j3 = 0;
                    while (j3 != j2) {
                        if (this.cancelled) {
                            Arrays.fill(listArr, (Object) null);
                            return;
                        }
                        Throwable th = this.error.get();
                        if (th != null) {
                            cancelAll();
                            Arrays.fill(listArr, (Object) null);
                            subscriber.onError(th);
                            return;
                        }
                        int i2 = 0;
                        int i3 = -1;
                        T t = (Object) null;
                        while (i2 < length) {
                            List<T> list = listArr[i2];
                            int i4 = iArr[i2];
                            if (list.size() != i4) {
                                if (t == null) {
                                    t = list.get(i4);
                                    i3 = i2;
                                } else {
                                    T t2 = list.get(i4);
                                    try {
                                        if (this.comparator.compare(t, t2) > 0) {
                                            t = (Object) t2;
                                            i3 = i2;
                                        }
                                    } catch (Throwable th2) {
                                        Exceptions.throwIfFatal(th2);
                                        cancelAll();
                                        Arrays.fill(listArr, (Object) null);
                                        if (!this.error.compareAndSet(null, th2)) {
                                            RxJavaPlugins.onError(th2);
                                        }
                                        subscriber.onError(this.error.get());
                                        return;
                                    }
                                }
                            }
                            i2++;
                            t = t;
                        }
                        if (t == null) {
                            Arrays.fill(listArr, (Object) null);
                            subscriber.onComplete();
                            return;
                        }
                        subscriber.onNext(t);
                        iArr[i3] = iArr[i3] + 1;
                        j3++;
                    }
                    if (j3 != j2) {
                        j = 0;
                    } else if (this.cancelled) {
                        Arrays.fill(listArr, (Object) null);
                        return;
                    } else {
                        Throwable th3 = this.error.get();
                        if (th3 != null) {
                            cancelAll();
                            Arrays.fill(listArr, (Object) null);
                            subscriber.onError(th3);
                            return;
                        }
                        int i5 = 0;
                        while (true) {
                            if (i5 >= length) {
                                z = true;
                                break;
                            } else if (iArr[i5] != listArr[i5].size()) {
                                z = false;
                                break;
                            } else {
                                i5++;
                            }
                        }
                        if (z) {
                            Arrays.fill(listArr, (Object) null);
                            subscriber.onComplete();
                            return;
                        }
                        j = 0;
                    }
                    if (!(j3 == j || j2 == Long.MAX_VALUE)) {
                        this.requested.addAndGet(-j3);
                    }
                    int i6 = get();
                    if (i6 == i) {
                        int addAndGet = addAndGet(-i);
                        if (addAndGet != 0) {
                            i = addAndGet;
                        } else {
                            return;
                        }
                    } else {
                        i = i6;
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelSortedJoin$SortedJoinInnerSubscriber */
    /* loaded from: classes.dex */
    public static final class SortedJoinInnerSubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<List<T>> {
        private static final long serialVersionUID = 6751017204873808094L;
        final int index;
        final SortedJoinSubscription<T> parent;

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
        }

        @Override // org.reactivestreams.Subscriber
        public /* bridge */ /* synthetic */ void onNext(Object obj) {
            onNext((List) ((List) obj));
        }

        SortedJoinInnerSubscriber(SortedJoinSubscription<T> sortedJoinSubscription, int i) {
            this.parent = sortedJoinSubscription;
            this.index = i;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this, subscription, Long.MAX_VALUE);
        }

        public void onNext(List<T> list) {
            this.parent.innerNext(list, this.index);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.parent.innerError(th);
        }

        void cancel() {
            SubscriptionHelper.cancel(this);
        }
    }
}
