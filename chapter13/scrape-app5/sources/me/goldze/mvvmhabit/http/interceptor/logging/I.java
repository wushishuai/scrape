package me.goldze.mvvmhabit.http.interceptor.logging;

import java.util.logging.Level;
import java.util.logging.Logger;
/* loaded from: classes.dex */
class I {
    protected I() {
        throw new UnsupportedOperationException();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void log(int type, String tag, String msg) {
        Logger logger = Logger.getLogger(tag);
        if (type != 4) {
            logger.log(Level.WARNING, msg);
        } else {
            logger.log(Level.INFO, msg);
        }
    }
}
