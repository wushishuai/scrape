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

    public FlowableCreate(FlowableOnSubscribe<T> source, BackpressureStrategy backpressure) {
        this.source = source;
        this.backpressure = backpressure;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> t) {
        BaseEmitter<T> emitter;
        switch (this.backpressure) {
            case MISSING:
                emitter = new MissingEmitter<>(t);
                break;
            case ERROR:
                emitter = new ErrorAsyncEmitter<>(t);
                break;
            case DROP:
                emitter = new DropAsyncEmitter<>(t);
                break;
            case LATEST:
                emitter = new LatestAsyncEmitter<>(t);
                break;
            default:
                emitter = new BufferAsyncEmitter<>(t, bufferSize());
                break;
        }
        t.onSubscribe(emitter);
        try {
            this.source.subscribe(emitter);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            emitter.onError(ex);
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

        SerializedEmitter(BaseEmitter<T> emitter) {
            this.emitter = emitter;
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            if (!this.emitter.isCancelled() && !this.done) {
                if (t == null) {
                    onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                    return;
                }
                if (get() != 0 || !compareAndSet(0, 1)) {
                    SimplePlainQueue<T> q = this.queue;
                    synchronized (q) {
                        q.offer(t);
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
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t);
            }
        }

        @Override // p005io.reactivex.FlowableEmitter
        public boolean tryOnError(Throwable t) {
            if (this.emitter.isCancelled() || this.done) {
                return false;
            }
            if (t == null) {
                t = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (!this.error.addThrowable(t)) {
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
            BaseEmitter<T> e = this.emitter;
            SimplePlainQueue<T> q = this.queue;
            AtomicThrowable error = this.error;
            int missed = 1;
            while (!e.isCancelled()) {
                if (error.get() != null) {
                    q.clear();
                    e.onError(error.terminate());
                    return;
                }
                boolean d = this.done;
                T v = q.poll();
                boolean empty = v == null;
                if (d && empty) {
                    e.onComplete();
                    return;
                } else if (empty) {
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        return;
                    }
                } else {
                    e.onNext(v);
                }
            }
            q.clear();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public void setDisposable(Disposable d) {
            this.emitter.setDisposable(d);
        }

        @Override // p005io.reactivex.FlowableEmitter
        public void setCancellable(Cancellable c) {
            this.emitter.setCancellable(c);
        }

        @Override // p005io.reactivex.FlowableEmitter
        public long requested() {
            return this.emitter.requested();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public boolean isCancelled() {
            return this.emitter.isCancelled();
        }

        @Override // p005io.reactivex.FlowableEmitter
        public FlowableEmitter<T> serialize() {
            return this;
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

        BaseEmitter(Subscriber<? super T> downstream) {
            this.downstream = downstream;
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
        public final void onError(Throwable e) {
            if (!tryOnError(e)) {
                RxJavaPlugins.onError(e);
            }
        }

        @Override // p005io.reactivex.FlowableEmitter
        public boolean tryOnError(Throwable e) {
            return error(e);
        }

        /* JADX WARN: Finally extract failed */
        protected boolean error(Throwable e) {
            if (e == null) {
                e = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (isCancelled()) {
                return false;
            }
            try {
                this.downstream.onError(e);
                this.serial.dispose();
                return true;
            } catch (Throwable th) {
                this.serial.dispose();
                throw th;
            }
        }

        @Override // org.reactivestreams.Subscription
        public final void cancel() {
            this.serial.dispose();
            onUnsubscribed();
        }

        void onUnsubscribed() {
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final boolean isCancelled() {
            return this.serial.isDisposed();
        }

        @Override // org.reactivestreams.Subscription
        public final void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this, n);
                onRequested();
            }
        }

        void onRequested() {
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final void setDisposable(Disposable d) {
            this.serial.update(d);
        }

        @Override // p005io.reactivex.FlowableEmitter
        public final void setCancellable(Cancellable c) {
            setDisposable(new CancellableDisposable(c));
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

        MissingEmitter(Subscriber<? super T> downstream) {
            super(downstream);
        }

        @Override // p005io.reactivex.Emitter
        public void onNext(T t) {
            long r;
            if (!isCancelled()) {
                if (t != null) {
                    this.downstream.onNext(t);
                    do {
                        r = get();
                        if (r == 0) {
                            return;
                        }
                    } while (!compareAndSet(r, r - 1));
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

        NoOverflowBaseAsyncEmitter(Subscriber<? super T> downstream) {
            super(downstream);
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

        DropAsyncEmitter(Subscriber<? super T> downstream) {
            super(downstream);
        }

        @Override // p005io.reactivex.internal.operators.flowable.FlowableCreate.NoOverflowBaseAsyncEmitter
        void onOverflow() {
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableCreate$ErrorAsyncEmitter */
    /* loaded from: classes.dex */
    static final class ErrorAsyncEmitter<T> extends NoOverflowBaseAsyncEmitter<T> {
        private static final long serialVersionUID = 338953216916120960L;

        ErrorAsyncEmitter(Subscriber<? super T> downstream) {
            super(downstream);
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

        BufferAsyncEmitter(Subscriber<? super T> actual, int capacityHint) {
            super(actual);
            this.queue = new SpscLinkedArrayQueue<>(capacityHint);
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
        public boolean tryOnError(Throwable e) {
            if (this.done || isCancelled()) {
                return false;
            }
            if (e == null) {
                e = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            this.error = e;
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
                int missed = 1;
                Subscriber<? super T> a = this.downstream;
                SpscLinkedArrayQueue<T> q = this.queue;
                do {
                    long r = get();
                    long e = 0;
                    while (e != r) {
                        if (isCancelled()) {
                            q.clear();
                            return;
                        }
                        boolean d = this.done;
                        T o = q.poll();
                        boolean empty = o == null;
                        if (d && empty) {
                            Throwable ex = this.error;
                            if (ex != null) {
                                error(ex);
                                return;
                            } else {
                                complete();
                                return;
                            }
                        } else if (empty) {
                            break;
                        } else {
                            a.onNext(o);
                            e++;
                        }
                    }
                    if (e == r) {
                        if (isCancelled()) {
                            q.clear();
                            return;
                        }
                        boolean d2 = this.done;
                        boolean empty2 = q.isEmpty();
                        if (d2 && empty2) {
                            Throwable ex2 = this.error;
                            if (ex2 != null) {
                                error(ex2);
                                return;
                            } else {
                                complete();
                                return;
                            }
                        }
                    }
                    if (e != 0) {
                        BackpressureHelper.produced(this, e);
                    }
                    missed = this.wip.addAndGet(-missed);
                } while (missed != 0);
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

        LatestAsyncEmitter(Subscriber<? super T> downstream) {
            super(downstream);
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
        public boolean tryOnError(Throwable e) {
            if (this.done || isCancelled()) {
                return false;
            }
            if (e == null) {
                onError(new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources."));
            }
            this.error = e;
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

        /* JADX WARN: Code restructure failed: missing block: B:27:0x004c, code lost:
            if (r5 != r3) goto L_0x0072;
         */
        /* JADX WARN: Code restructure failed: missing block: B:29:0x0052, code lost:
            if (isCancelled() == false) goto L_0x0058;
         */
        /* JADX WARN: Code restructure failed: missing block: B:30:0x0054, code lost:
            r2.lazySet(null);
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x0057, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:32:0x0058, code lost:
            r9 = r13.done;
         */
        /* JADX WARN: Code restructure failed: missing block: B:33:0x005e, code lost:
            if (r2.get() != null) goto L_0x0061;
         */
        /* JADX WARN: Code restructure failed: missing block: B:34:0x0061, code lost:
            r7 = false;
         */
        /* JADX WARN: Code restructure failed: missing block: B:35:0x0062, code lost:
            if (r9 == false) goto L_0x0072;
         */
        /* JADX WARN: Code restructure failed: missing block: B:36:0x0064, code lost:
            if (r7 == false) goto L_0x0072;
         */
        /* JADX WARN: Code restructure failed: missing block: B:37:0x0066, code lost:
            r8 = r13.error;
         */
        /* JADX WARN: Code restructure failed: missing block: B:38:0x0068, code lost:
            if (r8 == null) goto L_0x006e;
         */
        /* JADX WARN: Code restructure failed: missing block: B:39:0x006a, code lost:
            error(r8);
         */
        /* JADX WARN: Code restructure failed: missing block: B:40:0x006e, code lost:
            complete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:41:0x0071, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:43:0x0076, code lost:
            if (r5 == 0) goto L_0x007b;
         */
        /* JADX WARN: Code restructure failed: missing block: B:44:0x0078, code lost:
            p005io.reactivex.internal.util.BackpressureHelper.produced(r13, r5);
         */
        /* JADX WARN: Code restructure failed: missing block: B:45:0x007b, code lost:
            r0 = r13.wip.addAndGet(-r0);
         */
        /* JADX WARN: Code restructure failed: missing block: B:56:?, code lost:
            return;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drain() {
            /*
                r13 = this;
                java.util.concurrent.atomic.AtomicInteger r0 = r13.wip
                int r0 = r0.getAndIncrement()
                if (r0 == 0) goto L_0x0009
                return
            L_0x0009:
                r0 = 1
                org.reactivestreams.Subscriber r1 = r13.downstream
                java.util.concurrent.atomic.AtomicReference<T> r2 = r13.queue
            L_0x000e:
                long r3 = r13.get()
                r5 = 0
            L_0x0014:
                r7 = 1
                r8 = 0
                r9 = 0
                int r10 = (r5 > r3 ? 1 : (r5 == r3 ? 0 : -1))
                if (r10 == 0) goto L_0x004a
                boolean r10 = r13.isCancelled()
                if (r10 == 0) goto L_0x0025
                r2.lazySet(r9)
                return
            L_0x0025:
                boolean r10 = r13.done
                java.lang.Object r11 = r2.getAndSet(r9)
                if (r11 != 0) goto L_0x002f
                r12 = 1
                goto L_0x0030
            L_0x002f:
                r12 = 0
            L_0x0030:
                if (r10 == 0) goto L_0x0040
                if (r12 == 0) goto L_0x0040
                java.lang.Throwable r7 = r13.error
                if (r7 == 0) goto L_0x003c
                r13.error(r7)
                goto L_0x003f
            L_0x003c:
                r13.complete()
            L_0x003f:
                return
            L_0x0040:
                if (r12 == 0) goto L_0x0043
                goto L_0x004a
            L_0x0043:
                r1.onNext(r11)
                r7 = 1
                long r5 = r5 + r7
                goto L_0x0014
            L_0x004a:
                int r10 = (r5 > r3 ? 1 : (r5 == r3 ? 0 : -1))
                if (r10 != 0) goto L_0x0072
                boolean r10 = r13.isCancelled()
                if (r10 == 0) goto L_0x0058
                r2.lazySet(r9)
                return
            L_0x0058:
                boolean r9 = r13.done
                java.lang.Object r10 = r2.get()
                if (r10 != 0) goto L_0x0061
                goto L_0x0062
            L_0x0061:
                r7 = 0
            L_0x0062:
                if (r9 == 0) goto L_0x0072
                if (r7 == 0) goto L_0x0072
                java.lang.Throwable r8 = r13.error
                if (r8 == 0) goto L_0x006e
                r13.error(r8)
                goto L_0x0071
            L_0x006e:
                r13.complete()
            L_0x0071:
                return
            L_0x0072:
                r7 = 0
                int r9 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
                if (r9 == 0) goto L_0x007b
                p005io.reactivex.internal.util.BackpressureHelper.produced(r13, r5)
            L_0x007b:
                java.util.concurrent.atomic.AtomicInteger r7 = r13.wip
                int r8 = -r0
                int r0 = r7.addAndGet(r8)
                if (r0 != 0) goto L_0x0086
                return
            L_0x0086:
                goto L_0x000e
            */
            throw new UnsupportedOperationException("Method not decompiled: p005io.reactivex.internal.operators.flowable.FlowableCreate.LatestAsyncEmitter.drain():void");
        }
    }
}
