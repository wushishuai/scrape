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

    public FlowableFromArray(T[] array) {
        this.array = array;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> s) {
        if (s instanceof ConditionalSubscriber) {
            s.onSubscribe(new ArrayConditionalSubscription((ConditionalSubscriber) s, this.array));
        } else {
            s.onSubscribe(new ArraySubscription(s, this.array));
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

        abstract void slowPath(long j);

        BaseArraySubscription(T[] array) {
            this.array = array;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public final int requestFusion(int mode) {
            return mode & 1;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public final T poll() {
            int i = this.index;
            T[] arr = this.array;
            if (i == arr.length) {
                return null;
            }
            this.index = i + 1;
            return (T) ObjectHelper.requireNonNull(arr[i], "array element is null");
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
        public final void request(long n) {
            if (SubscriptionHelper.validate(n) && BackpressureHelper.add(this, n) == 0) {
                if (n == Long.MAX_VALUE) {
                    fastPath();
                } else {
                    slowPath(n);
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

        ArraySubscription(Subscriber<? super T> actual, T[] array) {
            super(array);
            this.downstream = actual;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void fastPath() {
            Object[] objArr = this.array;
            int f = objArr.length;
            Subscriber<? super T> a = this.downstream;
            for (int i = this.index; i != f; i++) {
                if (!this.cancelled) {
                    Object obj = objArr[i];
                    if (obj == null) {
                        a.onError(new NullPointerException("array element is null"));
                        return;
                    } else {
                        a.onNext(obj);
                    }
                } else {
                    return;
                }
            }
            if (!this.cancelled) {
                a.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void slowPath(long r) {
            long e = 0;
            Object[] objArr = this.array;
            int f = objArr.length;
            int i = this.index;
            Subscriber<? super T> a = this.downstream;
            while (true) {
                if (e == r || i == f) {
                    if (i != f) {
                        r = get();
                        if (e == r) {
                            this.index = i;
                            r = addAndGet(-e);
                            if (r != 0) {
                                e = 0;
                            } else {
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (!this.cancelled) {
                        a.onComplete();
                        return;
                    } else {
                        return;
                    }
                } else if (!this.cancelled) {
                    Object obj = objArr[i];
                    if (obj == null) {
                        a.onError(new NullPointerException("array element is null"));
                        return;
                    }
                    a.onNext(obj);
                    e++;
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

        ArrayConditionalSubscription(ConditionalSubscriber<? super T> actual, T[] array) {
            super(array);
            this.downstream = actual;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void fastPath() {
            Object[] objArr = this.array;
            int f = objArr.length;
            ConditionalSubscriber<? super T> a = this.downstream;
            for (int i = this.index; i != f; i++) {
                if (!this.cancelled) {
                    Object obj = objArr[i];
                    if (obj == null) {
                        a.onError(new NullPointerException("array element is null"));
                        return;
                    } else {
                        a.tryOnNext(obj);
                    }
                } else {
                    return;
                }
            }
            if (!this.cancelled) {
                a.onComplete();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromArray.BaseArraySubscription
        void slowPath(long r) {
            long e = 0;
            Object[] objArr = this.array;
            int f = objArr.length;
            int i = this.index;
            ConditionalSubscriber<? super T> a = this.downstream;
            while (true) {
                if (e == r || i == f) {
                    if (i != f) {
                        r = get();
                        if (e == r) {
                            this.index = i;
                            r = addAndGet(-e);
                            if (r != 0) {
                                e = 0;
                            } else {
                                return;
                            }
                        } else {
                            continue;
                        }
                    } else if (!this.cancelled) {
                        a.onComplete();
                        return;
                    } else {
                        return;
                    }
                } else if (!this.cancelled) {
                    Object obj = objArr[i];
                    if (obj == null) {
                        a.onError(new NullPointerException("array element is null"));
                        return;
                    }
                    if (a.tryOnNext(obj)) {
                        e++;
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }
}
