package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.fuseable.FuseToFlowable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableCountSingle */
/* loaded from: classes.dex */
public final class FlowableCountSingle<T> extends Single<Long> implements FuseToFlowable<Long> {
    final Flowable<T> source;

    public FlowableCountSingle(Flowable<T> flowable) {
        this.source = flowable;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Long> singleObserver) {
        this.source.subscribe((FlowableSubscriber) new CountSubscriber(singleObserver));
    }

    @Override // p005io.reactivex.internal.fuseable.FuseToFlowable
    public Flowable<Long> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableCount(this.source));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCountSingle$CountSubscriber */
    /* loaded from: classes.dex */
    static final class CountSubscriber implements FlowableSubscriber<Object>, Disposable {
        long count;
        final SingleObserver<? super Long> downstream;
        Subscription upstream;

        CountSubscriber(SingleObserver<? super Long> singleObserver) {
            this.downstream = singleObserver;
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
        public void onNext(Object obj) {
            this.count++;
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.upstream = SubscriptionHelper.CANCELLED;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.upstream = SubscriptionHelper.CANCELLED;
            this.downstream.onSuccess(Long.valueOf(this.count));
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.cancel();
            this.upstream = SubscriptionHelper.CANCELLED;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream == SubscriptionHelper.CANCELLED;
        }
    }
}
