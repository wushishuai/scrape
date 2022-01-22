package p006me.goldze.mvvmhabit.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.p000v4.view.ViewCompat;
import android.support.p003v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.Observer;
import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.functions.Function;
import p005io.reactivex.schedulers.Schedulers;
import p006me.goldze.mvvmhabit.utils.compression.Luban;

/* renamed from: me.goldze.mvvmhabit.utils.ImageUtils */
/* loaded from: classes.dex */
public class ImageUtils {
    private static final float MAX_SIZE = 200.0f;
    public static final int REQUEST_CODE_GETIMAGE_BYCAMERA = 1;
    public static final int REQUEST_CODE_GETIMAGE_BYCROP = 2;
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0;
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD_info = 4;
    public static final int REQUEST_CODE_GETIMAGE_IMAGEPAVER = 3;
    public static final String SDCARD = "/sdcard";
    public static final String SDCARD_MNT = "/mnt/sdcard";
    static Bitmap bitmap;

    public static void saveImage(Context context, String str, Bitmap bitmap2) throws IOException {
        saveImage(context, str, bitmap2, 100);
    }

    public static void saveImage(Context context, String str, Bitmap bitmap2, int i) throws IOException {
        if (bitmap2 != null && str != null && context != null) {
            FileOutputStream openFileOutput = context.openFileOutput(str, 0);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap2.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
            openFileOutput.write(byteArrayOutputStream.toByteArray());
            openFileOutput.close();
        }
    }

