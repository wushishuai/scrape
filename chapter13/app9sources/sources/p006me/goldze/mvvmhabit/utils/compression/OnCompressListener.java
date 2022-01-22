package p006me.goldze.mvvmhabit.utils.compression;

import java.io.File;

/* renamed from: me.goldze.mvvmhabit.utils.compression.OnCompressListener */
/* loaded from: classes.dex */
public interface OnCompressListener {
    void onError(Throwable th);

    void onStart();

    void onSuccess(File file);
}
