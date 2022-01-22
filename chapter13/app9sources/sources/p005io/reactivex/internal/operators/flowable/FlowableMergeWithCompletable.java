package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.HalfSerializer;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableMergeWithCompletable */
/* loaded from: classes.dex */
public final class FlowableMergeWithCompletable<T> extends AbstractFlowableWithUpstream<T, T> {
    final CompletableSource other;

    public FlowableMergeWithCompletable(Flowable<T> flowable, CompletableSource completableSource) {
        super(flowable);
        this.other = completableSource;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        MergeWithSubscriber mergeWithSubscriber = new MergeWithSubscriber(subscriber);
        subscriber.onSubscribe(mergeWithSubscriber);
        this.source.subscribe((FlowableSubscriber) mergeWithSubscriber);
        this.other.subscribe(mergeWithSubscriber.otherObserver);
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableMergeWithCompletable$MergeWithSubscriber */
    /* loaded from: classes.dex */
    static final class MergeWithSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = -4592979584110982903L;
        final Subscriber<? super T> downstream;
        volatile boolean mainDone;
        volatile boolean otherDone;
        final AtomicReference<Subscription> mainSubscription = new AtomicReference<>();
        final OtherObserver otherObserver = new OtherObserver(this);
        final AtomicThrowable error = new AtomicThrowable();
        final AtomicLong requested = new AtomicLong();

        MergeWithSubscriber(Subscriber<? super T> subscriber) {
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.deferredSetOnce(this.mainSubscription, this.requested, subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            HalfSerializer.onNext(this.downstream, t, this, this.error);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            SubscriptionHelper.cancel(this.mainSubscription);
            HalfSerializer.onError(this.downstream, th, this, this.error);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.mainDone = true;
            if (this.otherDone) {
                HalfSerializer.onComplete(this.downstream, this, this.error);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            SubscriptionHelper.deferredRequest(this.mainSubscription, this.requested, j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            SubscriptionHelper.cancel(this.mainSubscription);
            DisposableHelper.dispose(this.otherObserver);
        }

        void otherError(Throwable th) {
            SubscriptionHelper.cancel(this.mainSubscription);
            HalfSerializer.onError(this.downstream, th, this, this.error);
        }

        void otherComplete() {
            this.otherDone = true;
            if (this.mainDone) {
                HalfSerializer.onComplete(this.downstream, this, this.error);
            }
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableMergeWithCompletable$MergeWithSubscriber$OtherObserver */
        /* loaded from: classes.dex */
        static final class OtherObserver extends AtomicReference<Disposable> implements CompletableObserver {
            private static final long serialVersionUID = -2935427570954647017L;
            final MergeWithSubscriber<?> parent;

            OtherObserver(MergeWithSubscriber<?> mergeWithSubscriber) {
                this.parent = mergeWithSubscriber;
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(this, disposable);
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onError(Throwable th) {
                this.parent.otherError(th);
            }

            @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
            public void onComplete() {
                this.parent.otherComplete();
            }
        }
    }
}
