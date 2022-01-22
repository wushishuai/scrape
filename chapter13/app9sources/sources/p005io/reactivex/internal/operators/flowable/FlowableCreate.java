package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.BackpressureStrategy;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableEmitter;
import p005io.reactivex.FlowableOnSubscribe;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.functions.Cancellable;
import p005io.reactivex.internal.disposables.CancellableDisposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate */
/* loaded from: classes.dex */
public final class FlowableCreate<T> extends Flowable<T> {
    final BackpressureStrategy backpressure;
    final FlowableOnSubscribe<T> source;

    public FlowableCreate(FlowableOnSubscribe<T> flowableOnSubscribe, BackpressureStrategy backpressureStrategy) {
        this.source = flowableOnSubscribe;
        this.backpressure = backpressureStrategy;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> subscriber) {
        BaseEmitter baseEmitter;
        switch (this.backpressure) {
            case MISSING:
                baseEmitter = new MissingEmitter(subscriber);
                break;
            case ERROR:
                baseEmitter = new ErrorAsyncEmitter(subscriber);
                break;
            case DROP:
                baseEmitter = new DropAsyncEmitter(subscriber);
                break;
            case LATEST:
                baseEmitter = new LatestAsyncEmitter(subscriber);
                break;
            default:
                baseEmitter = new BufferAsyncEmitter(subscriber, bufferSize());
                break;
        }
        subscriber.onSubscribe(baseEmitter);
        try {
            this.source.subscribe(baseEmitter);
        } catch (Throwable th) {
            Exceptions.throwIfFatal(th);
            baseEmitter.onError(th);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$SerializedEmitter */
    /* loaded from: classes.dex */
    static final class SerializedEmitter<T> extends AtomicInteger implements FlowableEmitter<T> {
        private static final long serialVersionUID = 4883307006032401862L;
        volatile boolean done;
        final BaseEmitter<T> emitter;
        final AtomicThrowable error = new AtomicThrowable();
        final SimplePlainQueue<T> queue = new SpscLinkedArrayQueue(16);

        @Override // p005io.reactivex.FlowableEmitter
        public FlowableEmitter<T> serialize() {
            return this;
        }

        SerializedEmitter(BaseEmitter<T> baseEmitter) {
            this.emitter = baseEmitter;
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            if (!this.emitter.isCancelled() && !this.done) {
                if (t == null) {
                    onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                    return;
                }
                if (get() != 0 || !compareAndSet(0, 1)) {
                    SimplePlainQueue<T> simplePlainQueue = this.queue;
                    synchronized (simplePlainQueue) {
                        simplePlainQueue.offer(t);
                    }
                    if (getAndIncrement() != 0) {
                        return;
                    }
                } else {
                    this.emitter.onNext(t);
                    if (decrementAndGet() == 0) {
                        return;
                    }
                }
                drainLoop();
            }
        }

        @Override // p005io.reactivex.Emitter
        public void onError(Throwable th) {
            if (!tryOnError(th)) {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.FlowableEmitter
        public boolean tryOnError(Throwable th) {
            if (this.emitter.isCancelled() || this.done) {
                return false;
            }
            if (th == null) {
                th = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (!this.error.addThrowable(th)) {
                return false;
            }
            this.done = true;
            drain();
            return true;
        }

        @Override // p005io.reactivex.Emitter
        public void onComplete() {
            if (!this.emitter.isCancelled() && !this.done) {
                this.done = true;
                drain();
            }
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void drainLoop() {
            BaseEmitter<T> baseEmitter = this.emitter;
            SimplePlainQueue<T> simplePlainQueue = this.queue;
            AtomicThrowable atomicThrowable = this.error;
            int i = 1;
            while (!baseEmitter.isCancelled()) {
                if (atomicThrowable.get() != null) {
                    simplePlainQueue.clear();
                    baseEmitter.onError(atomicThrowable.terminate());
                    return;
                }
                boolean z = this.done;
                T poll = simplePlainQueue.poll();
                boolean z2 = poll == null;
                if (z && z2) {
                    baseEmitter.onComplete();
                    return;
                } else if (z2) {
                    i = addAndGet(-i);
                    if (i == 0) {
                        return;
                    }
                } else {
                    baseEmitter.onNext(poll);
                }
            }
            simplePlainQueue.clear();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public void setDisposable(Disposable disposable) {
            this.emitter.setDisposable(disposable);
        }

        @Override // p005io.reactivex.FlowableEmitter
        public void setCancellable(Cancellable cancellable) {
            this.emitter.setCancellable(cancellable);
        }

        @Override // p005io.reactivex.FlowableEmitter
        public long requested() {
            return this.emitter.requested();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public boolean isCancelled() {
            return this.emitter.isCancelled();
        }

        @Override // java.util.concurrent.atomic.AtomicInteger, java.lang.Object
        public String toString() {
            return this.emitter.toString();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$BaseEmitter */
    /* loaded from: classes.dex */
    public static abstract class BaseEmitter<T> extends AtomicLong implements FlowableEmitter<T>, Subscription {
        private static final long serialVersionUID = 7326289992464377023L;
        final Subscriber<? super T> downstream;
        final SequentialDisposable serial = new SequentialDisposable();

        void onRequested() {
        }

        void onUnsubscribed() {
        }

        BaseEmitter(Subscriber<? super T> subscriber) {
            this.downstream = subscriber;
        }

        @Override // p005io.reactivex.Emitter
        public void onComplete() {
            complete();
        }

        protected void complete() {
            if (!isCancelled()) {
                try {
                    this.downstream.onComplete();
                } finally {
                    this.serial.dispose();
                }
            }
        }

        @Override // p005io.reactivex.Emitter
        public final void onError(Throwable th) {
            if (!tryOnError(th)) {
                RxJavaPlugins.onError(th);
            }
        }

        @Override // p005io.reactivex.FlowableEmitter
        public boolean tryOnError(Throwable th) {
            return error(th);
        }

        /* JADX WARN: Finally extract failed */
        protected boolean error(Throwable th) {
            if (th == null) {
                th = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (isCancelled()) {
                return false;
            }
            try {
                this.downstream.onError(th);
                this.serial.dispose();
                return true;
            } catch (Throwable th2) {
                this.serial.dispose();
                throw th2;
            }
        }

        @Override // org.reactivestreams.Subscription
        public final void cancel() {
            this.serial.dispose();
            onUnsubscribed();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final boolean isCancelled() {
            return this.serial.isDisposed();
        }

        @Override // org.reactivestreams.Subscription
        public final void request(long j) {
            if (SubscriptionHelper.validate(j)) {
                BackpressureHelper.add(this, j);
                onRequested();
            }
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final void setDisposable(Disposable disposable) {
            this.serial.update(disposable);
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final void setCancellable(Cancellable cancellable) {
            setDisposable(new CancellableDisposable(cancellable));
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final long requested() {
            return get();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final FlowableEmitter<T> serialize() {
            return new SerializedEmitter(this);
        }

        @Override // java.util.concurrent.atomic.AtomicLong, java.lang.Object
        public String toString() {
            return String.format("%s{%s}", getClass().getSimpleName(), super.toString());
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$MissingEmitter */
    /* loaded from: classes.dex */
    static final class MissingEmitter<T> extends BaseEmitter<T> {
        private static final long serialVersionUID = 3776720187248809713L;

        MissingEmitter(Subscriber<? super T> subscriber) {
            super(subscriber);
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            long j;
            if (!isCancelled()) {
                if (t != null) {
                    this.downstream.onNext(t);
                    do {
                        j = get();
                        if (j == 0) {
                            return;
                        }
                    } while (!compareAndSet(j, j - 1));
                    return;
                }
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$NoOverflowBaseAsyncEmitter */
    /* loaded from: classes.dex */
    static abstract class NoOverflowBaseAsyncEmitter<T> extends BaseEmitter<T> {
        private static final long serialVersionUID = 4127754106204442833L;

        abstract void onOverflow();

        NoOverflowBaseAsyncEmitter(Subscriber<? super T> subscriber) {
            super(subscriber);
        }

        @Override // p005io.reactivex.Emitter
        public final void onNext(T t) {
            if (!isCancelled()) {
                if (t == null) {
                    onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                } else if (get() != 0) {
                    this.downstream.onNext(t);
                    BackpressureHelper.produced(this, 1);
                } else {
                    onOverflow();
                }
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$DropAsyncEmitter */
    /* loaded from: classes.dex */
    static final class DropAsyncEmitter<T> extends NoOverflowBaseAsyncEmitter<T> {
        private static final long serialVersionUID = 8360058422307496563L;

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.NoOverflowBaseAsyncEmitter
        void onOverflow() {
        }

        DropAsyncEmitter(Subscriber<? super T> subscriber) {
            super(subscriber);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$ErrorAsyncEmitter */
    /* loaded from: classes.dex */
    static final class ErrorAsyncEmitter<T> extends NoOverflowBaseAsyncEmitter<T> {
        private static final long serialVersionUID = 338953216916120960L;

        ErrorAsyncEmitter(Subscriber<? super T> subscriber) {
            super(subscriber);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.NoOverflowBaseAsyncEmitter
        void onOverflow() {
            onError(new MissingBackpressureException("create: could not emit value due to lack of requests"));
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$BufferAsyncEmitter */
    /* loaded from: classes.dex */
    static final class BufferAsyncEmitter<T> extends BaseEmitter<T> {
        private static final long serialVersionUID = 2427151001689639875L;
        volatile boolean done;
        Throwable error;
        final SpscLinkedArrayQueue<T> queue;
        final AtomicInteger wip = new AtomicInteger();

        BufferAsyncEmitter(Subscriber<? super T> subscriber, int i) {
            super(subscriber);
            this.queue = new SpscLinkedArrayQueue<>(i);
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            if (!this.done && !isCancelled()) {
                if (t == null) {
                    onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                    return;
                }
                this.queue.offer(t);
                drain();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter, p005io.reactivex.FlowableEmitter
        public boolean tryOnError(Throwable th) {
            if (this.done || isCancelled()) {
                return false;
            }
            if (th == null) {
                th = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            this.error = th;
            this.done = true;
            drain();
            return true;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter, p005io.reactivex.Emitter
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter
        void onRequested() {
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter
        void onUnsubscribed() {
            if (this.wip.getAndIncrement() == 0) {
                this.queue.clear();
            }
        }

        void drain() {
            if (this.wip.getAndIncrement() == 0) {
                Subscriber subscriber = this.downstream;
                SpscLinkedArrayQueue<T> spscLinkedArrayQueue = this.queue;
                int i = 1;
                do {
                    long j = get();
                    long j2 = 0;
                    while (j2 != j) {
                        if (isCancelled()) {
                            spscLinkedArrayQueue.clear();
                            return;
                        }
                        boolean z = this.done;
                        T poll = spscLinkedArrayQueue.poll();
                        boolean z2 = poll == null;
                        if (z && z2) {
                            Throwable th = this.error;
                            if (th != null) {
                                error(th);
                                return;
                            } else {
                                complete();
                                return;
                            }
                        } else if (z2) {
                            break;
                        } else {
                            subscriber.onNext(poll);
                            j2++;
                        }
                    }
                    if (j2 == j) {
                        if (isCancelled()) {
                            spscLinkedArrayQueue.clear();
                            return;
                        }
                        boolean z3 = this.done;
                        boolean isEmpty = spscLinkedArrayQueue.isEmpty();
                        if (z3 && isEmpty) {
                            Throwable th2 = this.error;
                            if (th2 != null) {
                                error(th2);
                                return;
                            } else {
                                complete();
                                return;
                            }
                        }
                    }
                    if (j2 != 0) {
                        BackpressureHelper.produced(this, j2);
                    }
                    i = this.wip.addAndGet(-i);
                } while (i != 0);
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$LatestAsyncEmitter */
    /* loaded from: classes.dex */
    static final class LatestAsyncEmitter<T> extends BaseEmitter<T> {
        private static final long serialVersionUID = 4023437720691792495L;
        volatile boolean done;
        Throwable error;
        final AtomicReference<T> queue = new AtomicReference<>();
        final AtomicInteger wip = new AtomicInteger();

        LatestAsyncEmitter(Subscriber<? super T> subscriber) {
            super(subscriber);
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            if (!this.done && !isCancelled()) {
                if (t == null) {
                    onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                    return;
                }
                this.queue.set(t);
                drain();
            }
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter, p005io.reactivex.FlowableEmitter
        public boolean tryOnError(Throwable th) {
            if (this.done || isCancelled()) {
                return false;
            }
            if (th == null) {
                onError(new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources."));
            }
            this.error = th;
            this.done = true;
            drain();
            return true;
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter, p005io.reactivex.Emitter
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter
        void onRequested() {
            drain();
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.BaseEmitter
        void onUnsubscribed() {
            if (this.wip.getAndIncrement() == 0) {
                this.queue.lazySet(null);
            }
        }

        void drain() {
            boolean z;
            if (this.wip.getAndIncrement() == 0) {
                Subscriber subscriber = this.downstream;
                AtomicReference<T> atomicReference = this.queue;
                int i = 1;
                do {
                    long j = get();
                    long j2 = 0;
                    while (true) {
                        z = false;
                        if (j2 == j) {
                            break;
                        } else if (isCancelled()) {
                            atomicReference.lazySet(null);
                            return;
                        } else {
                            boolean z2 = this.done;
                            T andSet = atomicReference.getAndSet(null);
                            boolean z3 = andSet == null;
                            if (z2 && z3) {
                                Throwable th = this.error;
                                if (th != null) {
                                    error(th);
                                    return;
                                } else {
                                    complete();
                                    return;
                                }
                            } else if (z3) {
                                break;
                            } else {
                                subscriber.onNext(andSet);
                                j2++;
                            }
                        }
                    }
                    if (j2 == j) {
                        if (isCancelled()) {
                            atomicReference.lazySet(null);
                            return;
                        }
                        boolean z4 = this.done;
                        if (atomicReference.get() == null) {
                            z = true;
                        }
                        if (z4 && z) {
                            Throwable th2 = this.error;
                            if (th2 != null) {
                                error(th2);
                                return;
                            } else {
                                complete();
                                return;
                            }
                        }
                    }
                    if (j2 != 0) {
                        BackpressureHelper.produced(this, j2);
                    }
                    i = this.wip.addAndGet(-i);
                } while (i != 0);
            }
        }
    }
}
