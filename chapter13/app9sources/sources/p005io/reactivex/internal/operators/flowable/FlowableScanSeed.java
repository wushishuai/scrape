package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableScanSeed */
/* loaded from: classes.dex */
public final class FlowableScanSeed<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final BiFunction<R, ? super T, R> accumulator;
    final Callable<R> seedSupplier;

    public FlowableScanSeed(Flowable<T> flowable, Callable<R> callable, BiFunction<R, ? super T, R> biFunction) {
        super(flowable);
        this.accumulator = biFunction;
        this.seedSupplier = callable;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> subscriber) {
        try {
            this.source.subscribe((FlowableSubscriber) new ScanSeedSubscriber(subscriber, this.accumulator, ObjectHelper.requireNonNull(this.seedSupplier.call(), "The seed supplied is null"), bufferSize()));
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptySubscription.error(th, subscriber);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableScanSeed$ScanSeedSubscriber */
    /* loaded from: classes.dex */
    static final class ScanSeedSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = -1776795561228106469L;
        final BiFunction<R, ? super T, R> accumulator;
        volatile boolean cancelled;
        int consumed;
        volatile boolean done;
        final Subscriber<? super R> downstream;
        Throwable error;
        final int limit;
        final int prefetch;
        final SimplePlainQueue<R> queue;
        final AtomicLong requested = new AtomicLong();
        Subscription upstream;
        R value;

        ScanSeedSubscriber(Subscriber<? super R> subscriber, BiFunction<R, ? super T, R> biFunction, R r, int i) {
            this.downstream = subscriber;
            this.accumulator = biFunction;
            this.value = r;
            this.prefetch = i;
            this.limit = i - (i >> 2);
            this.queue = new SpscArrayQueue(i);
            this.queue.offer(r);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                subscription.request((long) (this.prefetch - 1));
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                try {
                    R r = (R) ObjectHelper.requireNonNull(this.accumulator.apply(this.value, t), "The accumulator returned a null value");
                    this.value = r;
                    this.queue.offer(r);
                    drain();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.upstream.cancel();
                    onError(th);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.error = th;
            this.done = true;
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
            this.upstream.cancel();
            if (getAndIncrement() == 0) {
                this.queue.clear();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                drain();
            }
        }

        void drain() {
            Throwable th;
            if (getAndIncrement() == 0) {
                Subscriber<? super R> subscriber = this.downstream;
                SimplePlainQueue<R> simplePlainQueue = this.queue;
                int i = this.limit;
                int i2 = this.consumed;
                int i3 = 1;
                do {
                    long j = this.requested.get();
                    long j2 = 0;
                    while (j2 != j) {
                        if (this.cancelled) {
                            simplePlainQueue.clear();
                            return;
                        }
                        boolean z = this.done;
                        if (!z || (th = this.error) == null) {
                            Object obj = (R) simplePlainQueue.poll();
                            boolean z2 = obj == null;
                            if (z && z2) {
                                subscriber.onComplete();
                                return;
                            } else if (z2) {
                                break;
                            } else {
                                subscriber.onNext(obj);
                                j2++;
                                i2++;
                                if (i2 == i) {
                                    this.upstream.request((long) i);
                                    i2 = 0;
                                }
                            }
                        } else {
                            simplePlainQueue.clear();
                            subscriber.onError(th);
                            return;
                        }
                    }
                    if (j2 == j && this.done) {
                        Throwable th2 = this.error;
                        if (th2 != null) {
                            simplePlainQueue.clear();
                            subscriber.onError(th2);
                            return;
                        } else if (simplePlainQueue.isEmpty()) {
                            subscriber.onComplete();
                            return;
                        }
                    }
                    if (j2 != 0) {
                        BackpressureHelper.produced(this.requested, j2);
                    }
                    this.consumed = i2;
                    i3 = addAndGet(-i3);
                } while (i3 != 0);
            }
        }
    }
}
