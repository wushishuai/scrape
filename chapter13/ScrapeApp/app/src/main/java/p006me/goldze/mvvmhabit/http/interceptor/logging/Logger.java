package p006me.goldze.mvvmhabit.http.interceptor.logging;

import okhttp3.internal.platform.Platform;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.logging.Logger */
/* loaded from: classes.dex */
public interface Logger {
    public static final Logger DEFAULT = new Logger() { // from class: me.goldze.mvvmhabit.http.interceptor.logging.Logger.1
        @Override // p006me.goldze.mvvmhabit.http.interceptor.logging.Logger
        public void log(int level, String tag, String message) {
            Platform.get().log(level, message, null);
        }
    };

    void log(int i, String str, String str2);
}
