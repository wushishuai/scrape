package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeoutTimed */
/* loaded from: classes.dex */
public final class FlowableTimeoutTimed<T> extends AbstractFlowableWithUpstream<T, T> {
    final Publisher<? extends T> other;
    final Scheduler scheduler;
    final long timeout;
    final TimeUnit unit;

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeoutTimed$TimeoutSupport */
    /* loaded from: classes.dex */
    public interface TimeoutSupport {
        void onTimeout(long j);
    }

    public FlowableTimeoutTimed(Flowable<T> flowable, long j, TimeUnit timeUnit, Scheduler scheduler, Publisher<? extends T> publisher) {
        super(flowable);
        this.timeout = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
        this.other = publisher;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        if (this.other == null) {
            TimeoutSubscriber timeoutSubscriber = new TimeoutSubscriber(subscriber, this.timeout, this.unit, this.scheduler.createWorker());
            subscriber.onSubscribe(timeoutSubscriber);
            timeoutSubscriber.startTimeout(0);
            this.source.subscribe((FlowableSubscriber) timeoutSubscriber);
            return;
        }
        TimeoutFallbackSubscriber timeoutFallbackSubscriber = new TimeoutFallbackSubscriber(subscriber, this.timeout, this.unit, this.scheduler.createWorker(), this.other);
        subscriber.onSubscribe(timeoutFallbackSubscriber);
        timeoutFallbackSubscriber.startTimeout(0);
        this.source.subscribe((FlowableSubscriber) timeoutFallbackSubscriber);
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeoutTimed$TimeoutSubscriber */
    /* loaded from: classes.dex */
    static final class TimeoutSubscriber<T> extends AtomicLong implements FlowableSubscriber<T>, Subscription, TimeoutSupport {
        private static final long serialVersionUID = 3764492702657003550L;
        final Subscriber<? super T> downstream;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;
        final SequentialDisposable task = new SequentialDisposable();
        final AtomicReference<Subscription> upstream = new AtomicReference<>();
        final AtomicLong requested = new AtomicLong();

        TimeoutSubscriber(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler.Worker worker) {
            this.downstream = subscriber;
            this.timeout = j;
            this.unit = timeUnit;
            this.worker = worker;
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
                    this.task.get().dispose();
                    this.downstream.onNext(t);
                    startTimeout(j2);
                }
            }
        }

        void startTimeout(long j) {
            this.task.replace(this.worker.schedule(new TimeoutTask(j, this), this.timeout, this.unit));
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onError(th);
                this.worker.dispose();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onComplete();
                this.worker.dispose();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableTimeoutTimed.TimeoutSupport
        public void onTimeout(long j) {
            if (compareAndSet(j, Long.MAX_VALUE)) {
                SubscriptionHelper.cancel(this.upstream);
                this.downstream.onError(new TimeoutException(ExceptionHelper.timeoutMessage(this.timeout, this.unit)));
                this.worker.dispose();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            SubscriptionHelper.deferredRequest(this.upstream, this.requested, j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            SubscriptionHelper.cancel(this.upstream);
            this.worker.dispose();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeoutTimed$TimeoutTask */
    /* loaded from: classes.dex */
    public static final class TimeoutTask implements Runnable {
        final long idx;
        final TimeoutSupport parent;

        TimeoutTask(long j, TimeoutSupport timeoutSupport) {
            this.idx = j;
            this.parent = timeoutSupport;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.parent.onTimeout(this.idx);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeoutTimed$TimeoutFallbackSubscriber */
    /* loaded from: classes.dex */
    static final class TimeoutFallbackSubscriber<T> extends SubscriptionArbiter implements FlowableSubscriber<T>, TimeoutSupport {
        private static final long serialVersionUID = 3764492702657003550L;
        long consumed;
        final Subscriber<? super T> downstream;
        Publisher<? extends T> fallback;
        final long timeout;
        final TimeUnit unit;
        final Scheduler.Worker worker;
        final SequentialDisposable task = new SequentialDisposable();
        final AtomicReference<Subscription> upstream = new AtomicReference<>();
        final AtomicLong index = new AtomicLong();

        TimeoutFallbackSubscriber(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler.Worker worker, Publisher<? extends T> publisher) {
            this.downstream = subscriber;
            this.timeout = j;
            this.unit = timeUnit;
            this.worker = worker;
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
                    this.task.get().dispose();
                    this.consumed++;
                    this.downstream.onNext(t);
                    startTimeout(j2);
                }
            }
        }

        void startTimeout(long j) {
            this.task.replace(this.worker.schedule(new TimeoutTask(j, this), this.timeout, this.unit));
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.index.getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onError(th);
                this.worker.dispose();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.index.getAndSet(Long.MAX_VALUE) != Long.MAX_VALUE) {
                this.task.dispose();
                this.downstream.onComplete();
                this.worker.dispose();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableTimeoutTimed.TimeoutSupport
        public void onTimeout(long j) {
            if (this.index.compareAndSet(j, Long.MAX_VALUE)) {
                SubscriptionHelper.cancel(this.upstream);
                long j2 = this.consumed;
                if (j2 != 0) {
                    produced(j2);
                }
                Publisher<? extends T> publisher = this.fallback;
                this.fallback = null;
                publisher.subscribe(new FallbackSubscriber(this.downstream, this));
                this.worker.dispose();
            }
        }

        @Override // p005io.reactivex.internal.subscriptions.SubscriptionArbiter, org.reactivestreams.Subscription
        public void cancel() {
            super.cancel();
            this.worker.dispose();
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTimeoutTimed$FallbackSubscriber */
    /* loaded from: classes.dex */
    static final class FallbackSubscriber<T> implements FlowableSubscriber<T> {
        final SubscriptionArbiter arbiter;
        final Subscriber<? super T> downstream;

        /* JADX INFO: Access modifiers changed from: package-private */
        public FallbackSubscriber(Subscriber<? super T> subscriber, SubscriptionArbiter subscriptionArbiter) {
            this.downstream = subscriber;
            this.arbiter = subscriptionArbiter;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            this.arbiter.setSubscription(subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
        }
    }
}
