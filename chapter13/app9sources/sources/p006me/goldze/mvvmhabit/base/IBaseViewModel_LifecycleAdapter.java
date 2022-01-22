package p006me.goldze.mvvmhabit.base;

import android.arch.lifecycle.GeneratedAdapter;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MethodCallsLogger;

/* renamed from: me.goldze.mvvmhabit.base.IBaseViewModel_LifecycleAdapter */
/* loaded from: classes.dex */
public class IBaseViewModel_LifecycleAdapter implements GeneratedAdapter {
    final IBaseViewModel mReceiver;

    IBaseViewModel_LifecycleAdapter(IBaseViewModel iBaseViewModel) {
        this.mReceiver = iBaseViewModel;
    }

    @Override // android.arch.lifecycle.GeneratedAdapter
    public void callMethods(LifecycleOwner lifecycleOwner, Lifecycle.Event event, boolean z, MethodCallsLogger methodCallsLogger) {
        boolean z2 = methodCallsLogger != null;
        if (z) {
            if (!z2 || methodCallsLogger.approveCall("onAny", 4)) {
                this.mReceiver.onAny(lifecycleOwner, event);
            }
        } else if (event == Lifecycle.Event.ON_CREATE) {
            if (!z2 || methodCallsLogger.approveCall("onCreate", 1)) {
                this.mReceiver.onCreate();
            }
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            if (!z2 || methodCallsLogger.approveCall("onDestroy", 1)) {
                this.mReceiver.onDestroy();
            }
        } else if (event == Lifecycle.Event.ON_START) {
            if (!z2 || methodCallsLogger.approveCall("onStart", 1)) {
                this.mReceiver.onStart();
            }
        } else if (event == Lifecycle.Event.ON_STOP) {
            if (!z2 || methodCallsLogger.approveCall("onStop", 1)) {
                this.mReceiver.onStop();
            }
        } else if (event == Lifecycle.Event.ON_RESUME) {
            if (!z2 || methodCallsLogger.approveCall("onResume", 1)) {
                this.mReceiver.onResume();
            }
        } else if (event != Lifecycle.Event.ON_PAUSE) {
        } else {
            if (!z2 || methodCallsLogger.approveCall("onPause", 1)) {
                this.mReceiver.onPause();
            }
        }
    }
}
