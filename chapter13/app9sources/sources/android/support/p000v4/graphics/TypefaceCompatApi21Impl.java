package android.support.p000v4.graphics;

import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import java.io.File;

/* JADX INFO: Access modifiers changed from: package-private */
@RequiresApi(21)
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
/* renamed from: android.support.v4.graphics.TypefaceCompatApi21Impl */
/* loaded from: classes.dex */
public class TypefaceCompatApi21Impl extends TypefaceCompatBaseImpl {
    private static final String TAG = "TypefaceCompatApi21Impl";

    private File getFile(ParcelFileDescriptor parcelFileDescriptor) {
        try {
            String readlink = Os.readlink("/proc/self/fd/" + parcelFileDescriptor.getFd());
            if (OsConstants.S_ISREG(Os.stat(readlink).st_mode)) {
                return new File(readlink);
            }
            return null;
        } catch (ErrnoException unused) {
            return null;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:32:0x0059 A[Catch: Throwable -> 0x0060, all -> 0x005d, TryCatch #4 {Throwable -> 0x0060, blocks: (B:7:0x0018, B:9:0x001e, B:12:0x0025, B:16:0x002f, B:18:0x003c, B:31:0x0055, B:32:0x0059, B:33:0x005c), top: B:47:0x0018 }] */
    /* JADX WARN: Removed duplicated region for block: B:41:0x0068  */
    /* JADX WARN: Removed duplicated region for block: B:50:0x0050 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    @Override // android.support.p000v4.graphics.TypefaceCompatBaseImpl
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public android.graphics.Typeface createFromFontInfo(android.content.Context r5, android.os.CancellationSignal r6, @android.support.annotation.NonNull android.support.p000v4.provider.FontsContractCompat.FontInfo[] r7, int r8) {
        /*
            r4 = this;
            int r0 = r7.length
            r1 = 0
            r2 = 1
            if (r0 >= r2) goto L_0x0006
            return r1
        L_0x0006:
            android.support.v4.provider.FontsContractCompat$FontInfo r7 = r4.findBestInfo(r7, r8)
            android.content.ContentResolver r8 = r5.getContentResolver()
            android.net.Uri r7 = r7.getUri()     // Catch: IOException -> 0x0077
            java.lang.String r0 = "r"
            android.os.ParcelFileDescriptor r6 = r8.openFileDescriptor(r7, r0, r6)     // Catch: IOException -> 0x0077
            java.io.File r7 = r4.getFile(r6)     // Catch: Throwable -> 0x0060, all -> 0x005d
            if (r7 == 0) goto L_0x002f
            boolean r8 = r7.canRead()     // Catch: Throwable -> 0x0060, all -> 0x005d
            if (r8 != 0) goto L_0x0025
            goto L_0x002f
        L_0x0025:
            android.graphics.Typeface r5 = android.graphics.Typeface.createFromFile(r7)     // Catch: Throwable -> 0x0060, all -> 0x005d
            if (r6 == 0) goto L_0x002e
            r6.close()     // Catch: IOException -> 0x0077
        L_0x002e:
            return r5
        L_0x002f:
            java.io.FileInputStream r7 = new java.io.FileInputStream     // Catch: Throwable -> 0x0060, all -> 0x005d
            java.io.FileDescriptor r8 = r6.getFileDescriptor()     // Catch: Throwable -> 0x0060, all -> 0x005d
            r7.<init>(r8)     // Catch: Throwable -> 0x0060, all -> 0x005d
            android.graphics.Typeface r5 = super.createFromInputStream(r5, r7)     // Catch: Throwable -> 0x0048, all -> 0x0045
            r7.close()     // Catch: Throwable -> 0x0060, all -> 0x005d
            if (r6 == 0) goto L_0x0044
            r6.close()     // Catch: IOException -> 0x0077
        L_0x0044:
            return r5
        L_0x0045:
            r5 = move-exception
            r8 = r1
            goto L_0x004e
        L_0x0048:
            r5 = move-exception
            throw r5     // Catch: all -> 0x004a
        L_0x004a:
            r8 = move-exception
            r3 = r8
            r8 = r5
            r5 = r3
        L_0x004e:
            if (r8 == 0) goto L_0x0059
            r7.close()     // Catch: Throwable -> 0x0054, all -> 0x005d
            goto L_0x005c
        L_0x0054:
            r7 = move-exception
            r8.addSuppressed(r7)     // Catch: Throwable -> 0x0060, all -> 0x005d
            goto L_0x005c
        L_0x0059:
            r7.close()     // Catch: Throwable -> 0x0060, all -> 0x005d
        L_0x005c:
            throw r5     // Catch: Throwable -> 0x0060, all -> 0x005d
        L_0x005d:
            r5 = move-exception
            r7 = r1
            goto L_0x0066
        L_0x0060:
            r5 = move-exception
            throw r5     // Catch: all -> 0x0062
        L_0x0062:
            r7 = move-exception
            r3 = r7
            r7 = r5
            r5 = r3
        L_0x0066:
            if (r6 == 0) goto L_0x0076
            if (r7 == 0) goto L_0x0073
            r6.close()     // Catch: Throwable -> 0x006e, IOException -> 0x0077
            goto L_0x0076
        L_0x006e:
            r6 = move-exception
            r7.addSuppressed(r6)     // Catch: IOException -> 0x0077
            goto L_0x0076
        L_0x0073:
            r6.close()     // Catch: IOException -> 0x0077
        L_0x0076:
            throw r5     // Catch: IOException -> 0x0077
        L_0x0077:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.p000v4.graphics.TypefaceCompatApi21Impl.createFromFontInfo(android.content.Context, android.os.CancellationSignal, android.support.v4.provider.FontsContractCompat$FontInfo[], int):android.graphics.Typeface");
    }
}
