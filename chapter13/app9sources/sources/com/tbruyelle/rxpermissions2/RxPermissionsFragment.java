package com.tbruyelle.rxpermissions2;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentActivity;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import p005io.reactivex.subjects.PublishSubject;

/* loaded from: classes.dex */
public class RxPermissionsFragment extends Fragment {
    private static final int PERMISSIONS_REQUEST_CODE = 42;
    private boolean mLogging;
    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap();

    @Override // android.support.p000v4.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @TargetApi(23)
    public void requestPermissions(@NonNull String[] strArr) {
        requestPermissions(strArr, 42);
    }

    @Override // android.support.p000v4.app.Fragment
    @TargetApi(23)
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 42) {
            boolean[] zArr = new boolean[strArr.length];
            for (int i2 = 0; i2 < strArr.length; i2++) {
                zArr[i2] = shouldShowRequestPermissionRationale(strArr[i2]);
            }
            onRequestPermissionsResult(strArr, iArr, zArr);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onRequestPermissionsResult(String[] strArr, int[] iArr, boolean[] zArr) {
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            log("onRequestPermissionsResult  " + strArr[i]);
            PublishSubject<Permission> publishSubject = this.mSubjects.get(strArr[i]);
            if (publishSubject == null) {
                Log.e(RxPermissions.TAG, "RxPermissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
                return;
            }
            this.mSubjects.remove(strArr[i]);
            publishSubject.onNext(new Permission(strArr[i], iArr[i] == 0, zArr[i]));
            publishSubject.onComplete();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @TargetApi(23)
    public boolean isGranted(String str) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            return activity.checkSelfPermission(str) == 0;
        }
        throw new IllegalStateException("This fragment must be attached to an activity.");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @TargetApi(23)
    public boolean isRevoked(String str) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            return activity.getPackageManager().isPermissionRevokedByPolicy(str, getActivity().getPackageName());
        }
        throw new IllegalStateException("This fragment must be attached to an activity.");
    }

    public void setLogging(boolean z) {
        this.mLogging = z;
    }

    public PublishSubject<Permission> getSubjectByPermission(@NonNull String str) {
        return this.mSubjects.get(str);
    }

    public boolean containsByPermission(@NonNull String str) {
        return this.mSubjects.containsKey(str);
    }

    public void setSubjectForPermission(@NonNull String str, @NonNull PublishSubject<Permission> publishSubject) {
        this.mSubjects.put(str, publishSubject);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void log(String str) {
        if (this.mLogging) {
            Log.d(RxPermissions.TAG, str);
        }
    }
}
