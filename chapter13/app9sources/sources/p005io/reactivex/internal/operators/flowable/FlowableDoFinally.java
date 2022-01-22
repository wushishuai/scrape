package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Action;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.subscriptions.BasicIntQueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableDoFinally */
/* loaded from: classes.dex */
public final class FlowableDoFinally<T> extends AbstractFlowableWithUpstream<T, T> {
    final Action onFinally;

    public FlowableDoFinally(Flowable<T> flowable, Action action) {
        super(flowable);
        this.onFinally = action;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            this.source.subscribe((FlowableSubscriber) new DoFinallyConditionalSubscriber((ConditionalSubscriber) subscriber, this.onFinally));
        } else {
            this.source.subscribe((FlowableSubscriber) new DoFinallySubscriber(subscriber, this.onFinally));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableDoFinally$DoFinallySubscriber */
    /* loaded from: classes.dex */
    static final class DoFinallySubscriber<T> extends BasicIntQueueSubscription<T> implements FlowableSubscriber<T> {
        private static final long serialVersionUID = 4109457741734051389L;
        final Subscriber<? super T> downstream;
        final Action onFinally;

        /* renamed from: qs */
        QueueSubscription<T> f121qs;
        boolean syncFused;
        Subscription upstream;

        DoFinallySubscriber(Subscriber<? super T> subscriber, Action action) {
            this.downstream = subscriber;
            this.onFinally = action;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                if (subscription instanceof QueueSubscription) {
                    this.f121qs = (QueueSubscription) subscription;
                }
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.downstream.onError(th);
            runFinally();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
            runFinally();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
            runFinally();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            QueueSubscription<T> queueSubscription = this.f121qs;
            if (queueSubscription == null || (i & 4) != 0) {
                return 0;
            }
            int requestFusion = queueSubscription.requestFusion(i);
            if (requestFusion != 0) {
                boolean z = true;
                if (requestFusion != 1) {
                    z = false;
                }
                this.syncFused = z;
            }
            return requestFusion;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.f121qs.clear();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.f121qs.isEmpty();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T poll = this.f121qs.poll();
            if (poll == null && this.syncFused) {
                runFinally();
            }
            return poll;
        }

        void runFinally() {
            if (compareAndSet(0, 1)) {
                try {
                    this.onFinally.run();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    RxJavaPlugins.onError(th);
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableDoFinally$DoFinallyConditionalSubscriber */
    /* loaded from: classes.dex */
    static final class DoFinallyConditionalSubscriber<T> extends BasicIntQueueSubscription<T> implements ConditionalSubscriber<T> {
        private static final long serialVersionUID = 4109457741734051389L;
        final ConditionalSubscriber<? super T> downstream;
        final Action onFinally;

        /* renamed from: qs */
        QueueSubscription<T> f120qs;
        boolean syncFused;
        Subscription upstream;

        DoFinallyConditionalSubscriber(ConditionalSubscriber<? super T> conditionalSubscriber, Action action) {
            this.downstream = conditionalSubscriber;
            this.onFinally = action;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                if (subscription instanceof QueueSubscription) {
                    this.f120qs = (QueueSubscription) subscription;
                }
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // p005io.reactivex.internal.fuseable.ConditionalSubscriber
        public boolean tryOnNext(T t) {
            return this.downstream.tryOnNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.downstream.onError(th);
            runFinally();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
            runFinally();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
            runFinally();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            QueueSubscription<T> queueSubscription = this.f120qs;
            if (queueSubscription == null || (i & 4) != 0) {
                return 0;
            }
            int requestFusion = queueSubscription.requestFusion(i);
            if (requestFusion != 0) {
                boolean z = true;
                if (requestFusion != 1) {
                    z = false;
                }
                this.syncFused = z;
            }
            return requestFusion;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.f120qs.clear();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.f120qs.isEmpty();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            T poll = this.f120qs.poll();
            if (poll == null && this.syncFused) {
                runFinally();
            }
            return poll;
        }

        void runFinally() {
            if (compareAndSet(0, 1)) {
                try {
                    this.onFinally.run();
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    RxJavaPlugins.onError(th);
                }
            }
        }
    }
}
