package p005io.reactivex.internal.operators.flowable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.MissingBackpressureException;
import p005io.reactivex.internal.queue.SpscArrayQueue;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BlockingHelper;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.internal.operators.flowable.BlockingFlowableIterable */
/* loaded from: classes.dex */
public final class BlockingFlowableIterable<T> implements Iterable<T> {
    final int bufferSize;
    final Flowable<T> source;

    public BlockingFlowableIterable(Flowable<T> flowable, int i) {
        this.source = flowable;
        this.bufferSize = i;
    }

    @Override // java.lang.Iterable
    public Iterator<T> iterator() {
        BlockingFlowableIterator blockingFlowableIterator = new BlockingFlowableIterator(this.bufferSize);
        this.source.subscribe((FlowableSubscriber) blockingFlowableIterator);
        return blockingFlowableIterator;
    }

    /* renamed from: io.reactivex.internal.operators.flowable.BlockingFlowableIterable$BlockingFlowableIterator */
    /* loaded from: classes.dex */
    static final class BlockingFlowableIterator<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T>, Iterator<T>, Runnable, Disposable {
        private static final long serialVersionUID = 6695226475494099826L;
        final long batchSize;
        volatile boolean done;
        Throwable error;
        final long limit;
        long produced;
        final SpscArrayQueue<T> queue;
        final Lock lock = new ReentrantLock();
        final Condition condition = this.lock.newCondition();

        BlockingFlowableIterator(int i) {
            this.queue = new SpscArrayQueue<>(i);
            this.batchSize = (long) i;
            this.limit = (long) (i - (i >> 2));
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            while (true) {
                boolean z = this.done;
                boolean isEmpty = this.queue.isEmpty();
                if (z) {
                    Throwable th = this.error;
                    if (th != null) {
                        throw ExceptionHelper.wrapOrThrow(th);
                    } else if (isEmpty) {
                        return false;
                    }
                }
                if (!isEmpty) {
                    return true;
                }
                BlockingHelper.verifyNonBlocking();
                this.lock.lock();
                while (!this.done && this.queue.isEmpty()) {
                    try {
                        try {
                            this.condition.await();
                        } catch (InterruptedException e) {
                            run();
                            throw ExceptionHelper.wrapOrThrow(e);
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }
            }
        }

        @Override // java.util.Iterator
        public T next() {
            if (hasNext()) {
                T poll = this.queue.poll();
                long j = this.produced + 1;
                if (j == this.limit) {
                    this.produced = 0;
                    get().request(j);
                } else {
                    this.produced = j;
                }
                return poll;
            }
            throw new NoSuchElementException();
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this, subscription, this.batchSize);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.queue.offer(t)) {
                SubscriptionHelper.cancel(this);
                onError(new MissingBackpressureException("Queue full?!"));
                return;
            }
            signalConsumer();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.error = th;
            this.done = true;
            signalConsumer();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            signalConsumer();
        }

        void signalConsumer() {
            this.lock.lock();
            try {
                this.condition.signalAll();
            } finally {
                this.lock.unlock();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            SubscriptionHelper.cancel(this);
            signalConsumer();
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            SubscriptionHelper.cancel(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return SubscriptionHelper.isCancelled(get());
        }
    }
}
