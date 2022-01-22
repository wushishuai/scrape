package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.subscriptions.BasicQueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableRange */
/* loaded from: classes.dex */
public final class FlowableRange extends Flowable<Integer> {
    final int end;
    final int start;

    public FlowableRange(int i, int i2) {
        this.start = i;
        this.end = i + i2;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super Integer> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            subscriber.onSubscribe(new RangeConditionalSubscription((ConditionalSubscriber) subscriber, this.start, this.end));
        } else {
            subscriber.onSubscribe(new RangeSubscription(subscriber, this.start, this.end));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRange$BaseRangeSubscription */
    /* loaded from: classes.dex */
    static abstract class BaseRangeSubscription extends BasicQueueSubscription<Integer> {
        private static final long serialVersionUID = -2252972430506210021L;
        volatile boolean cancelled;
        final int end;
        int index;

        abstract void fastPath();

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public final int requestFusion(int i) {
            return i & 1;
        }

        abstract void slowPath(long j);

        BaseRangeSubscription(int i, int i2) {
            this.index = i;
            this.end = i2;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public final Integer poll() {
            int i = this.index;
            if (i == this.end) {
                return null;
            }
            this.index = i + 1;
            return Integer.valueOf(i);
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final boolean isEmpty() {
            return this.index == this.end;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final void clear() {
            this.index = this.end;
        }

        @Override // org.reactivestreams.Subscription
        public final void request(long j) {
            if (SubscriptionHelper.validate(j) && BackpressureHelper.add(this, j) == 0) {
                if (j == Long.MAX_VALUE) {
                    fastPath();
                } else {
                    slowPath(j);
                }
            }
        }

        @Override // org.reactivestreams.Subscription
        public final void cancel() {
            this.cancelled = true;
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRange$RangeSubscription */
    /* loaded from: classes.dex */
    static final class RangeSubscription extends BaseRangeSubscription {
        private static final long serialVersionUID = 2587302975077663557L;
        final Subscriber<? super Integer> downstream;

        RangeSubscription(Subscriber<? super Integer> subscriber, int i, int i2) {
            super(i, i2);
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableRange.BaseRangeSubscription
        void fastPath() {
            int i = this.end;
            Subscriber<? super Integer> subscriber = this.downstream;
            for (int i2 = this.index; i2 != i; i2++) {
                if (!this.cancelled) {
                    subscriber.onNext(Integer.valueOf(i2));
                } else {
                    return;
                }
            }
            if (!this.cancelled) {
                subscriber.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableRange.BaseRangeSubscription
        void slowPath(long j) {
            int i = this.end;
            int i2 = this.index;
            Subscriber<? super Integer> subscriber = this.downstream;
            long j2 = j;
            long j3 = 0;
            while (true) {
                if (j3 == j2 || i2 == i) {
                    if (i2 != i) {
                        j2 = get();
                        if (j3 == j2) {
                            this.index = i2;
                            j2 = addAndGet(-j3);
                            if (j2 != 0) {
                                j3 = 0;
                            } else {
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (!this.cancelled) {
                        subscriber.onComplete();
                        return;
                    } else {
                        return;
                    }
                } else if (!this.cancelled) {
                    subscriber.onNext(Integer.valueOf(i2));
                    j3++;
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRange$RangeConditionalSubscription */
    /* loaded from: classes.dex */
    static final class RangeConditionalSubscription extends BaseRangeSubscription {
        private static final long serialVersionUID = 2587302975077663557L;
        final ConditionalSubscriber<? super Integer> downstream;

        RangeConditionalSubscription(ConditionalSubscriber<? super Integer> conditionalSubscriber, int i, int i2) {
            super(i, i2);
            this.downstream = conditionalSubscriber;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableRange.BaseRangeSubscription
        void fastPath() {
            int i = this.end;
            ConditionalSubscriber<? super Integer> conditionalSubscriber = this.downstream;
            for (int i2 = this.index; i2 != i; i2++) {
                if (!this.cancelled) {
                    conditionalSubscriber.tryOnNext(Integer.valueOf(i2));
                } else {
                    return;
                }
            }
            if (!this.cancelled) {
                conditionalSubscriber.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableRange.BaseRangeSubscription
        void slowPath(long j) {
            int i = this.end;
            int i2 = this.index;
            ConditionalSubscriber<? super Integer> conditionalSubscriber = this.downstream;
            long j2 = j;
            long j3 = 0;
            while (true) {
                if (j3 == j2 || i2 == i) {
                    if (i2 != i) {
                        j2 = get();
                        if (j3 == j2) {
                            this.index = i2;
                            j2 = addAndGet(-j3);
                            if (j2 != 0) {
                                j3 = 0;
                            } else {
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (!this.cancelled) {
                        conditionalSubscriber.onComplete();
                        return;
                    } else {
                        return;
                    }
                } else if (!this.cancelled) {
                    if (conditionalSubscriber.tryOnNext(Integer.valueOf(i2))) {
                        j3++;
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }
    }
}
