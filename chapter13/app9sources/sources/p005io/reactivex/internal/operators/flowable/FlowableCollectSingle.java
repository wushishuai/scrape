package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Callable;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiConsumer;
import p005io.reactivex.internal.disposables.EmptyDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.FuseToFlowable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableCollectSingle */
/* loaded from: classes.dex */
public final class FlowableCollectSingle<T, U> extends Single<U> implements FuseToFlowable<U> {
    final BiConsumer<? super U, ? super T> collector;
    final Callable<? extends U> initialSupplier;
    final Flowable<T> source;

    public FlowableCollectSingle(Flowable<T> flowable, Callable<? extends U> callable, BiConsumer<? super U, ? super T> biConsumer) {
        this.source = flowable;
        this.initialSupplier = callable;
        this.collector = biConsumer;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super U> singleObserver) {
        try {
            this.source.subscribe((FlowableSubscriber) new CollectSubscriber(singleObserver, ObjectHelper.requireNonNull(this.initialSupplier.call(), "The initialSupplier returned a null value"), this.collector));
        } catch (Throwable th) {
            EmptyDisposable.error(th, singleObserver);
        }
    }

    @Override // p005io.reactivex.internal.fuseable.FuseToFlowable
    public Flowable<U> fuseToFlowable() {
        return RxJavaPlugins.onAssembly(new FlowableCollect(this.source, this.initialSupplier, this.collector));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCollectSingle$CollectSubscriber */
    /* loaded from: classes.dex */
    static final class CollectSubscriber<T, U> implements FlowableSubscriber<T>, Disposable {
        final BiConsumer<? super U, ? super T> collector;
        boolean done;
        final SingleObserver<? super U> downstream;

        /* renamed from: u */
        final U f116u;
        Subscription upstream;

        CollectSubscriber(SingleObserver<? super U> singleObserver, U u, BiConsumer<? super U, ? super T> biConsumer) {
            this.downstream = singleObserver;
            this.collector = biConsumer;
            this.f116u = u;
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
                    this.collector.accept((U) this.f116u, t);
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
            this.upstream = SubscriptionHelper.CANCELLED;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.upstream = SubscriptionHelper.CANCELLED;
                this.downstream.onSuccess((U) this.f116u);
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
