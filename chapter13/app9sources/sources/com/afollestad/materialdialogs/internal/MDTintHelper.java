package com.afollestad.materialdialogs.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.graphics.drawable.DrawableCompat;
import android.support.p003v7.widget.AppCompatEditText;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.afollestad.materialdialogs.C0582R;
import com.afollestad.materialdialogs.util.DialogUtils;
import java.lang.reflect.Field;

@SuppressLint({"PrivateResource"})
/* loaded from: classes.dex */
public class MDTintHelper {
    public static void setTint(@NonNull RadioButton radioButton, @NonNull ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= 21) {
            radioButton.setButtonTintList(colorStateList);
            return;
        }
        Drawable wrap = DrawableCompat.wrap(ContextCompat.getDrawable(radioButton.getContext(), C0582R.C0584drawable.abc_btn_radio_material));
        DrawableCompat.setTintList(wrap, colorStateList);
        radioButton.setButtonDrawable(wrap);
    }

    public static void setTint(@NonNull RadioButton radioButton, @ColorInt int i) {
        int disabledColor = DialogUtils.getDisabledColor(radioButton.getContext());
        setTint(radioButton, new ColorStateList(new int[][]{new int[]{16842910, -16842912}, new int[]{16842910, 16842912}, new int[]{-16842910, -16842912}, new int[]{-16842910, 16842912}}, new int[]{DialogUtils.resolveColor(radioButton.getContext(), C0582R.attr.colorControlNormal), i, disabledColor, disabledColor}));
    }

    public static void setTint(@NonNull CheckBox checkBox, @NonNull ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= 21) {
            checkBox.setButtonTintList(colorStateList);
            return;
        }
        Drawable wrap = DrawableCompat.wrap(ContextCompat.getDrawable(checkBox.getContext(), C0582R.C0584drawable.abc_btn_check_material));
        DrawableCompat.setTintList(wrap, colorStateList);
        checkBox.setButtonDrawable(wrap);
    }

    public static void setTint(@NonNull CheckBox checkBox, @ColorInt int i) {
        int disabledColor = DialogUtils.getDisabledColor(checkBox.getContext());
        setTint(checkBox, new ColorStateList(new int[][]{new int[]{16842910, -16842912}, new int[]{16842910, 16842912}, new int[]{-16842910, -16842912}, new int[]{-16842910, 16842912}}, new int[]{DialogUtils.resolveColor(checkBox.getContext(), C0582R.attr.colorControlNormal), i, disabledColor, disabledColor}));
    }

    public static void setTint(@NonNull SeekBar seekBar, @ColorInt int i) {
        ColorStateList valueOf = ColorStateList.valueOf(i);
        if (Build.VERSION.SDK_INT >= 21) {
            seekBar.setThumbTintList(valueOf);
            seekBar.setProgressTintList(valueOf);
        } else if (Build.VERSION.SDK_INT > 10) {
            Drawable wrap = DrawableCompat.wrap(seekBar.getProgressDrawable());
            seekBar.setProgressDrawable(wrap);
            DrawableCompat.setTintList(wrap, valueOf);
            if (Build.VERSION.SDK_INT >= 16) {
                Drawable wrap2 = DrawableCompat.wrap(seekBar.getThumb());
                DrawableCompat.setTintList(wrap2, valueOf);
                seekBar.setThumb(wrap2);
            }
        } else {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
            if (Build.VERSION.SDK_INT <= 10) {
                mode = PorterDuff.Mode.MULTIPLY;
            }
            if (seekBar.getIndeterminateDrawable() != null) {
                seekBar.getIndeterminateDrawable().setColorFilter(i, mode);
            }
            if (seekBar.getProgressDrawable() != null) {
                seekBar.getProgressDrawable().setColorFilter(i, mode);
            }
        }
    }

    public static void setTint(@NonNull ProgressBar progressBar, @ColorInt int i) {
        setTint(progressBar, i, false);
    }

    private static void setTint(@NonNull ProgressBar progressBar, @ColorInt int i, boolean z) {
        ColorStateList valueOf = ColorStateList.valueOf(i);
        if (Build.VERSION.SDK_INT >= 21) {
            progressBar.setProgressTintList(valueOf);
            progressBar.setSecondaryProgressTintList(valueOf);
            if (!z) {
                progressBar.setIndeterminateTintList(valueOf);
                return;
            }
            return;
        }
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
        if (Build.VERSION.SDK_INT <= 10) {
            mode = PorterDuff.Mode.MULTIPLY;
        }
        if (!z && progressBar.getIndeterminateDrawable() != null) {
            progressBar.getIndeterminateDrawable().setColorFilter(i, mode);
        }
        if (progressBar.getProgressDrawable() != null) {
            progressBar.getProgressDrawable().setColorFilter(i, mode);
        }
    }

    private static ColorStateList createEditTextColorStateList(@NonNull Context context, @ColorInt int i) {
        return new ColorStateList(new int[][]{new int[]{-16842910}, new int[]{-16842919, -16842908}, new int[0]}, new int[]{DialogUtils.resolveColor(context, C0582R.attr.colorControlNormal), DialogUtils.resolveColor(context, C0582R.attr.colorControlNormal), i});
    }

    public static void setTint(@NonNull EditText editText, @ColorInt int i) {
        ColorStateList createEditTextColorStateList = createEditTextColorStateList(editText.getContext(), i);
        if (editText instanceof AppCompatEditText) {
            ((AppCompatEditText) editText).setSupportBackgroundTintList(createEditTextColorStateList);
        } else if (Build.VERSION.SDK_INT >= 21) {
            editText.setBackgroundTintList(createEditTextColorStateList);
        }
        setCursorTint(editText, i);
    }

    private static void setCursorTint(@NonNull EditText editText, @ColorInt int i) {
        try {
            Field declaredField = TextView.class.getDeclaredField("mCursorDrawableRes");
            declaredField.setAccessible(true);
            int i2 = declaredField.getInt(editText);
            Field declaredField2 = TextView.class.getDeclaredField("mEditor");
            declaredField2.setAccessible(true);
            Object obj = declaredField2.get(editText);
            Field declaredField3 = obj.getClass().getDeclaredField("mCursorDrawable");
            declaredField3.setAccessible(true);
            Drawable[] drawableArr = {ContextCompat.getDrawable(editText.getContext(), i2), ContextCompat.getDrawable(editText.getContext(), i2)};
            drawableArr[0].setColorFilter(i, PorterDuff.Mode.SRC_IN);
            drawableArr[1].setColorFilter(i, PorterDuff.Mode.SRC_IN);
            declaredField3.set(obj, drawableArr);
        } catch (NoSuchFieldException e) {
            Log.d("MDTintHelper", "Device issue with cursor tinting: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
