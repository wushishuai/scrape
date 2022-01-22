package android.databinding;

import android.databinding.CallbackRegistry;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.p000v4.util.Pools;

/* loaded from: classes.dex */
public class ListChangeRegistry extends CallbackRegistry<ObservableList.OnListChangedCallback, ObservableList, ListChanges> {
    private static final int ALL = 0;
    private static final int CHANGED = 1;
    private static final int INSERTED = 2;
    private static final int MOVED = 3;
    private static final int REMOVED = 4;
    private static final Pools.SynchronizedPool<ListChanges> sListChanges = new Pools.SynchronizedPool<>(10);
    private static final CallbackRegistry.NotifierCallback<ObservableList.OnListChangedCallback, ObservableList, ListChanges> NOTIFIER_CALLBACK = new CallbackRegistry.NotifierCallback<ObservableList.OnListChangedCallback, ObservableList, ListChanges>() { // from class: android.databinding.ListChangeRegistry.1
        public void onNotifyCallback(ObservableList.OnListChangedCallback onListChangedCallback, ObservableList observableList, int i, ListChanges listChanges) {
            switch (i) {
                case 1:
                    onListChangedCallback.onItemRangeChanged(observableList, listChanges.start, listChanges.count);
                    return;
                case 2:
                    onListChangedCallback.onItemRangeInserted(observableList, listChanges.start, listChanges.count);
                    return;
                case 3:
                    onListChangedCallback.onItemRangeMoved(observableList, listChanges.start, listChanges.f0to, listChanges.count);
                    return;
                case 4:
                    onListChangedCallback.onItemRangeRemoved(observableList, listChanges.start, listChanges.count);
                    return;
                default:
                    onListChangedCallback.onChanged(observableList);
                    return;
            }
        }
    };

    public void notifyChanged(@NonNull ObservableList observableList) {
        notifyCallbacks(observableList, 0, (ListChanges) null);
    }

    public void notifyChanged(@NonNull ObservableList observableList, int i, int i2) {
        notifyCallbacks(observableList, 1, acquire(i, 0, i2));
    }

    public void notifyInserted(@NonNull ObservableList observableList, int i, int i2) {
        notifyCallbacks(observableList, 2, acquire(i, 0, i2));
    }

    public void notifyMoved(@NonNull ObservableList observableList, int i, int i2, int i3) {
        notifyCallbacks(observableList, 3, acquire(i, i2, i3));
    }

    public void notifyRemoved(@NonNull ObservableList observableList, int i, int i2) {
        notifyCallbacks(observableList, 4, acquire(i, 0, i2));
    }

    private static ListChanges acquire(int i, int i2, int i3) {
        ListChanges acquire = sListChanges.acquire();
        if (acquire == null) {
            acquire = new ListChanges();
        }
        acquire.start = i;
        acquire.f0to = i2;
        acquire.count = i3;
        return acquire;
    }

    public synchronized void notifyCallbacks(@NonNull ObservableList observableList, int i, ListChanges listChanges) {
        super.notifyCallbacks((ListChangeRegistry) observableList, i, (int) listChanges);
        if (listChanges != null) {
            sListChanges.release(listChanges);
        }
    }

    public ListChangeRegistry() {
        super(NOTIFIER_CALLBACK);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class ListChanges {
        public int count;
        public int start;

        /* renamed from: to */
        public int f0to;

        ListChanges() {
        }
    }
}
