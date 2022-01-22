package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.subscribers.BasicFuseableConditionalSubscriber;
import p005io.reactivex.internal.subscribers.BasicFuseableSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableMap */
/* loaded from: classes.dex */
public final class FlowableMap<T, U> extends AbstractFlowableWithUpstream<T, U> {
    final Function<? super T, ? extends U> mapper;

    public FlowableMap(Flowable<T> flowable, Function<? super T, ? extends U> function) {
        super(flowable);
        this.mapper = function;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super U> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            this.source.subscribe((FlowableSubscriber) new MapConditionalSubscriber((ConditionalSubscriber) subscriber, this.mapper));
        } else {
            this.source.subscribe((FlowableSubscriber) new MapSubscriber(subscriber, this.mapper));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableMap$MapSubscriber */
    /* loaded from: classes.dex */
    public static final class MapSubscriber<T, U> extends BasicFuseableSubscriber<T, U> {
        final Function<? super T, ? extends U> mapper;

        public MapSubscriber(Subscriber<? super U> subscriber, Function<? super T, ? extends U> function) {
            super(subscriber);
            this.mapper = function;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                if (this.sourceMode != 0) {
                    this.downstream.onNext(null);
                    return;
                }
                try {
                    this.downstream.onNext(ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper function returned a null value."));
                } catch (Throwable th) {
                    fail(th);
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            return transitiveBoundaryFusion(i);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public U poll() throws Exception {
            T poll = this.f180qs.poll();
            if (poll != null) {
                return (U) ObjectHelper.requireNonNull(this.mapper.apply(poll), "The mapper function returned a null value.");
            }
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableMap$MapConditionalSubscriber */
    /* loaded from: classes.dex */
    public static final class MapConditionalSubscriber<T, U> extends BasicFuseableConditionalSubscriber<T, U> {
        final Function<? super T, ? extends U> mapper;

        MapConditionalSubscriber(ConditionalSubscriber<? super U> conditionalSubscriber, Function<? super T, ? extends U> function) {
            super(conditionalSubscriber);
            this.mapper = function;
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                if (this.sourceMode != 0) {
                    this.downstream.onNext(null);
                    return;
                }
                try {
                    this.downstream.onNext(ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper function returned a null value."));
                } catch (Throwable th) {
                    fail(th);
                }
            }
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.fuseable.ConditionalSubscriber
        public boolean tryOnNext(T t) {
            if (this.done) {
                return false;
            }
            try {
                return this.downstream.tryOnNext(ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper function returned a null value."));
            } catch (Throwable th) {
                fail(th);
                return true;
            }
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            return transitiveBoundaryFusion(i);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public U poll() throws Exception {
            T poll = this.f179qs.poll();
            if (poll != null) {
                return (U) ObjectHelper.requireNonNull(this.mapper.apply(poll), "The mapper function returned a null value.");
            }
            return null;
        }
    }
}
