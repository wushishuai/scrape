package p005io.reactivex.internal.operators.flowable;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.queue.MpscLinkedQueue;
import p005io.reactivex.internal.subscribers.QueueDrainSubscriber;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.QueueDrainHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.subscribers.DisposableSubscriber;
import p005io.reactivex.subscribers.SerializedSubscriber;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableBufferBoundarySupplier */
/* loaded from: classes.dex */
public final class FlowableBufferBoundarySupplier<T, U extends Collection<? super T>, B> extends AbstractFlowableWithUpstream<T, U> {
    final Callable<? extends Publisher<B>> boundarySupplier;
    final Callable<U> bufferSupplier;

    public FlowableBufferBoundarySupplier(Flowable<T> source, Callable<? extends Publisher<B>> boundarySupplier, Callable<U> bufferSupplier) {
        super(source);
        this.boundarySupplier = boundarySupplier;
        this.bufferSupplier = bufferSupplier;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super U> s) {
        this.source.subscribe((FlowableSubscriber) new BufferBoundarySupplierSubscriber(new SerializedSubscriber(s), this.bufferSupplier, this.boundarySupplier));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableBufferBoundarySupplier$BufferBoundarySupplierSubscriber */
    /* loaded from: classes.dex */
    static final class BufferBoundarySupplierSubscriber<T, U extends Collection<? super T>, B> extends QueueDrainSubscriber<T, U, U> implements FlowableSubscriber<T>, Subscription, Disposable {
        final Callable<? extends Publisher<B>> boundarySupplier;
        U buffer;
        final Callable<U> bufferSupplier;
        final AtomicReference<Disposable> other = new AtomicReference<>();
        Subscription upstream;

        /* JADX WARN: Multi-variable type inference failed */
        @Override // p005io.reactivex.internal.subscribers.QueueDrainSubscriber, p005io.reactivex.internal.util.QueueDrain
        public /* bridge */ /* synthetic */ boolean accept(Subscriber subscriber, Object obj) {
            return accept((Subscriber<? super Subscriber>) subscriber, (Subscriber) ((Collection) obj));
        }

        BufferBoundarySupplierSubscriber(Subscriber<? super U> actual, Callable<U> bufferSupplier, Callable<? extends Publisher<B>> boundarySupplier) {
            super(actual, new MpscLinkedQueue());
            this.bufferSupplier = bufferSupplier;
            this.boundarySupplier = boundarySupplier;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                Subscriber<? super U> actual = this.downstream;
                try {
                    this.buffer = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                    try {
                        Publisher<B> boundary = (Publisher) ObjectHelper.requireNonNull(this.boundarySupplier.call(), "The boundary publisher supplied is null");
                        BufferBoundarySubscriber<T, U, B> bs = new BufferBoundarySubscriber<>(this);
                        this.other.set(bs);
                        actual.onSubscribe(this);
                        if (!this.cancelled) {
                            s.request(Long.MAX_VALUE);
                            boundary.subscribe(bs);
                        }
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        this.cancelled = true;
                        s.cancel();
                        EmptySubscription.error(ex, actual);
                    }
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    this.cancelled = true;
                    s.cancel();
                    EmptySubscription.error(e, actual);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            synchronized (this) {
                U b = this.buffer;
                if (b != null) {
                    b.add(t);
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            cancel();
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            synchronized (this) {
                U b = this.buffer;
                if (b != null) {
                    this.buffer = null;
                    this.queue.offer(b);
                    this.done = true;
                    if (enter()) {
                        QueueDrainHelper.drainMaxLoop(this.queue, this.downstream, false, this, this);
                    }
                }
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            requested(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.upstream.cancel();
                disposeOther();
                if (enter()) {
                    this.queue.clear();
                }
            }
        }

        void disposeOther() {
            DisposableHelper.dispose(this.other);
        }

        void next() {
            try {
                U next = (U) ((Collection) ObjectHelper.requireNonNull(this.bufferSupplier.call(), "The buffer supplied is null"));
                try {
                    Publisher<B> boundary = (Publisher) ObjectHelper.requireNonNull(this.boundarySupplier.call(), "The boundary publisher supplied is null");
                    BufferBoundarySubscriber<T, U, B> bs = new BufferBoundarySubscriber<>(this);
                    if (DisposableHelper.replace(this.other, bs)) {
                        synchronized (this) {
                            U b = this.buffer;
                            if (b != null) {
                                this.buffer = next;
                                boundary.subscribe(bs);
                                fastPathEmitMax(b, false, this);
                            }
                        }
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    this.cancelled = true;
                    this.upstream.cancel();
                    this.downstream.onError(ex);
                }
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                cancel();
                this.downstream.onError(e);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.cancel();
            disposeOther();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.other.get() == DisposableHelper.DISPOSED;
        }

        public boolean accept(Subscriber<? super U> a, U v) {
            this.downstream.onNext(v);
            return true;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableBufferBoundarySupplier$BufferBoundarySubscriber */
    /* loaded from: classes.dex */
    public static final class BufferBoundarySubscriber<T, U extends Collection<? super T>, B> extends DisposableSubscriber<B> {
        boolean once;
        final BufferBoundarySupplierSubscriber<T, U, B> parent;

        BufferBoundarySubscriber(BufferBoundarySupplierSubscriber<T, U, B> parent) {
            this.parent = parent;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(B t) {
            if (!this.once) {
                this.once = true;
                cancel();
                this.parent.next();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.once) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.once = true;
            this.parent.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.once) {
                this.once = true;
                this.parent.next();
            }
        }
    }
}