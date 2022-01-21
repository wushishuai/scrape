package com.goldze.mvvmhabit.app;

import com.goldze.mvvmhabit.BuildConfig;
import com.goldze.mvvmhabit.C0690R;
import com.goldze.mvvmhabit.p004ui.MainActivity;
import p006me.goldze.mvvmhabit.base.BaseApplication;
import p006me.goldze.mvvmhabit.crash.CaocConfig;
import p006me.goldze.mvvmhabit.utils.KLog;

/* loaded from: classes.dex */
public class AppApplication extends BaseApplication {
    @Override // p006me.goldze.mvvmhabit.base.BaseApplication, android.app.Application
    public void onCreate() {
        super.onCreate();
        KLog.init(BuildConfig.DEBUG);
        initCrash();
    }

    private void initCrash() {
        CaocConfig.Builder.create().backgroundMode(0).enabled(true).showErrorDetails(true).showRestartButton(true).trackActivities(true).minTimeBetweenCrashesMs(2000).errorDrawable(Integer.valueOf((int) C0690R.mipmap.ic_launcher)).restartActivity(MainActivity.class).apply();
    }
}
