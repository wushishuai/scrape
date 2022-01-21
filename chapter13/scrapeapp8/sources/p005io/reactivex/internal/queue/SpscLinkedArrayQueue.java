package p005io.reactivex.internal.queue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.util.Pow2;

/* renamed from: io.reactivex.internal.queue.SpscLinkedArrayQueue */
/* loaded from: classes.dex */
public final class SpscLinkedArrayQueue<T> implements SimplePlainQueue<T> {
    AtomicReferenceArray<Object> consumerBuffer;
    final int consumerMask;
    AtomicReferenceArray<Object> producerBuffer;
    long producerLookAhead;
    int producerLookAheadStep;
    final int producerMask;
    static final int MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.spsc.max.lookahead.step", 4096).intValue();
    private static final Object HAS_NEXT = new Object();
    final AtomicLong producerIndex = new AtomicLong();
    final AtomicLong consumerIndex = new AtomicLong();

    public SpscLinkedArrayQueue(int bufferSize) {
        int p2capacity = Pow2.roundToPowerOfTwo(Math.max(8, bufferSize));
        int mask = p2capacity - 1;
        AtomicReferenceArray<Object> buffer = new AtomicReferenceArray<>(p2capacity + 1);
        this.producerBuffer = buffer;
        this.producerMask = mask;
        adjustLookAheadStep(p2capacity);
        this.consumerBuffer = buffer;
        this.consumerMask = mask;
        this.producerLookAhead = (long) (mask - 1);
        soProducerIndex(0);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(T e) {
        if (e != null) {
            AtomicReferenceArray<Object> buffer = this.producerBuffer;
            long index = lpProducerIndex();
            int mask = this.producerMask;
            int offset = calcWrappedOffset(index, mask);
            if (index < this.producerLookAhead) {
                return writeToQueue(buffer, e, index, offset);
            }
            int lookAheadStep = this.producerLookAheadStep;
            if (lvElement(buffer, calcWrappedOffset(((long) lookAheadStep) + index, mask)) == null) {
                this.producerLookAhead = (((long) lookAheadStep) + index) - 1;
                return writeToQueue(buffer, e, index, offset);
            } else if (lvElement(buffer, calcWrappedOffset(1 + index, mask)) == null) {
                return writeToQueue(buffer, e, index, offset);
            } else {
                resize(buffer, index, offset, e, (long) mask);
                return true;
            }
        } else {
            throw new NullPointerException("Null is not a valid element");
        }
    }

    private boolean writeToQueue(AtomicReferenceArray<Object> buffer, T e, long index, int offset) {
        soElement(buffer, offset, e);
        soProducerIndex(1 + index);
        return true;
    }

    private void resize(AtomicReferenceArray<Object> oldBuffer, long currIndex, int offset, T e, long mask) {
        AtomicReferenceArray<Object> newBuffer = new AtomicReferenceArray<>(oldBuffer.length());
        this.producerBuffer = newBuffer;
        this.producerLookAhead = (currIndex + mask) - 1;
        soElement(newBuffer, offset, e);
        soNext(oldBuffer, newBuffer);
        soElement(oldBuffer, offset, HAS_NEXT);
        soProducerIndex(1 + currIndex);
    }

    private void soNext(AtomicReferenceArray<Object> curr, AtomicReferenceArray<Object> next) {
        soElement(curr, calcDirectOffset(curr.length() - 1), next);
    }

    private AtomicReferenceArray<Object> lvNextBufferAndUnlink(AtomicReferenceArray<Object> curr, int nextIndex) {
        int nextOffset = calcDirectOffset(nextIndex);
        AtomicReferenceArray<Object> nextBuffer = (AtomicReferenceArray) lvElement(curr, nextOffset);
        soElement(curr, nextOffset, null);
        return nextBuffer;
    }

    @Override // p005io.reactivex.internal.fuseable.SimplePlainQueue, p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public T poll() {
        AtomicReferenceArray<Object> buffer = this.consumerBuffer;
        long index = lpConsumerIndex();
        int mask = this.consumerMask;
        int offset = calcWrappedOffset(index, mask);
        T t = (T) lvElement(buffer, offset);
        boolean isNextBuffer = t == HAS_NEXT;
        if (t != null && !isNextBuffer) {
            soElement(buffer, offset, null);
            soConsumerIndex(1 + index);
            return t;
        } else if (isNextBuffer) {
            return newBufferPoll(lvNextBufferAndUnlink(buffer, mask + 1), index, mask);
        } else {
            return null;
        }
    }

