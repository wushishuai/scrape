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
        int cpus = Runtime.getRuntime().availableProcessors();
        if (Build.VERSION.SDK_INT < 17) {
            return Math.max(getCoreCountPre17(), cpus);
        }
        return cpus;
    }

    /* JADX WARN: Finally extract failed */
    private static int getCoreCountPre17() {
        StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
        try {
            File cpuInfo = new File(CPU_LOCATION);
            final Pattern cpuNamePattern = Pattern.compile(CPU_NAME_REGEX);
            File[] cpus = cpuInfo.listFiles(new FilenameFilter() { // from class: com.bumptech.glide.load.engine.executor.RuntimeCompat.1
                @Override // java.io.FilenameFilter
                public boolean accept(File file, String s) {
                    return cpuNamePattern.matcher(s).matches();
                }
            });
            StrictMode.setThreadPolicy(originalPolicy);
            return Math.max(1, cpus != null ? cpus.length : 0);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(originalPolicy);
            throw th;
        }
    }
}
