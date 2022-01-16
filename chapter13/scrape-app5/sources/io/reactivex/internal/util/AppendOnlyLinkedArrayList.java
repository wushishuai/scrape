package io.reactivex.internal.util;

import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.Predicate;
/* loaded from: classes.dex */
public class AppendOnlyLinkedArrayList<T> {
    final int capacity;
    final Object[] head;
    int offset;
    Object[] tail;

    /* loaded from: classes.dex */
    public interface NonThrowingPredicate<T> extends Predicate<T> {
        @Override // io.reactivex.functions.Predicate
        boolean test(T t);
    }

    public AppendOnlyLinkedArrayList(int capacity) {
        this.capacity = capacity;
        this.head = new Object[capacity + 1];
        this.tail = this.head;
    }

    public void add(T value) {
        int c = this.capacity;
        int o = this.offset;
        if (o == c) {
            Object[] next = new Object[c + 1];
            this.tail[c] = next;
            this.tail = next;
            o = 0;
        }
        this.tail[o] = value;
        this.offset = o + 1;
    }

    public void setFirst(T value) {
        this.head[0] = value;
    }

    /* JADX INFO: Multiple debug info for r2v2 java.lang.Object[]: [D('a' java.lang.Object[]), D('i' int)] */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0018, code lost:
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void forEachWhile(io.reactivex.internal.util.AppendOnlyLinkedArrayList.NonThrowingPredicate<? super T> r6) {
        /*
            r5 = this;
            java.lang.Object[] r0 = r5.head
            int r1 = r5.capacity
        L_0x0004:
            if (r0 == 0) goto L_0x001e
            r2 = 0
        L_0x0007:
            if (r2 >= r1) goto L_0x0018
            r3 = r0[r2]
            if (r3 != 0) goto L_0x000e
            goto L_0x0018
        L_0x000e:
            boolean r4 = r6.test(r3)
            if (r4 == 0) goto L_0x0015
            return
        L_0x0015:
            int r2 = r2 + 1
            goto L_0x0007
        L_0x0018:
            r2 = r0[r1]
            r0 = r2
            java.lang.Object[] r0 = (java.lang.Object[]) r0
            goto L_0x0004
        L_0x001e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.util.AppendOnlyLinkedArrayList.forEachWhile(io.reactivex.internal.util.AppendOnlyLinkedArrayList$NonThrowingPredicate):void");
    }

    /* JADX INFO: Multiple debug info for r2v3 java.lang.Object: [D('a' java.lang.Object[]), D('i' int)] */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0019, code lost:
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public <U> boolean accept(org.reactivestreams.Subscriber<? super U> r6) {
        /*
            r5 = this;
            java.lang.Object[] r0 = r5.head
            int r1 = r5.capacity
        L_0x0004:
            if (r0 == 0) goto L_0x001f
            r2 = 0
        L_0x0007:
            if (r2 >= r1) goto L_0x0019
            r3 = r0[r2]
            if (r3 != 0) goto L_0x000e
            goto L_0x0019
        L_0x000e:
            boolean r4 = io.reactivex.internal.util.NotificationLite.acceptFull(r3, r6)
            if (r4 == 0) goto L_0x0016
            r4 = 1
            return r4
        L_0x0016:
            int r2 = r2 + 1
            goto L_0x0007
        L_0x0019:
            r2 = r0[r1]
            r0 = r2
            java.lang.Object[] r0 = (java.lang.Object[]) r0
            goto L_0x0004
        L_0x001f:
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.util.AppendOnlyLinkedArrayList.accept(org.reactivestreams.Subscriber):boolean");
    }

    /* JADX INFO: Multiple debug info for r2v3 java.lang.Object: [D('a' java.lang.Object[]), D('i' int)] */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0019, code lost:
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public <U> boolean accept(io.reactivex.Observer<? super U> r6) {
        /*
            r5 = this;
            java.lang.Object[] r0 = r5.head
            int r1 = r5.capacity
        L_0x0004:
            if (r0 == 0) goto L_0x001f
            r2 = 0
        L_0x0007:
            if (r2 >= r1) goto L_0x0019
            r3 = r0[r2]
            if (r3 != 0) goto L_0x000e
            goto L_0x0019
        L_0x000e:
            boolean r4 = io.reactivex.internal.util.NotificationLite.acceptFull(r3, r6)
            if (r4 == 0) goto L_0x0016
            r4 = 1
            return r4
        L_0x0016:
            int r2 = r2 + 1
            goto L_0x0007
        L_0x0019:
            r2 = r0[r1]
            r0 = r2
            java.lang.Object[] r0 = (java.lang.Object[]) r0
            goto L_0x0004
        L_0x001f:
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: io.reactivex.internal.util.AppendOnlyLinkedArrayList.accept(io.reactivex.Observer):boolean");
    }

    /* JADX INFO: Multiple debug info for r2v2 java.lang.Object[]: [D('a' java.lang.Object[]), D('i' int)] */
    public <S> void forEachWhile(S state, BiPredicate<? super S, ? super T> consumer) throws Exception {
        Object[] a = this.head;
        int c = this.capacity;
        while (true) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null || consumer.test(state, o)) {
                    return;
                }
            }
            a = a[c];
        }
    }
}
