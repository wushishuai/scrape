package p005io.reactivex.flowables;

import p005io.reactivex.Flowable;
import p005io.reactivex.annotations.Nullable;

/* renamed from: io.reactivex.flowables.GroupedFlowable */
/* loaded from: classes.dex */
public abstract class GroupedFlowable<K, T> extends Flowable<T> {
    final K key;

    /* JADX INFO: Access modifiers changed from: protected */
    public GroupedFlowable(@Nullable K k) {
        this.key = k;
    }

    @Nullable
    public K getKey() {
        return this.key;
    }
}
