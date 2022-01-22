package p005io.reactivex.internal.operators.completable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;

/* renamed from: io.reactivex.internal.operators.completable.CompletableFromPublisher */
/* loaded from: classes.dex */
public final class CompletableFromPublisher<T> extends Completable {
    final Publisher<T> flowable;

    public CompletableFromPublisher(Publisher<T> publisher) {
        this.flowable = publisher;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        this.flowable.subscribe(new FromPublisherSubscriber(completableObserver));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableFromPublisher$FromPublisherSubscriber */
    /* loaded from: classes.dex */
    static final class FromPublisherSubscriber<T> implements FlowableSubscriber<T>, Disposable {
        final CompletableObserver downstream;
        Subscription upstream;

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
        }

        FromPublisherSubscriber(CompletableObserver completableObserver) {
            this.downstream = completableObserver;
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
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
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
