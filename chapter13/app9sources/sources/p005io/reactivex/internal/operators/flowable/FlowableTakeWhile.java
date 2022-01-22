package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableTakeWhile */
/* loaded from: classes.dex */
public final class FlowableTakeWhile<T> extends AbstractFlowableWithUpstream<T, T> {
    final Predicate<? super T> predicate;

    public FlowableTakeWhile(Flowable<T> flowable, Predicate<? super T> predicate) {
        super(flowable);
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((FlowableSubscriber) new TakeWhileSubscriber(subscriber, this.predicate));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableTakeWhile$TakeWhileSubscriber */
    /* loaded from: classes.dex */
    static final class TakeWhileSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        boolean done;
        final Subscriber<? super T> downstream;
        final Predicate<? super T> predicate;
        Subscription upstream;

        TakeWhileSubscriber(Subscriber<? super T> subscriber, Predicate<? super T> predicate) {
            this.downstream = subscriber;
            this.predicate = predicate;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                try {
                    if (!this.predicate.test(t)) {
                        this.done = true;
                        this.upstream.cancel();
                        this.downstream.onComplete();
                        return;
                    }
                    this.downstream.onNext(t);
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
            this.upstream.request(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }
    }
}
