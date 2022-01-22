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
    static final String TAG = "RxPermissions";
    static final Object TRIGGER = new Object();
    @VisibleForTesting
    Lazy<RxPermissionsFragment> mRxPermissionsFragment;

    @FunctionalInterface
    /* loaded from: classes.dex */
    public interface Lazy<V> {
        V get();
    }

    public RxPermissions(@NonNull FragmentActivity fragmentActivity) {
        this.mRxPermissionsFragment = getLazySingleton(fragmentActivity.getSupportFragmentManager());
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
        RxPermissionsFragment findRxPermissionsFragment = findRxPermissionsFragment(fragmentManager);
        if (!(findRxPermissionsFragment == null)) {
            return findRxPermissionsFragment;
        }
        RxPermissionsFragment rxPermissionsFragment = new RxPermissionsFragment();
        fragmentManager.beginTransaction().add(rxPermissionsFragment, TAG).commitNow();
        return rxPermissionsFragment;
    }

    private RxPermissionsFragment findRxPermissionsFragment(@NonNull FragmentManager fragmentManager) {
        return (RxPermissionsFragment) fragmentManager.findFragmentByTag(TAG);
    }

    public void setLogging(boolean z) {
        this.mRxPermissionsFragment.get().setLogging(z);
    }

    public <T> ObservableTransformer<T, Boolean> ensure(final String... strArr) {
        return new ObservableTransformer<T, Boolean>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.2
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource<Boolean> apply(Observable<T> observable) {
                return RxPermissions.this.request(observable, strArr).buffer(strArr.length).flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.2.1
                    public ObservableSource<Boolean> apply(List<Permission> list) {
                        if (list.isEmpty()) {
                            return Observable.empty();
                        }
                        for (Permission permission : list) {
                            if (!permission.granted) {
                                return Observable.just(false);
                            }
                        }
                        return Observable.just(true);
                    }
                });
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEach(final String... strArr) {
        return new ObservableTransformer<T, Permission>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.3
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource<Permission> apply(Observable<T> observable) {
                return RxPermissions.this.request(observable, strArr);
            }
        };
    }

    public <T> ObservableTransformer<T, Permission> ensureEachCombined(final String... strArr) {
        return new ObservableTransformer<T, Permission>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.4
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource<Permission> apply(Observable<T> observable) {
                return RxPermissions.this.request(observable, strArr).buffer(strArr.length).flatMap(new Function<List<Permission>, ObservableSource<Permission>>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.4.1
                    public ObservableSource<Permission> apply(List<Permission> list) {
                        if (list.isEmpty()) {
                            return Observable.empty();
                        }
                        return Observable.just(new Permission(list));
                    }
                });
            }
        };
    }

    public Observable<Boolean> request(String... strArr) {
        return Observable.just(TRIGGER).compose(ensure(strArr));
    }

    public Observable<Permission> requestEach(String... strArr) {
        return Observable.just(TRIGGER).compose(ensureEach(strArr));
    }

    public Observable<Permission> requestEachCombined(String... strArr) {
        return Observable.just(TRIGGER).compose(ensureEachCombined(strArr));
    }

    public Observable<Permission> request(Observable<?> observable, final String... strArr) {
        if (strArr != null && strArr.length != 0) {
            return oneOf(observable, pending(strArr)).flatMap(new Function<Object, Observable<Permission>>() { // from class: com.tbruyelle.rxpermissions2.RxPermissions.5
                @Override // p005io.reactivex.functions.Function
                public Observable<Permission> apply(Object obj) {
                    return RxPermissions.this.requestImplementation(strArr);
                }
            });
        }
        throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
    }

    private Observable<?> pending(String... strArr) {
        for (String str : strArr) {
            if (!this.mRxPermissionsFragment.get().containsByPermission(str)) {
                return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> observable, Observable<?> observable2) {
        if (observable == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(observable, observable2);
    }

    @TargetApi(23)
    public Observable<Permission> requestImplementation(String... strArr) {
        ArrayList arrayList = new ArrayList(strArr.length);
        ArrayList arrayList2 = new ArrayList();
        for (String str : strArr) {
            this.mRxPermissionsFragment.get().log("Requesting permission " + str);
            if (isGranted(str)) {
                arrayList.add(Observable.just(new Permission(str, true, false)));
            } else if (isRevoked(str)) {
                arrayList.add(Observable.just(new Permission(str, false, false)));
            } else {
                PublishSubject<Permission> subjectByPermission = this.mRxPermissionsFragment.get().getSubjectByPermission(str);
                if (subjectByPermission == null) {
                    arrayList2.add(str);
                    subjectByPermission = PublishSubject.create();
                    this.mRxPermissionsFragment.get().setSubjectForPermission(str, subjectByPermission);
                }
                arrayList.add(subjectByPermission);
            }
        }
        if (!arrayList2.isEmpty()) {
            requestPermissionsFromFragment((String[]) arrayList2.toArray(new String[arrayList2.size()]));
        }
        return Observable.concat(Observable.fromIterable(arrayList));
    }

    public Observable<Boolean> shouldShowRequestPermissionRationale(Activity activity, String... strArr) {
        if (!isMarshmallow()) {
            return Observable.just(false);
        }
        return Observable.just(Boolean.valueOf(shouldShowRequestPermissionRationaleImplementation(activity, strArr)));
    }

    @TargetApi(23)
    private boolean shouldShowRequestPermissionRationaleImplementation(Activity activity, String... strArr) {
        for (String str : strArr) {
            if (!(isGranted(str) || activity.shouldShowRequestPermissionRationale(str))) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(23)
    void requestPermissionsFromFragment(String[] strArr) {
        this.mRxPermissionsFragment.get().log("requestPermissionsFromFragment " + TextUtils.join(", ", strArr));
        this.mRxPermissionsFragment.get().requestPermissions(strArr);
    }

    public boolean isGranted(String str) {
        return !isMarshmallow() || this.mRxPermissionsFragment.get().isGranted(str);
    }

    public boolean isRevoked(String str) {
        return isMarshmallow() && this.mRxPermissionsFragment.get().isRevoked(str);
    }

    boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= 23;
    }

    void onRequestPermissionsResult(String[] strArr, int[] iArr) {
        this.mRxPermissionsFragment.get().onRequestPermissionsResult(strArr, iArr, new boolean[strArr.length]);
    }
}
