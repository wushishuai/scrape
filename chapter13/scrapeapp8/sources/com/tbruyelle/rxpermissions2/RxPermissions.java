package com.tbruyelle.rxpermissions2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentActivity;
import android.support.p000v4.app.FragmentManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.ObservableTransformer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.subjects.PublishSubject;

/* loaded from: classes.dex */
public class RxPermissions {
    static final String TAG = RxPermissions.class.getSimpleName();
    static final Object TRIGGER = new Object();
    @VisibleForTesting
    Lazy<RxPermissionsFragment> mRxPermissionsFragment;

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface Lazy<V> {
        V get();
    }

    public RxPermissions(@NonNull FragmentActivity activity) {
        this.mRxPermissionsFragment = getLazySingleton(activity.getSupportFragmentManager());
    }

    public RxPermissions(@NonNull Fragment fragment) {
        this.mRxPermissionsFragment = getLazySingleton(fragment.getChildFragmentManager());
    }

    @NonNull
    private Lazy<RxPermissionsFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new Lazy<RxPermissionsFragment>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.1
            private RxPermissionsFragment rxPermissionsFragment;

            @Override // com.tbruyelle.rxpermissions2.RxPermissions.Lazy
            public synchronized RxPermissionsFragment get() {
                if (this.rxPermissionsFragment == null) {
                    this.rxPermissionsFragment = RxPermissions.this.getRxPermissionsFragment(fragmentManager);
                }
                return this.rxPermissionsFragment;
            }
        };
    }

    public RxPermissionsFragment getRxPermissionsFragment(@NonNull FragmentManager fragmentManager) {
        RxPermissionsFragment rxPermissionsFragment = findRxPermissionsFragment(fragmentManager);
        if (!(rxPermissionsFragment == null)) {
            return rxPermissionsFragment;
        }
        RxPermissionsFragment rxPermissionsFragment2 = new RxPermissionsFragment();
        fragmentManager.beginTransaction().add(rxPermissionsFragment2, TAG).commitNow();
        return rxPermissionsFragment2;
    }

    private RxPermissionsFragment findRxPermissionsFragment(@NonNull FragmentManager fragmentManager) {
        return (RxPermissionsFragment) fragmentManager.findFragmentByTag(TAG);
    }

    public void setLogging(boolean logging) {
        this.mRxPermissionsFragment.get().setLogging(logging);
    }

    public <T> ObservableTransformer<T, Boolean> ensure(final String... permissions) {
        return new ObservableTransformer<T, Boolean>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.2
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource<Boolean> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions).buffer(permissions.length).flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.2.1
                    public ObservableSource<Boolean> apply(List<Permission> permissions2) {
                        if (permissions2.isEmpty()) {
                            return Observable.empty();
                        }
                        for (Permission p : permissions2) {
                            if (!p.granted) {
                                return Observable.just(false);
                            }
                        }
                        return Observable.just(true);
                    }
                });
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEach(final String... permissions) {
        return new ObservableTransformer<T, Permission>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.3
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource<Permission> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions);
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEachCombined(final String... permissions) {
        return new ObservableTransformer<T, Permission>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.4
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource<Permission> apply(Observable<T> o) {
                return RxPermissions.this.request(o, permissions).buffer(permissions.length).flatMap(new Function<List<Permission>, ObservableSource<Permission>>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.4.1
                    public ObservableSource<Permission> apply(List<Permission> permissions2) {
                        if (permissions2.isEmpty()) {
                            return Observable.empty();
                        }
                        return Observable.just(new Permission(permissions2));
                    }
                });
            }
        };
    }

    public Observable<Boolean> request(String... permissions) {
        return Observable.just(TRIGGER).compose(ensure(permissions));
    }

    public Observable<Permission> requestEach(String... permissions) {
        return Observable.just(TRIGGER).compose(ensureEach(permissions));
    }

    public Observable<Permission> requestEachCombined(String... permissions) {
        return Observable.just(TRIGGER).compose(ensureEachCombined(permissions));
    }

    public Observable<Permission> request(Observable<?> trigger, final String... permissions) {
        if (permissions != null && permissions.length != 0) {
            return oneOf(trigger, pending(permissions)).flatMap(new Function<Object, Observable<Permission>>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.5
                @Override // p005io.reactivex.functions.Function
                public Observable<Permission> apply(Object o) {
                    return RxPermissions.this.requestImplementation(permissions);
                }
            });
        }
        throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
    }

    private Observable<?> pending(String... permissions) {
        for (String p : permissions) {
            if (!this.mRxPermissionsFragment.get().containsByPermission(p)) {
                return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        if (trigger == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(trigger, pending);
    }

    @TargetApi(23)
    public Observable<Permission> requestImplementation(String... permissions) {
        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            this.mRxPermissionsFragment.get().log("Requesting permission " + permission);
            if (isGranted(permission)) {
                list.add(Observable.just(new Permission(permission, true, false)));
            } else if (isRevoked(permission)) {
                list.add(Observable.just(new Permission(permission, false, false)));
            } else {
                PublishSubject<Permission> subject = this.mRxPermissionsFragment.get().getSubjectByPermission(permission);
                if (subject == null) {
                    unrequestedPermissions.add(permission);
                    subject = PublishSubject.create();
                    this.mRxPermissionsFragment.get().setSubjectForPermission(permission, subject);
                }
                list.add(subject);
            }
        }
        if (!unrequestedPermissions.isEmpty()) {
            requestPermissionsFromFragment((String[]) unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]));
        }
        return Observable.concat(Observable.fromIterable(list));
    }

    public Observable<Boolean> shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        if (!isMarshmallow()) {
            return Observable.just(false);
        }
        return Observable.just(Boolean.valueOf(shouldShowRequestPermissionRationaleImplementation(activity, permissions)));
    }

    @TargetApi(23)
    private boolean shouldShowRequestPermissionRationaleImplementation(Activity activity, String... permissions) {
        for (String p : permissions) {
            if (!(isGranted(p) || activity.shouldShowRequestPermissionRationale(p))) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    void requestPermissionsFromFragment(String[] permissions) {
        this.mRxPermissionsFragment.get().log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions));
        this.mRxPermissionsFragment.get().requestPermissions(permissions);
    }

    public boolean isGranted(String permission) {
        return !isMarshmallow() || this.mRxPermissionsFragment.get().isGranted(permission);
    }

    public boolean isRevoked(String permission) {
        return isMarshmallow() && this.mRxPermissionsFragment.get().isRevoked(permission);
    }

    boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= 23;
    }

    void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        this.mRxPermissionsFragment.get().onRequestPermissionsResult(permissions, grantResults, new boolean[permissions.length]);
    }
}
