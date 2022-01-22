package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFlatMapMaybe */
/* loaded from: classes.dex */
public final class FlowableFlatMapMaybe<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final boolean delayErrors;
    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
    final int maxConcurrency;

    public FlowableFlatMapMaybe(Flowable<T> flowable, Function<? super T, ? extends MaybeSource<? extends R>> function, boolean z, int i) {
        super(flowable);
        this.mapper = function;
        this.delayErrors = z;
        this.maxConcurrency = i;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super R> subscriber) {
        this.source.subscribe((FlowableSubscriber) new FlatMapMaybeSubscriber(subscriber, this.mapper, this.delayErrors, this.maxConcurrency));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFlatMapMaybe$FlatMapMaybeSubscriber */
    /* loaded from: classes.dex */
    static final class FlatMapMaybeSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = 8600231336733376951L;
        volatile boolean cancelled;
        final boolean delayErrors;
        final Subscriber<? super R> downstream;
        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
        final int maxConcurrency;
        Subscription upstream;
        final AtomicLong requested = new AtomicLong();
        final CompositeDisposable set = new CompositeDisposable();
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicInteger active = new AtomicInteger(1);
        final AtomicReference<SpscLinkedArrayQueue<R>> queue = new AtomicReference<>();

        FlatMapMaybeSubscriber(Subscriber<? super R> subscriber, Function<? super T, ? extends MaybeSource<? extends R>> function, boolean z, int i) {
            this.downstream = subscriber;
            this.mapper = function;
            this.delayErrors = z;
            this.maxConcurrency = i;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                int i = this.maxConcurrency;
                if (i == Integer.MAX_VALUE) {
                    subscription.request(Long.MAX_VALUE);
                } else {
                    subscription.request((long) i);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            try {
                MaybeSource maybeSource = (MaybeSource) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null MaybeSource");
                this.active.getAndIncrement();
                InnerObserver innerObserver = new InnerObserver();
                if (!this.cancelled && this.set.add(innerObserver)) {
                    maybeSource.subscribe(innerObserver);
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.upstream.cancel();
                onError(th);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.active.decrementAndGet();
            if (this.errors.addThrowable(th)) {
                if (!this.delayErrors) {
                    this.set.dispose();
                }
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.active.decrementAndGet();
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
            this.upstream.cancel();
            this.set.dispose();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this.requested, j);
                drain();
            }
        }

        void innerSuccess(FlatMapMaybeSubscriber<T, R>.InnerObserver innerObserver, R r) {
            this.set.delete(innerObserver);
            if (get() == 0) {
                boolean z = true;
                if (compareAndSet(0, 1)) {
                    if (this.active.decrementAndGet() != 0) {
                        z = false;
                    }
                    if (this.requested.get() != 0) {
                        this.downstream.onNext(r);
                        SpscLinkedArrayQueue<R> spscLinkedArrayQueue = this.queue.get();
                        if (!z || (spscLinkedArrayQueue != null && !spscLinkedArrayQueue.isEmpty())) {
                            BackpressureHelper.produced(this.requested, 1);
                            if (this.maxConcurrency != Integer.MAX_VALUE) {
                                this.upstream.request(1);
                            }
                        } else {
                            Throwable terminate = this.errors.terminate();
                            if (terminate != null) {
                                this.downstream.onError(terminate);
                                return;
                            } else {
                                this.downstream.onComplete();
                                return;
                            }
                        }
                    } else {
                        SpscLinkedArrayQueue<R> orCreateQueue = getOrCreateQueue();
                        synchronized (orCreateQueue) {
                            orCreateQueue.offer(r);
                        }
                    }
                    if (decrementAndGet() == 0) {
                        return;
                    }
                    drainLoop();
                }
            }
            SpscLinkedArrayQueue<R> orCreateQueue2 = getOrCreateQueue();
            synchronized (orCreateQueue2) {
                orCreateQueue2.offer(r);
            }
            this.active.decrementAndGet();
            if (getAndIncrement() != 0) {
                return;
            }
            drainLoop();
        }

        SpscLinkedArrayQueue<R> getOrCreateQueue() {
            SpscLinkedArrayQueue<R> spscLinkedArrayQueue;
            do {
                SpscLinkedArrayQueue<R> spscLinkedArrayQueue2 = this.queue.get();
                if (spscLinkedArrayQueue2 != null) {
                    return spscLinkedArrayQueue2;
                }
                spscLinkedArrayQueue = new SpscLinkedArrayQueue<>(Flowable.bufferSize());
            } while (!this.queue.compareAndSet(null, spscLinkedArrayQueue));
            return spscLinkedArrayQueue;
        }

        void innerError(FlatMapMaybeSubscriber<T, R>.InnerObserver innerObserver, Throwable th) {
            this.set.delete(innerObserver);
            if (this.errors.addThrowable(th)) {
                if (!this.delayErrors) {
                    this.upstream.cancel();
                    this.set.dispose();
                } else if (this.maxConcurrency != Integer.MAX_VALUE) {
                    this.upstream.request(1);
                }
                this.active.decrementAndGet();
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        void innerComplete(FlatMapMaybeSubscriber<T, R>.InnerObserver innerObserver) {
            this.set.delete(innerObserver);
            if (get() == 0) {
                boolean z = true;
                if (compareAndSet(0, 1)) {
                    if (this.active.decrementAndGet() != 0) {
                        z = false;
                    }
                    SpscLinkedArrayQueue<R> spscLinkedArrayQueue = this.queue.get();
                    if (!z || (spscLinkedArrayQueue != null && !spscLinkedArrayQueue.isEmpty())) {
                        if (this.maxConcurrency != Integer.MAX_VALUE) {
                            this.upstream.request(1);
                        }
                        if (decrementAndGet() != 0) {
                            drainLoop();
                            return;
                        }
                        return;
                    }
                    Throwable terminate = this.errors.terminate();
                    if (terminate != null) {
                        this.downstream.onError(terminate);
                        return;
                    } else {
                        this.downstream.onComplete();
                        return;
                    }
                }
            }
            this.active.decrementAndGet();
            if (this.maxConcurrency != Integer.MAX_VALUE) {
                this.upstream.request(1);
            }
            drain();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void clear() {
            SpscLinkedArrayQueue<R> spscLinkedArrayQueue = this.queue.get();
            if (spscLinkedArrayQueue != null) {
                spscLinkedArrayQueue.clear();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:37:0x0075, code lost:
            if (r9 != r5) goto L_0x00c6;
         */
        /* JADX WARN: Code restructure failed: missing block: B:39:0x0079, code lost:
            if (r15.cancelled == false) goto L_0x007f;
         */
        /* JADX WARN: Code restructure failed: missing block: B:40:0x007b, code lost:
            clear();
         */
        /* JADX WARN: Code restructure failed: missing block: B:41:0x007e, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:43:0x0081, code lost:
            if (r15.delayErrors != false) goto L_0x009a;
         */
        /* JADX WARN: Code restructure failed: missing block: B:45:0x008b, code lost:
            if (r15.errors.get() == null) goto L_0x009a;
         */
        /* JADX WARN: Code restructure failed: missing block: B:46:0x008d, code lost:
            r1 = r15.errors.terminate();
            clear();
            r0.onError(r1);
         */
        /* JADX WARN: Code restructure failed: missing block: B:47:0x0099, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:49:0x009e, code lost:
            if (r1.get() != 0) goto L_0x00a2;
         */
        /* JADX WARN: Code restructure failed: missing block: B:50:0x00a0, code lost:
            r5 = true;
         */
        /* JADX WARN: Code restructure failed: missing block: B:51:0x00a2, code lost:
            r5 = false;
         */
        /* JADX WARN: Code restructure failed: missing block: B:52:0x00a3, code lost:
            r6 = r2.get();
         */
        /* JADX WARN: Code restructure failed: missing block: B:53:0x00a9, code lost:
            if (r6 == null) goto L_0x00b1;
         */
        /* JADX WARN: Code restructure failed: missing block: B:55:0x00af, code lost:
            if (r6.isEmpty() == false) goto L_0x00b2;
         */
        /* JADX WARN: Code restructure failed: missing block: B:56:0x00b1, code lost:
            r11 = true;
         */
        /* JADX WARN: Code restructure failed: missing block: B:57:0x00b2, code lost:
            if (r5 == false) goto L_0x00c6;
         */
        /* JADX WARN: Code restructure failed: missing block: B:58:0x00b4, code lost:
            if (r11 == false) goto L_0x00c6;
         */
        /* JADX WARN: Code restructure failed: missing block: B:59:0x00b6, code lost:
            r1 = r15.errors.terminate();
         */
        /* JADX WARN: Code restructure failed: missing block: B:60:0x00bc, code lost:
            if (r1 == null) goto L_0x00c2;
         */
        /* JADX WARN: Code restructure failed: missing block: B:61:0x00be, code lost:
            r0.onError(r1);
         */
        /* JADX WARN: Code restructure failed: missing block: B:62:0x00c2, code lost:
            r0.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:63:0x00c5, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:65:0x00c8, code lost:
            if (r9 == 0) goto L_0x00db;
         */
        /* JADX WARN: Code restructure failed: missing block: B:66:0x00ca, code lost:
            p005io.reactivex.internal.util.BackpressureHelper.produced(r15.requested, r9);
         */
        /* JADX WARN: Code restructure failed: missing block: B:67:0x00d4, code lost:
            if (r15.maxConcurrency == Integer.MAX_VALUE) goto L_0x00db;
         */
        /* JADX WARN: Code restructure failed: missing block: B:68:0x00d6, code lost:
            r15.upstream.request(r9);
         */
        /* JADX WARN: Code restructure failed: missing block: B:69:0x00db, code lost:
            r4 = addAndGet(-r4);
         */
        /* JADX WARN: Code restructure failed: missing block: B:82:?, code lost:
            return;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                Method dump skipped, instructions count: 227
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.flowable.FlowableFlatMapMaybe.FlatMapMaybeSubscriber.drainLoop():void");
        }

        /* renamed from: io.reactivex.internal.operators.flowable.FlowableFlatMapMaybe$FlatMapMaybeSubscriber$InnerObserver */
        /* loaded from: classes.dex */
        final class InnerObserver extends AtomicReference<Disposable> implements MaybeObserver<R>, Disposable {
            private static final long serialVersionUID = -502562646270949838L;

            InnerObserver() {
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(this, disposable);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSuccess(R r) {
                FlatMapMaybeSubscriber.this.innerSuccess(this, r);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onError(Throwable th) {
                FlatMapMaybeSubscriber.this.innerError(this, th);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onComplete() {
                FlatMapMaybeSubscriber.this.innerComplete(this);
            }

            @Override // p005io.reactivex.disposables.Disposable
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }

            @Override // p005io.reactivex.disposables.Disposable
            public void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
