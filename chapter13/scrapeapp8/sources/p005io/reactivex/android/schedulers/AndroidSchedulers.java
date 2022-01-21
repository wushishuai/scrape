package p005io.reactivex.android.schedulers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.Callable;
import p005io.reactivex.Scheduler;
import p005io.reactivex.android.plugins.RxAndroidPlugins;

/* renamed from: io.reactivex.android.schedulers.AndroidSchedulers */
/* loaded from: classes.dex */
public final class AndroidSchedulers {
    private static final Scheduler MAIN_THREAD = RxAndroidPlugins.initMainThreadScheduler(new Callable<Scheduler>() { // from class: io.reactivex.android.schedulers.AndroidSchedulers.1
        @Override // java.util.concurrent.Callable
        public Scheduler call() throws Exception {
            return MainHolder.DEFAULT;
        }
    });

    /* renamed from: io.reactivex.android.schedulers.AndroidSchedulers$MainHolder */
    /* loaded from: classes.dex */
    public static final class MainHolder {
        static final Scheduler DEFAULT = new HandlerScheduler(new Handler(Looper.getMainLooper()), false);

        private MainHolder() {
        }
    }

    public static Scheduler mainThread() {
        return RxAndroidPlugins.onMainThreadScheduler(MAIN_THREAD);
    }

    public static Scheduler from(Looper looper) {
        return from(looper, false);
    }

    @SuppressLint({"NewApi"})
    public static Scheduler from(Looper looper, boolean async) {
        if (looper != null) {
            if (Build.VERSION.SDK_INT < 16) {
                async = false;
            } else if (async && Build.VERSION.SDK_INT < 22) {
                Message message = Message.obtain();
                try {
                    message.setAsynchronous(true);
                } catch (NoSuchMethodError e) {
                    async = false;
                }
                message.recycle();
            }
            return new HandlerScheduler(new Handler(looper), async);
        }
        throw new NullPointerException("looper == null");
    }

    private AndroidSchedulers() {
        throw new AssertionError("No instances.");
    }
}
