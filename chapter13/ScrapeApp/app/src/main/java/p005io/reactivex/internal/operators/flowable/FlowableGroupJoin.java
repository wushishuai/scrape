package p005io.reactivex.internal.operators.flowable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.SimpleQueue;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.processors.UnicastProcessor;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableGroupJoin */
/* loaded from: classes.dex */
public final class FlowableGroupJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends AbstractFlowableWithUpstream<TLeft, R> {
    final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;
    final Publisher<? extends TRight> other;
    final BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector;
    final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableGroupJoin$JoinSupport */
    /* loaded from: classes.dex */
    public interface JoinSupport {
        void innerClose(boolean z, LeftRightEndSubscriber leftRightEndSubscriber);

        void innerCloseError(Throwable th);

        void innerComplete(LeftRightSubscriber leftRightSubscriber);

        void innerError(Throwable th);

        void innerValue(boolean z, Object obj);
    }

    public FlowableGroupJoin(Flowable<TLeft> source, Publisher<? extends TRight> other, Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd, Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd, BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector) {
        super(source);
        this.other = other;
        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.resultSelector = resultSelector;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> s) {
        GroupJoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> parent = new GroupJoinSubscription<>(s, this.leftEnd, this.rightEnd, this.resultSelector);
        s.onSubscribe(parent);
        LeftRightSubscriber left = new LeftRightSubscriber(parent, true);
        parent.disposables.add(left);
        LeftRightSubscriber right = new LeftRightSubscriber(parent, false);
        parent.disposables.add(right);
        this.source.subscribe((FlowableSubscriber) left);
        this.other.subscribe(right);
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableGroupJoin$GroupJoinSubscription */
    /* loaded from: classes.dex */
    static final class GroupJoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> extends AtomicInteger implements Subscription, JoinSupport {
        private static final long serialVersionUID = -6071216598687999801L;
        volatile boolean cancelled;
        final Subscriber<? super R> downstream;
        final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;
        int leftIndex;
        final BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector;
        final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;
        int rightIndex;
        static final Integer LEFT_VALUE = 1;
        static final Integer RIGHT_VALUE = 2;
        static final Integer LEFT_CLOSE = 3;
        static final Integer RIGHT_CLOSE = 4;
        final AtomicLong requested = new AtomicLong();
        final CompositeDisposable disposables = new CompositeDisposable();
        final SpscLinkedArrayQueue<Object> queue = new SpscLinkedArrayQueue<>(Flowable.bufferSize());
        final Map<Integer, UnicastProcessor<TRight>> lefts = new LinkedHashMap();
        final Map<Integer, TRight> rights = new LinkedHashMap();
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final AtomicInteger active = new AtomicInteger(2);

        GroupJoinSubscription(Subscriber<? super R> actual, Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd, Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd, BiFunction<? super TLeft, ? super Flowable<TRight>, ? extends R> resultSelector) {
            this.downstream = actual;
            this.leftEnd = leftEnd;
            this.rightEnd = rightEnd;
            this.resultSelector = resultSelector;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelAll();
                if (getAndIncrement() == 0) {
                    this.queue.clear();
                }
            }
        }

        void cancelAll() {
            this.disposables.dispose();
        }

        void errorAll(Subscriber<?> a) {
            Throwable ex = ExceptionHelper.terminate(this.error);
            for (UnicastProcessor<TRight> up : this.lefts.values()) {
                up.onError(ex);
            }
            this.lefts.clear();
            this.rights.clear();
            a.onError(ex);
        }

        void fail(Throwable exc, Subscriber<?> a, SimpleQueue<?> q) {
            Exceptions.throwIfFatal(exc);
            ExceptionHelper.addThrowable(this.error, exc);
            q.clear();
            cancelAll();
            errorAll(a);
        }

        /* JADX WARN: Multi-variable type inference failed */
        void drain() {
            int missed;
            if (getAndIncrement() == 0) {
                SpscLinkedArrayQueue<Object> q = this.queue;
                Subscriber<? super R> a = this.downstream;
                int missed2 = 1;
                while (!this.cancelled) {
                    if (this.error.get() != null) {
                        q.clear();
                        cancelAll();
                        errorAll(a);
                        return;
                    }
                    boolean d = this.active.get() == 0;
                    Integer mode = (Integer) q.poll();
                    boolean empty = mode == null;
                    if (d && empty) {
                        for (UnicastProcessor<TRight> unicastProcessor : this.lefts.values()) {
                            unicastProcessor.onComplete();
                        }
                        this.lefts.clear();
                        this.rights.clear();
                        this.disposables.dispose();
                        a.onComplete();
                        return;
                    } else if (empty) {
                        missed2 = addAndGet(-missed2);
                        if (missed2 == 0) {
                            return;
                        }
                    } else {
                        Object val = q.poll();
                        if (mode == LEFT_VALUE) {
                            UnicastProcessor<TRight> up = UnicastProcessor.create();
                            int idx = this.leftIndex;
                            this.leftIndex = idx + 1;
                            this.lefts.put(Integer.valueOf(idx), up);
                            try {
                                Publisher<TLeftEnd> p = (Publisher) ObjectHelper.requireNonNull(this.leftEnd.apply(val), "The leftEnd returned a null Publisher");
                                LeftRightEndSubscriber end = new LeftRightEndSubscriber(this, true, idx);
                                this.disposables.add(end);
                                p.subscribe(end);
                                if (this.error.get() != null) {
                                    q.clear();
                                    cancelAll();
                                    errorAll(a);
                                    return;
                                }
                                try {
                                    Object obj = (Object) ObjectHelper.requireNonNull(this.resultSelector.apply(val, up), "The resultSelector returned a null value");
                                    if (this.requested.get() != 0) {
                                        a.onNext(obj);
                                        missed = missed2;
                                        BackpressureHelper.produced(this.requested, 1);
                                        for (TRight right : this.rights.values()) {
                                            up.onNext(right);
                                        }
                                    } else {
                                        fail(new MissingBackpressureException("Could not emit value due to lack of requests"), a, q);
                                        return;
                                    }
                                } catch (Throwable exc) {
                                    fail(exc, a, q);
                                    return;
                                }
                            } catch (Throwable exc2) {
                                fail(exc2, a, q);
                                return;
                            }
                        } else {
                            missed = missed2;
                            if (mode == RIGHT_VALUE) {
                                int idx2 = this.rightIndex;
                                this.rightIndex = idx2 + 1;
                                this.rights.put(Integer.valueOf(idx2), val);
                                try {
                                    Publisher<TRightEnd> p2 = (Publisher) ObjectHelper.requireNonNull(this.rightEnd.apply(val), "The rightEnd returned a null Publisher");
                                    LeftRightEndSubscriber end2 = new LeftRightEndSubscriber(this, false, idx2);
                                    this.disposables.add(end2);
                                    p2.subscribe(end2);
                                    if (this.error.get() != null) {
                                        q.clear();
                                        cancelAll();
                                        errorAll(a);
                                        return;
                                    }
                                    for (UnicastProcessor<TRight> up2 : this.lefts.values()) {
                                        up2.onNext(val);
                                    }
                                } catch (Throwable exc3) {
                                    fail(exc3, a, q);
                                    return;
                                }
                            } else if (mode == LEFT_CLOSE) {
                                LeftRightEndSubscriber end3 = (LeftRightEndSubscriber) val;
                                UnicastProcessor<TRight> up3 = this.lefts.remove(Integer.valueOf(end3.index));
                                this.disposables.remove(end3);
                                if (up3 != null) {
                                    up3.onComplete();
                                }
                            } else if (mode == RIGHT_CLOSE) {
                                LeftRightEndSubscriber end4 = (LeftRightEndSubscriber) val;
                                this.rights.remove(Integer.valueOf(end4.index));
                                this.disposables.remove(end4);
                            }
                        }
                        missed2 = missed;
                    }
                }
                q.clear();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableGroupJoin.JoinSupport
        public void innerError(Throwable ex) {
            if (ExceptionHelper.addThrowable(this.error, ex)) {
                this.active.decrementAndGet();
                drain();
                return;
            }
            RxJavaPlugins.onError(ex);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableGroupJoin.JoinSupport
        public void innerComplete(LeftRightSubscriber sender) {
            this.disposables.delete(sender);
            this.active.decrementAndGet();
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableGroupJoin.JoinSupport
        public void innerValue(boolean isLeft, Object o) {
            synchronized (this) {
                this.queue.offer(isLeft ? LEFT_VALUE : RIGHT_VALUE, o);
            }
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableGroupJoin.JoinSupport
        public void innerClose(boolean isLeft, LeftRightEndSubscriber index) {
            synchronized (this) {
                this.queue.offer(isLeft ? LEFT_CLOSE : RIGHT_CLOSE, index);
            }
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableGroupJoin.JoinSupport
        public void innerCloseError(Throwable ex) {
            if (ExceptionHelper.addThrowable(this.error, ex)) {
                drain();
            } else {
                RxJavaPlugins.onError(ex);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableGroupJoin$LeftRightSubscriber */
    /* loaded from: classes.dex */
    static final class LeftRightSubscriber extends AtomicReference<Subscription> implements FlowableSubscriber<Object>, Disposable {
        private static final long serialVersionUID = 1883890389173668373L;
        final boolean isLeft;
        final JoinSupport parent;

        public LeftRightSubscriber(JoinSupport parent, boolean isLeft) {
            this.parent = parent;
            this.isLeft = isLeft;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return SubscriptionHelper.isCancelled(get());
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object t) {
            this.parent.innerValue(this.isLeft, t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.parent.innerError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.parent.innerComplete(this);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableGroupJoin$LeftRightEndSubscriber */
    /* loaded from: classes.dex */
    public static final class LeftRightEndSubscriber extends AtomicReference<Subscription> implements FlowableSubscriber<Object>, Disposable {
        private static final long serialVersionUID = 1883890389173668373L;
        final int index;
        final boolean isLeft;
        final JoinSupport parent;

        public LeftRightEndSubscriber(JoinSupport parent, boolean isLeft, int index) {
            this.parent = parent;
            this.isLeft = isLeft;
            this.index = index;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return SubscriptionHelper.isCancelled(get());
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, Long.MAX_VALUE);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object t) {
            if (SubscriptionHelper.cancel(this)) {
                this.parent.innerClose(this.isLeft, this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.parent.innerCloseError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.parent.innerClose(this.isLeft, this);
        }
    }
}