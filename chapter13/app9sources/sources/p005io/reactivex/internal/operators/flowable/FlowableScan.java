package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableScan */
/* loaded from: classes.dex */
public final class FlowableScan<T> extends AbstractFlowableWithUpstream<T, T> {
    final BiFunction<T, T, T> accumulator;

    public FlowableScan(Flowable<T> flowable, BiFunction<T, T, T> biFunction) {
        super(flowable);
        this.accumulator = biFunction;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        this.source.subscribe((FlowableSubscriber) new ScanSubscriber(subscriber, this.accumulator));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableScan$ScanSubscriber */
    /* loaded from: classes.dex */
    static final class ScanSubscriber<T> implements FlowableSubscriber<T>, Subscription {
        final BiFunction<T, T, T> accumulator;
        boolean done;
        final Subscriber<? super T> downstream;
        Subscription upstream;
        T value;

        ScanSubscriber(Subscriber<? super T> subscriber, BiFunction<T, T, T> biFunction) {
            this.downstream = subscriber;
            this.accumulator = biFunction;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }

        /* JADX WARN: Type inference failed for: r4v3, types: [T, java.lang.Object] */
        /* JADX WARN: Unknown variable types count: 1 */
        @Override // org.reactivestreams.Subscriber
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        public void onNext(T r4) {
            /*
                r3 = this;
                boolean r0 = r3.done
                if (r0 == 0) goto L_0x0005
                return
            L_0x0005:
                org.reactivestreams.Subscriber<? super T> r0 = r3.downstream
                T r1 = r3.value
                if (r1 != 0) goto L_0x0011
                r3.value = r4
                r0.onNext(r4)
                goto L_0x0022
            L_0x0011:
                io.reactivex.functions.BiFunction<T, T, T> r2 = r3.accumulator     // Catch: Throwable -> 0x0023
                java.lang.Object r4 = r2.apply(r1, r4)     // Catch: Throwable -> 0x0023
                java.lang.String r1 = "The value returned by the accumulator is null"
                java.lang.Object r4 = p005io.reactivex.internal.functions.ObjectHelper.requireNonNull(r4, r1)     // Catch: Throwable -> 0x0023
                r3.value = r4
                r0.onNext(r4)
            L_0x0022:
                return
            L_0x0023:
                r4 = move-exception
                p005io.reactivex.exceptions.Exceptions.throwIfFatal(r4)
                org.reactivestreams.Subscription r0 = r3.upstream
                r0.cancel()
                r3.onError(r4)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.flowable.FlowableScan.ScanSubscriber.onNext(java.lang.Object):void");
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }
    }
}
