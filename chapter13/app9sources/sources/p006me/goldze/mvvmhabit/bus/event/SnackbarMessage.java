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

    public void observe(LifecycleOwner lifecycleOwner, final SnackbarObserver snackbarObserver) {
        super.observe(lifecycleOwner, new Observer<Integer>() { // from class: me.goldze.mvvmhabit.bus.event.SnackbarMessage.1
            public void onChanged(@Nullable Integer num) {
                if (num != null) {
                    snackbarObserver.onNewMessage(num.intValue());
                }
            }
        });
    }
}
