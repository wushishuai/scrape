package p005io.reactivex.observables;

import p005io.reactivex.Observable;
import p005io.reactivex.annotations.Nullable;

/* renamed from: io.reactivex.observables.GroupedObservable */
/* loaded from: classes.dex */
public abstract class GroupedObservable<K, T> extends Observable<T> {
    final K key;

    /* JADX INFO: Access modifiers changed from: protected */
    public GroupedObservable(@Nullable K key) {
        this.key = key;
    }

    @Nullable
    public K getKey() {
        return this.key;
    }
}
