package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.subscriptions.BasicQueueSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFromArray */
/* loaded from: classes.dex */
public final class FlowableFromArray<T> extends Flowable<T> {
    final T[] array;

    public FlowableFromArray(T[] tArr) {
        this.array = tArr;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        if (subscriber instanceof ConditionalSubscriber) {
            subscriber.onSubscribe(new ArrayConditionalSubscription((ConditionalSubscriber) subscriber, this.array));
        } else {
            subscriber.onSubscribe(new ArraySubscription(subscriber, this.array));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromArray$BaseArraySubscription */
    /* loaded from: classes.dex */
    static abstract class BaseArraySubscription<T> extends BasicQueueSubscription<T> {
        private static final long serialVersionUID = -2252972430506210021L;
        final T[] array;
        volatile boolean cancelled;
        int index;

        abstract void fastPath();

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public final int requestFusion(int i) {
            return i & 1;
        }

        abstract void slowPath(long j);

        BaseArraySubscription(T[] tArr) {
            this.array = tArr;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public final T poll() {
            int i = this.index;
            T[] tArr = this.array;
            if (i == tArr.length) {
                return null;
            }
            this.index = i + 1;
            return (T) ObjectHelper.requireNonNull(tArr[i], "array element is null");
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final boolean isEmpty() {
            return this.index == this.array.length;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final void clear() {
            this.index = this.array.length;
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

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromArray$ArraySubscription */
    /* loaded from: classes.dex */
    static final class ArraySubscription<T> extends BaseArraySubscription<T> {
        private static final long serialVersionUID = 2587302975077663557L;
        final Subscriber<? super T> downstream;

        ArraySubscription(Subscriber<? super T> subscriber, T[] tArr) {
            super(tArr);
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void fastPath() {
            Object[] objArr = this.array;
            int length = objArr.length;
            Subscriber<? super T> subscriber = this.downstream;
            for (int i = this.index; i != length; i++) {
                if (!this.cancelled) {
                    Object obj = objArr[i];
                    if (obj == null) {
                        subscriber.onError(new NullPointerException("array element is null"));
                        return;
                    } else {
                        subscriber.onNext(obj);
                    }
                } else {
                    return;
                }
            }
            if (!this.cancelled) {
                subscriber.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void slowPath(long j) {
            Object[] objArr = this.array;
            int length = objArr.length;
            int i = this.index;
            Subscriber<? super T> subscriber = this.downstream;
            long j2 = j;
            long j3 = 0;
            while (true) {
                if (j3 == j2 || i == length) {
                    if (i != length) {
                        j2 = get();
                        if (j3 == j2) {
                            this.index = i;
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
                    Object obj = objArr[i];
                    if (obj == null) {
                        subscriber.onError(new NullPointerException("array element is null"));
                        return;
                    }
                    subscriber.onNext(obj);
                    j3++;
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromArray$ArrayConditionalSubscription */
    /* loaded from: classes.dex */
    static final class ArrayConditionalSubscription<T> extends BaseArraySubscription<T> {
        private static final long serialVersionUID = 2587302975077663557L;
        final ConditionalSubscriber<? super T> downstream;

        ArrayConditionalSubscription(ConditionalSubscriber<? super T> conditionalSubscriber, T[] tArr) {
            super(tArr);
            this.downstream = conditionalSubscriber;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void fastPath() {
            Object[] objArr = this.array;
            int length = objArr.length;
            ConditionalSubscriber<? super T> conditionalSubscriber = this.downstream;
            for (int i = this.index; i != length; i++) {
                if (!this.cancelled) {
                    Object obj = objArr[i];
                    if (obj == null) {
                        conditionalSubscriber.onError(new NullPointerException("array element is null"));
                        return;
                    } else {
                        conditionalSubscriber.tryOnNext(obj);
                    }
                } else {
                    return;
                }
            }
            if (!this.cancelled) {
                conditionalSubscriber.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void slowPath(long j) {
            Object[] objArr = this.array;
            int length = objArr.length;
            int i = this.index;
            ConditionalSubscriber<? super T> conditionalSubscriber = this.downstream;
            long j2 = j;
            long j3 = 0;
            while (true) {
                if (j3 == j2 || i == length) {
                    if (i != length) {
                        j2 = get();
                        if (j3 == j2) {
                            this.index = i;
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
                    Object obj = objArr[i];
                    if (obj == null) {
                        conditionalSubscriber.onError(new NullPointerException("array element is null"));
                        return;
                    }
                    if (conditionalSubscriber.tryOnNext(obj)) {
                        j3++;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }
}
