package android.databinding;

import android.databinding.ViewDataBinding;

/* loaded from: classes.dex */
public abstract class OnRebindCallback<T extends ViewDataBinding> {
    public void onBound(T t) {
    }

    public void onCanceled(T t) {
    }

    public boolean onPreBind(T t) {
        return true;
    }
}
