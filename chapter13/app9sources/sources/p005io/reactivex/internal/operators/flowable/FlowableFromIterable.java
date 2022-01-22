package p005io.reactivex.internal.operators.flowable;

import java.util.Iterator;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.subscriptions.BasicQueueSubscription;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFromIterable */
/* loaded from: classes.dex */
public final class FlowableFromIterable<T> extends Flowable<T> {
    final Iterable<? extends T> source;

    public FlowableFromIterable(Iterable<? extends T> iterable) {
        this.source = iterable;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        try {
            subscribe(subscriber, this.source.iterator());
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptySubscription.error(th, subscriber);
        }
    }

    public static <T> void subscribe(Subscriber<? super T> subscriber, Iterator<? extends T> it) {
        try {
            if (!it.hasNext()) {
                EmptySubscription.complete(subscriber);
            } else if (subscriber instanceof ConditionalSubscriber) {
                subscriber.onSubscribe(new IteratorConditionalSubscription((ConditionalSubscriber) subscriber, it));
            } else {
                subscriber.onSubscribe(new IteratorSubscription(subscriber, it));
            }
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            EmptySubscription.error(th, subscriber);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromIterable$BaseRangeSubscription */
    /* loaded from: classes.dex */
    static abstract class BaseRangeSubscription<T> extends BasicQueueSubscription<T> {
        private static final long serialVersionUID = -2252972430506210021L;
        volatile boolean cancelled;

        /* renamed from: it */
        Iterator<? extends T> f123it;
        boolean once;

        abstract void fastPath();

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public final int requestFusion(int i) {
            return i & 1;
        }

        abstract void slowPath(long j);

        BaseRangeSubscription(Iterator<? extends T> it) {
            this.f123it = it;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public final T poll() {
            Iterator<? extends T> it = this.f123it;
            if (it == null) {
                return null;
            }
            if (!this.once) {
                this.once = true;
            } else if (!it.hasNext()) {
                return null;
            }
            return (T) ObjectHelper.requireNonNull(this.f123it.next(), "Iterator.next() returned a null value");
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final boolean isEmpty() {
            Iterator<? extends T> it = this.f123it;
            return it == null || !it.hasNext();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public final void clear() {
            this.f123it = null;
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

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromIterable$IteratorSubscription */
    /* loaded from: classes.dex */
    public static final class IteratorSubscription<T> extends BaseRangeSubscription<T> {
        private static final long serialVersionUID = -6022804456014692607L;
        final Subscriber<? super T> downstream;

        IteratorSubscription(Subscriber<? super T> subscriber, Iterator<? extends T> it) {
            super(it);
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromIterable.BaseRangeSubscription
        void fastPath() {
            Iterator it = this.f123it;
            Subscriber<? super T> subscriber = this.downstream;
            while (!this.cancelled) {
                try {
                    Object obj = (Object) it.next();
                    if (!this.cancelled) {
                        if (obj == 0) {
                            subscriber.onError(new NullPointerException("Iterator.next() returned a null value"));
                            return;
                        }
                        subscriber.onNext(obj);
                        if (!this.cancelled) {
                            try {
                                if (!it.hasNext()) {
                                    if (!this.cancelled) {
                                        subscriber.onComplete();
                                        return;
                                    }
                                    return;
                                }
                            } catch (Throwable th) {
                                Exceptions.throwIfFatal(th);
                                subscriber.onError(th);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    subscriber.onError(th2);
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromIterable.BaseRangeSubscription
        void slowPath(long j) {
            Iterator it = this.f123it;
            Subscriber<? super T> subscriber = this.downstream;
            long j2 = j;
            long j3 = 0;
            while (true) {
                if (j3 == j2) {
                    j2 = get();
                    if (j3 == j2) {
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
                    try {
                        Object obj = (Object) it.next();
                        if (!this.cancelled) {
                            if (obj == 0) {
                                subscriber.onError(new NullPointerException("Iterator.next() returned a null value"));
                                return;
                            }
                            subscriber.onNext(obj);
                            if (!this.cancelled) {
                                try {
                                    if (it.hasNext()) {
                                        j3++;
                                    } else if (!this.cancelled) {
                                        subscriber.onComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                } catch (Throwable th) {
                                    Exceptions.throwIfFatal(th);
                                    subscriber.onError(th);
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        subscriber.onError(th2);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromIterable$IteratorConditionalSubscription */
    /* loaded from: classes.dex */
    public static final class IteratorConditionalSubscription<T> extends BaseRangeSubscription<T> {
        private static final long serialVersionUID = -6022804456014692607L;
        final ConditionalSubscriber<? super T> downstream;

        IteratorConditionalSubscription(ConditionalSubscriber<? super T> conditionalSubscriber, Iterator<? extends T> it) {
            super(it);
            this.downstream = conditionalSubscriber;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromIterable.BaseRangeSubscription
        void fastPath() {
            Iterator it = this.f123it;
            ConditionalSubscriber<? super T> conditionalSubscriber = this.downstream;
            while (!this.cancelled) {
                try {
                    Object obj = (Object) it.next();
                    if (!this.cancelled) {
                        if (obj == 0) {
                            conditionalSubscriber.onError(new NullPointerException("Iterator.next() returned a null value"));
                            return;
                        }
                        conditionalSubscriber.tryOnNext(obj);
                        if (!this.cancelled) {
                            try {
                                if (!it.hasNext()) {
                                    if (!this.cancelled) {
                                        conditionalSubscriber.onComplete();
                                        return;
                                    }
                                    return;
                                }
                            } catch (Throwable th) {
                                Exceptions.throwIfFatal(th);
                                conditionalSubscriber.onError(th);
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } catch (Throwable th2) {
                    Exceptions.throwIfFatal(th2);
                    conditionalSubscriber.onError(th2);
                    return;
                }
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableFromIterable.BaseRangeSubscription
        void slowPath(long j) {
            Iterator it = this.f123it;
            ConditionalSubscriber<? super T> conditionalSubscriber = this.downstream;
            long j2 = j;
            long j3 = 0;
            while (true) {
                if (j3 == j2) {
                    j2 = get();
                    if (j3 == j2) {
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
                    try {
                        Object obj = (Object) it.next();
                        if (!this.cancelled) {
                            if (obj == 0) {
                                conditionalSubscriber.onError(new NullPointerException("Iterator.next() returned a null value"));
                                return;
                            }
                            boolean tryOnNext = conditionalSubscriber.tryOnNext(obj);
                            if (!this.cancelled) {
                                try {
                                    if (!it.hasNext()) {
                                        if (!this.cancelled) {
                                            conditionalSubscriber.onComplete();
                                            return;
                                        }
                                        return;
                                    } else if (tryOnNext) {
                                        j3++;
                                    }
                                } catch (Throwable th) {
                                    Exceptions.throwIfFatal(th);
                                    conditionalSubscriber.onError(th);
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        conditionalSubscriber.onError(th2);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }
}
