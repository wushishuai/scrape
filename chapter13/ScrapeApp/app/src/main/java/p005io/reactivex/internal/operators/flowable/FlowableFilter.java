package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.subscribers.BasicFuseableConditionalSubscriber;
import p005io.reactivex.internal.subscribers.BasicFuseableSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFilter */
/* loaded from: classes.dex */
public final class FlowableFilter<T> extends AbstractFlowableWithUpstream<T, T> {
    final Predicate<? super T> predicate;

    public FlowableFilter(Flowable<T> source, Predicate<? super T> predicate) {
        super(source);
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        if (s instanceof ConditionalSubscriber) {
            this.source.subscribe((FlowableSubscriber) new FilterConditionalSubscriber((ConditionalSubscriber) s, this.predicate));
        } else {
            this.source.subscribe((FlowableSubscriber) new FilterSubscriber(s, this.predicate));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFilter$FilterSubscriber */
    /* loaded from: classes.dex */
    static final class FilterSubscriber<T> extends BasicFuseableSubscriber<T, T> implements ConditionalSubscriber<T> {
        final Predicate<? super T> filter;

        FilterSubscriber(Subscriber<? super T> actual, Predicate<? super T> filter) {
            super(actual);
            this.filter = filter;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!tryOnNext(t)) {
                this.upstream.request(1);
            }
        }

        @Override // p005io.reactivex.internal.fuseable.ConditionalSubscriber
        public boolean tryOnNext(T t) {
            if (this.done) {
                return false;
            }
            if (this.sourceMode != 0) {
                this.downstream.onNext(null);
                return true;
            }
            try {
                boolean b = this.filter.test(t);
                if (b) {
                    this.downstream.onNext(t);
                }
                return b;
            } catch (Throwable e) {
                fail(e);
                return true;
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            QueueSubscription<T> qs = this.f180qs;
            Predicate<? super T> f = this.filter;
            while (true) {
                T t = qs.poll();
                if (t == null) {
                    return null;
                }
                if (f.test(t)) {
                    return t;
                }
                if (this.sourceMode == 2) {
                    qs.request(1);
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFilter$FilterConditionalSubscriber */
    /* loaded from: classes.dex */
    static final class FilterConditionalSubscriber<T> extends BasicFuseableConditionalSubscriber<T, T> {
        final Predicate<? super T> filter;

        FilterConditionalSubscriber(ConditionalSubscriber<? super T> actual, Predicate<? super T> filter) {
            super(actual);
            this.filter = filter;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!tryOnNext(t)) {
                this.upstream.request(1);
            }
        }

        @Override // p005io.reactivex.internal.fuseable.ConditionalSubscriber
        public boolean tryOnNext(T t) {
            if (this.done) {
                return false;
            }
            if (this.sourceMode != 0) {
                return this.downstream.tryOnNext(null);
            }
            try {
                if (!this.filter.test(t) || !this.downstream.tryOnNext(t)) {
                    return false;
                }
                return true;
            } catch (Throwable e) {
                fail(e);
                return true;
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() throws Exception {
            QueueSubscription<T> qs = this.f179qs;
            Predicate<? super T> f = this.filter;
            while (true) {
                T t = qs.poll();
                if (t == null) {
                    return null;
                }
                if (f.test(t)) {
                    return t;
                }
                if (this.sourceMode == 2) {
                    qs.request(1);
                }
            }
        }
    }
}
