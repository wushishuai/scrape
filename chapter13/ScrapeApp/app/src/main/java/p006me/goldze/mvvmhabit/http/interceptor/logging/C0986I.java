package p006me.goldze.mvvmhabit.http.interceptor.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.logging.I */
/* loaded from: classes.dex */
class C0986I {
    protected C0986I() {
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
