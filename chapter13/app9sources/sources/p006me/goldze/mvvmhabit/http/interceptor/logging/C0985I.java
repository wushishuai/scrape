package p006me.goldze.mvvmhabit.http.interceptor.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.logging.I */
/* loaded from: classes.dex */
class C0985I {
    protected C0985I() {
        throw new UnsupportedOperationException();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void log(int i, String str, String str2) {
        Logger logger = Logger.getLogger(str);
        if (i != 4) {
            logger.log(Level.WARNING, str2);
        } else {
            logger.log(Level.INFO, str2);
        }
    }
}
