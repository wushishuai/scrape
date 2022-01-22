package com.bumptech.glide.load.engine.executor;

import android.os.Build;
import android.os.StrictMode;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/* loaded from: classes.dex */
final class RuntimeCompat {
    private static final String CPU_LOCATION = "/sys/devices/system/cpu/";
    private static final String CPU_NAME_REGEX = "cpu[0-9]+";
    private static final String TAG = "GlideRuntimeCompat";

    private RuntimeCompat() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int availableProcessors() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return Build.VERSION.SDK_INT < 17 ? Math.max(getCoreCountPre17(), availableProcessors) : availableProcessors;
    }

    private static int getCoreCountPre17() {
        StrictMode.ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        try {
            File file = new File(CPU_LOCATION);
            final Pattern compile = Pattern.compile(CPU_NAME_REGEX);
            File[] listFiles = file.listFiles(new FilenameFilter() { // from class: com.bumptech.glide.load.engine.executor.RuntimeCompat.1
                @Override // java.io.FilenameFilter
                public boolean accept(File file2, String str) {
                    return compile.matcher(str).matches();
                }
            });
            return Math.max(1, listFiles != null ? listFiles.length : 0);
        } finally {
            StrictMode.setThreadPolicy(allowThreadDiskReads);
        }
    }
}
