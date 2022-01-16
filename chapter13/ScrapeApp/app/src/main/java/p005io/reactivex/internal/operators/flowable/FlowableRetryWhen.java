package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.operators.flowable.FlowableRepeatWhen;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.processors.FlowableProcessor;
import p005io.reactivex.processors.UnicastProcessor;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableRetryWhen */
/* loaded from: classes.dex */
public final class FlowableRetryWhen<T> extends AbstractFlowableWithUpstream<T, T> {
    final Function<? super Flowable<Throwable>, ? extends Publisher<?>> handler;

    public FlowableRetryWhen(Flowable<T> source, Function<? super Flowable<Throwable>, ? extends Publisher<?>> handler) {
        super(source);
        this.handler = handler;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> s) {
        SerializedSubscriber<T> z = new SerializedSubscriber<>(s);
        FlowableProcessor<T> serialized = UnicastProcessor.create(8).toSerialized();
        try {
            Publisher<?> when = (Publisher) ObjectHelper.requireNonNull(this.handler.apply(serialized), "handler returned a null Publisher");
            FlowableRepeatWhen.WhenReceiver<T, Throwable> receiver = new FlowableRepeatWhen.WhenReceiver<>(this.source);
            RetryWhenSubscriber<T> subscriber = new RetryWhenSubscriber<>(z, serialized, receiver);
            receiver.subscriber = subscriber;
            s.onSubscribe(subscriber);
            when.subscribe(receiver);
            receiver.onNext(0);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, s);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRetryWhen$RetryWhenSubscriber */
    /* loaded from: classes.dex */
    static final class RetryWhenSubscriber<T> extends FlowableRepeatWhen.WhenSourceSubscriber<T, Throwable> {
        private static final long serialVersionUID = -2680129890138081029L;

        RetryWhenSubscriber(Subscriber<? super T> actual, FlowableProcessor<Throwable> processor, Subscription receiver) {
            super(actual, processor, receiver);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            again(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.receiver.cancel();
            this.downstream.onComplete();
        }
    }
}
