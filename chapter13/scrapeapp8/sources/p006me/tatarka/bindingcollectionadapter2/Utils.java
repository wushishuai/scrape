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
    public static void throwMissingVariable(ViewDataBinding binding, int bindingVariable, int layoutRes) {
        String layoutName = binding.getRoot().getContext().getResources().getResourceName(layoutRes);
        String bindingVariableName = DataBindingUtil.convertBrIdToString(bindingVariable);
        throw new IllegalStateException("Could not bind variable '" + bindingVariableName + "' in layout '" + layoutName + "'");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void ensureChangeOnMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("You must only modify the ObservableList on the main thread.");
        }
    }

    static <T, A extends BindingCollectionAdapter<T>> A createClass(Class<? extends BindingCollectionAdapter> adapterClass, ItemBinding<T> itemBinding) {
        try {
            return (A) ((BindingCollectionAdapter) adapterClass.getConstructor(ItemBinding.class).newInstance(itemBinding));
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }
}
