package p005io.reactivex.internal.queue;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.fuseable.SimplePlainQueue;

/* renamed from: io.reactivex.internal.queue.MpscLinkedQueue */
/* loaded from: classes.dex */
public final class MpscLinkedQueue<T> implements SimplePlainQueue<T> {
    private final AtomicReference<LinkedQueueNode<T>> producerNode = new AtomicReference<>();
    private final AtomicReference<LinkedQueueNode<T>> consumerNode = new AtomicReference<>();

    public MpscLinkedQueue() {
        LinkedQueueNode<T> linkedQueueNode = new LinkedQueueNode<>();
        spConsumerNode(linkedQueueNode);
        xchgProducerNode(linkedQueueNode);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(T t) {
        if (t != null) {
            LinkedQueueNode<T> linkedQueueNode = new LinkedQueueNode<>(t);
            xchgProducerNode(linkedQueueNode).soNext(linkedQueueNode);
            return true;
        }
        throw new NullPointerException("Null is not a valid element");
    }

    @Override // p005io.reactivex.internal.fuseable.SimplePlainQueue, p005io.reactivex.internal.fuseable.SimpleQueue
    @Nullable
    public T poll() {
        LinkedQueueNode<T> lvNext;
        LinkedQueueNode<T> lpConsumerNode = lpConsumerNode();
        LinkedQueueNode<T> lvNext2 = lpConsumerNode.lvNext();
        if (lvNext2 != null) {
            T andNullValue = lvNext2.getAndNullValue();
            spConsumerNode(lvNext2);
            return andNullValue;
        } else if (lpConsumerNode == lvProducerNode()) {
            return null;
        } else {
            do {
                lvNext = lpConsumerNode.lvNext();
            } while (lvNext == null);
            T andNullValue2 = lvNext.getAndNullValue();
            spConsumerNode(lvNext);
            return andNullValue2;
        }
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean offer(T t, T t2) {
        offer(t);
        offer(t2);
        return true;
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public void clear() {
        while (poll() != null && !isEmpty()) {
        }
    }

    LinkedQueueNode<T> lvProducerNode() {
        return this.producerNode.get();
    }

    LinkedQueueNode<T> xchgProducerNode(LinkedQueueNode<T> linkedQueueNode) {
        return this.producerNode.getAndSet(linkedQueueNode);
    }

    LinkedQueueNode<T> lvConsumerNode() {
        return this.consumerNode.get();
    }

    LinkedQueueNode<T> lpConsumerNode() {
        return this.consumerNode.get();
    }

    void spConsumerNode(LinkedQueueNode<T> linkedQueueNode) {
        this.consumerNode.lazySet(linkedQueueNode);
    }

    @Override // p005io.reactivex.internal.fuseable.SimpleQueue
    public boolean isEmpty() {
        return lvConsumerNode() == lvProducerNode();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.queue.MpscLinkedQueue$LinkedQueueNode */
    /* loaded from: classes.dex */
    public static final class LinkedQueueNode<E> extends AtomicReference<LinkedQueueNode<E>> {
        private static final long serialVersionUID = 2404266111789071508L;
        private E value;

        LinkedQueueNode() {
        }

        LinkedQueueNode(E e) {
            spValue(e);
        }

        public E getAndNullValue() {
            E lpValue = lpValue();
            spValue(null);
            return lpValue;
        }

        public E lpValue() {
            return this.value;
        }

        public void spValue(E e) {
            this.value = e;
        }

        public void soNext(LinkedQueueNode<E> linkedQueueNode) {
            lazySet(linkedQueueNode);
        }

        public LinkedQueueNode<E> lvNext() {
            return get();
        }
    }
}
