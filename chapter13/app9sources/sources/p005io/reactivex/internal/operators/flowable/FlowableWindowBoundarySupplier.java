package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.processors.UnicastProcessor;
import p005io.reactivex.subscribers.DisposableSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowBoundarySupplier */
/* loaded from: classes.dex */
public final class FlowableWindowBoundarySupplier<T, B> extends AbstractFlowableWithUpstream<T, Flowable<T>> {
    final int capacityHint;
    final Callable<? extends Publisher<B>> other;

    public FlowableWindowBoundarySupplier(Flowable<T> flowable, Callable<? extends Publisher<B>> callable, int i) {
        super(flowable);
        this.other = callable;
        this.capacityHint = i;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super Flowable<T>> subscriber) {
        this.source.subscribe((FlowableSubscriber) new WindowBoundaryMainSubscriber(subscriber, this.capacityHint, this.other));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowBoundarySupplier$WindowBoundaryMainSubscriber */
    /* loaded from: classes.dex */
    static final class WindowBoundaryMainSubscriber<T, B> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, Runnable {
        static final WindowBoundaryInnerSubscriber<Object, Object> BOUNDARY_DISPOSED = new WindowBoundaryInnerSubscriber<>(null);
        static final Object NEXT_WINDOW = new Object();
        private static final long serialVersionUID = 2233020065421370272L;
        final int capacityHint;
        volatile boolean done;
        final Subscriber<? super Flowable<T>> downstream;
        long emitted;
        final Callable<? extends Publisher<B>> other;
        Subscription upstream;
        UnicastProcessor<T> window;
        final AtomicReference<WindowBoundaryInnerSubscriber<T, B>> boundarySubscriber = new AtomicReference<>();
        final AtomicInteger windows = new AtomicInteger(1);
        final MpscLinkedQueue<Object> queue = new MpscLinkedQueue<>();
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicBoolean stopWindows = new AtomicBoolean();
        final AtomicLong requested = new AtomicLong();

        WindowBoundaryMainSubscriber(Subscriber<? super Flowable<T>> subscriber, int i, Callable<? extends Publisher<B>> callable) {
            this.downstream = subscriber;
            this.capacityHint = i;
            this.other = callable;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                this.queue.offer(NEXT_WINDOW);
                drain();
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.queue.offer(t);
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            disposeBoundary();
            if (this.errors.addThrowable(th)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            disposeBoundary();
            this.done = true;
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (this.stopWindows.compareAndSet(false, true)) {
                disposeBoundary();
                if (this.windows.decrementAndGet() == 0) {
                    this.upstream.cancel();
                }
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            BackpressureHelper.add(this.requested, j);
        }

        /* JADX WARN: Multi-variable type inference failed */
        void disposeBoundary() {
            Disposable disposable = (Disposable) this.boundarySubscriber.getAndSet(BOUNDARY_DISPOSED);
            if (disposable != null && disposable != BOUNDARY_DISPOSED) {
                disposable.dispose();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.windows.decrementAndGet() == 0) {
                this.upstream.cancel();
            }
        }

        void innerNext(WindowBoundaryInnerSubscriber<T, B> windowBoundaryInnerSubscriber) {
            this.boundarySubscriber.compareAndSet(windowBoundaryInnerSubscriber, null);
            this.queue.offer(NEXT_WINDOW);
            drain();
        }

        void innerError(Throwable th) {
            this.upstream.cancel();
            if (this.errors.addThrowable(th)) {
                this.done = true;
                drain();
                return;
            }
            RxJavaPlugins.onError(th);
        }

        void innerComplete() {
            this.upstream.cancel();
            this.done = true;
            drain();
        }

        /* JADX WARN: Multi-variable type inference failed */
        void drain() {
            if (getAndIncrement() == 0) {
                Subscriber<? super Flowable<T>> subscriber = this.downstream;
                MpscLinkedQueue<Object> mpscLinkedQueue = this.queue;
                AtomicThrowable atomicThrowable = this.errors;
                long j = this.emitted;
                int i = 1;
                while (this.windows.get() != 0) {
                    UnicastProcessor<T> unicastProcessor = this.window;
                    boolean z = this.done;
                    if (!z || atomicThrowable.get() == null) {
                        Object poll = mpscLinkedQueue.poll();
                        boolean z2 = poll == null;
                        if (z && z2) {
                            Throwable terminate = atomicThrowable.terminate();
                            if (terminate == null) {
                                if (unicastProcessor != 0) {
                                    this.window = null;
                                    unicastProcessor.onComplete();
                                }
                                subscriber.onComplete();
                                return;
                            }
                            if (unicastProcessor != 0) {
                                this.window = null;
                                unicastProcessor.onError(terminate);
                            }
                            subscriber.onError(terminate);
                            return;
                        } else if (z2) {
                            this.emitted = j;
                            i = addAndGet(-i);
                            if (i == 0) {
                                return;
                            }
                        } else if (poll != NEXT_WINDOW) {
                            unicastProcessor.onNext(poll);
                        } else {
                            if (unicastProcessor != 0) {
                                this.window = null;
                                unicastProcessor.onComplete();
                            }
                            if (!this.stopWindows.get()) {
                                if (j != this.requested.get()) {
                                    UnicastProcessor<T> create = UnicastProcessor.create(this.capacityHint, this);
                                    this.window = create;
                                    this.windows.getAndIncrement();
                                    try {
                                        Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.other.call(), "The other Callable returned a null Publisher");
                                        WindowBoundaryInnerSubscriber<T, B> windowBoundaryInnerSubscriber = new WindowBoundaryInnerSubscriber<>(this);
                                        if (this.boundarySubscriber.compareAndSet(null, windowBoundaryInnerSubscriber)) {
                                            publisher.subscribe(windowBoundaryInnerSubscriber);
                                            j++;
                                            subscriber.onNext(create);
                                        }
                                    } catch (Throwable th) {
                                        Exceptions.throwIfFatal(th);
                                        atomicThrowable.addThrowable(th);
                                        this.done = true;
                                    }
                                } else {
                                    this.upstream.cancel();
                                    disposeBoundary();
                                    atomicThrowable.addThrowable(new MissingBackpressureException("Could not deliver a window due to lack of requests"));
                                    this.done = true;
                                }
                            }
                        }
                    } else {
                        mpscLinkedQueue.clear();
                        Throwable terminate2 = atomicThrowable.terminate();
                        if (unicastProcessor != 0) {
                            this.window = null;
                            unicastProcessor.onError(terminate2);
                        }
                        subscriber.onError(terminate2);
                        return;
                    }
                }
                mpscLinkedQueue.clear();
                this.window = null;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableWindowBoundarySupplier$WindowBoundaryInnerSubscriber */
    /* loaded from: classes.dex */
    public static final class WindowBoundaryInnerSubscriber<T, B> extends DisposableSubscriber<B> {
        boolean done;
        final WindowBoundaryMainSubscriber<T, B> parent;

        WindowBoundaryInnerSubscriber(WindowBoundaryMainSubscriber<T, B> windowBoundaryMainSubscriber) {
            this.parent = windowBoundaryMainSubscriber;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(B b) {
            if (!this.done) {
                this.done = true;
                dispose();
                this.parent.innerNext(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.parent.innerError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.parent.innerComplete();
            }
        }
    }
}
