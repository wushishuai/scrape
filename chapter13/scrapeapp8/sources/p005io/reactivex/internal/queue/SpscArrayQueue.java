package p005io.reactivex.internal.queue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceArray;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;
import p005io.reactivex.internal.util.Pow2;

/* renamed from: io.reactivex.internal.queue.SpscArrayQueue */
/* loaded from: classes.dex */
public final class SpscArrayQueue<E> extends AtomicReferenceArray<E> implements SimplePlainQueue<E> {
    private static final Integer MAX_LOOK_AHEAD_STEP = Integer.getInteger("jctools.spsc.max.lookahead.step", 4096);
    private static final long serialVersionUID = -1296597691183856449L;
    final int lookAheadStep;
    long producerLookAhead;
    final int mask = length() - 1;
    final AtomicLong producerIndex = new AtomicLong();
    final AtomicLong consumerIndex = new AtomicLong();

    public SpscArrayQueue(int capacity) {
        super(Pow2.roundToPowerOfTwo(capacity));
        this.lookAheadStep = Math.min(capacity / 4, MAX_LOOK_AHEAD_STEP.intValue());
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(E e) {
        if (e != null) {
            int mask = this.mask;
            long index = this.producerIndex.get();
            int offset = calcElementOffset(index, mask);
            if (index >= this.producerLookAhead) {
                int step = this.lookAheadStep;
                if (lvElement(calcElementOffset(((long) step) + index, mask)) == null) {
                    this.producerLookAhead = ((long) step) + index;
                } else if (lvElement(offset) != null) {
                    return false;
                }
            }
            soElement(offset, e);
            soProducerIndex(1 + index);
            return true;
        }
        throw new NullPointerException("Null is not a valid element");
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(E v1, E v2) {
        return offer(v1) && offer(v2);
    }

    @Override // p005io.reactivex.internal.fuseable.SimplePlainQueue, p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public E poll() {
        long index = this.consumerIndex.get();
        int offset = calcElementOffset(index);
        E e = lvElement(offset);
        if (e == null) {
            return null;
        }
        soConsumerIndex(1 + index);
        soElement(offset, null);
        return e;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return this.producerIndex.get() == this.consumerIndex.get();
    }

    void soProducerIndex(long newIndex) {
        this.producerIndex.lazySet(newIndex);
    }

    void soConsumerIndex(long newIndex) {
        this.consumerIndex.lazySet(newIndex);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
        while (true) {
            if (poll() == null && isEmpty()) {
                return;
            }
        }
    }

    int calcElementOffset(long index, int mask) {
        return ((int) index) & mask;
    }

    int calcElementOffset(long index) {
        return ((int) index) & this.mask;
    }

    void soElement(int offset, E value) {
        lazySet(offset, value);
    }

    E lvElement(int offset) {
        return get(offset);
    }
}
