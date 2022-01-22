package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop */
/* loaded from: classes.dex */
public final class FlowableOnBackpressureDrop<T> extends AbstractFlowableWithUpstream<T, T> implements Consumer<T> {
    final Consumer<? super T> onDrop;

    @Override // p005io.reactivex.functions.Consumer
    public void accept(T t) {
    }

    public FlowableOnBackpressureDrop(Flowable<T> flowable) {
        super(flowable);
        this.onDrop = this;
    }

    public FlowableOnBackpressureDrop(Flowable<T> flowable, Consumer<? super T> consumer) {
        super(flowable);
        this.onDrop = consumer;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((FlowableSubscriber) new BackpressureDropSubscriber(subscriber, this.onDrop));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableOnBackpressureDrop$BackpressureDropSubscriber */
    /* loaded from: classes.dex */
    static final class BackpressureDropSubscriber<T> extends AtomicLong implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = -6246093802440953054L;
        boolean done;
        final Subscriber<? super T> downstream;
        final Consumer<? super T> onDrop;
        Subscription upstream;

        BackpressureDropSubscriber(Subscriber<? super T> subscriber, Consumer<? super T> consumer) {
            this.downstream = subscriber;
            this.onDrop = consumer;
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
            if (!this.done) {
                if (get() != 0) {
                    this.downstream.onNext(t);
                    BackpressureHelper.produced(this, 1);
                    return;
                }
                try {
                    this.onDrop.accept(t);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    cancel();
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
            this.done = true;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
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
        }
    }
}