    private T newBufferPoll(AtomicReferenceArray<Object> nextBuffer, long index, int mask) {
        this.consumerBuffer = nextBuffer;
        int offsetInNew = calcWrappedOffset(index, mask);
        T n = (T) lvElement(nextBuffer, offsetInNew);
        if (n != null) {
            soElement(nextBuffer, offsetInNew, null);
            soConsumerIndex(1 + index);
        }
        return n;
    }

    public T peek() {
        AtomicReferenceArray<Object> buffer = this.consumerBuffer;
        long index = lpConsumerIndex();
        int mask = this.consumerMask;
        T t = (T) lvElement(buffer, calcWrappedOffset(index, mask));
        if (t == HAS_NEXT) {
            return newBufferPeek(lvNextBufferAndUnlink(buffer, mask + 1), index, mask);
        }
        return t;
    }

    private T newBufferPeek(AtomicReferenceArray<Object> nextBuffer, long index, int mask) {
        this.consumerBuffer = nextBuffer;
        return (T) lvElement(nextBuffer, calcWrappedOffset(index, mask));
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
        while (true) {
            if (poll() == null && isEmpty()) {
                return;
            }
        }
    }

    public int size() {
        long currentProducerIndex;
        long after = lvConsumerIndex();
        do {
            currentProducerIndex = lvProducerIndex();
            after = lvConsumerIndex();
        } while (after != after);
        return (int) (currentProducerIndex - after);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return lvProducerIndex() == lvConsumerIndex();
    }

    private void adjustLookAheadStep(int capacity) {
        this.producerLookAheadStep = Math.min(capacity / 4, MAX_LOOK_AHEAD_STEP);
    }

    private long lvProducerIndex() {
        return this.producerIndex.get();
    }

    private long lvConsumerIndex() {
        return this.consumerIndex.get();
    }

    private long lpProducerIndex() {
        return this.producerIndex.get();
    }

    private long lpConsumerIndex() {
        return this.consumerIndex.get();
    }

    private void soProducerIndex(long v) {
        this.producerIndex.lazySet(v);
    }

    private void soConsumerIndex(long v) {
        this.consumerIndex.lazySet(v);
    }

    private static int calcWrappedOffset(long index, int mask) {
        return calcDirectOffset(((int) index) & mask);
    }

    private static int calcDirectOffset(int index) {
        return index;
    }

    private static void soElement(AtomicReferenceArray<Object> buffer, int offset, Object e) {
        buffer.lazySet(offset, e);
    }

    private static <E> Object lvElement(AtomicReferenceArray<Object> buffer, int offset) {
        return buffer.get(offset);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(T first, T second) {
        AtomicReferenceArray<Object> buffer = this.producerBuffer;
        long p = lvProducerIndex();
        int m = this.producerMask;
        if (lvElement(buffer, calcWrappedOffset(p + 2, m)) == null) {
            int pi = calcWrappedOffset(p, m);
            soElement(buffer, pi + 1, second);
            soElement(buffer, pi, first);
            soProducerIndex(2 + p);
            return true;
        }
        AtomicReferenceArray<Object> newBuffer = new AtomicReferenceArray<>(buffer.length());
        this.producerBuffer = newBuffer;
        int pi2 = calcWrappedOffset(p, m);
        soElement(newBuffer, pi2 + 1, second);
        soElement(newBuffer, pi2, first);
        soNext(buffer, newBuffer);
        soElement(buffer, pi2, HAS_NEXT);
        soProducerIndex(2 + p);
        return true;
    }
}
