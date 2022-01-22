package p006me.tatarka.bindingcollectionadapter2;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Looper;

/* JADX INFO: Access modifiers changed from: package-private */
/* renamed from: me.tatarka.bindingcollectionadapter2.Utils */
/* loaded from: classes.dex */
public class Utils {
    private static final String TAG = "BCAdapters";

    Utils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void throwMissingVariable(ViewDataBinding viewDataBinding, int i, int i2) {
        String resourceName = viewDataBinding.getRoot().getContext().getResources().getResourceName(i2);
        String convertBrIdToString = DataBindingUtil.convertBrIdToString(i);
        throw new IllegalStateException("Could not bind variable '" + convertBrIdToString + "' in layout '" + resourceName + "'");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void ensureChangeOnMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("You must only modify the ObservableList on the main thread.");
        }
    }

    static <T, A extends BindingCollectionAdapter<T>> A createClass(Class<? extends BindingCollectionAdapter> cls, ItemBinding<T> itemBinding) {
        try {
            return (A) ((BindingCollectionAdapter) cls.getConstructor(ItemBinding.class).newInstance(itemBinding));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
