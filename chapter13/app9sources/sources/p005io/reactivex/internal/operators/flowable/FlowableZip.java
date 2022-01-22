package p005io.reactivex.internal.operators.flowable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableZip */
/* loaded from: classes.dex */
public final class FlowableZip<T, R> extends Flowable<R> {
    final int bufferSize;
    final boolean delayError;
    final Publisher<? extends T>[] sources;
    final Iterable<? extends Publisher<? extends T>> sourcesIterable;
    final Function<? super Object[], ? extends R> zipper;

    public FlowableZip(Publisher<? extends T>[] publisherArr, Iterable<? extends Publisher<? extends T>> iterable, Function<? super Object[], ? extends R> function, int i, boolean z) {
        this.sources = publisherArr;
        this.sourcesIterable = iterable;
        this.zipper = function;
        this.bufferSize = i;
        this.delayError = z;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super R> subscriber) {
        int i;
        Publisher<? extends T>[] publisherArr = this.sources;
        if (publisherArr == null) {
            publisherArr = new Publisher[8];
            i = 0;
            for (Publisher<? extends T> publisher : this.sourcesIterable) {
                if (i == publisherArr.length) {
                    Publisher<? extends T>[] publisherArr2 = new Publisher[(i >> 2) + i];
                    System.arraycopy(publisherArr, 0, publisherArr2, 0, i);
                    publisherArr = publisherArr2;
                }
                publisherArr[i] = publisher;
                i++;
            }
        } else {
            i = publisherArr.length;
        }
        if (i == 0) {
            EmptySubscription.complete(subscriber);
            return;
        }
        ZipCoordinator zipCoordinator = new ZipCoordinator(subscriber, this.zipper, i, this.bufferSize, this.delayError);
        subscriber.onSubscribe(zipCoordinator);
        zipCoordinator.subscribe(publisherArr, i);
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableZip$ZipCoordinator */
    /* loaded from: classes.dex */
    static final class ZipCoordinator<T, R> extends AtomicInteger implements Subscription {
        private static final long serialVersionUID = -2434867452883857743L;
        volatile boolean cancelled;
        final Object[] current;
        final boolean delayErrors;
        final Subscriber<? super R> downstream;
        final AtomicThrowable errors;
        final AtomicLong requested;
        final ZipSubscriber<T, R>[] subscribers;
        final Function<? super Object[], ? extends R> zipper;

        ZipCoordinator(Subscriber<? super R> subscriber, Function<? super Object[], ? extends R> function, int i, int i2, boolean z) {
            this.downstream = subscriber;
            this.zipper = function;
            this.delayErrors = z;
            ZipSubscriber<T, R>[] zipSubscriberArr = new ZipSubscriber[i];
            for (int i3 = 0; i3 < i; i3++) {
                zipSubscriberArr[i3] = new ZipSubscriber<>(this, i2);
            }
            this.current = new Object[i];
            this.subscribers = zipSubscriberArr;
            this.requested = new AtomicLong();
            this.errors = new AtomicThrowable();
        }

        void subscribe(Publisher<? extends T>[] publisherArr, int i) {
            ZipSubscriber<T, R>[] zipSubscriberArr = this.subscribers;
            for (int i2 = 0; i2 < i && !this.cancelled; i2++) {
                if (this.delayErrors || this.errors.get() == null) {
                    publisherArr[i2].subscribe(zipSubscriberArr[i2]);
                } else {
                    return;
                }
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelAll();
            }
        }

        void error(ZipSubscriber<T, R> zipSubscriber, Throwable th) {
            if (this.errors.addThrowable(th)) {
                zipSubscriber.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        void cancelAll() {
            for (ZipSubscriber<T, R> zipSubscriber : this.subscribers) {
                zipSubscriber.cancel();
            }
        }

        void drain() {
            long j;
            if (getAndIncrement() == 0) {
                Subscriber<? super R> subscriber = this.downstream;
                ZipSubscriber<T, R>[] zipSubscriberArr = this.subscribers;
                int length = zipSubscriberArr.length;
                Object[] objArr = this.current;
                int i = 1;
                do {
                    long j2 = this.requested.get();
                    long j3 = 0;
                    while (j2 != j3) {
                        if (this.cancelled) {
                            return;
                        }
                        if (this.delayErrors || this.errors.get() == null) {
                            boolean z = false;
                            for (int i2 = 0; i2 < length; i2++) {
                                ZipSubscriber<T, R> zipSubscriber = zipSubscriberArr[i2];
                                if (objArr[i2] == null) {
                                    try {
                                        boolean z2 = zipSubscriber.done;
                                        SimpleQueue<T> simpleQueue = zipSubscriber.queue;
                                        T poll = simpleQueue != null ? simpleQueue.poll() : null;
                                        boolean z3 = poll == null;
                                        if (z2 && z3) {
                                            cancelAll();
                                            if (this.errors.get() != null) {
                                                subscriber.onError(this.errors.terminate());
                                                return;
                                            } else {
                                                subscriber.onComplete();
                                                return;
                                            }
                                        } else if (!z3) {
                                            objArr[i2] = poll;
                                        } else {
                                            z = true;
                                        }
                                    } catch (Throwable th) {
                                        Exceptions.throwIfFatal(th);
                                        this.errors.addThrowable(th);
                                        if (!this.delayErrors) {
                                            cancelAll();
                                            subscriber.onError(this.errors.terminate());
                                            return;
                                        }
                                        z = true;
                                    }
                                }
                            }
                            if (z) {
                                break;
                            }
                            try {
                                subscriber.onNext((Object) ObjectHelper.requireNonNull(this.zipper.apply(objArr.clone()), "The zipper returned a null value"));
                                j3++;
                                Arrays.fill(objArr, (Object) null);
                            } catch (Throwable th2) {
                                Exceptions.throwIfFatal(th2);
                                cancelAll();
                                this.errors.addThrowable(th2);
                                subscriber.onError(this.errors.terminate());
                                return;
                            }
                        } else {
                            cancelAll();
                            subscriber.onError(this.errors.terminate());
                            return;
                        }
                    }
                    if (j2 != j3) {
                        j = 0;
                    } else if (this.cancelled) {
                        return;
                    } else {
                        if (this.delayErrors || this.errors.get() == null) {
                            for (int i3 = 0; i3 < length; i3++) {
                                ZipSubscriber<T, R> zipSubscriber2 = zipSubscriberArr[i3];
                                if (objArr[i3] == null) {
                                    try {
                                        boolean z4 = zipSubscriber2.done;
                                        SimpleQueue<T> simpleQueue2 = zipSubscriber2.queue;
                                        T poll2 = simpleQueue2 != null ? simpleQueue2.poll() : null;
                                        boolean z5 = poll2 == null;
                                        if (z4 && z5) {
                                            cancelAll();
                                            if (this.errors.get() != null) {
                                                subscriber.onError(this.errors.terminate());
                                                return;
                                            } else {
                                                subscriber.onComplete();
                                                return;
                                            }
                                        } else if (!z5) {
                                            objArr[i3] = poll2;
                                        }
                                    } catch (Throwable th3) {
                                        Exceptions.throwIfFatal(th3);
                                        this.errors.addThrowable(th3);
                                        if (!this.delayErrors) {
                                            cancelAll();
                                            subscriber.onError(this.errors.terminate());
                                            return;
                                        }
                                    }
                                }
                            }
                            j = 0;
                        } else {
                            cancelAll();
                            subscriber.onError(this.errors.terminate());
                            return;
                        }
                    }
                    if (j3 != j) {
                        for (ZipSubscriber<T, R> zipSubscriber3 : zipSubscriberArr) {
                            zipSubscriber3.request(j3);
                        }
                        if (j2 != Long.MAX_VALUE) {
                            this.requested.addAndGet(-j3);
                        }
                    }
                    i = addAndGet(-i);
                } while (i != 0);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableZip$ZipSubscriber */
    /* loaded from: classes.dex */
    public static final class ZipSubscriber<T, R> extends AtomicReference<Subscription> implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = -4627193790118206028L;
        volatile boolean done;
        final int limit;
        final ZipCoordinator<T, R> parent;
        final int prefetch;
        long produced;
        SimpleQueue<T> queue;
        int sourceMode;

        ZipSubscriber(ZipCoordinator<T, R> zipCoordinator, int i) {
            this.parent = zipCoordinator;
            this.prefetch = i;
            this.limit = i - (i >> 2);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.setOnce(this, subscription)) {
                if (subscription instanceof QueueSubscription) {
                    QueueSubscription queueSubscription = (QueueSubscription) subscription;
                    int requestFusion = queueSubscription.requestFusion(7);
                    if (requestFusion == 1) {
                        this.sourceMode = requestFusion;
                        this.queue = queueSubscription;
                        this.done = true;
                        this.parent.drain();
                        return;
                    } else if (requestFusion == 2) {
                        this.sourceMode = requestFusion;
                        this.queue = queueSubscription;
                        subscription.request((long) this.prefetch);
                        return;
                    }
                }
                this.queue = new SpscArrayQueue(this.prefetch);
                subscription.request((long) this.prefetch);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (this.sourceMode != 2) {
                this.queue.offer(t);
            }
            this.parent.drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.parent.error(this, th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            this.parent.drain();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            SubscriptionHelper.cancel(this);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (this.sourceMode != 1) {
                long j2 = this.produced + j;
                if (j2 >= ((long) this.limit)) {
                    this.produced = 0;
                    get().request(j2);
                    return;
                }
                this.produced = j2;
            }
        }
    }
}
