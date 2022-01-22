package p006me.goldze.mvvmhabit.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.lang.ref.WeakReference;

/* renamed from: me.goldze.mvvmhabit.utils.ToastUtils */
/* loaded from: classes.dex */
public final class ToastUtils {
    private static int gravity = 81;
    private static Toast sToast;
    private static WeakReference<View> sViewWeakReference;
    private static int xOffset;
    private static int yOffset;
    private static final int DEFAULT_COLOR = 301989888;
    private static int backgroundColor = DEFAULT_COLOR;
    private static int bgResource = -1;
    private static int messageColor = DEFAULT_COLOR;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    static {
        double d = (double) (Utils.getContext().getResources().getDisplayMetrics().density * 64.0f);
        Double.isNaN(d);
        yOffset = (int) (d + 0.5d);
    }

    private ToastUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void setGravity(int i, int i2, int i3) {
        gravity = i;
        xOffset = i2;
        yOffset = i3;
    }

    public static void setView(@LayoutRes int i) {
        sViewWeakReference = new WeakReference<>(((LayoutInflater) Utils.getContext().getSystemService("layout_inflater")).inflate(i, (ViewGroup) null));
    }

    public static void setView(@Nullable View view) {
        sViewWeakReference = view == null ? null : new WeakReference<>(view);
    }

    public static View getView() {
        View view;
        WeakReference<View> weakReference = sViewWeakReference;
        if (weakReference != null && (view = weakReference.get()) != null) {
            return view;
        }
        Toast toast = sToast;
        if (toast != null) {
            return toast.getView();
        }
        return null;
    }

    public static void setBackgroundColor(@ColorInt int i) {
        backgroundColor = i;
    }

    public static void setBgResource(@DrawableRes int i) {
        bgResource = i;
    }

    public static void setMessageColor(@ColorInt int i) {
        messageColor = i;
    }

    public static void showShortSafe(final CharSequence charSequence) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.1
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(charSequence, 0);
            }
        });
    }

    public static void showShortSafe(@StringRes final int i) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.2
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(i, 0);
            }
        });
    }

    public static void showShortSafe(@StringRes final int i, final Object... objArr) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.3
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(i, 0, objArr);
            }
        });
    }

    public static void showShortSafe(final String str, final Object... objArr) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.4
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(str, 0, objArr);
            }
        });
    }

    public static void showLongSafe(final CharSequence charSequence) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.5
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(charSequence, 1);
            }
        });
    }

    public static void showLongSafe(@StringRes final int i) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.6
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(i, 1);
            }
        });
    }

    public static void showLongSafe(@StringRes final int i, final Object... objArr) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.7
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(i, 1, objArr);
            }
        });
    }

    public static void showLongSafe(final String str, final Object... objArr) {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.8
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show(str, 1, objArr);
            }
        });
    }

    public static void showShort(CharSequence charSequence) {
        show(charSequence, 0);
    }

    public static void showShort(@StringRes int i) {
        show(i, 0);
    }

    public static void showShort(@StringRes int i, Object... objArr) {
        show(i, 0, objArr);
    }

    public static void showShort(String str, Object... objArr) {
        show(str, 0, objArr);
    }

    public static void showLong(CharSequence charSequence) {
        show(charSequence, 1);
    }

    public static void showLong(@StringRes int i) {
        show(i, 1);
    }

    public static void showLong(@StringRes int i, Object... objArr) {
        show(i, 1, objArr);
    }

    public static void showLong(String str, Object... objArr) {
        show(str, 1, objArr);
    }

    public static void showCustomShortSafe() {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.9
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show("", 0);
            }
        });
    }

    public static void showCustomLongSafe() {
        sHandler.post(new Runnable() { // from class: me.goldze.mvvmhabit.utils.ToastUtils.10
            @Override // java.lang.Runnable
            public void run() {
                ToastUtils.show("", 1);
            }
        });
    }

    public static void showCustomShort() {
        show("", 0);
    }

    public static void showCustomLong() {
        show("", 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void show(@StringRes int i, int i2) {
        show(Utils.getContext().getResources().getText(i).toString(), i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void show(@StringRes int i, int i2, Object... objArr) {
        show(String.format(Utils.getContext().getResources().getString(i), objArr), i2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void show(String str, int i, Object... objArr) {
        show(String.format(str, objArr), i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void show(CharSequence charSequence, int i) {
        boolean z;
        View view;
        cancel();
        WeakReference<View> weakReference = sViewWeakReference;
        if (weakReference == null || (view = weakReference.get()) == null) {
            z = false;
        } else {
            sToast = new Toast(Utils.getContext());
            sToast.setView(view);
            sToast.setDuration(i);
            z = true;
        }
        if (!z) {
            if (messageColor != DEFAULT_COLOR) {
                SpannableString spannableString = new SpannableString(charSequence);
                spannableString.setSpan(new ForegroundColorSpan(messageColor), 0, spannableString.length(), 33);
                sToast = Toast.makeText(Utils.getContext(), spannableString, i);
            } else {
                sToast = Toast.makeText(Utils.getContext(), charSequence, i);
            }
        }
        View view2 = sToast.getView();
        int i2 = bgResource;
        if (i2 != -1) {
            view2.setBackgroundResource(i2);
        } else {
            int i3 = backgroundColor;
            if (i3 != DEFAULT_COLOR) {
                view2.setBackgroundColor(i3);
            }
        }
        sToast.setGravity(gravity, xOffset, yOffset);
        sToast.show();
    }

    public static void cancel() {
        Toast toast = sToast;
        if (toast != null) {
            toast.cancel();
            sToast = null;
        }
    }
}
