package p005io.reactivex.internal.operators.flowable;

import java.util.NoSuchElementException;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.fuseable.FuseToFlowable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableElementAtSingle */
/* loaded from: classes.dex */
public final class FlowableElementAtSingle<T> extends Single<T> implements FuseToFlowable<T> {
    final T defaultValue;
    final long index;
    final Flowable<T> source;

    public FlowableElementAtSingle(Flowable<T> flowable, long j, T t) {
        this.source = flowable;
        this.index = j;
        this.defaultValue = t;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.source.subscribe((FlowableSubscriber) new ElementAtSubscriber(singleObserver, this.index, this.defaultValue));
    }

    @Override // p005io.reactivex.internal.fuseable.FuseToFlowable
    public Flowable<T> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableElementAt(this.source, this.index, this.defaultValue, true));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableElementAtSingle$ElementAtSubscriber */
    /* loaded from: classes.dex */
    static final class ElementAtSubscriber<T> implements FlowableSubscriber<T>, Disposable {
        long count;
        final T defaultValue;
        boolean done;
        final SingleObserver<? super T> downstream;
        final long index;
        Subscription upstream;

        ElementAtSubscriber(SingleObserver<? super T> singleObserver, long j, T t) {
            this.downstream = singleObserver;
            this.index = j;
            this.defaultValue = t;
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
                long j = this.count;
                if (j == this.index) {
                    this.done = true;
                    this.upstream.cancel();
                    this.upstream = SubscriptionHelper.CANCELLED;
                    this.downstream.onSuccess(t);
                    return;
                }
                this.count = j + 1;
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
            this.upstream = SubscriptionHelper.CANCELLED;
            if (!this.done) {
                this.done = true;
                T t = this.defaultValue;
                if (t != null) {
                    this.downstream.onSuccess(t);
                } else {
                    this.downstream.onError(new NoSuchElementException());
                }
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
