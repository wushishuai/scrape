package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableLimit */
/* loaded from: classes.dex */
public final class FlowableLimit<T> extends AbstractFlowableWithUpstream<T, T> {

    /* renamed from: n */
    final long f125n;

    public FlowableLimit(Flowable<T> source, long n) {
        super(source);
        this.f125n = n;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        this.source.subscribe((FlowableSubscriber) new LimitSubscriber(s, this.f125n));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableLimit$LimitSubscriber */
    /* loaded from: classes.dex */
    static final class LimitSubscriber<T> extends AtomicLong implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = 2288246011222124525L;
        final Subscriber<? super T> downstream;
        long remaining;
        Subscription upstream;

        LimitSubscriber(Subscriber<? super T> actual, long remaining) {
            this.downstream = actual;
            this.remaining = remaining;
            lazySet(remaining);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (!SubscriptionHelper.validate(this.upstream, s)) {
                return;
            }
            if (this.remaining == 0) {
                s.cancel();
                EmptySubscription.complete(this.downstream);
                return;
            }
            this.upstream = s;
            this.downstream.onSubscribe(this);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            long r = this.remaining;
            if (r > 0) {
                long r2 = r - 1;
                this.remaining = r2;
                this.downstream.onNext(t);
                if (r2 == 0) {
                    this.upstream.cancel();
                    this.downstream.onComplete();
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.remaining > 0) {
                this.remaining = 0;
                this.downstream.onError(t);
                return;
            }
            RxJavaPlugins.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.remaining > 0) {
                this.remaining = 0;
                this.downstream.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            long r;
            long toRequest;
            if (SubscriptionHelper.validate(n)) {
                do {
                    r = get();
                    if (r != 0) {
                        if (r <= n) {
                            toRequest = r;
                        } else {
                            toRequest = n;
                        }
                    } else {
                        return;
                    }
                } while (!compareAndSet(r, r - toRequest));
                this.upstream.request(toRequest);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }
    }
}
