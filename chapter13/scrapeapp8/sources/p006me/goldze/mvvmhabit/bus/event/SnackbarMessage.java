package p006me.goldze.mvvmhabit.bus.event;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/* renamed from: me.goldze.mvvmhabit.bus.event.SnackbarMessage */
/* loaded from: classes.dex */
public class SnackbarMessage extends SingleLiveEvent<Integer> {

    /* renamed from: me.goldze.mvvmhabit.bus.event.SnackbarMessage$SnackbarObserver */
    /* loaded from: classes.dex */
    public interface SnackbarObserver {
        void onNewMessage(@StringRes int i);
    }

    public void observe(LifecycleOwner owner, final SnackbarObserver observer) {
        super.observe(owner, new Observer<Integer>() { // from class: me.goldze.mvvmhabit.bus.event.SnackbarMessage.1
            public void onChanged(@Nullable Integer t) {
                if (t != null) {
                    observer.onNewMessage(t.intValue());
                }
            }
        });
    }
}
