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
    static Bitmap bitmap = null;

    public static void saveImage(Context context, String fileName, Bitmap bitmap2) throws IOException {
        saveImage(context, fileName, bitmap2, 100);
    }

    public static void saveImage(Context context, String fileName, Bitmap bitmap2, int quality) throws IOException {
        if (bitmap2 != null && fileName != null && context != null) {
            FileOutputStream fos = context.openFileOutput(fileName, 0);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap2.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            fos.write(stream.toByteArray());
            fos.close();
        }
    }

    public static void saveImageToSD(Context ctx, String filePath, Bitmap bitmap2, int quality) throws IOException {
        if (bitmap2 != null) {
            File file = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
            bitmap2.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
            if (ctx != null) {
                scanPhoto(ctx, filePath);
            }
        }
    }

    public static void saveBackgroundImage(Context ctx, String filePath, Bitmap bitmap2, int quality) throws IOException {
        if (bitmap2 != null) {
            File file = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
            bitmap2.compress(Bitmap.CompressFormat.PNG, quality, bos);
            bos.flush();
            bos.close();
            if (ctx != null) {
                scanPhoto(ctx, filePath);
            }
        }
    }

    private static void scanPhoto(Context ctx, String imgFileName) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        mediaScanIntent.setData(Uri.fromFile(new File(imgFileName)));
        ctx.sendBroadcast(mediaScanIntent);
    }

    public static Bitmap getBitmap(Context context, String fileName) {
        Bitmap bitmap2;
        FileInputStream fis;
        try {
            try {
                fis = null;
                bitmap2 = null;
                try {
                    try {
                        fis = context.openFileInput(fileName);
                        bitmap2 = BitmapFactory.decodeStream(fis);
                        fis.close();
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        fis.close();
                    }
                } catch (FileNotFoundException e2) {
                    e2.printStackTrace();
                    fis.close();
                }
            } catch (Exception e3) {
            }
            return bitmap2;
        } catch (Throwable th) {
            try {
                fis.close();
            } catch (Exception e4) {
            }
            throw th;
        }
    }

    public static Bitmap getBitmapByPath(String filePath) {
        return getBitmapByPath(filePath, null);
    }

    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options opts) {
        Bitmap bitmap2;
        FileInputStream fis;
        try {
            try {
                fis = null;
                bitmap2 = null;
                try {
                    fis = new FileInputStream(new File(filePath));
                    bitmap2 = BitmapFactory.decodeStream(fis, null, opts);
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fis.close();
                } catch (OutOfMemoryError e2) {
                    e2.printStackTrace();
                    fis.close();
                }
            } catch (Exception e3) {
            }
            return bitmap2;
        } catch (Throwable th) {
            try {
                fis.close();
            } catch (Exception e4) {
            }
            throw th;
        }
    }

    public static Bitmap getBitmapByFile(File file) {
        FileInputStream fis;
        Bitmap bitmap2;
        try {
            try {
                fis = null;
                bitmap2 = null;
                try {
                    fis = new FileInputStream(file);
                    bitmap2 = BitmapFactory.decodeStream(fis);
                    fis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fis.close();
                } catch (OutOfMemoryError e2) {
                    e2.printStackTrace();
                    fis.close();
                }
            } catch (Throwable th) {
                try {
                    fis.close();
                } catch (Exception e3) {
                }
                throw th;
            }
        } catch (Exception e4) {
        }
        return bitmap2;
    }

    public static String getTempFileName() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SS").format((Date) new Timestamp(System.currentTimeMillis()));
    }

    public static String getCamerPath() {
        return Environment.getExternalStorageDirectory() + File.separator + "FounderNews" + File.separator;
    }

    public static String getAbsolutePathFromNoStandardUri(Uri mUri) {
        String mUriString = Uri.decode(mUri.toString());
        String pre1 = "file:///sdcard" + File.separator;
        String pre2 = "file:///mnt/sdcard" + File.separator;
        if (mUriString.startsWith(pre1)) {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + mUriString.substring(pre1.length());
        } else if (!mUriString.startsWith(pre2)) {
            return null;
        } else {
            return Environment.getExternalStorageDirectory().getPath() + File.separator + mUriString.substring(pre2.length());
        }
    }

    public static String getAbsoluteImagePath(Activity context, Uri uri) {
        Cursor cursor = context.managedQuery(uri, new String[]{"_data"}, null, null, null);
        if (cursor == null) {
            return "";
        }
        int column_index = cursor.getColumnIndexOrThrow("_data");
        if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            return "";
        }
        return cursor.getString(column_index);
    }

    public static Bitmap loadImgThumbnail(Activity context, String imgName, int kind) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.managedQuery(uri, new String[]{"_id", "_display_name"}, "_display_name='" + imgName + "'", null, null);
        if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
            return null;
        }
        ContentResolver crThumb = context.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        return MediaStore.Images.Thumbnails.getThumbnail(crThumb, (long) cursor.getInt(0), kind, options);
    }

    public static Bitmap loadImgThumbnail(String filePath, int w, int h) {
        return zoomBitmap(getBitmapByPath(filePath), w, h);
    }

    public static String getLatestImage(Activity context) {
        Cursor cursor = context.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "_data"}, null, null, "_id desc");
        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }
        cursor.moveToFirst();
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            return cursor.getString(1);
        }
        return null;
    }

    public static int[] scaleImageSize(int[] img_size, int square_size) {
        if (img_size[0] <= square_size && img_size[1] <= square_size) {
            return img_size;
        }
        double d = (double) square_size;
        double max = (double) Math.max(img_size[0], img_size[1]);
        Double.isNaN(d);
        Double.isNaN(max);
        double ratio = d / max;
        double d2 = (double) img_size[0];
        Double.isNaN(d2);
        double d3 = (double) img_size[1];
        Double.isNaN(d3);
        return new int[]{(int) (d2 * ratio), (int) (d3 * ratio)};
    }

    public static void createImageThumbnail(Context context, String largeImagePath, String thumbfilePath, int square_size, int quality) throws IOException {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        Bitmap cur_bitmap = getBitmapByPath(largeImagePath, opts);
        if (cur_bitmap != null) {
            int[] new_img_size = scaleImageSize(new int[]{cur_bitmap.getWidth(), cur_bitmap.getHeight()}, square_size);
            saveImageToSD(null, thumbfilePath, zoomBitmap(cur_bitmap, new_img_size[0], new_img_size[1]), quality);
        }
    }

    public static Bitmap zoomBitmap(Bitmap bitmap2, int w, int h) {
        if (bitmap2 == null) {
            return null;
        }
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float) w) / ((float) width), ((float) h) / ((float) height));
        return Bitmap.createBitmap(bitmap2, 0, 0, width, height, matrix, true);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap2) {
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        float scaleWidth = ((float) ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION) / ((float) width);
        float scaleHeight = ((float) ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION) / ((float) height);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap2, 0, 0, width, height, matrix, true);
    }

    public static Bitmap reDrawBitMap(Activity context, Bitmap bitmap2) {
        float zoomScale;
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int i = dm.heightPixels;
        int rWidth = dm.widthPixels;
        bitmap2.getHeight();
        int width = bitmap2.getWidth();
        if (width >= rWidth) {
            zoomScale = ((float) rWidth) / ((float) width);
        } else {
            zoomScale = 1.0f;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(zoomScale, zoomScale);
        return Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap2 = Bitmap.createBitmap(width, height, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap2);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap2;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap2, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap2, rect, rect, paint);
        return output;
    }

    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap2) {
        int width = bitmap2.getWidth();
        int height = bitmap2.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f);
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap2, 0, height / 2, width, height / 2, matrix, false);
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height / 2) + height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
        canvas.drawRect(0.0f, (float) height, (float) width, (float) (height + 4), new Paint());
        canvas.drawBitmap(reflectionImage, 0.0f, (float) (height + 4), (Paint) null);
        Paint paint = new Paint();
        paint.setShader(new LinearGradient(0.0f, (float) bitmap2.getHeight(), 0.0f, (float) (bitmapWithReflection.getHeight() + 4), 1895825407, (int) ViewCompat.MEASURED_SIZE_MASK, Shader.TileMode.CLAMP));
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0.0f, (float) height, (float) width, (float) (bitmapWithReflection.getHeight() + 4), paint);
        return bitmapWithReflection;
    }

    public static Drawable bitmapToDrawable(Bitmap bitmap2) {
        return new BitmapDrawable(bitmap2);
    }

    public static String getImageType(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            String type = getImageType(in);
            try {
                in.close();
            } catch (IOException e) {
            }
            return type;
        } catch (IOException e2) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                }
            }
            return null;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                }
            }
            throw th;
        }
    }

    public static String getImageType(InputStream in) {
        if (in == null) {
            return null;
        }
        try {
            byte[] bytes = new byte[8];
            in.read(bytes);
            return getImageType(bytes);
        } catch (IOException e) {
            return null;
        }
    }

    public static String getImageType(byte[] bytes) {
        if (isJPEG(bytes)) {
            return "image/jpeg";
        }
        if (isGIF(bytes)) {
            return "image/gif";
        }
        if (isPNG(bytes)) {
            return "image/png";
        }
        if (isBMP(bytes)) {
            return "application/x-bmp";
        }
        return null;
    }

    private static boolean isJPEG(byte[] b) {
        if (b.length >= 2 && b[0] == -1 && b[1] == -40) {
            return true;
        }
        return false;
    }

    private static boolean isGIF(byte[] b) {
        if (b.length < 6 || b[0] != 71 || b[1] != 73 || b[2] != 70 || b[3] != 56) {
            return false;
        }
        if ((b[4] == 55 || b[4] == 57) && b[5] == 97) {
            return true;
        }
        return false;
    }

    private static boolean isPNG(byte[] b) {
        if (b.length >= 8 && b[0] == -119 && b[1] == 80 && b[2] == 78 && b[3] == 71 && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10) {
            return true;
        }
        return false;
    }

    private static boolean isBMP(byte[] b) {
        if (b.length >= 2 && b[0] == 66 && b[1] == 77) {
            return true;
        }
        return false;
    }

    public static String getImagePath(Uri uri, Activity context) {
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        if (cursor == null) {
            return uri.toString();
        }
        cursor.moveToFirst();
        String ImagePath = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
        cursor.close();
        return ImagePath;
    }

    public static Bitmap loadPicasaImageFromGalley(final Uri uri, final Activity context) {
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data", "_display_name"}, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        if (cursor.getColumnIndex("_display_name") != -1) {
            new Thread(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        ImageUtils.bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }).start();
        }
        cursor.close();
        return bitmap;
    }

    public static File compressBitmap(String url, String storageDir, String prefix) throws IOException {
        if (!TextUtils.isEmpty(url)) {
            return convertToFile(compressImage(revitionImageSize(new File(url))), storageDir, prefix);
        }
        return null;
    }

    public static Bitmap zoomBitmap(Bitmap source, float expectWidth, float expectHeight) {
        float scaleWidth;
        float scaleHeight;
        float width = (float) source.getWidth();
        float height = (float) source.getHeight();
        Matrix matrix = new Matrix();
        if (expectWidth < width) {
            scaleWidth = expectWidth / width;
        } else {
            scaleWidth = 1.0f;
        }
        if (expectHeight < height) {
            scaleHeight = expectHeight / height;
        } else {
            scaleHeight = 1.0f;
        }
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(source, 0, 0, (int) width, (int) height, matrix, true);
    }

    public static Bitmap revitionImageSize(File file) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);
        in.close();
        int i = 1;
        while (true) {
            if (options.outWidth / i > 600 || options.outHeight / i > 600) {
                i++;
            } else {
                BufferedInputStream in2 = new BufferedInputStream(new FileInputStream(file));
                options.inSampleSize = i;
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeStream(in2, null, options);
            }
        }
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int offset = 100;
        while (((float) (baos.toByteArray().length / 1024)) > MAX_SIZE) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, offset, baos);
            offset -= 10;
        }
        return BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);
    }

    public static File convertToFile(Bitmap bitmap2, String storageDir, String prefix) throws IOException {
        File cacheDir = createFile(checkTargetCacheDir(storageDir), prefix, ".jpg");
        boolean created = false;
        if (!cacheDir.exists()) {
            created = cacheDir.createNewFile();
        }
        if (created) {
            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(cacheDir));
        }
        return cacheDir;
    }

    public static File checkTargetCacheDir(String storageDir) {
        File file = new File(storageDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.CHINA);
        return new File(folder, prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix);
    }

    public static void compressWithRx(List<String> files, Observer observer) {
        Luban.get(Utils.getContext()).load(files).putGear(3).asListObservable().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.3
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends File>>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.2
            public ObservableSource<? extends File> apply(Throwable throwable) throws Exception {
                return Observable.empty();
            }
        }).subscribe(observer);
    }

    public static void compressWithRx(String url, Consumer consumer) {
        Luban.get(Utils.getContext()).load(url).putGear(3).asObservable().subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).doOnError(new Consumer<Throwable>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.5
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        }).onErrorResumeNext(new Function<Throwable, ObservableSource<? extends File>>() { // from class: me.goldze.mvvmhabit.utils.ImageUtils.4
            public ObservableSource<? extends File> apply(Throwable throwable) throws Exception {
                return Observable.empty();
            }
        }).subscribe(consumer);
    }
}
