package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.flowable.FlowableTimeoutTimed;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeout */
/* loaded from: classes.dex */
public final class FlowableTimeout<T, U, V> extends AbstractFlowableWithUpstream<T, T> {
    final Publisher<U> firstTimeoutIndicator;
    final Function<? super T, ? extends Publisher<V>> itemTimeoutIndicator;
    final Publisher<? extends T> other;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeout$TimeoutSelectorSupport */
    /* loaded from: classes.dex */
    public interface TimeoutSelectorSupport extends FlowableTimeoutTimed.TimeoutSupport {
        void onTimeoutError(long j, Throwable th);
    }

    public FlowableTimeout(Flowable<T> flowable, Publisher<U> publisher, Function<? super T, ? extends Publisher<V>> function, Publisher<? extends T> publisher2) {
        super(flowable);
        this.firstTimeoutIndicator = publisher;
        this.itemTimeoutIndicator = function;
        this.other = publisher2;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        Publisher<? extends T> publisher = this.other;
        if (publisher == null) {
            TimeoutSubscriber timeoutSubscriber = new TimeoutSubscriber(subscriber, this.itemTimeoutIndicator);
            subscriber.onSubscribe(timeoutSubscriber);
            timeoutSubscriber.startFirstTimeout(this.firstTimeoutIndicator);
            this.source.subscribe((FlowableSubscriber) timeoutSubscriber);
            return;
        }
        TimeoutFallbackSubscriber timeoutFallbackSubscriber = new TimeoutFallbackSubscriber(subscriber, this.itemTimeoutIndicator, publisher);
        subscriber.onSubscribe(timeoutFallbackSubscriber);
        timeoutFallbackSubscriber.startFirstTimeout(this.firstTimeoutIndicator);
        this.source.subscribe((FlowableSubscriber) timeoutFallbackSubscriber);
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeout$TimeoutSubscriber */
    /* loaded from: classes.dex */
    static final class TimeoutSubscriber<T> extends AtomicLong implements FlowableSubscriber<T>, Subscription, TimeoutSelectorSupport {
        private static final long serialVersionUID = 3764492702657003550L;
        final Subscriber<? super T> downstream;
        final Function<? super T, ? extends Publisher<?>> itemTimeoutIndicator;
        final SequentialDisposable task = new SequentialDisposable();
        final AtomicReference<Subscription> upstream = new AtomicReference<>();
        final AtomicLong requested = new AtomicLong();

        TimeoutSubscriber(Subscriber<? super T> subscriber, Function<? super T, ? extends Publisher<?>> function) {
            this.downstream = subscriber;
            this.itemTimeoutIndicator = function;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.deferredSetOnce(this.upstream, this.requested, subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            long j = get();
            if (j != Long.MAX_VALUE) {
                long j2 = 1 + j;
                if (compareAndSet(j, j2)) {
                    Disposable disposable = this.task.get();
                    if (disposable != null) {
                        disposable.dispose();
                    }
                    this.downstream.onNext(t);
                    try {
                        Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.itemTimeoutIndicator.apply(t), "The itemTimeoutIndicator returned a null Publisher.");
                        TimeoutConsumer timeoutConsumer = new TimeoutConsumer(j2, this);
                        if (this.task.replace(timeoutConsumer)) {
                            publisher.subscribe(timeoutConsumer);
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.upstream.get().cancel();
                        getAndSet(Long.MAX_VALUE);
                        this.downstream.onError(th);
                    }
                }
            }
        }

        void startFirstTimeout(Publisher<?> publisher) {
            if (publisher != null) {
                TimeoutConsumer timeoutConsumer = new TimeoutConsumer(0, this);
                if (this.task.replace(timeoutConsumer)) {
                    publisher.subscribe(timeoutConsumer);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableTimeoutTimed.TimeoutSupport
        public void onTimeout(long j) {
            if (compareAndSet(j, Long.MAX_VALUE)) {
                SubscriptionHelper.cancel(this.upstream);
                this.downstream.onError(new TimeoutException());
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableTimeout.TimeoutSelectorSupport
        public void onTimeoutError(long j, Throwable th) {
            if (compareAndSet(j, Long.MAX_VALUE)) {
                SubscriptionHelper.cancel(this.upstream);
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            SubscriptionHelper.deferredRequest(this.upstream, this.requested, j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            SubscriptionHelper.cancel(this.upstream);
            this.task.dispose();
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeout$TimeoutFallbackSubscriber */
    /* loaded from: classes.dex */
    static final class TimeoutFallbackSubscriber<T> extends SubscriptionArbiter implements FlowableSubscriber<T>, TimeoutSelectorSupport {
        private static final long serialVersionUID = 3764492702657003550L;
        long consumed;
        final Subscriber<? super T> downstream;
        Publisher<? extends T> fallback;
        final Function<? super T, ? extends Publisher<?>> itemTimeoutIndicator;
        final SequentialDisposable task = new SequentialDisposable();
        final AtomicReference<Subscription> upstream = new AtomicReference<>();
        final AtomicLong index = new AtomicLong();

        TimeoutFallbackSubscriber(Subscriber<? super T> subscriber, Function<? super T, ? extends Publisher<?>> function, Publisher<? extends T> publisher) {
            this.downstream = subscriber;
            this.itemTimeoutIndicator = function;
            this.fallback = publisher;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.setOnce(this.upstream, subscription)) {
                setSubscription(subscription);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            long j = this.index.get();
            if (j != Long.MAX_VALUE) {
                long j2 = j + 1;
                if (this.index.compareAndSet(j, j2)) {
                    Disposable disposable = this.task.get();
                    if (disposable != null) {
                        disposable.dispose();
                    }
                    this.consumed++;
                    this.downstream.onNext(t);
                    try {
                        Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.itemTimeoutIndicator.apply(t), "The itemTimeoutIndicator returned a null Publisher.");
                        TimeoutConsumer timeoutConsumer = new TimeoutConsumer(j2, this);
                        if (this.task.replace(timeoutConsumer)) {
                            publisher.subscribe(timeoutConsumer);
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        this.upstream.get().cancel();
                        this.index.getAndSet(Long.MAX_VALUE);
                        this.downstream.onError(th);
                    }
                }
            }
        }

        void startFirstTimeout(Publisher<?> publisher) {
            if (publisher != null) {
                TimeoutConsumer timeoutConsumer = new TimeoutConsumer(0, this);
                if (this.task.replace(timeoutConsumer)) {
                    publisher.subscribe(timeoutConsumer);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.index.getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onError(th);
                this.task.dispose();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.index.getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onComplete();
                this.task.dispose();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableTimeoutTimed.TimeoutSupport
        public void onTimeout(long j) {
            if (this.index.compareAndSet(j, Long.MAX_VALUE)) {
                SubscriptionHelper.cancel(this.upstream);
                Publisher<? extends T> publisher = this.fallback;
                this.fallback = null;
                long j2 = this.consumed;
                if (j2 != 0) {
                    produced(j2);
                }
                publisher.subscribe(new FlowableTimeoutTimed.FallbackSubscriber(this.downstream, this));
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableTimeout.TimeoutSelectorSupport
        public void onTimeoutError(long j, Throwable th) {
            if (this.index.compareAndSet(j, Long.MAX_VALUE)) {
                SubscriptionHelper.cancel(this.upstream);
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // p005io.reactivex.internal.subscriptions.SubscriptionArbiter, org.reactivestreams.Subscription
        public void cancel() {
            super.cancel();
            this.task.dispose();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeout$TimeoutConsumer */
    /* loaded from: classes.dex */
    public static final class TimeoutConsumer extends AtomicReference<Subscription> implements FlowableSubscriber<Object>, Disposable {
        private static final long serialVersionUID = 8708641127342403073L;
        final long idx;
        final TimeoutSelectorSupport parent;

        TimeoutConsumer(long j, TimeoutSelectorSupport timeoutSelectorSupport) {
            this.idx = j;
            this.parent = timeoutSelectorSupport;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this, subscription, Long.MAX_VALUE);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object obj) {
            Subscription subscription = (Subscription) get();
            if (subscription != SubscriptionHelper.CANCELLED) {
                subscription.cancel();
                lazySet(SubscriptionHelper.CANCELLED);
                this.parent.onTimeout(this.idx);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (get() != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                this.parent.onTimeoutError(this.idx, th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (get() != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                this.parent.onTimeout(this.idx);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return SubscriptionHelper.isCancelled(get());
        }
    }
}
