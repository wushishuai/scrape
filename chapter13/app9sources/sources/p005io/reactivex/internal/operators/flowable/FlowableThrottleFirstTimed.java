package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableThrottleFirstTimed */
/* loaded from: classes.dex */
public final class FlowableThrottleFirstTimed<T> extends AbstractFlowableWithUpstream<T, T> {
    final Scheduler scheduler;
    final long timeout;
    final TimeUnit unit;

    public FlowableThrottleFirstTimed(Flowable<T> flowable, long j, TimeUnit timeUnit, Scheduler scheduler) {
        super(flowable);
        this.timeout = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((FlowableSubscriber) new DebounceTimedSubscriber(new SerializedSubscriber(subscriber), this.timeout, this.unit, this.scheduler.createWorker()));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableThrottleFirstTimed$DebounceTimedSubscriber */
    /* loaded from: classes.dex */
    static final class DebounceTimedSubscriber<T> extends AtomicLong implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = -9102637559663639004L;
        boolean done;
        final Subscriber<? super T> downstream;
        volatile boolean gate;
        final long timeout;
        final SequentialDisposable timer = new SequentialDisposable();
        final TimeUnit unit;
        Subscription upstream;
        final Scheduler.Worker worker;

        DebounceTimedSubscriber(Subscriber<? super T> subscriber, long j, TimeUnit timeUnit, Scheduler.Worker worker) {
            this.downstream = subscriber;
            this.timeout = j;
            this.unit = timeUnit;
            this.worker = worker;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done && !this.gate) {
                this.gate = true;
                if (get() != 0) {
                    this.downstream.onNext(t);
                    BackpressureHelper.produced(this, 1);
                    Disposable disposable = this.timer.get();
                    if (disposable != null) {
                        disposable.dispose();
                    }
                    this.timer.replace(this.worker.schedule(this, this.timeout, this.unit));
                    return;
                }
                this.done = true;
                cancel();
                this.downstream.onError(new MissingBackpressureException("Could not deliver value due to lack of requests"));
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            this.gate = false;
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.downstream.onError(th);
            this.worker.dispose();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
                this.worker.dispose();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this, j);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
            this.worker.dispose();
        }
    }
}
