package android.databinding;

import android.databinding.ObservableMap;
import android.support.p000v4.util.ArrayMap;
import java.util.Collection;
import java.util.Iterator;

/* loaded from: classes.dex */
public class ObservableArrayMap<K, V> extends ArrayMap<K, V> implements ObservableMap<K, V> {
    private transient MapChangeRegistry mListeners;

    @Override // android.databinding.ObservableMap
    public void addOnMapChangedCallback(ObservableMap.OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> onMapChangedCallback) {
        if (this.mListeners == null) {
            this.mListeners = new MapChangeRegistry();
        }
        this.mListeners.add(onMapChangedCallback);
    }

    @Override // android.databinding.ObservableMap
    public void removeOnMapChangedCallback(ObservableMap.OnMapChangedCallback<? extends ObservableMap<K, V>, K, V> onMapChangedCallback) {
        MapChangeRegistry mapChangeRegistry = this.mListeners;
        if (mapChangeRegistry != null) {
            mapChangeRegistry.remove(onMapChangedCallback);
        }
    }

    @Override // android.support.p000v4.util.SimpleArrayMap, java.util.Map
    public void clear() {
        if (!isEmpty()) {
            super.clear();
            notifyChange(null);
        }
    }

    @Override // android.support.p000v4.util.SimpleArrayMap, java.util.Map
    public V put(K k, V v) {
        super.put(k, v);
        notifyChange(k);
        return v;
    }

    @Override // android.support.p000v4.util.ArrayMap
    public boolean removeAll(Collection<?> collection) {
        Iterator<?> it = collection.iterator();
        boolean z = false;
        while (it.hasNext()) {
            int indexOfKey = indexOfKey(it.next());
            if (indexOfKey >= 0) {
                z = true;
                removeAt(indexOfKey);
            }
        }
        return z;
    }

    @Override // android.support.p000v4.util.ArrayMap
    public boolean retainAll(Collection<?> collection) {
        boolean z = false;
        for (int size = size() - 1; size >= 0; size--) {
            if (!collection.contains(keyAt(size))) {
                removeAt(size);
                z = true;
            }
        }
        return z;
    }

    @Override // android.support.p000v4.util.SimpleArrayMap
    public V removeAt(int i) {
        K keyAt = keyAt(i);
        V v = (V) super.removeAt(i);
        if (v != null) {
            notifyChange(keyAt);
        }
        return v;
    }

    @Override // android.support.p000v4.util.SimpleArrayMap
    public V setValueAt(int i, V v) {
        K keyAt = keyAt(i);
        V v2 = (V) super.setValueAt(i, v);
        notifyChange(keyAt);
        return v2;
    }

    private void notifyChange(Object obj) {
        MapChangeRegistry mapChangeRegistry = this.mListeners;
        if (mapChangeRegistry != null) {
            mapChangeRegistry.notifyCallbacks(this, 0, obj);
        }
    }
}
