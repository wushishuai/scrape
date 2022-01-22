package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.subscribers.InnerQueuedSubscriber;
import p005io.reactivex.internal.subscribers.InnerQueuedSubscriberSupport;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.ErrorMode;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableConcatMapEager */
/* loaded from: classes.dex */
public final class FlowableConcatMapEager<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int maxConcurrency;
    final int prefetch;

    public FlowableConcatMapEager(Flowable<T> flowable, Function<? super T, ? extends Publisher<? extends R>> function, int i, int i2, ErrorMode errorMode) {
        super(flowable);
        this.mapper = function;
        this.maxConcurrency = i;
        this.prefetch = i2;
        this.errorMode = errorMode;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> subscriber) {
        this.source.subscribe((FlowableSubscriber) new ConcatMapEagerDelayErrorSubscriber(subscriber, this.mapper, this.maxConcurrency, this.prefetch, this.errorMode));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableConcatMapEager$ConcatMapEagerDelayErrorSubscriber */
    /* loaded from: classes.dex */
    static final class ConcatMapEagerDelayErrorSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, InnerQueuedSubscriberSupport<R> {
        private static final long serialVersionUID = -4255299542215038287L;
        volatile boolean cancelled;
        volatile InnerQueuedSubscriber<R> current;
        volatile boolean done;
        final Subscriber<? super R> downstream;
        final ErrorMode errorMode;
        final Function<? super T, ? extends Publisher<? extends R>> mapper;
        final int maxConcurrency;
        final int prefetch;
        final SpscLinkedArrayQueue<InnerQueuedSubscriber<R>> subscribers;
        Subscription upstream;
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicLong requested = new AtomicLong();

        public ConcatMapEagerDelayErrorSubscriber(Subscriber<? super R> subscriber, Function<? super T, ? extends Publisher<? extends R>> function, int i, int i2, ErrorMode errorMode) {
            this.downstream = subscriber;
            this.mapper = function;
            this.maxConcurrency = i;
            this.prefetch = i2;
            this.errorMode = errorMode;
            this.subscribers = new SpscLinkedArrayQueue<>(Math.min(i2, i));
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                int i = this.maxConcurrency;
                subscription.request(i == Integer.MAX_VALUE ? Long.MAX_VALUE : (long) i);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            try {
                Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null Publisher");
                InnerQueuedSubscriber<R> innerQueuedSubscriber = new InnerQueuedSubscriber<>(this, this.prefetch);
                if (!this.cancelled) {
                    this.subscribers.offer(innerQueuedSubscriber);
                    publisher.subscribe(innerQueuedSubscriber);
                    if (this.cancelled) {
                        innerQueuedSubscriber.cancel();
                        drainAndCancel();
                    }
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.upstream.cancel();
                onError(th);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.errors.addThrowable(th)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.cancel();
                drainAndCancel();
            }
        }

        void drainAndCancel() {
            if (getAndIncrement() == 0) {
                do {
                    cancelAll();
                } while (decrementAndGet() != 0);
            }
        }

        void cancelAll() {
            while (true) {
                InnerQueuedSubscriber<R> poll = this.subscribers.poll();
                if (poll != null) {
                    poll.cancel();
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

        @Override // p005io.reactivex.internal.subscribers.InnerQueuedSubscriberSupport
        public void innerNext(InnerQueuedSubscriber<R> innerQueuedSubscriber, R r) {
            if (innerQueuedSubscriber.queue().offer(r)) {
                drain();
                return;
            }
            innerQueuedSubscriber.cancel();
            innerError(innerQueuedSubscriber, new MissingBackpressureException());
        }

        @Override // p005io.reactivex.internal.subscribers.InnerQueuedSubscriberSupport
        public void innerError(InnerQueuedSubscriber<R> innerQueuedSubscriber, Throwable th) {
            if (this.errors.addThrowable(th)) {
                innerQueuedSubscriber.setDone();
                if (this.errorMode != ErrorMode.END) {
                    this.upstream.cancel();
                }
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // p005io.reactivex.internal.subscribers.InnerQueuedSubscriberSupport
        public void innerComplete(InnerQueuedSubscriber<R> innerQueuedSubscriber) {
            innerQueuedSubscriber.setDone();
            drain();
        }

        /* JADX WARN: Removed duplicated region for block: B:78:0x0121  */
        /* JADX WARN: Removed duplicated region for block: B:89:0x0136 A[SYNTHETIC] */
        /* JADX WARN: Removed duplicated region for block: B:95:0x0132 A[SYNTHETIC] */
        @Override // p005io.reactivex.internal.subscribers.InnerQueuedSubscriberSupport
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void drain() {
            /*
                Method dump skipped, instructions count: 320
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.flowable.FlowableConcatMapEager.ConcatMapEagerDelayErrorSubscriber.drain():void");
        }
    }
}
