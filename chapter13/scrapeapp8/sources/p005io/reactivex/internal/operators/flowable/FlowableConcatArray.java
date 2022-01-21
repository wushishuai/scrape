package p005io.reactivex.internal.operators.flowable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.internal.subscriptions.SubscriptionArbiter;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableConcatArray */
/* loaded from: classes.dex */
public final class FlowableConcatArray<T> extends Flowable<T> {
    final boolean delayError;
    final Publisher<? extends T>[] sources;

    public FlowableConcatArray(Publisher<? extends T>[] sources, boolean delayError) {
        this.sources = sources;
        this.delayError = delayError;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        ConcatArraySubscriber<T> parent = new ConcatArraySubscriber<>(this.sources, this.delayError, s);
        s.onSubscribe(parent);
        parent.onComplete();
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableConcatArray$ConcatArraySubscriber */
    /* loaded from: classes.dex */
    static final class ConcatArraySubscriber<T> extends SubscriptionArbiter implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -8158322871608889516L;
        final boolean delayError;
        final Subscriber<? super T> downstream;
        List<Throwable> errors;
        int index;
        long produced;
        final Publisher<? extends T>[] sources;
        final AtomicInteger wip = new AtomicInteger();

        ConcatArraySubscriber(Publisher<? extends T>[] sources, boolean delayError, Subscriber<? super T> downstream) {
            this.downstream = downstream;
            this.sources = sources;
            this.delayError = delayError;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            setSubscription(s);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.produced++;
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.delayError) {
                List<Throwable> list = this.errors;
                if (list == null) {
                    list = new ArrayList<>((this.sources.length - this.index) + 1);
                    this.errors = list;
                }
                list.add(t);
                onComplete();
                return;
            }
            this.downstream.onError(t);
        }

        /* JADX INFO: Multiple debug info for r4v1 org.reactivestreams.Publisher<? extends T>: [D('list' java.util.List<java.lang.Throwable>), D('p' org.reactivestreams.Publisher<? extends T>)] */
        /* JADX INFO: Multiple debug info for r5v6 long: [D('r' long), D('ex' java.lang.Throwable)] */
        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.wip.getAndIncrement() == 0) {
                Publisher<? extends T>[] sources = this.sources;
                int n = sources.length;
                int i = this.index;
                while (i != n) {
                    Publisher<? extends T> p = sources[i];
                    if (p == null) {
                        Throwable nullPointerException = new NullPointerException("A Publisher entry is null");
                        if (this.delayError) {
                            List<Throwable> list = this.errors;
                            if (list == null) {
                                list = new ArrayList<>((n - i) + 1);
                                this.errors = list;
                            }
                            list.add(nullPointerException);
                            i++;
                        } else {
                            this.downstream.onError(nullPointerException);
                            return;
                        }
                    } else {
                        long r = this.produced;
                        if (r != 0) {
                            this.produced = 0;
                            produced(r);
                        }
                        p.subscribe(this);
                        i++;
                        this.index = i;
                        if (this.wip.decrementAndGet() == 0) {
                            return;
                        }
                    }
                }
                List<Throwable> list2 = this.errors;
                if (list2 == null) {
                    this.downstream.onComplete();
                } else if (list2.size() == 1) {
                    this.downstream.onError(list2.get(0));
                } else {
                    this.downstream.onError(new CompositeException(list2));
                }
            }
        }
    }
}
