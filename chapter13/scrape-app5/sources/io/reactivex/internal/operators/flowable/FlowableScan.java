package io.reactivex.internal.operators.flowable;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.functions.BiFunction;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.plugins.RxJavaPlugins;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
/* loaded from: classes.dex */
public final class FlowableScan<T> extends AbstractFlowableWithUpstream<T, T> {
    final BiFunction<T, T, T> accumulator;

    public FlowableScan(Flowable<T> source, BiFunction<T, T, T> accumulator) {
        super(source);
        this.accumulator = accumulator;
    }

    @Override // io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        this.source.subscribe((FlowableSubscriber) new ScanSubscriber(s, this.accumulator));
    }

    /* loaded from: classes.dex */
    static final class ScanSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final BiFunction<T, T, T> accumulator;
        boolean done;
        final Subscriber<? super T> downstream;
        Subscription upstream;
        T value;

        ScanSubscriber(Subscriber<? super T> actual, BiFunction<T, T, T> accumulator) {
            this.downstream = actual;
            this.accumulator = accumulator;
        }

        @Override // io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
            }
        }

        /* JADX WARN: Type inference failed for: r2v3, types: [T, java.lang.Object] */
        /* JADX WARN: Unknown variable types count: 1 */
        @Override // org.reactivestreams.Subscriber
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct add '--show-bad-code' argument
        */
        public void onNext(T r5) {
            /*
                r4 = this;
                boolean r0 = r4.done
                if (r0 == 0) goto L_0x0005
                return
            L_0x0005:
                org.reactivestreams.Subscriber<? super T> r0 = r4.downstream
                T r1 = r4.value
                if (r1 != 0) goto L_0x0011
                r4.value = r5
                r0.onNext(r5)
                goto L_0x0023
            L_0x0011:
                io.reactivex.functions.BiFunction<T, T, T> r2 = r4.accumulator     // Catch: Throwable -> 0x0024
                java.lang.Object r2 = r2.apply(r1, r5)     // Catch: Throwable -> 0x0024
                java.lang.String r3 = "The value returned by the accumulator is null"
                java.lang.Object r2 = io.reactivex.internal.functions.ObjectHelper.requireNonNull(r2, r3)     // Catch: Throwable -> 0x0024
                r4.value = r2
                r0.onNext(r2)
            L_0x0023:
                return
            L_0x0024:
                r2 = move-exception
                io.reactivex.exceptions.Exceptions.throwIfFatal(r2)
                org.reactivestreams.Subscription r3 = r4.upstream
                r3.cancel()
                r4.onError(r2)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.operators.flowable.FlowableScan.ScanSubscriber.onNext(java.lang.Object):void");
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