    public static void saveImageToSD(Context context, String str, Bitmap bitmap2, int i) throws IOException {
        if (bitmap2 != null) {
            File file = new File(str.substring(0, str.lastIndexOf(File.separator)));
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str));
            bitmap2.compress(Bitmap.CompressFormat.JPEG, i, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            if (context != null) {
                scanPhoto(context, str);
            }
        }
    }

    public static void saveBackgroundImage(Context context, String str, Bitmap bitmap2, int i) throws IOException {
        if (bitmap2 != null) {
            File file = new File(str.substring(0, str.lastIndexOf(File.separator)));
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str));
            bitmap2.compress(Bitmap.CompressFormat.PNG, i, bufferedOutputStream);
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
            if (context != null) {
                scanPhoto(context, str);
            }
        }
    }

    private static void scanPhoto(Context context, String str) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(new File(str)));
        context.sendBroadcast(intent);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0, types: [android.content.Context] */
    /* JADX WARN: Type inference failed for: r1v1, types: [java.io.FileInputStream] */
    /* JADX WARN: Type inference failed for: r1v10 */
    /* JADX WARN: Type inference failed for: r1v11 */
    /* JADX WARN: Type inference failed for: r1v12 */
    /* JADX WARN: Type inference failed for: r1v2 */
    /* JADX WARN: Type inference failed for: r1v3, types: [java.io.FileInputStream] */
    /* JADX WARN: Type inference failed for: r1v8 */
    public static Bitmap getBitmap(Context context, String str) {
        Throwable th;
        Bitmap bitmap2;
        FileNotFoundException e;
        FileInputStream fileInputStream;
        OutOfMemoryError e2;
        try {
            bitmap2 = null;
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            fileInputStream = context.openFileInput(str);
            try {
                bitmap2 = BitmapFactory.decodeStream(fileInputStream);
                context = fileInputStream;
            } catch (FileNotFoundException e3) {
                e = e3;
                e.printStackTrace();
                context = fileInputStream;
                context.close();
            } catch (OutOfMemoryError e4) {
                e2 = e4;
                e2.printStackTrace();
                context = fileInputStream;
                context.close();
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            fileInputStream = null;
        } catch (OutOfMemoryError e6) {
            e2 = e6;
            fileInputStream = null;
        } catch (Throwable th3) {
            th = th3;
            context = 0;
            try {
                context.close();
            } catch (Exception unused) {
            }
            throw th;
        }
        try {
            context.close();
        } catch (Exception unused2) {
            return bitmap2;
        }
    }

    public static Bitmap getBitmapByPath(String str) {
        return getBitmapByPath(str, null);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [java.lang.String] */
    /* JADX WARN: Type inference failed for: r2v1 */
    /* JADX WARN: Type inference failed for: r2v10 */
    /* JADX WARN: Type inference failed for: r2v2, types: [java.io.FileInputStream] */
    /* JADX WARN: Type inference failed for: r2v8 */
    /* JADX WARN: Type inference failed for: r2v9 */
    public static Bitmap getBitmapByPath(String str, BitmapFactory.Options options) {
        Throwable th;
        FileInputStream fileInputStream;
        Bitmap bitmap2;
        FileNotFoundException e;
        FileInputStream fileInputStream2;
        OutOfMemoryError e2;
        try {
            fileInputStream = null;
            bitmap2 = null;
            bitmap2 = null;
        } catch (Throwable th2) {
            th = th2;
            fileInputStream = str;
        }
        try {
            fileInputStream2 = new FileInputStream(new File((String) str));
            try {
                bitmap2 = BitmapFactory.decodeStream(fileInputStream2, null, options);
                str = fileInputStream2;
            } catch (FileNotFoundException e3) {
                e = e3;
                e.printStackTrace();
                str = fileInputStream2;
                str.close();
            } catch (OutOfMemoryError e4) {
                e2 = e4;
                e2.printStackTrace();
                str = fileInputStream2;
                str.close();
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            fileInputStream2 = null;
        } catch (OutOfMemoryError e6) {
            e2 = e6;
            fileInputStream2 = null;
        } catch (Throwable th3) {
            th = th3;
            try {
                fileInputStream.close();
            } catch (Exception unused) {
            }
            throw th;
        }
        try {
            str.close();
        } catch (Exception unused2) {
            return bitmap2;
        }
    }

    public static Bitmap getBitmapByFile(File file) {
        Throwable th;
        FileInputStream fileInputStream;
        Bitmap bitmap2;
        FileNotFoundException e;
        OutOfMemoryError e2;
        try {
            bitmap2 = null;
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            fileInputStream = new FileInputStream(file);
            try {
                bitmap2 = BitmapFactory.decodeStream(fileInputStream);
            } catch (FileNotFoundException e3) {
                e = e3;
                e.printStackTrace();
                fileInputStream.close();
            } catch (OutOfMemoryError e4) {
                e2 = e4;
                e2.printStackTrace();
                fileInputStream.close();
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            fileInputStream = null;
        } catch (OutOfMemoryError e6) {
            e2 = e6;
            fileInputStream = null;
        } catch (Throwable th3) {
            th = th3;
            fileInputStream = null;
            try {
                fileInputStream.close();
            } catch (Exception unused) {
            }
            throw th;
        }
        try {
            fileInputStream.close();
        } catch (Exception unused2) {
            return bitmap2;
        }
    }

    public static String getTempFileName() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS").format((Date) new Timestamp(System.currentTimeMillis()));
    }

    public static String getCamerPath() {
        return Environment.getExternalStorageDirectory() + File.separator + "FounderNews" + File.separator;
    }

    public static String getAbsolutePathFromNoStandardUri(Uri uri) {
        String decode = Uri.decode(uri.toString());
        String str = "file:///sdcard" + File.separator;
        String str2 = "file:///mnt/sdcard" + File.separator;
        if (decode.startsWith(str)) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + decode.substring(str.length());
        } else if (!decode.startsWith(str2)) {
            return null;
        } else {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + decode.substring(str2.length());
        }
    }

    public static String getAbsoluteImagePath(Activity activity, Uri uri) {
        Cursor managedQuery = activity.managedQuery(uri, new String[]{"_data"}, null, null, null);
        if (managedQuery == null) {
            return "";
        }
        int columnIndexOrThrow = managedQuery.getColumnIndexOrThrow("_data");
        if (managedQuery.getCount() <= 0 || !managedQuery.moveToFirst()) {
            return "";
        }
        return managedQuery.getString(columnIndexOrThrow);
    }

    public static Bitmap loadImgThumbnail(Activity activity, String str, int i) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor managedQuery = activity.managedQuery(uri, new String[]{"_id", "_display_name"}, "_display_name='" + str + "'", null, null);
        if (managedQuery == null || managedQuery.getCount() <= 0 || !managedQuery.moveToFirst()) {
            return null;
        }
        ContentResolver contentResolver = activity.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        return MediaStore.Images.Thumbnails.getThumbnail(contentResolver, (long) managedQuery.getInt(0), i, options);
    }

    public static Bitmap loadImgThumbnail(String str, int i, int i2) {
        return zoomBitmap(getBitmapByPath(str), i, i2);
    }

    public static String getLatestImage(Activity activity) {
        Cursor managedQuery = activity.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_data"}, null, null, "_id desc");
        if (managedQuery != null && managedQuery.getCount() > 0) {
            managedQuery.moveToFirst();
            managedQuery.moveToFirst();
            if (!managedQuery.isAfterLast()) {
                return managedQuery.getString(1);
            }
        }
        return null;
    }

    public static int[] scaleImageSize(int[] iArr, int i) {
        if (iArr[0] <= i && iArr[1] <= i) {
            return iArr;
        }
        double d = (double) i;
        double max = (double) Math.max(iArr[0], iArr[1]);
        Double.isNaN(d);
        Double.isNaN(max);
        double d2 = d / max;
        double d3 = (double) iArr[0];
        Double.isNaN(d3);
        double d4 = (double) iArr[1];
        Double.isNaN(d4);
        return new int[]{(int) (d3 * d2), (int) (d4 * d2)};
    }

    public static void createImageThumbnail(Context context, String str, String str2, int i, int i2) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmapByPath = getBitmapByPath(str, options);
        if (bitmapByPath != null) {
            int[] scaleImageSize = scaleImageSize(new int[]{bitmapByPath.getWidth(), bitmapByPath.getHeight()}, i);
            saveImageToSD(null, str2, zoomBitmap(bitmapByPath, scaleImageSize[0], scaleImageSize[1]), i2);
        }
    }

    public static Bitmap zoomBitmap(Bitmap bitmap2, int i, int i2) {
        if (bitmap2 == null) {
            return null;
        }
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float) i) / ((float) width), ((float) i2) / ((float) height));
        return Bitmap.createBitmap(bitmap2, 0, 0, width, height, matrix, true);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap2) {
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        float f = (float) ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        Matrix matrix = new Matrix();
        matrix.postScale(f / ((float) width), f / ((float) height));
        return Bitmap.createBitmap(bitmap2, 0, 0, width, height, matrix, true);
    }

    public static Bitmap reDrawBitMap(Activity activity, Bitmap bitmap2) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int i = displayMetrics.heightPixels;
        int i2 = displayMetrics.widthPixels;
        bitmap2.getHeight();
        int width = bitmap2.getWidth();
        float f = width >= i2 ? ((float) i2) / ((float) width) : 1.0f;
        Matrix matrix = new Matrix();
        matrix.postScale(f, f);
        return Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        return createBitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap2, float f) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawRoundRect(rectF, f, f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap2, rect, rect, paint);
        return createBitmap;
    }

    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap2) {
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        int i = height / 2;
        Bitmap createBitmap = Bitmap.createBitmap(bitmap2, 0, i, width, i, matrix, false);
        Bitmap createBitmap2 = Bitmap.createBitmap(width, i + height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap2);
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
        float f = (float) height;
        float f2 = (float) width;
        float f3 = (float) (height + 4);
        canvas.drawRect(0.0f, f, f2, f3, new Paint());
        canvas.drawBitmap(createBitmap, 0.0f, f3, (Paint) null);
        Paint paint = new Paint();
        paint.setShader(new LinearGradient(0.0f, (float) bitmap2.getHeight(), 0.0f, (float) (createBitmap2.getHeight() + 4), 1895825407, (int) ViewCompat.MEASURED_SIZE_MASK, Shader.TileMode.CLAMP));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0.0f, f, f2, (float) (createBitmap2.getHeight() + 4), paint);
        return createBitmap2;
    }

    public static Drawable bitmapToDrawable(Bitmap bitmap2) {
        return new BitmapDrawable(bitmap2);
    }

    public static String getImageType(File file) {
        FileInputStream fileInputStream;
        Throwable th;
        FileInputStream fileInputStream2 = null;
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            fileInputStream = new FileInputStream(file);
            try {
                String imageType = getImageType(fileInputStream);
                try {
                    fileInputStream.close();
                } catch (IOException unused) {
                }
                return imageType;
            } catch (IOException unused2) {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException unused3) {
                    }
                }
                return null;
            } catch (Throwable th2) {
                th = th2;
                fileInputStream2 = fileInputStream;
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException unused4) {
                    }
                }
                throw th;
            }
        } catch (IOException unused5) {
            fileInputStream = null;
        } catch (Throwable th3) {
            th = th3;
        }
    }

    public static String getImageType(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        try {
            byte[] bArr = new byte[8];
            inputStream.read(bArr);
            return getImageType(bArr);
        } catch (IOException unused) {
            return null;
        }
    }

    public static String getImageType(byte[] bArr) {
        if (isJPEG(bArr)) {
            return "image/jpeg";
        }
        if (isGIF(bArr)) {
            return "image/gif";
        }
        if (isPNG(bArr)) {
            return "image/png";
        }
        if (isBMP(bArr)) {
            return "application/x-bmp";
        }
        return null;
    }

    private static boolean isJPEG(byte[] bArr) {
        if (bArr.length >= 2 && bArr[0] == -1 && bArr[1] == -40) {
            return true;
        }
        return false;
    }

    private static boolean isGIF(byte[] bArr) {
        if (bArr.length < 6 || bArr[0] != 71 || bArr[1] != 73 || bArr[2] != 70 || bArr[3] != 56) {
            return false;
        }
        if ((bArr[4] == 55 || bArr[4] == 57) && bArr[5] == 97) {
            return true;
        }
        return false;
    }

    private static boolean isPNG(byte[] bArr) {
        if (bArr.length >= 8 && bArr[0] == -119 && bArr[1] == 80 && bArr[2] == 78 && bArr[3] == 71 && bArr[4] == 13 && bArr[5] == 10 && bArr[6] == 26 && bArr[7] == 10) {
            return true;
        }
        return false;
    }

    private static boolean isBMP(byte[] bArr) {
        if (bArr.length >= 2 && bArr[0] == 66 && bArr[1] == 77) {
            return true;
        }
        return false;
    }

    public static String getImagePath(Uri uri, Activity activity) {
        Cursor query = activity.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        if (query == null) {
            return uri.toString();
        }
        query.moveToFirst();
        String string = query.getString(query.getColumnIndexOrThrow("_data"));
        query.close();
        return string;
    }

    public static Bitmap loadPicasaImageFromGalley(final Uri uri, final Activity activity) {
        Cursor query = activity.getContentResolver().query(uri, new String[]{"_data", "_display_name"}, null, null, null);
        if (query == null) {
            return null;
        }
        query.moveToFirst();
        if (query.getColumnIndex("_display_name") != -1) {
            new Thread(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        ImageUtils.bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }).start();
        }
        query.close();
        return bitmap;
    }

    public static File compressBitmap(String str, String str2, String str3) throws IOException {
        if (!TextUtils.isEmpty(str)) {
            return convertToFile(compressImage(revitionImageSize(new File(str))), str2, str3);
        }
        return null;
    }

    public static Bitmap zoomBitmap(Bitmap bitmap2, float f, float f2) {
        float width = (float) bitmap2.getWidth();
        float height = (float) bitmap2.getHeight();
        Matrix matrix = new Matrix();
        float f3 = 1.0f;
        float f4 = f < width ? f / width : 1.0f;
        if (f2 < height) {
            f3 = f2 / height;
        }
        matrix.postScale(f4, f3);
        return Bitmap.createBitmap(bitmap2, 0, 0, (int) width, (int) height, matrix, true);
    }

    public static Bitmap revitionImageSize(File file) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        BitmapFactory.Options options = new BitmapFactory.Options();
        int i = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(bufferedInputStream, null, options);
        bufferedInputStream.close();
        while (true) {
            if (options.outWidth / i > 600 || options.outHeight / i > 600) {
                i++;
            } else {
                BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new FileInputStream(file));
                options.inSampleSize = i;
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(bufferedInputStream2, null, options);
            }
        }
    }

    public static Bitmap compressImage(Bitmap bitmap2) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i = 100;
        bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        while (((float) (byteArrayOutputStream.toByteArray().length / 1024)) > MAX_SIZE) {
            byteArrayOutputStream.reset();
            bitmap2.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
            i -= 10;
        }
        return BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, null);
    }

    public static File convertToFile(Bitmap bitmap2, String str, String str2) throws IOException {
        File createFile = createFile(checkTargetCacheDir(str), str2, ".jpg");
        if (!createFile.exists() ? createFile.createNewFile() : false) {
            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(createFile));
        }
        return createFile;
    }

    public static File checkTargetCacheDir(String str) {
        File file = new File(str);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static File createFile(File file, String str, String str2) {
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.CHINA);
        return new File(file, str + simpleDateFormat.format(new Date(System.currentTimeMillis())) + str2);
    }

    public static void compressWithRx(List<String> list, Observer observer) {
        Luban.get(Utils.getContext()).load(list).putGear(3).asListObservable().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.3
            public void accept(Throwable th) throws Exception {
                th.printStackTrace();
            }
        }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends File>>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.2
            public ObservableSource<? extends File> apply(Throwable th) throws Exception {
                return Observable.empty();
            }
        }).subscribe(observer);
    }

    public static void compressWithRx(String str, Consumer consumer) {
        Luban.get(Utils.getContext()).load(str).putGear(3).asObservable().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.5
            public void accept(Throwable th) throws Exception {
                th.printStackTrace();
            }
        }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends File>>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.4
            public ObservableSource<? extends File> apply(Throwable th) throws Exception {
                return Observable.empty();
            }
        }).subscribe(consumer);
    }
}
