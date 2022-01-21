package p005io.reactivex.internal.operators.flowable;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.QueueSubscription;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.BasicIntQueueSubscription;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFlattenIterable */
/* loaded from: classes.dex */
public final class FlowableFlattenIterable<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final Function<? super T, ? extends Iterable<? extends R>> mapper;
    final int prefetch;

    public FlowableFlattenIterable(Flowable<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch) {
        super(source);
        this.mapper = mapper;
        this.prefetch = prefetch;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super R> s) {
        if (this.source instanceof Callable) {
            try {
                Object call = ((Callable) this.source).call();
                if (call == null) {
                    EmptySubscription.complete(s);
                    return;
                }
                try {
                    FlowableFromIterable.subscribe(s, ((Iterable) this.mapper.apply(call)).iterator());
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    EmptySubscription.error(ex, s);
                }
            } catch (Throwable ex2) {
                Exceptions.throwIfFatal(ex2);
                EmptySubscription.error(ex2, s);
            }
        } else {
            this.source.subscribe((FlowableSubscriber) new FlattenIterableSubscriber(s, this.mapper, this.prefetch));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFlattenIterable$FlattenIterableSubscriber */
    /* loaded from: classes.dex */
    static final class FlattenIterableSubscriber<T, R> extends BasicIntQueueSubscription<R> implements FlowableSubscriber<T> {
        private static final long serialVersionUID = -3096000382929934955L;
        volatile boolean cancelled;
        int consumed;
        Iterator<? extends R> current;
        volatile boolean done;
        final Subscriber<? super R> downstream;
        int fusionMode;
        final int limit;
        final Function<? super T, ? extends Iterable<? extends R>> mapper;
        final int prefetch;
        SimpleQueue<T> queue;
        Subscription upstream;
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicLong requested = new AtomicLong();

        FlattenIterableSubscriber(Subscriber<? super R> actual, Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch) {
            this.downstream = actual;
            this.mapper = mapper;
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                if (s instanceof QueueSubscription) {
                    QueueSubscription<T> qs = (QueueSubscription) s;
                    int m = qs.requestFusion(3);
                    if (m == 1) {
                        this.fusionMode = m;
                        this.queue = qs;
                        this.done = true;
                        this.downstream.onSubscribe(this);
                        return;
                    } else if (m == 2) {
                        this.fusionMode = m;
                        this.queue = qs;
                        this.downstream.onSubscribe(this);
                        s.request((long) this.prefetch);
                        return;
                    }
                }
                this.queue = new SpscArrayQueue(this.prefetch);
                this.downstream.onSubscribe(this);
                s.request((long) this.prefetch);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.done) {
                if (this.fusionMode != 0 || this.queue.offer(t)) {
                    drain();
                } else {
                    onError(new MissingBackpressureException("Queue is full?!"));
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done || !ExceptionHelper.addThrowable(this.error, t)) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.cancel();
                if (getAndIncrement() == 0) {
                    this.queue.clear();
                }
            }
        }

        void drain() {
            if (getAndIncrement() == 0) {
                Subscriber<? super R> a = this.downstream;
                SimpleQueue<T> q = this.queue;
                boolean replenish = this.fusionMode != 1;
                int missed = 1;
                Iterator<? extends R> it = this.current;
                while (true) {
                    if (it == null) {
                        boolean d = this.done;
                        try {
                            T t = q.poll();
                            if (!checkTerminated(d, t == null, a, q)) {
                                if (t != null) {
                                    try {
                                        it = ((Iterable) this.mapper.apply(t)).iterator();
                                        if (!it.hasNext()) {
                                            it = null;
                                            consumedOne(replenish);
                                        } else {
                                            this.current = it;
                                        }
                                    } catch (Throwable ex) {
                                        Exceptions.throwIfFatal(ex);
                                        this.upstream.cancel();
                                        ExceptionHelper.addThrowable(this.error, ex);
                                        a.onError(ExceptionHelper.terminate(this.error));
                                        return;
                                    }
                                }
                            } else {
                                return;
                            }
                        } catch (Throwable ex2) {
                            Exceptions.throwIfFatal(ex2);
                            this.upstream.cancel();
                            ExceptionHelper.addThrowable(this.error, ex2);
                            Throwable ex3 = ExceptionHelper.terminate(this.error);
                            this.current = null;
                            q.clear();
                            a.onError(ex3);
                            return;
                        }
                    }
                    if (it != null) {
                        long r = this.requested.get();
                        long e = 0;
                        while (true) {
                            if (e == r) {
                                break;
                            } else if (!checkTerminated(this.done, false, a, q)) {
                                try {
                                    a.onNext((Object) ObjectHelper.requireNonNull(it.next(), "The iterator returned a null value"));
                                    if (!checkTerminated(this.done, false, a, q)) {
                                        e++;
                                        try {
                                            if (!it.hasNext()) {
                                                consumedOne(replenish);
                                                it = null;
                                                this.current = null;
                                                break;
                                            }
                                        } catch (Throwable ex4) {
                                            Exceptions.throwIfFatal(ex4);
                                            this.current = null;
                                            this.upstream.cancel();
                                            ExceptionHelper.addThrowable(this.error, ex4);
                                            a.onError(ExceptionHelper.terminate(this.error));
                                            return;
                                        }
                                    } else {
                                        return;
                                    }
                                } catch (Throwable ex5) {
                                    Exceptions.throwIfFatal(ex5);
                                    this.current = null;
                                    this.upstream.cancel();
                                    ExceptionHelper.addThrowable(this.error, ex5);
                                    a.onError(ExceptionHelper.terminate(this.error));
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                        if (e == r) {
                            if (checkTerminated(this.done, q.isEmpty() && it == null, a, q)) {
                                return;
                            }
                        }
                        if (!(e == 0 || r == Long.MAX_VALUE)) {
                            this.requested.addAndGet(-e);
                        }
                        if (it == null) {
                            continue;
                        }
                    }
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        return;
                    }
                }
            }
        }

        void consumedOne(boolean enabled) {
            if (enabled) {
                int c = this.consumed + 1;
                if (c == this.limit) {
                    this.consumed = 0;
                    this.upstream.request((long) c);
                    return;
                }
                this.consumed = c;
            }
        }

        boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, SimpleQueue<?> q) {
            if (this.cancelled) {
                this.current = null;
                q.clear();
                return true;
            } else if (!d) {
                return false;
            } else {
                if (this.error.get() != null) {
                    Throwable ex = ExceptionHelper.terminate(this.error);
                    this.current = null;
                    q.clear();
                    a.onError(ex);
                    return true;
                } else if (!empty) {
                    return false;
                } else {
                    a.onComplete();
                    return true;
                }
            }
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.current = null;
            this.queue.clear();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.current == null && this.queue.isEmpty();
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public R poll() throws Exception {
            Iterator<? extends R> it = this.current;
            while (true) {
                if (it == null) {
                    T v = this.queue.poll();
                    if (v != null) {
                        it = ((Iterable) this.mapper.apply(v)).iterator();
                        if (it.hasNext()) {
                            this.current = it;
                            break;
                        }
                        it = null;
                    } else {
                        return null;
                    }
                } else {
                    break;
                }
            }
            R r = (R) ObjectHelper.requireNonNull(it.next(), "The iterator returned a null value");
            if (!it.hasNext()) {
                this.current = null;
            }
            return r;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int requestedMode) {
            if ((requestedMode & 1) == 0 || this.fusionMode != 1) {
                return 0;
            }
            return 1;
        }
    }
}
