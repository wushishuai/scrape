package p006me.goldze.mvvmhabit.http.download;

import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Consumer;
import p006me.goldze.mvvmhabit.bus.RxBus;
import p006me.goldze.mvvmhabit.bus.RxSubscriptions;

/* renamed from: me.goldze.mvvmhabit.http.download.ProgressCallBack */
/* loaded from: classes.dex */
public abstract class ProgressCallBack<T> {
    private String destFileDir;
    private String destFileName;
    private Disposable mSubscription;

    public void onCompleted() {
    }

    public abstract void onError(Throwable th);

    public void onStart() {
    }

    public abstract void onSuccess(T t);

    public abstract void progress(long j, long j2);

    public ProgressCallBack(String str, String str2) {
        this.destFileDir = str;
        this.destFileName = str2;
        subscribeLoadProgress();
    }

    /* JADX WARN: Removed duplicated region for block: B:53:0x008d A[Catch: IOException -> 0x0089, TRY_LEAVE, TryCatch #0 {IOException -> 0x0089, blocks: (B:49:0x0085, B:53:0x008d), top: B:56:0x0085 }] */
    /* JADX WARN: Removed duplicated region for block: B:56:0x0085 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void saveFile(okhttp3.ResponseBody r6) {
        /*
            r5 = this;
            r0 = 2048(0x800, float:2.87E-42)
            byte[] r0 = new byte[r0]
            r1 = 0
            java.io.InputStream r6 = r6.byteStream()     // Catch: FileNotFoundException -> 0x0063, IOException -> 0x0053, all -> 0x0050
            java.io.File r2 = new java.io.File     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            java.lang.String r3 = r5.destFileDir     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            r2.<init>(r3)     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            boolean r3 = r2.exists()     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            if (r3 != 0) goto L_0x0019
            r2.mkdirs()     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
        L_0x0019:
            java.io.File r3 = new java.io.File     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            java.lang.String r4 = r5.destFileName     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            r3.<init>(r2, r4)     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
            r2.<init>(r3)     // Catch: FileNotFoundException -> 0x004c, IOException -> 0x0048, all -> 0x0046
        L_0x0025:
            int r1 = r6.read(r0)     // Catch: FileNotFoundException -> 0x0044, IOException -> 0x0042, all -> 0x0040
            r3 = -1
            if (r1 == r3) goto L_0x0031
            r3 = 0
            r2.write(r0, r3, r1)     // Catch: FileNotFoundException -> 0x0044, IOException -> 0x0042, all -> 0x0040
            goto L_0x0025
        L_0x0031:
            r2.flush()     // Catch: FileNotFoundException -> 0x0044, IOException -> 0x0042, all -> 0x0040
            r5.unsubscribe()     // Catch: FileNotFoundException -> 0x0044, IOException -> 0x0042, all -> 0x0040
            if (r6 == 0) goto L_0x003c
            r6.close()     // Catch: IOException -> 0x006e
        L_0x003c:
            r2.close()     // Catch: IOException -> 0x006e
            goto L_0x007f
        L_0x0040:
            r0 = move-exception
            goto L_0x0082
        L_0x0042:
            r0 = move-exception
            goto L_0x004a
        L_0x0044:
            r0 = move-exception
            goto L_0x004e
        L_0x0046:
            r0 = move-exception
            goto L_0x0083
        L_0x0048:
            r0 = move-exception
            r2 = r1
        L_0x004a:
            r1 = r6
            goto L_0x0055
        L_0x004c:
            r0 = move-exception
            r2 = r1
        L_0x004e:
            r1 = r6
            goto L_0x0065
        L_0x0050:
            r0 = move-exception
            r6 = r1
            goto L_0x0083
        L_0x0053:
            r0 = move-exception
            r2 = r1
        L_0x0055:
            r0.printStackTrace()     // Catch: all -> 0x0080
            if (r1 == 0) goto L_0x005d
            r1.close()     // Catch: IOException -> 0x006e
        L_0x005d:
            if (r2 == 0) goto L_0x007f
            r2.close()     // Catch: IOException -> 0x006e
            goto L_0x007f
        L_0x0063:
            r0 = move-exception
            r2 = r1
        L_0x0065:
            r0.printStackTrace()     // Catch: all -> 0x0080
            if (r1 == 0) goto L_0x0070
            r1.close()     // Catch: IOException -> 0x006e
            goto L_0x0070
        L_0x006e:
            r6 = move-exception
            goto L_0x0076
        L_0x0070:
            if (r2 == 0) goto L_0x007f
            r2.close()     // Catch: IOException -> 0x006e
            goto L_0x007f
        L_0x0076:
            java.lang.String r0 = "saveFile"
            java.lang.String r6 = r6.getMessage()
            android.util.Log.e(r0, r6)
        L_0x007f:
            return
        L_0x0080:
            r0 = move-exception
            r6 = r1
        L_0x0082:
            r1 = r2
        L_0x0083:
            if (r6 == 0) goto L_0x008b
            r6.close()     // Catch: IOException -> 0x0089
            goto L_0x008b
        L_0x0089:
            r6 = move-exception
            goto L_0x0091
        L_0x008b:
            if (r1 == 0) goto L_0x009a
            r1.close()     // Catch: IOException -> 0x0089
            goto L_0x009a
        L_0x0091:
            java.lang.String r6 = r6.getMessage()
            java.lang.String r1 = "saveFile"
            android.util.Log.e(r1, r6)
        L_0x009a:
            throw r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: p006me.goldze.mvvmhabit.http.download.ProgressCallBack.saveFile(okhttp3.ResponseBody):void");
    }

    public void subscribeLoadProgress() {
        this.mSubscription = RxBus.getDefault().toObservable(DownLoadStateBean.class).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<DownLoadStateBean>() { // from class: me.goldze.mvvmhabit.http.download.ProgressCallBack.1
            public void accept(DownLoadStateBean downLoadStateBean) throws Exception {
                ProgressCallBack.this.progress(downLoadStateBean.getBytesLoaded(), downLoadStateBean.getTotal());
            }
        });
        RxSubscriptions.add(this.mSubscription);
    }

    public void unsubscribe() {
        RxSubscriptions.remove(this.mSubscription);
    }
}
