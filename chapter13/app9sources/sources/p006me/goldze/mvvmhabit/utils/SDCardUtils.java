package p006me.goldze.mvvmhabit.utils;

import android.annotation.TargetApi;
import android.os.Environment;
import android.os.StatFs;
import java.io.File;

/* renamed from: me.goldze.mvvmhabit.utils.SDCardUtils */
/* loaded from: classes.dex */
public final class SDCardUtils {
    private SDCardUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static boolean isSDCardEnable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v1, types: [java.io.Closeable[]] */
    /* JADX WARN: Type inference failed for: r1v15, types: [java.io.Closeable[]] */
    /* JADX WARN: Type inference failed for: r2v0 */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v3, types: [java.io.BufferedReader] */
    /* JADX WARN: Unknown variable types count: 1 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.lang.String getSDCardPath() {
        /*
            boolean r0 = isSDCardEnable()
            r1 = 0
            if (r0 != 0) goto L_0x0008
            return r1
        L_0x0008:
            java.lang.String r0 = "cat /proc/mounts"
            java.lang.Runtime r2 = java.lang.Runtime.getRuntime()
            r3 = 0
            r4 = 1
            java.lang.Process r0 = r2.exec(r0)     // Catch: Exception -> 0x0083, all -> 0x0080
            java.io.BufferedReader r2 = new java.io.BufferedReader     // Catch: Exception -> 0x0083, all -> 0x0080
            java.io.InputStreamReader r5 = new java.io.InputStreamReader     // Catch: Exception -> 0x0083, all -> 0x0080
            java.io.BufferedInputStream r6 = new java.io.BufferedInputStream     // Catch: Exception -> 0x0083, all -> 0x0080
            java.io.InputStream r7 = r0.getInputStream()     // Catch: Exception -> 0x0083, all -> 0x0080
            r6.<init>(r7)     // Catch: Exception -> 0x0083, all -> 0x0080
            r5.<init>(r6)     // Catch: Exception -> 0x0083, all -> 0x0080
            r2.<init>(r5)     // Catch: Exception -> 0x0083, all -> 0x0080
        L_0x0027:
            java.lang.String r1 = r2.readLine()     // Catch: Exception -> 0x007d, all -> 0x007b
            if (r1 == 0) goto L_0x0076
            java.lang.String r5 = "sdcard"
            boolean r5 = r1.contains(r5)     // Catch: Exception -> 0x007d, all -> 0x007b
            if (r5 == 0) goto L_0x006a
            java.lang.String r5 = ".android_secure"
            boolean r5 = r1.contains(r5)     // Catch: Exception -> 0x007d, all -> 0x007b
            if (r5 == 0) goto L_0x006a
            java.lang.String r5 = " "
            java.lang.String[] r1 = r1.split(r5)     // Catch: Exception -> 0x007d, all -> 0x007b
            int r5 = r1.length     // Catch: Exception -> 0x007d, all -> 0x007b
            r6 = 5
            if (r5 < r6) goto L_0x006a
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch: Exception -> 0x007d, all -> 0x007b
            r0.<init>()     // Catch: Exception -> 0x007d, all -> 0x007b
            r1 = r1[r4]     // Catch: Exception -> 0x007d, all -> 0x007b
            java.lang.String r5 = "/.android_secure"
            java.lang.String r6 = ""
            java.lang.String r1 = r1.replace(r5, r6)     // Catch: Exception -> 0x007d, all -> 0x007b
            r0.append(r1)     // Catch: Exception -> 0x007d, all -> 0x007b
            java.lang.String r1 = java.io.File.separator     // Catch: Exception -> 0x007d, all -> 0x007b
            r0.append(r1)     // Catch: Exception -> 0x007d, all -> 0x007b
            java.lang.String r0 = r0.toString()     // Catch: Exception -> 0x007d, all -> 0x007b
            java.io.Closeable[] r1 = new java.io.Closeable[r4]
            r1[r3] = r2
            p006me.goldze.mvvmhabit.utils.CloseUtils.closeIO(r1)
            return r0
        L_0x006a:
            int r1 = r0.waitFor()     // Catch: Exception -> 0x007d, all -> 0x007b
            if (r1 == 0) goto L_0x0027
            int r1 = r0.exitValue()     // Catch: Exception -> 0x007d, all -> 0x007b
            if (r1 != r4) goto L_0x0027
        L_0x0076:
            java.io.Closeable[] r0 = new java.io.Closeable[r4]
            r0[r3] = r2
            goto L_0x008b
        L_0x007b:
            r0 = move-exception
            goto L_0x00a8
        L_0x007d:
            r0 = move-exception
            r1 = r2
            goto L_0x0084
        L_0x0080:
            r0 = move-exception
            r2 = r1
            goto L_0x00a8
        L_0x0083:
            r0 = move-exception
        L_0x0084:
            r0.printStackTrace()     // Catch: all -> 0x0080
            java.io.Closeable[] r0 = new java.io.Closeable[r4]
            r0[r3] = r1
        L_0x008b:
            p006me.goldze.mvvmhabit.utils.CloseUtils.closeIO(r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.io.File r1 = android.os.Environment.getExternalStorageDirectory()
            java.lang.String r1 = r1.getPath()
            r0.append(r1)
            java.lang.String r1 = java.io.File.separator
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            return r0
        L_0x00a8:
            java.io.Closeable[] r1 = new java.io.Closeable[r4]
            r1[r3] = r2
            p006me.goldze.mvvmhabit.utils.CloseUtils.closeIO(r1)
            throw r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: p006me.goldze.mvvmhabit.utils.SDCardUtils.getSDCardPath():java.lang.String");
    }

    public static String getDataPath() {
        if (!isSDCardEnable()) {
            return null;
        }
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "data" + File.separator;
    }

    @TargetApi(18)
    public static String getFreeSpace() {
        if (!isSDCardEnable()) {
            return null;
        }
        StatFs statFs = new StatFs(getSDCardPath());
        return ConvertUtils.byte2FitMemorySize(statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong());
    }

    @TargetApi(18)
    public static String getSDCardInfo() {
        if (!isSDCardEnable()) {
            return null;
        }
        SDCardInfo sDCardInfo = new SDCardInfo();
        sDCardInfo.isExist = true;
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        sDCardInfo.totalBlocks = statFs.getBlockCountLong();
        sDCardInfo.blockByteSize = statFs.getBlockSizeLong();
        sDCardInfo.availableBlocks = statFs.getAvailableBlocksLong();
        sDCardInfo.availableBytes = statFs.getAvailableBytes();
        sDCardInfo.freeBlocks = statFs.getFreeBlocksLong();
        sDCardInfo.freeBytes = statFs.getFreeBytes();
        sDCardInfo.totalBytes = statFs.getTotalBytes();
        return sDCardInfo.toString();
    }

    /* renamed from: me.goldze.mvvmhabit.utils.SDCardUtils$SDCardInfo */
    /* loaded from: classes.dex */
    public static class SDCardInfo {
        long availableBlocks;
        long availableBytes;
        long blockByteSize;
        long freeBlocks;
        long freeBytes;
        boolean isExist;
        long totalBlocks;
        long totalBytes;

        public String toString() {
            return "isExist=" + this.isExist + "\ntotalBlocks=" + this.totalBlocks + "\nfreeBlocks=" + this.freeBlocks + "\navailableBlocks=" + this.availableBlocks + "\nblockByteSize=" + this.blockByteSize + "\ntotalBytes=" + this.totalBytes + "\nfreeBytes=" + this.freeBytes + "\navailableBytes=" + this.availableBytes;
        }
    }
}
