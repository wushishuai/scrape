package p006me.goldze.mvvmhabit.utils.compression;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.p000v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import p005io.reactivex.Observable;
import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.schedulers.Schedulers;

/* renamed from: me.goldze.mvvmhabit.utils.compression.Luban */
/* loaded from: classes.dex */
public class Luban {
    private static String DEFAULT_DISK_CACHE_DIR = "smartcity_disk_cache";
    private static final int FIRST_GEAR = 1;
    private static volatile Luban INSTANCE = null;
    private static final String TAG = "smartcity";
    public static final int THIRD_GEAR = 3;
    private OnCompressListener compressListener;
    private String filename;
    private final File mCacheDir;
    private String mFile;
    private List<String> mListFile = new ArrayList();
    private int gear = 3;

    private Luban(File file) {
        this.mCacheDir = file;
    }

    private static synchronized File getPhotoCacheDir(Context context) {
        File photoCacheDir;
        synchronized (Luban.class) {
            photoCacheDir = getPhotoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
        }
        return photoCacheDir;
    }

    private static File getPhotoCacheDir(Context context, String str) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File file = new File(cacheDir, str);
            if (!file.mkdirs() && (!file.exists() || !file.isDirectory())) {
                return null;
            }
            File file2 = new File(cacheDir + "/.nomedia");
            if (file2.mkdirs() || (file2.exists() && file2.isDirectory())) {
                return file;
            }
            return null;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    public static Luban get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Luban(getPhotoCacheDir(context));
        }
        return INSTANCE;
    }

    public Luban launch() {
        Preconditions.checkNotNull(this.mFile, "the image file cannot be null, please call .load() before this method!");
        OnCompressListener onCompressListener = this.compressListener;
        if (onCompressListener != null) {
            onCompressListener.onStart();
        }
        int i = this.gear;
        if (i == 1) {
            Observable.just(this.mFile).map(new Function<String, File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.4
                public File apply(String str) throws Exception {
                    return Luban.this.firstCompress(new File(str));
                }
            }).subscribeOn(Schedulers.m27io()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.3
                public void accept(Throwable th) throws Exception {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onError(th);
                    }
                }
            }).onErrorResumeNext(Observable.empty()).filter(new Predicate<File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.2
                public boolean test(File file) throws Exception {
                    return file != null;
                }
            }).subscribe(new Consumer<File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.1
                public void accept(File file) throws Exception {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onSuccess(file);
                    }
                }
            });
        } else if (i == 3) {
            Observable.just(this.mFile).map(new Function<String, File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.8
                public File apply(String str) throws Exception {
                    return Luban.this.thirdCompress(new File(str));
                }
            }).subscribeOn(Schedulers.m27io()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.7
                public void accept(Throwable th) throws Exception {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onError(th);
                    }
                }
            }).onErrorResumeNext(Observable.empty()).filter(new Predicate<File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.6
                public boolean test(File file) throws Exception {
                    return file != null;
                }
            }).subscribe(new Consumer<File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.5
                public void accept(File file) throws Exception {
                    if (Luban.this.compressListener != null) {
                        Luban.this.compressListener.onSuccess(file);
                    }
                }
            });
        }
        return this;
    }

    public Luban load(String str) {
        this.mFile = str;
        return this;
    }

    public Luban load(List<String> list) {
        this.mListFile = list;
        return this;
    }

    public Luban setCompressListener(OnCompressListener onCompressListener) {
        this.compressListener = onCompressListener;
        return this;
    }

    public Luban putGear(int i) {
        this.gear = i;
        return this;
    }

    public Luban setFilename(String str) {
        this.filename = str;
        return this;
    }

    public Observable<File> asObservable() {
        int i = this.gear;
        if (i == 1) {
            return Observable.just(this.mFile).map(new Function<String, File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.9
                public File apply(String str) throws Exception {
                    if (TextUtils.isEmpty(str) || str.contains("http")) {
                        return null;
                    }
                    File file = new File(str);
                    if (file.exists()) {
                        return Luban.this.firstCompress(file);
                    }
                    return null;
                }
            });
        }
        return i == 3 ? Observable.just(this.mFile).map(new Function<String, File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.10
            public File apply(String str) throws Exception {
                if (TextUtils.isEmpty(str) || str.contains("http")) {
                    return null;
                }
                File file = new File(str);
                if (file.exists()) {
                    return Luban.this.thirdCompress(file);
                }
                return null;
            }
        }) : Observable.empty();
    }

    public Observable<File> asListObservable() {
        int i = this.gear;
        if (i == 1) {
            return Observable.fromIterable(this.mListFile).map(new Function<String, File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.11
                public File apply(String str) throws Exception {
                    if (TextUtils.isEmpty(str)) {
                        return null;
                    }
                    File file = new File(str);
                    if (file.exists()) {
                        return Luban.this.firstCompress(file);
                    }
                    return null;
                }
            });
        }
        return i == 3 ? Observable.fromIterable(this.mListFile).map(new Function<String, File>() { // from class: me.goldze.mvvmhabit.utils.compression.Luban.12
            public File apply(String str) throws Exception {
                if (TextUtils.isEmpty(str)) {
                    return null;
                }
                File file = new File(str);
                if (file.exists()) {
                    return Luban.this.thirdCompress(file);
                }
                return null;
            }
        }) : Observable.empty();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public File thirdCompress(@NonNull File file) {
        double d;
        int i;
        int i2;
        StringBuilder sb = new StringBuilder();
        sb.append(this.mCacheDir.getAbsolutePath());
        sb.append(File.separator);
        sb.append(TextUtils.isEmpty(this.filename) ? Long.valueOf(System.currentTimeMillis()) : this.filename);
        sb.append(".jpg");
        String sb2 = sb.toString();
        String absolutePath = file.getAbsolutePath();
        int imageSpinAngle = getImageSpinAngle(absolutePath);
        int i3 = getImageSize(absolutePath)[0];
        int i4 = getImageSize(absolutePath)[1];
        if (i3 % 2 == 1) {
            i3++;
        }
        if (i4 % 2 == 1) {
            i4++;
        }
        int i5 = i3 > i4 ? i4 : i3;
        int i6 = i3 > i4 ? i3 : i4;
        double d2 = (double) i5;
        double d3 = (double) i6;
        Double.isNaN(d2);
        Double.isNaN(d3);
        double d4 = d2 / d3;
        if (d4 > 1.0d || d4 <= 0.5625d) {
            if (d4 > 0.5625d || d4 <= 0.5d) {
                double d5 = 1280.0d / d4;
                Double.isNaN(d3);
                int ceil = (int) Math.ceil(d3 / d5);
                int i7 = i5 / ceil;
                int i8 = i6 / ceil;
                double d6 = (double) (i7 * i8);
                Double.isNaN(d6);
                double d7 = 500.0d * (d6 / (d5 * 1280.0d));
                if (d7 < 100.0d) {
                    d7 = 100.0d;
                }
                i2 = i7;
                i = i8;
                d = d7;
            } else if (i6 < 1280 && file.length() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID < 200) {
                return file;
            } else {
                int i9 = i6 / 1280;
                if (i9 == 0) {
                    i9 = 1;
                }
                int i10 = i5 / i9;
                int i11 = i6 / i9;
                double d8 = (double) (i10 * i11);
                Double.isNaN(d8);
                double d9 = (d8 / 3686400.0d) * 400.0d;
                if (d9 < 100.0d) {
                    d9 = 100.0d;
                }
                i2 = i10;
                i = i11;
                d = d9;
            }
        } else if (i6 < 1664) {
            if (file.length() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID < 150) {
                return file;
            }
            double d10 = (double) (i5 * i6);
            double pow = Math.pow(1664.0d, 2.0d);
            Double.isNaN(d10);
            d = (d10 / pow) * 150.0d;
            if (d < 60.0d) {
                d = 60.0d;
            }
            i = i4;
            i2 = i3;
        } else if (i6 >= 1664 && i6 < 4990) {
            int i12 = i5 / 2;
            int i13 = i6 / 2;
            double d11 = (double) (i12 * i13);
            double pow2 = Math.pow(2495.0d, 2.0d);
            Double.isNaN(d11);
            double d12 = (d11 / pow2) * 300.0d;
            double d13 = 60.0d;
            if (d12 >= 60.0d) {
                d13 = d12;
            }
            i2 = i12;
            i = i13;
            d = d13;
        } else if (i6 < 4990 || i6 >= 10240) {
            int i14 = i6 / 1280;
            if (i14 == 0) {
                i14 = 1;
            }
            int i15 = i5 / i14;
            int i16 = i6 / i14;
            double d14 = (double) (i15 * i16);
            double pow3 = Math.pow(2560.0d, 2.0d);
            Double.isNaN(d14);
            double d15 = (d14 / pow3) * 300.0d;
            if (d15 < 100.0d) {
                d15 = 100.0d;
            }
            i2 = i15;
            i = i16;
            d = d15;
        } else {
            int i17 = i5 / 4;
            int i18 = i6 / 4;
            double d16 = (double) (i17 * i18);
            double pow4 = Math.pow(2560.0d, 2.0d);
            Double.isNaN(d16);
            double d17 = (d16 / pow4) * 300.0d;
            if (d17 < 100.0d) {
                d17 = 100.0d;
            }
            i2 = i17;
            i = i18;
            d = d17;
        }
        return compress(absolutePath, sb2, i2, i, imageSpinAngle, (long) d);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public File firstCompress(@NonNull File file) {
        long j;
        int i;
        int i2;
        int i3;
        String absolutePath = file.getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        sb.append(this.mCacheDir.getAbsolutePath());
        sb.append(File.separator);
        sb.append(TextUtils.isEmpty(this.filename) ? Long.valueOf(System.currentTimeMillis()) : this.filename);
        sb.append(".jpg");
        String sb2 = sb.toString();
        long length = file.length() / 5;
        int imageSpinAngle = getImageSpinAngle(absolutePath);
        int[] imageSize = getImageSize(absolutePath);
        int i4 = 0;
        if (imageSize[0] <= imageSize[1]) {
            double d = (double) imageSize[0];
            double d2 = (double) imageSize[1];
            Double.isNaN(d);
            Double.isNaN(d2);
            double d3 = d / d2;
            if (d3 <= 1.0d && d3 > 0.5625d) {
                int i5 = imageSize[0] > 1280 ? 1280 : imageSize[0];
                i3 = (imageSize[1] * i5) / imageSize[0];
                j = (long) 60;
                i4 = i5;
            } else if (d3 <= 0.5625d) {
                i3 = imageSize[1] > 720 ? 720 : imageSize[1];
                i4 = (imageSize[0] * i3) / imageSize[1];
                j = length;
            } else {
                i3 = 0;
                j = 0;
            }
            i = i3;
            i2 = i4;
        } else {
            double d4 = (double) imageSize[1];
            double d5 = (double) imageSize[0];
            Double.isNaN(d4);
            Double.isNaN(d5);
            double d6 = d4 / d5;
            if (d6 <= 1.0d && d6 > 0.5625d) {
                i = 1280;
                if (imageSize[1] <= 1280) {
                    i = imageSize[1];
                }
                i2 = (imageSize[0] * i) / imageSize[1];
                j = (long) 60;
            } else if (d6 <= 0.5625d) {
                int i6 = 720;
                if (imageSize[0] <= 720) {
                    i6 = imageSize[0];
                }
                i = (imageSize[1] * i6) / imageSize[0];
                j = length;
                i2 = i6;
            } else {
                i2 = 0;
                i = 0;
                j = 0;
            }
        }
        return compress(absolutePath, sb2, i2, i, imageSpinAngle, j);
    }

    public int[] getImageSize(String str) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(str, options);
        return new int[]{options.outWidth, options.outHeight};
    }

    private Bitmap compress(String str, int i, int i2) {
        int i3;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(str, options);
        int i4 = options.outHeight;
        int i5 = options.outWidth;
        if (i4 > i2 || i5 > i) {
            int i6 = i4 / 2;
            int i7 = i5 / 2;
            i3 = 1;
            while (i6 / i3 > i2 && i7 / i3 > i) {
                i3 *= 2;
            }
        } else {
            i3 = 1;
        }
        options.inSampleSize = i3;
        options.inJustDecodeBounds = false;
        int ceil = (int) Math.ceil((double) (((float) options.outHeight) / ((float) i2)));
        int ceil2 = (int) Math.ceil((double) (((float) options.outWidth) / ((float) i)));
        if (ceil > 1 || ceil2 > 1) {
            if (ceil > ceil2) {
                options.inSampleSize = ceil;
            } else {
                options.inSampleSize = ceil2;
            }
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(str, options);
    }

    private int getImageSpinAngle(String str) {
        try {
            int attributeInt = new ExifInterface(str).getAttributeInt("Orientation", 1);
            if (attributeInt == 3) {
                return 180;
            }
            if (attributeInt == 6) {
                return 90;
            }
            if (attributeInt != 8) {
                return 0;
            }
            return 270;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private File compress(String str, String str2, int i, int i2, int i3, long j) {
        return saveImage(str2, rotatingImage(i3, compress(str, i, i2)), j);
    }

    private static Bitmap rotatingImage(int i, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private File saveImage(String str, Bitmap bitmap, long j) {
        Preconditions.checkNotNull(bitmap, "smartcitybitmap cannot be null");
        File file = new File(str.substring(0, str.lastIndexOf("/")));
        if (!file.exists() && !file.mkdirs()) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        while (((long) (byteArrayOutputStream.toByteArray().length / 1024)) > j && i > 6) {
            byteArrayOutputStream.reset();
            i -= 6;
            bitmap.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(str);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(str);
    }
}
