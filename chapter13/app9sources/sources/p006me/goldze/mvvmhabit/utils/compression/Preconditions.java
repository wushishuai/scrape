package p006me.goldze.mvvmhabit.utils.compression;

import android.support.annotation.Nullable;

/* renamed from: me.goldze.mvvmhabit.utils.compression.Preconditions */
/* loaded from: classes.dex */
final class Preconditions {
    Preconditions() {
    }

    static <T> T checkNotNull(T t) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> T checkNotNull(T t, @Nullable Object obj) {
        if (t != null) {
            return t;
        }
        throw new NullPointerException(String.valueOf(obj));
    }
}
