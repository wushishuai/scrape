package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleFlatMapPublisher */
/* loaded from: classes.dex */
public final class SingleFlatMapPublisher<T, R> extends Flowable<R> {
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final SingleSource<T> source;

    public SingleFlatMapPublisher(SingleSource<T> singleSource, Function<? super T, ? extends Publisher<? extends R>> function) {
        this.source = singleSource;
        this.mapper = function;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> subscriber) {
        this.source.subscribe(new SingleFlatMapPublisherObserver(subscriber, this.mapper));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleFlatMapPublisher$SingleFlatMapPublisherObserver */
    /* loaded from: classes.dex */
    static final class SingleFlatMapPublisherObserver<S, T> extends AtomicLong implements SingleObserver<S>, FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = 7759721921468635667L;
        Disposable disposable;
        final Subscriber<? super T> downstream;
        final Function<? super S, ? extends Publisher<? extends T>> mapper;
        final AtomicReference<Subscription> parent = new AtomicReference<>();

        SingleFlatMapPublisherObserver(Subscriber<? super T> subscriber, Function<? super S, ? extends Publisher<? extends T>> function) {
            this.downstream = subscriber;
            this.mapper = function;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            this.disposable = disposable;
            this.downstream.onSubscribe(this);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(S s) {
            try {
                ((Publisher) ObjectHelper.requireNonNull(this.mapper.apply(s), "the mapper returned a null Publisher")).subscribe(this);
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.downstream.onError(th);
            }
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.deferredSetOnce(this.parent, this, subscription);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            SubscriptionHelper.deferredRequest(this.parent, this, j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.disposable.dispose();
            SubscriptionHelper.cancel(this.parent);
        }
    }
}
