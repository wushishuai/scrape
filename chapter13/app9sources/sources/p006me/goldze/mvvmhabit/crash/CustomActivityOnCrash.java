package p006me.goldze.mvvmhabit.crash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.Thread;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;

/* renamed from: me.goldze.mvvmhabit.crash.CustomActivityOnCrash */
/* loaded from: classes.dex */
public final class CustomActivityOnCrash {
    private static final String CAOC_HANDLER_PACKAGE_NAME = "cat.ereza.customactivityoncrash";
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";
    private static final String EXTRA_ACTIVITY_LOG = "cat.ereza.customactivityoncrash.EXTRA_ACTIVITY_LOG";
    private static final String EXTRA_CONFIG = "cat.ereza.customactivityoncrash.EXTRA_CONFIG";
    private static final String EXTRA_STACK_TRACE = "cat.ereza.customactivityoncrash.EXTRA_STACK_TRACE";
    private static final String INTENT_ACTION_ERROR_ACTIVITY = "cat.ereza.customactivityoncrash.ERROR";
    private static final String INTENT_ACTION_RESTART_ACTIVITY = "cat.ereza.customactivityoncrash.RESTART";
    private static final int MAX_ACTIVITIES_IN_LOG = 50;
    private static final int MAX_STACK_TRACE_SIZE = 131071;
    private static final String SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp";
    private static final String SHARED_PREFERENCES_FILE = "custom_activity_on_crash";
    private static final String TAG = "CustomActivityOnCrash";
    @SuppressLint({"StaticFieldLeak"})
    private static Application application;
    private static CaocConfig config = new CaocConfig();
    private static Deque<String> activityLog = new ArrayDeque(50);
    private static WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);
    private static boolean isInBackground = true;

    /* renamed from: me.goldze.mvvmhabit.crash.CustomActivityOnCrash$EventListener */
    /* loaded from: classes.dex */
    public interface EventListener extends Serializable {
        void onCloseAppFromErrorActivity();

        void onLaunchErrorActivity();

        void onRestartAppFromErrorActivity();
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static void install(@Nullable Context context) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null!");
                return;
            }
            final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (defaultUncaughtExceptionHandler == null || !defaultUncaughtExceptionHandler.getClass().getName().startsWith(CAOC_HANDLER_PACKAGE_NAME)) {
                if (defaultUncaughtExceptionHandler != null && !defaultUncaughtExceptionHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                    Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler, are you sure this is correct? If you use a custom UncaughtExceptionHandler, you must initialize it AFTER CustomActivityOnCrash! Installing anyway, but your original handler will not be called.");
                }
                application = (Application) context.getApplicationContext();
                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: me.goldze.mvvmhabit.crash.CustomActivityOnCrash.1
                    @Override // java.lang.Thread.UncaughtExceptionHandler
                    public void uncaughtException(Thread thread, Throwable th) {
                        Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
                        if (CustomActivityOnCrash.config.isEnabled()) {
                            Log.e(CustomActivityOnCrash.TAG, "App has crashed, executing CustomActivityOnCrash's UncaughtExceptionHandler", th);
                            if (CustomActivityOnCrash.hasCrashedInTheLastSeconds(CustomActivityOnCrash.application)) {
                                Log.e(CustomActivityOnCrash.TAG, "App already crashed recently, not starting custom error activity because we could enter a restart loop. Are you sure that your app does not crash directly on init?", th);
                                Thread.UncaughtExceptionHandler uncaughtExceptionHandler2 = defaultUncaughtExceptionHandler;
                                if (uncaughtExceptionHandler2 != null) {
                                    uncaughtExceptionHandler2.uncaughtException(thread, th);
                                    return;
                                }
                            } else {
                                CustomActivityOnCrash.setLastCrashTimestamp(CustomActivityOnCrash.application, new Date().getTime());
                                Class<? extends Activity> errorActivityClass = CustomActivityOnCrash.config.getErrorActivityClass();
                                if (errorActivityClass == null) {
                                    errorActivityClass = CustomActivityOnCrash.guessErrorActivityClass(CustomActivityOnCrash.application);
                                }
                                if (CustomActivityOnCrash.isStackTraceLikelyConflictive(th, errorActivityClass)) {
                                    Log.e(CustomActivityOnCrash.TAG, "Your application class or your error activity have crashed, the custom activity will not be launched!");
                                    Thread.UncaughtExceptionHandler uncaughtExceptionHandler3 = defaultUncaughtExceptionHandler;
                                    if (uncaughtExceptionHandler3 != null) {
                                        uncaughtExceptionHandler3.uncaughtException(thread, th);
                                        return;
                                    }
                                } else if (CustomActivityOnCrash.config.getBackgroundMode() == 1 || !CustomActivityOnCrash.isInBackground) {
                                    Intent intent = new Intent(CustomActivityOnCrash.application, errorActivityClass);
                                    StringWriter stringWriter = new StringWriter();
                                    th.printStackTrace(new PrintWriter(stringWriter));
                                    String stringWriter2 = stringWriter.toString();
                                    if (stringWriter2.length() > CustomActivityOnCrash.MAX_STACK_TRACE_SIZE) {
                                        stringWriter2 = stringWriter2.substring(0, 131047) + " [stack trace too large]";
                                    }
                                    intent.putExtra(CustomActivityOnCrash.EXTRA_STACK_TRACE, stringWriter2);
                                    if (CustomActivityOnCrash.config.isTrackActivities()) {
                                        String str = "";
                                        while (!CustomActivityOnCrash.activityLog.isEmpty()) {
                                            str = str + ((String) CustomActivityOnCrash.activityLog.poll());
                                        }
                                        intent.putExtra(CustomActivityOnCrash.EXTRA_ACTIVITY_LOG, str);
                                    }
                                    if (CustomActivityOnCrash.config.isShowRestartButton() && CustomActivityOnCrash.config.getRestartActivityClass() == null) {
                                        CustomActivityOnCrash.config.setRestartActivityClass(CustomActivityOnCrash.guessRestartActivityClass(CustomActivityOnCrash.application));
                                    }
                                    intent.putExtra(CustomActivityOnCrash.EXTRA_CONFIG, CustomActivityOnCrash.config);
                                    intent.setFlags(268468224);
                                    if (CustomActivityOnCrash.config.getEventListener() != null) {
                                        CustomActivityOnCrash.config.getEventListener().onLaunchErrorActivity();
                                    }
                                    CustomActivityOnCrash.application.startActivity(intent);
                                } else if (CustomActivityOnCrash.config.getBackgroundMode() == 2 && (uncaughtExceptionHandler = defaultUncaughtExceptionHandler) != null) {
                                    uncaughtExceptionHandler.uncaughtException(thread, th);
                                    return;
                                }
                            }
                            Activity activity = (Activity) CustomActivityOnCrash.lastActivityCreated.get();
                            if (activity != null) {
                                activity.finish();
                                CustomActivityOnCrash.lastActivityCreated.clear();
                            }
                            CustomActivityOnCrash.killCurrentProcess();
                            return;
                        }
                        Thread.UncaughtExceptionHandler uncaughtExceptionHandler4 = defaultUncaughtExceptionHandler;
                        if (uncaughtExceptionHandler4 != null) {
                            uncaughtExceptionHandler4.uncaughtException(thread, th);
                        }
                    }
                });
                application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() { // from class: me.goldze.mvvmhabit.crash.CustomActivityOnCrash.2
                    int currentlyStartedActivities = 0;
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                    }

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivityCreated(Activity activity, Bundle bundle) {
                        if (activity.getClass() != CustomActivityOnCrash.config.getErrorActivityClass()) {
                            WeakReference unused = CustomActivityOnCrash.lastActivityCreated = new WeakReference(activity);
                        }
                        if (CustomActivityOnCrash.config.isTrackActivities()) {
                            Deque deque = CustomActivityOnCrash.activityLog;
                            deque.add(this.dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " created\n");
                        }
                    }

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivityStarted(Activity activity) {
                        boolean z = true;
                        this.currentlyStartedActivities++;
                        if (this.currentlyStartedActivities != 0) {
                            z = false;
                        }
                        boolean unused = CustomActivityOnCrash.isInBackground = z;
                    }

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivityResumed(Activity activity) {
                        if (CustomActivityOnCrash.config.isTrackActivities()) {
                            Deque deque = CustomActivityOnCrash.activityLog;
                            deque.add(this.dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " resumed\n");
                        }
                    }

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivityPaused(Activity activity) {
                        if (CustomActivityOnCrash.config.isTrackActivities()) {
                            Deque deque = CustomActivityOnCrash.activityLog;
                            deque.add(this.dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " paused\n");
                        }
                    }

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivityStopped(Activity activity) {
                        boolean z = true;
                        this.currentlyStartedActivities--;
                        if (this.currentlyStartedActivities != 0) {
                            z = false;
                        }
                        boolean unused = CustomActivityOnCrash.isInBackground = z;
                    }

                    @Override // android.app.Application.ActivityLifecycleCallbacks
                    public void onActivityDestroyed(Activity activity) {
                        if (CustomActivityOnCrash.config.isTrackActivities()) {
                            Deque deque = CustomActivityOnCrash.activityLog;
                            deque.add(this.dateFormat.format(new Date()) + ": " + activity.getClass().getSimpleName() + " destroyed\n");
                        }
                    }
                });
            } else {
                Log.e(TAG, "CustomActivityOnCrash was already installed, doing nothing!");
            }
            Log.i(TAG, "CustomActivityOnCrash has been installed.");
        } catch (Throwable th) {
            Log.e(TAG, "An unknown error occurred while installing CustomActivityOnCrash, it may not have been properly initialized. Please report this as a bug if needed.", th);
        }
    }

    @NonNull
    public static String getStackTraceFromIntent(@NonNull Intent intent) {
        return intent.getStringExtra(EXTRA_STACK_TRACE);
    }

    @NonNull
    public static CaocConfig getConfigFromIntent(@NonNull Intent intent) {
        return (CaocConfig) intent.getSerializableExtra(EXTRA_CONFIG);
    }

    @Nullable
    public static String getActivityLogFromIntent(@NonNull Intent intent) {
        return intent.getStringExtra(EXTRA_ACTIVITY_LOG);
    }

    @NonNull
    public static String getAllErrorDetailsFromIntent(@NonNull Context context, @NonNull Intent intent) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String buildDateAsString = getBuildDateAsString(context, simpleDateFormat);
        String str = "Build version: " + getVersionName(context) + " \n";
        if (buildDateAsString != null) {
            str = str + "Build date: " + buildDateAsString + " \n";
        }
        String str2 = (((str + "Current date: " + simpleDateFormat.format(date) + " \n") + "Device: " + getDeviceModelName() + " \n \n") + "Stack trace:  \n") + getStackTraceFromIntent(intent);
        String activityLogFromIntent = getActivityLogFromIntent(intent);
        if (activityLogFromIntent == null) {
            return str2;
        }
        return (str2 + "\nUser actions: \n") + activityLogFromIntent;
    }

    public static void restartApplicationWithIntent(@NonNull Activity activity, @NonNull Intent intent, @NonNull CaocConfig caocConfig) {
        intent.addFlags(270565376);
        if (intent.getComponent() != null) {
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
        }
        if (caocConfig.getEventListener() != null) {
            caocConfig.getEventListener().onRestartAppFromErrorActivity();
        }
        activity.finish();
        activity.startActivity(intent);
        killCurrentProcess();
    }

    public static void restartApplication(@NonNull Activity activity, @NonNull CaocConfig caocConfig) {
        restartApplicationWithIntent(activity, new Intent(activity, caocConfig.getRestartActivityClass()), caocConfig);
    }

    public static void closeApplication(@NonNull Activity activity, @NonNull CaocConfig caocConfig) {
        if (caocConfig.getEventListener() != null) {
            caocConfig.getEventListener().onCloseAppFromErrorActivity();
        }
        activity.finish();
        killCurrentProcess();
    }

    @NonNull
    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static CaocConfig getConfig() {
        return config;
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY})
    public static void setConfig(@NonNull CaocConfig caocConfig) {
        config = caocConfig;
    }

    public static boolean isStackTraceLikelyConflictive(@NonNull Throwable th, @NonNull Class<? extends Activity> cls) {
        do {
            StackTraceElement[] stackTrace = th.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ((stackTraceElement.getClassName().equals("android.app.ActivityThread") && stackTraceElement.getMethodName().equals("handleBindApplication")) || stackTraceElement.getClassName().equals(cls.getName())) {
                    return true;
                }
            }
            th = th.getCause();
        } while (th != null);
        return false;
    }

    @Nullable
    private static String getBuildDateAsString(@NonNull Context context, @NonNull DateFormat dateFormat) {
        long j;
        try {
            ZipFile zipFile = new ZipFile(context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir);
            j = zipFile.getEntry("classes.dex").getTime();
            zipFile.close();
        } catch (Exception unused) {
            j = 0;
        }
        if (j > 312764400000L) {
            return dateFormat.format(new Date(j));
        }
        return null;
    }

    @NonNull
    private static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception unused) {
            return "Unknown";
        }
    }

    @NonNull
    private static String getDeviceModelName() {
        String str = Build.MANUFACTURER;
        String str2 = Build.MODEL;
        if (str2.startsWith(str)) {
            return capitalize(str2);
        }
        return capitalize(str) + " " + str2;
    }

    @NonNull
    private static String capitalize(@Nullable String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        char charAt = str.charAt(0);
        if (Character.isUpperCase(charAt)) {
            return str;
        }
        return Character.toUpperCase(charAt) + str.substring(1);
    }

    @Nullable
    public static Class<? extends Activity> guessRestartActivityClass(@NonNull Context context) {
        Class<? extends Activity> restartActivityClassWithIntentFilter = getRestartActivityClassWithIntentFilter(context);
        return restartActivityClassWithIntentFilter == null ? getLauncherActivity(context) : restartActivityClassWithIntentFilter;
    }

    @Nullable
    private static Class<? extends Activity> getRestartActivityClassWithIntentFilter(@NonNull Context context) {
        List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(new Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY).setPackage(context.getPackageName()), 64);
        if (queryIntentActivities == null || queryIntentActivities.size() <= 0) {
            return null;
        }
        try {
            return Class.forName(queryIntentActivities.get(0).activityInfo.name);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed when resolving the restart activity class via intent filter, stack trace follows!", e);
            return null;
        }
    }

    @Nullable
    private static Class<? extends Activity> getLauncherActivity(@NonNull Context context) {
        Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launchIntentForPackage == null) {
            return null;
        }
        try {
            return Class.forName(launchIntentForPackage.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed when resolving the restart activity class via getLaunchIntentForPackage, stack trace follows!", e);
            return null;
        }
    }

    @NonNull
    public static Class<? extends Activity> guessErrorActivityClass(@NonNull Context context) {
        Class<? extends Activity> errorActivityClassWithIntentFilter = getErrorActivityClassWithIntentFilter(context);
        return errorActivityClassWithIntentFilter == null ? DefaultErrorActivity.class : errorActivityClassWithIntentFilter;
    }

    @Nullable
    private static Class<? extends Activity> getErrorActivityClassWithIntentFilter(@NonNull Context context) {
        List<ResolveInfo> queryIntentActivities = context.getPackageManager().queryIntentActivities(new Intent().setAction(INTENT_ACTION_ERROR_ACTIVITY).setPackage(context.getPackageName()), 64);
        if (queryIntentActivities == null || queryIntentActivities.size() <= 0) {
            return null;
        }
        try {
            return Class.forName(queryIntentActivities.get(0).activityInfo.name);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Failed when resolving the error activity class via intent filter, stack trace follows!", e);
            return null;
        }
    }

    public static void killCurrentProcess() {
        Process.killProcess(Process.myPid());
        System.exit(10);
    }

    @SuppressLint({"ApplySharedPref"})
    public static void setLastCrashTimestamp(@NonNull Context context, long j) {
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, 0).edit().putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, j).commit();
    }

    private static long getLastCrashTimestamp(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, 0).getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1);
    }

    public static boolean hasCrashedInTheLastSeconds(@NonNull Context context) {
        long lastCrashTimestamp = getLastCrashTimestamp(context);
        long time = new Date().getTime();
        return lastCrashTimestamp <= time && time - lastCrashTimestamp < ((long) config.getMinTimeBetweenCrashesMs());
    }
}
