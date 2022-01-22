package android.arch.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentActivity;

/* loaded from: classes.dex */
public class ViewModelProviders {
    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application != null) {
            return application;
        }
        throw new IllegalStateException("Your activity/fragment is not yet attached to Application. You can't request ViewModel before onCreate call.");
    }

    private static Activity checkActivity(Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        if (activity != null) {
            return activity;
        }
        throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
    }

    @NonNull
    @MainThread
    /* renamed from: of */
    public static ViewModelProvider m61of(@NonNull Fragment fragment) {
        return m60of(fragment, (ViewModelProvider.Factory) null);
    }

    @NonNull
    @MainThread
    /* renamed from: of */
    public static ViewModelProvider m59of(@NonNull FragmentActivity fragmentActivity) {
        return m58of(fragmentActivity, (ViewModelProvider.Factory) null);
    }

    @NonNull
    @MainThread
    /* renamed from: of */
    public static ViewModelProvider m60of(@NonNull Fragment fragment, @Nullable ViewModelProvider.Factory factory) {
        Application checkApplication = checkApplication(checkActivity(fragment));
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(checkApplication);
        }
        return new ViewModelProvider(ViewModelStores.m57of(fragment), factory);
    }

    @NonNull
    @MainThread
    /* renamed from: of */
    public static ViewModelProvider m58of(@NonNull FragmentActivity fragmentActivity, @Nullable ViewModelProvider.Factory factory) {
        Application checkApplication = checkApplication(fragmentActivity);
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(checkApplication);
        }
        return new ViewModelProvider(ViewModelStores.m56of(fragmentActivity), factory);
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static class DefaultFactory extends ViewModelProvider.AndroidViewModelFactory {
        @Deprecated
        public DefaultFactory(@NonNull Application application) {
            super(application);
        }
    }
}
