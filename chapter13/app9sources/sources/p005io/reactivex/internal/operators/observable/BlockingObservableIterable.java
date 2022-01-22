package p005io.reactivex.internal.operators.observable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.queue.SpscLinkedArrayQueue;
import p005io.reactivex.internal.util.BlockingHelper;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.internal.operators.observable.BlockingObservableIterable */
/* loaded from: classes.dex */
public final class BlockingObservableIterable<T> implements Iterable<T> {
    final int bufferSize;
    final ObservableSource<? extends T> source;

    public BlockingObservableIterable(ObservableSource<? extends T> observableSource, int i) {
        this.source = observableSource;
        this.bufferSize = i;
    }

    @Override // java.lang.Iterable
    public Iterator<T> iterator() {
        BlockingObservableIterator blockingObservableIterator = new BlockingObservableIterator(this.bufferSize);
        this.source.subscribe(blockingObservableIterator);
        return blockingObservableIterator;
    }

    /* renamed from: io.reactivex.internal.operators.observable.BlockingObservableIterable$BlockingObservableIterator */
    /* loaded from: classes.dex */
    static final class BlockingObservableIterator<T> extends AtomicReference<Disposable> implements Observer<T>, Iterator<T>, Disposable {
        private static final long serialVersionUID = 6695226475494099826L;
        volatile boolean done;
        Throwable error;
        final SpscLinkedArrayQueue<T> queue;
        final Lock lock = new ReentrantLock();
        final Condition condition = this.lock.newCondition();

        BlockingObservableIterator(int i) {
            this.queue = new SpscLinkedArrayQueue<>(i);
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
                try {
                    BlockingHelper.verifyNonBlocking();
                    this.lock.lock();
                    while (!this.done && this.queue.isEmpty()) {
                        this.condition.await();
                    }
                    this.lock.unlock();
                } catch (InterruptedException e) {
                    DisposableHelper.dispose(this);
                    signalConsumer();
                    throw ExceptionHelper.wrapOrThrow(e);
                }
            }
        }

        @Override // java.util.Iterator
        public T next() {
            if (hasNext()) {
                return this.queue.poll();
            }
            throw new NoSuchElementException();
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            signalConsumer();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
            this.error = th;
            this.done = true;
            signalConsumer();
        }

        @Override // p005io.reactivex.Observer
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

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
