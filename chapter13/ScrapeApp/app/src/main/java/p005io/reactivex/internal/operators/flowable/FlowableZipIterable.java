package p005io.reactivex.internal.operators.flowable;

import java.util.Iterator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableZipIterable */
/* loaded from: classes.dex */
public final class FlowableZipIterable<T, U, V> extends AbstractFlowableWithUpstream<T, V> {
    final Iterable<U> other;
    final BiFunction<? super T, ? super U, ? extends V> zipper;

    public FlowableZipIterable(Flowable<T> source, Iterable<U> other, BiFunction<? super T, ? super U, ? extends V> zipper) {
        super(source);
        this.other = other;
        this.zipper = zipper;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super V> t) {
        try {
            Iterator<U> it = (Iterator) ObjectHelper.requireNonNull(this.other.iterator(), "The iterator returned by other is null");
            try {
                if (!it.hasNext()) {
                    EmptySubscription.complete(t);
                } else {
                    this.source.subscribe((FlowableSubscriber) new ZipIterableSubscriber(t, it, this.zipper));
                }
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                EmptySubscription.error(e, t);
            }
        } catch (Throwable e2) {
            Exceptions.throwIfFatal(e2);
            EmptySubscription.error(e2, t);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableZipIterable$ZipIterableSubscriber */
    /* loaded from: classes.dex */
    static final class ZipIterableSubscriber<T, U, V> implements FlowableSubscriber<T>, Subscription {
        boolean done;
        final Subscriber<? super V> downstream;
        final Iterator<U> iterator;
        Subscription upstream;
        final BiFunction<? super T, ? super U, ? extends V> zipper;

        ZipIterableSubscriber(Subscriber<? super V> actual, Iterator<U> iterator, BiFunction<? super T, ? super U, ? extends V> zipper) {
            this.downstream = actual;
            this.iterator = iterator;
            this.zipper = zipper;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                try {
                    try {
                        this.downstream.onNext(ObjectHelper.requireNonNull(this.zipper.apply(t, ObjectHelper.requireNonNull(this.iterator.next(), "The iterator returned a null value")), "The zipper function returned a null value"));
                        try {
                            if (!this.iterator.hasNext()) {
                                this.done = true;
                                this.upstream.cancel();
                                this.downstream.onComplete();
                            }
                        } catch (Throwable e) {
                            error(e);
                        }
                    } catch (Throwable e2) {
                        error(e2);
                    }
                } catch (Throwable e3) {
                    error(e3);
                }
            }
        }

        void error(Throwable e) {
            Exceptions.throwIfFatal(e);
            this.done = true;
            this.upstream.cancel();
            this.downstream.onError(e);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            this.upstream.request(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }
    }
}
