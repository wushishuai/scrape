package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.fuseable.FuseToFlowable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableAnySingle */
/* loaded from: classes.dex */
public final class FlowableAnySingle<T> extends Single<Boolean> implements FuseToFlowable<Boolean> {
    final Predicate<? super T> predicate;
    final Flowable<T> source;

    public FlowableAnySingle(Flowable<T> flowable, Predicate<? super T> predicate) {
        this.source = flowable;
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Boolean> singleObserver) {
        this.source.subscribe((FlowableSubscriber) new AnySubscriber(singleObserver, this.predicate));
    }

    @Override // p005io.reactivex.internal.fuseable.FuseToFlowable
    public Flowable<Boolean> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableAny(this.source, this.predicate));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableAnySingle$AnySubscriber */
    /* loaded from: classes.dex */
    static final class AnySubscriber<T> implements FlowableSubscriber<T>, Disposable {
        boolean done;
        final SingleObserver<? super Boolean> downstream;
        final Predicate<? super T> predicate;
        Subscription upstream;

        AnySubscriber(SingleObserver<? super Boolean> singleObserver, Predicate<? super T> predicate) {
            this.downstream = singleObserver;
            this.predicate = predicate;
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
                try {
                    if (this.predicate.test(t)) {
                        this.done = true;
                        this.upstream.cancel();
                        this.upstream = SubscriptionHelper.CANCELLED;
                        this.downstream.onSuccess(true);
                    }
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.upstream.cancel();
                    this.upstream = SubscriptionHelper.CANCELLED;
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
            this.upstream = SubscriptionHelper.CANCELLED;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.upstream = SubscriptionHelper.CANCELLED;
                this.downstream.onSuccess(false);
            }
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
