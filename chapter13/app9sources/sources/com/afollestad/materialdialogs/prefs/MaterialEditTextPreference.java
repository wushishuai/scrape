package com.afollestad.materialdialogs.prefs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.p003v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.commons.C0592R;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.util.DialogUtils;

/* loaded from: classes.dex */
public class MaterialEditTextPreference extends EditTextPreference {
    private int color = 0;
    private MaterialDialog dialog;
    private EditText editText;

    public MaterialEditTextPreference(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialEditTextPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    @TargetApi(21)
    public MaterialEditTextPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context, attributeSet);
    }

    @TargetApi(21)
    public MaterialEditTextPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        PrefUtil.setLayoutResource(context, this, attributeSet);
        this.color = DialogUtils.resolveColor(context, C0592R.attr.md_widget_color, DialogUtils.resolveColor(context, C0592R.attr.colorAccent, Build.VERSION.SDK_INT >= 21 ? DialogUtils.resolveColor(context, 16843829) : 0));
        this.editText = new AppCompatEditText(context, attributeSet);
        this.editText.setId(16908291);
        this.editText.setEnabled(true);
    }

    @Override // android.preference.EditTextPreference
    protected void onAddEditTextToDialogView(@NonNull View view, @NonNull EditText editText) {
        ((ViewGroup) view).addView(editText, new LinearLayout.LayoutParams(-1, -2));
    }

    @Override // android.preference.EditTextPreference, android.preference.DialogPreference
    @SuppressLint({"MissingSuperCall"})
    protected void onBindDialogView(@NonNull View view) {
        EditText editText = this.editText;
        editText.setText(getText());
        if (editText.getText().length() > 0) {
            editText.setSelection(editText.length());
        }
        ViewParent parent = editText.getParent();
        if (parent != view) {
            if (parent != null) {
                ((ViewGroup) parent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    @Override // android.preference.EditTextPreference, android.preference.DialogPreference
    protected void onDialogClosed(boolean z) {
        if (z) {
            String obj = this.editText.getText().toString();
            if (callChangeListener(obj)) {
                setText(obj);
            }
        }
    }

    @Override // android.preference.EditTextPreference
    public EditText getEditText() {
        return this.editText;
    }

    @Override // android.preference.DialogPreference
    public Dialog getDialog() {
        return this.dialog;
    }

    @Override // android.preference.DialogPreference
    protected void showDialog(Bundle bundle) {
        MaterialDialog.Builder dismissListener = new MaterialDialog.Builder(getContext()).title(getDialogTitle()).icon(getDialogIcon()).positiveText(getPositiveButtonText()).negativeText(getNegativeButtonText()).dismissListener(this).onAny(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.prefs.MaterialEditTextPreference.1
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                switch (C06092.$SwitchMap$com$afollestad$materialdialogs$DialogAction[dialogAction.ordinal()]) {
                    case 1:
                        MaterialEditTextPreference.this.onClick(materialDialog, -3);
                        return;
                    case 2:
                        MaterialEditTextPreference.this.onClick(materialDialog, -2);
                        return;
                    default:
                        MaterialEditTextPreference.this.onClick(materialDialog, -1);
                        return;
                }
            }
        }).dismissListener(this);
        View inflate = LayoutInflater.from(getContext()).inflate(C0592R.layout.md_stub_inputpref, (ViewGroup) null);
        onBindDialogView(inflate);
        MDTintHelper.setTint(this.editText, this.color);
        TextView textView = (TextView) inflate.findViewById(16908299);
        if (getDialogMessage() == null || getDialogMessage().toString().length() <= 0) {
            textView.setVisibility(8);
        } else {
            textView.setVisibility(0);
            textView.setText(getDialogMessage());
        }
        dismissListener.customView(inflate, false);
        PrefUtil.registerOnActivityDestroyListener(this, this);
        this.dialog = dismissListener.build();
        if (bundle != null) {
            this.dialog.onRestoreInstanceState(bundle);
        }
        requestInputMethod(this.dialog);
        this.dialog.show();
    }

    /* renamed from: com.afollestad.materialdialogs.prefs.MaterialEditTextPreference$2 */
    /* loaded from: classes.dex */
    static /* synthetic */ class C06092 {
        static final /* synthetic */ int[] $SwitchMap$com$afollestad$materialdialogs$DialogAction = new int[DialogAction.values().length];

        static {
            try {
                $SwitchMap$com$afollestad$materialdialogs$DialogAction[DialogAction.NEUTRAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$afollestad$materialdialogs$DialogAction[DialogAction.NEGATIVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    @Override // android.preference.DialogPreference, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        PrefUtil.unregisterOnActivityDestroyListener(this, this);
    }

    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(5);
        }
    }

    @Override // android.preference.PreferenceManager.OnActivityDestroyListener, android.preference.DialogPreference
    public void onActivityDestroy() {
        super.onActivityDestroy();
        MaterialDialog materialDialog = this.dialog;
        if (materialDialog != null && materialDialog.isShowing()) {
            this.dialog.dismiss();
        }
    }

    @Override // android.preference.EditTextPreference, android.preference.Preference, android.preference.DialogPreference
    protected Parcelable onSaveInstanceState() {
        Parcelable onSaveInstanceState = super.onSaveInstanceState();
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            return onSaveInstanceState;
        }
        SavedState savedState = new SavedState(onSaveInstanceState);
        savedState.isDialogShowing = true;
        savedState.dialogBundle = dialog.onSaveInstanceState();
        return savedState;
    }

    @Override // android.preference.EditTextPreference, android.preference.Preference, android.preference.DialogPreference
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !parcelable.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.isDialogShowing) {
            showDialog(savedState.dialogBundle);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.afollestad.materialdialogs.prefs.MaterialEditTextPreference.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        Bundle dialogBundle;
        boolean isDialogShowing;

        SavedState(Parcel parcel) {
            super(parcel);
            this.isDialogShowing = parcel.readInt() != 1 ? false : true;
            this.dialogBundle = parcel.readBundle();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override // android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(@NonNull Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.isDialogShowing ? 1 : 0);
            parcel.writeBundle(this.dialogBundle);
        }
    }
}
