package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Notification;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableDematerialize */
/* loaded from: classes.dex */
public final class FlowableDematerialize<T> extends AbstractFlowableWithUpstream<Notification<T>, T> {
    public FlowableDematerialize(Flowable<Notification<T>> flowable) {
        super(flowable);
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((FlowableSubscriber) new DematerializeSubscriber(subscriber));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableDematerialize$DematerializeSubscriber */
    /* loaded from: classes.dex */
    static final class DematerializeSubscriber<T> implements FlowableSubscriber<Notification<T>>, Subscription {
        boolean done;
        final Subscriber<? super T> downstream;
        Subscription upstream;

        @Override // org.reactivestreams.Subscriber
        public /* bridge */ /* synthetic */ void onNext(Object obj) {
            onNext((Notification) ((Notification) obj));
        }

        DematerializeSubscriber(Subscriber<? super T> subscriber) {
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }

        public void onNext(Notification<T> notification) {
            if (this.done) {
                if (notification.isOnError()) {
                    RxJavaPlugins.onError(notification.getError());
                }
            } else if (notification.isOnError()) {
                this.upstream.cancel();
                onError(notification.getError());
            } else if (notification.isOnComplete()) {
                this.upstream.cancel();
                onComplete();
            } else {
                this.downstream.onNext(notification.getValue());
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
