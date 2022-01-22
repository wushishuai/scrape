package com.afollestad.materialdialogs.color;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.p000v4.app.DialogFragment;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.content.res.ResourcesCompat;
import android.support.p000v4.view.ViewCompat;
import android.support.p003v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.commons.C0592R;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.util.DialogUtils;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/* loaded from: classes.dex */
public class ColorChooserDialog extends DialogFragment implements View.OnClickListener, View.OnLongClickListener {
    public static final String TAG_ACCENT = "[MD_COLOR_CHOOSER]";
    public static final String TAG_CUSTOM = "[MD_COLOR_CHOOSER]";
    public static final String TAG_PRIMARY = "[MD_COLOR_CHOOSER]";
    private ColorCallback callback;
    private int circleSize;
    private View colorChooserCustomFrame;
    @Nullable
    private int[][] colorsSub;
    private int[] colorsTop;
    private EditText customColorHex;
    private View customColorIndicator;
    private SeekBar.OnSeekBarChangeListener customColorRgbListener;
    private TextWatcher customColorTextWatcher;
    private SeekBar customSeekA;
    private TextView customSeekAValue;
    private SeekBar customSeekB;
    private TextView customSeekBValue;
    private SeekBar customSeekG;
    private TextView customSeekGValue;
    private SeekBar customSeekR;
    private TextView customSeekRValue;
    private GridView grid;
    private int selectedCustomColor;

    /* loaded from: classes.dex */
    public interface ColorCallback {
        void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog);

        void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    /* loaded from: classes.dex */
    public @interface ColorChooserTag {
    }

    @Nullable
    public static ColorChooserDialog findVisible(@NonNull AppCompatActivity appCompatActivity, String str) {
        Fragment findFragmentByTag = appCompatActivity.getSupportFragmentManager().findFragmentByTag(str);
        if (findFragmentByTag == null || !(findFragmentByTag instanceof ColorChooserDialog)) {
            return null;
        }
        return (ColorChooserDialog) findFragmentByTag;
    }

    private void generateColors() {
        Builder builder = getBuilder();
        if (builder.colorsTop != null) {
            this.colorsTop = builder.colorsTop;
            this.colorsSub = builder.colorsSub;
        } else if (builder.accentMode) {
            this.colorsTop = ColorPalette.ACCENT_COLORS;
            this.colorsSub = ColorPalette.ACCENT_COLORS_SUB;
        } else {
            this.colorsTop = ColorPalette.PRIMARY_COLORS;
            this.colorsSub = ColorPalette.PRIMARY_COLORS_SUB;
        }
    }

