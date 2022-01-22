package p005io.reactivex.internal.operators.parallel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.DeferredScalarSubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.parallel.ParallelFlowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelReduceFull */
/* loaded from: classes.dex */
public final class ParallelReduceFull<T> extends Flowable<T> {
    final BiFunction<T, T, T> reducer;
    final ParallelFlowable<? extends T> source;

    public ParallelReduceFull(ParallelFlowable<? extends T> source, BiFunction<T, T, T> reducer) {
        this.source = source;
        this.reducer = reducer;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        ParallelReduceFullMainSubscriber<T> parent = new ParallelReduceFullMainSubscriber<>(s, this.source.parallelism(), this.reducer);
        s.onSubscribe(parent);
        this.source.subscribe(parent.subscribers);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelReduceFull$ParallelReduceFullMainSubscriber */
    /* loaded from: classes.dex */
    public static final class ParallelReduceFullMainSubscriber<T> extends DeferredScalarSubscription<T> {
        private static final long serialVersionUID = -5370107872170712765L;
        final BiFunction<T, T, T> reducer;
        final ParallelReduceFullInnerSubscriber<T>[] subscribers;
        final AtomicReference<SlotPair<T>> current = new AtomicReference<>();
        final AtomicInteger remaining = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        ParallelReduceFullMainSubscriber(Subscriber<? super T> subscriber, int n, BiFunction<T, T, T> reducer) {
            super(subscriber);
            ParallelReduceFullInnerSubscriber<T>[] a = new ParallelReduceFullInnerSubscriber[n];
            for (int i = 0; i < n; i++) {
                a[i] = new ParallelReduceFullInnerSubscriber<>(this, reducer);
            }
            this.subscribers = a;
            this.reducer = reducer;
            this.remaining.lazySet(n);
        }

        SlotPair<T> addValue(T value) {
            SlotPair<T> curr;
            int c;
            while (true) {
                curr = this.current.get();
                if (curr == null) {
                    curr = new SlotPair<>();
                    if (!this.current.compareAndSet(null, curr)) {
                        continue;
                    }
                }
                c = curr.tryAcquireSlot();
                if (c >= 0) {
                    break;
                }
                this.current.compareAndSet(curr, null);
            }
            if (c == 0) {
                curr.first = value;
            } else {
                curr.second = value;
            }
            if (!curr.releaseSlot()) {
                return null;
            }
            this.current.compareAndSet(curr, null);
            return curr;
        }

        @Override // p005io.reactivex.internal.subscriptions.DeferredScalarSubscription, org.reactivestreams.Subscription
        public void cancel() {
            for (ParallelReduceFullInnerSubscriber<T> inner : this.subscribers) {
                inner.cancel();
            }
        }

        void innerError(Throwable ex) {
            if (this.error.compareAndSet(null, ex)) {
                cancel();
                this.downstream.onError(ex);
            } else if (ex != this.error.get()) {
                RxJavaPlugins.onError(ex);
            }
        }

        void innerComplete(T value) {
            if (value != null) {
                while (true) {
                    SlotPair<T> sp = addValue(value);
                    if (sp == null) {
                        break;
                    }
                    try {
                        value = (T) ObjectHelper.requireNonNull(this.reducer.apply(sp.first, sp.second), "The reducer returned a null value");
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        innerError(ex);
                        return;
                    }
                }
            }
            if (this.remaining.decrementAndGet() == 0) {
                SlotPair<T> sp2 = this.current.get();
                this.current.lazySet(null);
                if (sp2 != null) {
                    complete(sp2.first);
                } else {
                    this.downstream.onComplete();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelReduceFull$ParallelReduceFullInnerSubscriber */
    /* loaded from: classes.dex */
    public static final class ParallelReduceFullInnerSubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -7954444275102466525L;
        boolean done;
        final ParallelReduceFullMainSubscriber<T> parent;
        final BiFunction<T, T, T> reducer;
        T value;

        ParallelReduceFullInnerSubscriber(ParallelReduceFullMainSubscriber<T> parent, BiFunction<T, T, T> reducer) {
            this.parent = parent;
            this.reducer = reducer;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                T v = this.value;
                if (v == null) {
                    this.value = t;
                    return;
                }
                try {
                    this.value = (T) ObjectHelper.requireNonNull(this.reducer.apply(v, t), "The reducer returned a null value");
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    get().cancel();
                    onError(ex);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.parent.innerError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.parent.innerComplete(this.value);
            }
        }

        void cancel() {
            SubscriptionHelper.cancel(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.parallel.ParallelReduceFull$SlotPair */
    /* loaded from: classes.dex */
    public static final class SlotPair<T> extends AtomicInteger {
        private static final long serialVersionUID = 473971317683868662L;
        T first;
        final AtomicInteger releaseIndex = new AtomicInteger();
        T second;

        SlotPair() {
        }

        int tryAcquireSlot() {
            int acquired;
            do {
                acquired = get();
                if (acquired >= 2) {
                    return -1;
                }
            } while (!compareAndSet(acquired, acquired + 1));
            return acquired;
        }

        boolean releaseSlot() {
            return this.releaseIndex.incrementAndGet() == 2;
        }
    }
}