    @Override // android.support.p000v4.app.DialogFragment, android.support.p000v4.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("top_index", topIndex());
        bundle.putBoolean("in_sub", isInSub());
        bundle.putInt("sub_index", subIndex());
        View view = this.colorChooserCustomFrame;
        bundle.putBoolean("in_custom", view != null && view.getVisibility() == 0);
    }

    @Override // android.support.p000v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ColorCallback) {
            this.callback = (ColorCallback) activity;
            return;
        }
        throw new IllegalStateException("ColorChooserDialog needs to be shown from an Activity implementing ColorCallback.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isInSub() {
        return getArguments().getBoolean("in_sub", false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void isInSub(boolean z) {
        getArguments().putBoolean("in_sub", z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int topIndex() {
        return getArguments().getInt("top_index", -1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void topIndex(int i) {
        if (i > -1) {
            findSubIndexForColor(i, this.colorsTop[i]);
        }
        getArguments().putInt("top_index", i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int subIndex() {
        if (this.colorsSub == null) {
            return -1;
        }
        return getArguments().getInt("sub_index", -1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void subIndex(int i) {
        if (this.colorsSub != null) {
            getArguments().putInt("sub_index", i);
        }
    }

    @StringRes
    public int getTitle() {
        int i;
        Builder builder = getBuilder();
        if (isInSub()) {
            i = builder.titleSub;
        } else {
            i = builder.title;
        }
        return i == 0 ? builder.title : i;
    }

    public String tag() {
        Builder builder = getBuilder();
        if (builder.tag != null) {
            return builder.tag;
        }
        return super.getTag();
    }

    public boolean isAccentMode() {
        return getBuilder().accentMode;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getTag() != null) {
            int parseInt = Integer.parseInt(((String) view.getTag()).split(":")[0]);
            MaterialDialog materialDialog = (MaterialDialog) getDialog();
            Builder builder = getBuilder();
            if (isInSub()) {
                subIndex(parseInt);
            } else {
                topIndex(parseInt);
                int[][] iArr = this.colorsSub;
                if (iArr != null && parseInt < iArr.length) {
                    materialDialog.setActionButton(DialogAction.NEGATIVE, builder.backBtn);
                    isInSub(true);
                }
            }
            if (builder.allowUserCustom) {
                this.selectedCustomColor = getSelectedColor();
            }
            invalidateDynamicButtonColors();
            invalidate();
        }
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        if (view.getTag() == null) {
            return false;
        }
        ((CircleView) view).showHint(Integer.parseInt(((String) view.getTag()).split(":")[1]));
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidateDynamicButtonColors() {
        MaterialDialog materialDialog = (MaterialDialog) getDialog();
        if (materialDialog != null && getBuilder().dynamicButtonColor) {
            int selectedColor = getSelectedColor();
            if (Color.alpha(selectedColor) < 64 || (Color.red(selectedColor) > 247 && Color.green(selectedColor) > 247 && Color.blue(selectedColor) > 247)) {
                selectedColor = Color.parseColor("#DEDEDE");
            }
            if (getBuilder().dynamicButtonColor) {
                materialDialog.getActionButton(DialogAction.POSITIVE).setTextColor(selectedColor);
                materialDialog.getActionButton(DialogAction.NEGATIVE).setTextColor(selectedColor);
                materialDialog.getActionButton(DialogAction.NEUTRAL).setTextColor(selectedColor);
            }
            if (this.customSeekR != null) {
                if (this.customSeekA.getVisibility() == 0) {
                    MDTintHelper.setTint(this.customSeekA, selectedColor);
                }
                MDTintHelper.setTint(this.customSeekR, selectedColor);
                MDTintHelper.setTint(this.customSeekG, selectedColor);
                MDTintHelper.setTint(this.customSeekB, selectedColor);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    @ColorInt
    public int getSelectedColor() {
        int i;
        View view = this.colorChooserCustomFrame;
        if (view != null && view.getVisibility() == 0) {
            return this.selectedCustomColor;
        }
        int i2 = 0;
        if (subIndex() > -1) {
            i = this.colorsSub[topIndex()][subIndex()];
        } else {
            i = topIndex() > -1 ? this.colorsTop[topIndex()] : 0;
        }
        if (i != 0) {
            return i;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            i2 = DialogUtils.resolveColor(getActivity(), 16843829);
        }
        return DialogUtils.resolveColor(getActivity(), C0592R.attr.colorAccent, i2);
    }

    private void findSubIndexForColor(int i, int i2) {
        int[][] iArr = this.colorsSub;
        if (iArr != null && iArr.length - 1 >= i) {
            int[] iArr2 = iArr[i];
            for (int i3 = 0; i3 < iArr2.length; i3++) {
                if (iArr2[i3] == i2) {
                    subIndex(i3);
                    return;
                }
            }
        }
    }

    @Override // android.support.p000v4.app.DialogFragment
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        boolean z;
        int i;
        if (getArguments() == null || !getArguments().containsKey("builder")) {
            throw new IllegalStateException("ColorChooserDialog should be created using its Builder interface.");
        }
        generateColors();
        if (bundle != null) {
            z = !bundle.getBoolean("in_custom", false);
            i = getSelectedColor();
        } else if (getBuilder().setPreselectionColor) {
            i = getBuilder().preselectColor;
            if (i != 0) {
                int i2 = 0;
                boolean z2 = false;
                while (true) {
                    int[] iArr = this.colorsTop;
                    if (i2 >= iArr.length) {
                        break;
                    } else if (iArr[i2] == i) {
                        topIndex(i2);
                        if (getBuilder().accentMode) {
                            subIndex(2);
                        } else if (this.colorsSub != null) {
                            findSubIndexForColor(i2, i);
                        } else {
                            subIndex(5);
                        }
                        z2 = true;
                    } else {
                        if (this.colorsSub != null) {
                            int i3 = 0;
                            while (true) {
                                int[][] iArr2 = this.colorsSub;
                                if (i3 >= iArr2[i2].length) {
                                    break;
                                } else if (iArr2[i2][i3] == i) {
                                    topIndex(i2);
                                    subIndex(i3);
                                    z2 = true;
                                    break;
                                } else {
                                    i3++;
                                }
                            }
                            if (z2) {
                                break;
                            }
                        }
                        i2++;
                    }
                }
                z = z2;
            } else {
                z = false;
            }
        } else {
            i = ViewCompat.MEASURED_STATE_MASK;
            z = true;
        }
        this.circleSize = getResources().getDimensionPixelSize(C0592R.dimen.md_colorchooser_circlesize);
        Builder builder = getBuilder();
        MaterialDialog.Builder showListener = new MaterialDialog.Builder(getActivity()).title(getTitle()).autoDismiss(false).customView(C0592R.layout.md_dialog_colorchooser, false).negativeText(builder.cancelBtn).positiveText(builder.doneBtn).neutralText(builder.allowUserCustom ? builder.customBtn : 0).typeface(builder.mediumFont, builder.regularFont).onPositive(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.color.ColorChooserDialog.4
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                ColorCallback colorCallback = ColorChooserDialog.this.callback;
                ColorChooserDialog colorChooserDialog = ColorChooserDialog.this;
                colorCallback.onColorSelection(colorChooserDialog, colorChooserDialog.getSelectedColor());
                ColorChooserDialog.this.dismiss();
            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.color.ColorChooserDialog.3
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                if (ColorChooserDialog.this.isInSub()) {
                    materialDialog.setActionButton(DialogAction.NEGATIVE, ColorChooserDialog.this.getBuilder().cancelBtn);
                    ColorChooserDialog.this.isInSub(false);
                    ColorChooserDialog.this.subIndex(-1);
                    ColorChooserDialog.this.invalidate();
                    return;
                }
                materialDialog.cancel();
            }
        }).onNeutral(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.color.ColorChooserDialog.2
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                ColorChooserDialog.this.toggleCustom(materialDialog);
            }
        }).showListener(new DialogInterface.OnShowListener() { // from class: com.afollestad.materialdialogs.color.ColorChooserDialog.1
            @Override // android.content.DialogInterface.OnShowListener
            public void onShow(DialogInterface dialogInterface) {
                ColorChooserDialog.this.invalidateDynamicButtonColors();
            }
        });
        if (builder.theme != null) {
            showListener.theme(builder.theme);
        }
        MaterialDialog build = showListener.build();
        View customView = build.getCustomView();
        this.grid = (GridView) customView.findViewById(C0592R.C0595id.md_grid);
        if (builder.allowUserCustom) {
            this.selectedCustomColor = i;
            this.colorChooserCustomFrame = customView.findViewById(C0592R.C0595id.md_colorChooserCustomFrame);
            this.customColorHex = (EditText) customView.findViewById(C0592R.C0595id.md_hexInput);
            this.customColorIndicator = customView.findViewById(C0592R.C0595id.md_colorIndicator);
            this.customSeekA = (SeekBar) customView.findViewById(C0592R.C0595id.md_colorA);
            this.customSeekAValue = (TextView) customView.findViewById(C0592R.C0595id.md_colorAValue);
            this.customSeekR = (SeekBar) customView.findViewById(C0592R.C0595id.md_colorR);
            this.customSeekRValue = (TextView) customView.findViewById(C0592R.C0595id.md_colorRValue);
            this.customSeekG = (SeekBar) customView.findViewById(C0592R.C0595id.md_colorG);
            this.customSeekGValue = (TextView) customView.findViewById(C0592R.C0595id.md_colorGValue);
            this.customSeekB = (SeekBar) customView.findViewById(C0592R.C0595id.md_colorB);
            this.customSeekBValue = (TextView) customView.findViewById(C0592R.C0595id.md_colorBValue);
            if (!builder.allowUserCustomAlpha) {
                customView.findViewById(C0592R.C0595id.md_colorALabel).setVisibility(8);
                this.customSeekA.setVisibility(8);
                this.customSeekAValue.setVisibility(8);
                this.customColorHex.setHint("2196F3");
                this.customColorHex.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            } else {
                this.customColorHex.setHint("FF2196F3");
                this.customColorHex.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
            }
            if (!z) {
                toggleCustom(build);
            }
        }
        invalidate();
        return build;
    }

    @Override // android.support.p000v4.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        ColorCallback colorCallback = this.callback;
        if (colorCallback != null) {
            colorCallback.onColorChooserDismissed(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleCustom(MaterialDialog materialDialog) {
        if (materialDialog == null) {
            materialDialog = (MaterialDialog) getDialog();
        }
        if (this.grid.getVisibility() == 0) {
            materialDialog.setTitle(getBuilder().customBtn);
            materialDialog.setActionButton(DialogAction.NEUTRAL, getBuilder().presetsBtn);
            materialDialog.setActionButton(DialogAction.NEGATIVE, getBuilder().cancelBtn);
            this.grid.setVisibility(4);
            this.colorChooserCustomFrame.setVisibility(0);
            this.customColorTextWatcher = new TextWatcher() { // from class: com.afollestad.materialdialogs.color.ColorChooserDialog.5
                @Override // android.text.TextWatcher
                public void afterTextChanged(Editable editable) {
                }

                @Override // android.text.TextWatcher
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override // android.text.TextWatcher
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    try {
                        ColorChooserDialog colorChooserDialog = ColorChooserDialog.this;
                        colorChooserDialog.selectedCustomColor = Color.parseColor("#" + charSequence.toString());
                    } catch (IllegalArgumentException unused) {
                        ColorChooserDialog.this.selectedCustomColor = ViewCompat.MEASURED_STATE_MASK;
                    }
                    ColorChooserDialog.this.customColorIndicator.setBackgroundColor(ColorChooserDialog.this.selectedCustomColor);
                    if (ColorChooserDialog.this.customSeekA.getVisibility() == 0) {
                        int alpha = Color.alpha(ColorChooserDialog.this.selectedCustomColor);
                        ColorChooserDialog.this.customSeekA.setProgress(alpha);
                        ColorChooserDialog.this.customSeekAValue.setText(String.format(Locale.US, "%d", Integer.valueOf(alpha)));
                    }
                    if (ColorChooserDialog.this.customSeekA.getVisibility() == 0) {
                        ColorChooserDialog.this.customSeekA.setProgress(Color.alpha(ColorChooserDialog.this.selectedCustomColor));
                    }
                    ColorChooserDialog.this.customSeekR.setProgress(Color.red(ColorChooserDialog.this.selectedCustomColor));
                    ColorChooserDialog.this.customSeekG.setProgress(Color.green(ColorChooserDialog.this.selectedCustomColor));
                    ColorChooserDialog.this.customSeekB.setProgress(Color.blue(ColorChooserDialog.this.selectedCustomColor));
                    ColorChooserDialog.this.isInSub(false);
                    ColorChooserDialog.this.topIndex(-1);
                    ColorChooserDialog.this.subIndex(-1);
                    ColorChooserDialog.this.invalidateDynamicButtonColors();
                }
            };
            this.customColorHex.addTextChangedListener(this.customColorTextWatcher);
            this.customColorRgbListener = new SeekBar.OnSeekBarChangeListener() { // from class: com.afollestad.materialdialogs.color.ColorChooserDialog.6
                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override // android.widget.SeekBar.OnSeekBarChangeListener
                @SuppressLint({"DefaultLocale"})
                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    if (z) {
                        if (ColorChooserDialog.this.getBuilder().allowUserCustomAlpha) {
                            ColorChooserDialog.this.customColorHex.setText(String.format("%08X", Integer.valueOf(Color.argb(ColorChooserDialog.this.customSeekA.getProgress(), ColorChooserDialog.this.customSeekR.getProgress(), ColorChooserDialog.this.customSeekG.getProgress(), ColorChooserDialog.this.customSeekB.getProgress()))));
                        } else {
                            ColorChooserDialog.this.customColorHex.setText(String.format("%06X", Integer.valueOf(Color.rgb(ColorChooserDialog.this.customSeekR.getProgress(), ColorChooserDialog.this.customSeekG.getProgress(), ColorChooserDialog.this.customSeekB.getProgress()) & ViewCompat.MEASURED_SIZE_MASK)));
                        }
                    }
                    ColorChooserDialog.this.customSeekAValue.setText(String.format("%d", Integer.valueOf(ColorChooserDialog.this.customSeekA.getProgress())));
                    ColorChooserDialog.this.customSeekRValue.setText(String.format("%d", Integer.valueOf(ColorChooserDialog.this.customSeekR.getProgress())));
                    ColorChooserDialog.this.customSeekGValue.setText(String.format("%d", Integer.valueOf(ColorChooserDialog.this.customSeekG.getProgress())));
                    ColorChooserDialog.this.customSeekBValue.setText(String.format("%d", Integer.valueOf(ColorChooserDialog.this.customSeekB.getProgress())));
                }
            };
            this.customSeekR.setOnSeekBarChangeListener(this.customColorRgbListener);
            this.customSeekG.setOnSeekBarChangeListener(this.customColorRgbListener);
            this.customSeekB.setOnSeekBarChangeListener(this.customColorRgbListener);
            if (this.customSeekA.getVisibility() == 0) {
                this.customSeekA.setOnSeekBarChangeListener(this.customColorRgbListener);
                this.customColorHex.setText(String.format("%08X", Integer.valueOf(this.selectedCustomColor)));
                return;
            }
            this.customColorHex.setText(String.format("%06X", Integer.valueOf(16777215 & this.selectedCustomColor)));
            return;
        }
        materialDialog.setTitle(getBuilder().title);
        materialDialog.setActionButton(DialogAction.NEUTRAL, getBuilder().customBtn);
        if (isInSub()) {
            materialDialog.setActionButton(DialogAction.NEGATIVE, getBuilder().backBtn);
        } else {
            materialDialog.setActionButton(DialogAction.NEGATIVE, getBuilder().cancelBtn);
        }
        this.grid.setVisibility(0);
        this.colorChooserCustomFrame.setVisibility(8);
        this.customColorHex.removeTextChangedListener(this.customColorTextWatcher);
        this.customColorTextWatcher = null;
        this.customSeekR.setOnSeekBarChangeListener(null);
        this.customSeekG.setOnSeekBarChangeListener(null);
        this.customSeekB.setOnSeekBarChangeListener(null);
        this.customColorRgbListener = null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void invalidate() {
        if (this.grid.getAdapter() == null) {
            this.grid.setAdapter((ListAdapter) new ColorGridAdapter());
            this.grid.setSelector(ResourcesCompat.getDrawable(getResources(), C0592R.C0594drawable.md_transparent, null));
        } else {
            ((BaseAdapter) this.grid.getAdapter()).notifyDataSetChanged();
        }
        if (getDialog() != null) {
            getDialog().setTitle(getTitle());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Builder getBuilder() {
        if (getArguments() == null || !getArguments().containsKey("builder")) {
            return null;
        }
        return (Builder) getArguments().getSerializable("builder");
    }

    private void dismissIfNecessary(AppCompatActivity appCompatActivity, String str) {
        Fragment findFragmentByTag = appCompatActivity.getSupportFragmentManager().findFragmentByTag(str);
        if (findFragmentByTag != null) {
            ((DialogFragment) findFragmentByTag).dismiss();
            appCompatActivity.getSupportFragmentManager().beginTransaction().remove(findFragmentByTag).commit();
        }
    }

    @NonNull
    public ColorChooserDialog show(AppCompatActivity appCompatActivity) {
        String str;
        Builder builder = getBuilder();
        if (builder.colorsTop != null) {
            str = "[MD_COLOR_CHOOSER]";
        } else {
            str = builder.accentMode ? "[MD_COLOR_CHOOSER]" : "[MD_COLOR_CHOOSER]";
        }
        dismissIfNecessary(appCompatActivity, str);
        show(appCompatActivity.getSupportFragmentManager(), str);
        return this;
    }

    /* loaded from: classes.dex */
    public static class Builder implements Serializable {
        @Nullable
        int[][] colorsSub;
        @Nullable
        int[] colorsTop;
        @NonNull
        final transient AppCompatActivity context;
        @Nullable
        String mediumFont;
        @ColorInt
        int preselectColor;
        @Nullable
        String regularFont;
        @Nullable
        String tag;
        @Nullable
        Theme theme;
        @StringRes
        final int title;
        @StringRes
        int titleSub;
        @StringRes
        int doneBtn = C0592R.string.md_done_label;
        @StringRes
        int backBtn = C0592R.string.md_back_label;
        @StringRes
        int cancelBtn = C0592R.string.md_cancel_label;
        @StringRes
        int customBtn = C0592R.string.md_custom_label;
        @StringRes
        int presetsBtn = C0592R.string.md_presets_label;
        boolean accentMode = false;
        boolean dynamicButtonColor = true;
        boolean allowUserCustom = true;
        boolean allowUserCustomAlpha = true;
        boolean setPreselectionColor = false;

        public <ActivityType extends AppCompatActivity & ColorCallback> Builder(@NonNull ActivityType activitytype, @StringRes int i) {
            this.context = activitytype;
            this.title = i;
        }

        @NonNull
        public Builder typeface(@Nullable String str, @Nullable String str2) {
            this.mediumFont = str;
            this.regularFont = str2;
            return this;
        }

        @NonNull
        public Builder titleSub(@StringRes int i) {
            this.titleSub = i;
            return this;
        }

        @NonNull
        public Builder tag(@Nullable String str) {
            this.tag = str;
            return this;
        }

        @NonNull
        public Builder theme(@NonNull Theme theme) {
            this.theme = theme;
            return this;
        }

        @NonNull
        public Builder preselect(@ColorInt int i) {
            this.preselectColor = i;
            this.setPreselectionColor = true;
            return this;
        }

        @NonNull
        public Builder accentMode(boolean z) {
            this.accentMode = z;
            return this;
        }

        @NonNull
        public Builder doneButton(@StringRes int i) {
            this.doneBtn = i;
            return this;
        }

        @NonNull
        public Builder backButton(@StringRes int i) {
            this.backBtn = i;
            return this;
        }

        @NonNull
        public Builder cancelButton(@StringRes int i) {
            this.cancelBtn = i;
            return this;
        }

        @NonNull
        public Builder customButton(@StringRes int i) {
            this.customBtn = i;
            return this;
        }

        @NonNull
        public Builder presetsButton(@StringRes int i) {
            this.presetsBtn = i;
            return this;
        }

        @NonNull
        public Builder dynamicButtonColor(boolean z) {
            this.dynamicButtonColor = z;
            return this;
        }

        @NonNull
        public Builder customColors(@NonNull int[] iArr, @Nullable int[][] iArr2) {
            this.colorsTop = iArr;
            this.colorsSub = iArr2;
            return this;
        }

        @NonNull
        public Builder customColors(@ArrayRes int i, @Nullable int[][] iArr) {
            this.colorsTop = DialogUtils.getColorArray(this.context, i);
            this.colorsSub = iArr;
            return this;
        }

        @NonNull
        public Builder allowUserColorInput(boolean z) {
            this.allowUserCustom = z;
            return this;
        }

        @NonNull
        public Builder allowUserColorInputAlpha(boolean z) {
            this.allowUserCustomAlpha = z;
            return this;
        }

        @NonNull
        public ColorChooserDialog build() {
            ColorChooserDialog colorChooserDialog = new ColorChooserDialog();
            Bundle bundle = new Bundle();
            bundle.putSerializable("builder", this);
            colorChooserDialog.setArguments(bundle);
            return colorChooserDialog;
        }

        @NonNull
        public ColorChooserDialog show() {
            ColorChooserDialog build = build();
            build.show(this.context);
            return build;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ColorGridAdapter extends BaseAdapter {
        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return (long) i;
        }

        ColorGridAdapter() {
        }

        @Override // android.widget.Adapter
        public int getCount() {
            if (ColorChooserDialog.this.isInSub()) {
                return ColorChooserDialog.this.colorsSub[ColorChooserDialog.this.topIndex()].length;
            }
            return ColorChooserDialog.this.colorsTop.length;
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            if (ColorChooserDialog.this.isInSub()) {
                return Integer.valueOf(ColorChooserDialog.this.colorsSub[ColorChooserDialog.this.topIndex()][i]);
            }
            return Integer.valueOf(ColorChooserDialog.this.colorsTop[i]);
        }

        @Override // android.widget.Adapter
        @SuppressLint({"DefaultLocale"})
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new CircleView(ColorChooserDialog.this.getContext());
                view.setLayoutParams(new AbsListView.LayoutParams(ColorChooserDialog.this.circleSize, ColorChooserDialog.this.circleSize));
            }
            CircleView circleView = (CircleView) view;
            int i2 = ColorChooserDialog.this.isInSub() ? ColorChooserDialog.this.colorsSub[ColorChooserDialog.this.topIndex()][i] : ColorChooserDialog.this.colorsTop[i];
            circleView.setBackgroundColor(i2);
            if (ColorChooserDialog.this.isInSub()) {
                circleView.setSelected(ColorChooserDialog.this.subIndex() == i);
            } else {
                circleView.setSelected(ColorChooserDialog.this.topIndex() == i);
            }
            circleView.setTag(String.format("%d:%d", Integer.valueOf(i), Integer.valueOf(i2)));
            circleView.setOnClickListener(ColorChooserDialog.this);
            circleView.setOnLongClickListener(ColorChooserDialog.this);
            return view;
        }
    }
}
