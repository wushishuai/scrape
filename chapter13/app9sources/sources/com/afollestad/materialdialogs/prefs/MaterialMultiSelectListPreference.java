package com.afollestad.materialdialogs.prefs;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import java.util.ArrayList;
import java.util.HashSet;

@TargetApi(11)
/* loaded from: classes.dex */
public class MaterialMultiSelectListPreference extends MultiSelectListPreference {
    private Context context;
    private MaterialDialog mDialog;

    public MaterialMultiSelectListPreference(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialMultiSelectListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    @TargetApi(21)
    public MaterialMultiSelectListPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context, attributeSet);
    }

    @TargetApi(21)
    public MaterialMultiSelectListPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init(context, attributeSet);
    }

    @Override // android.preference.MultiSelectListPreference
    public void setEntries(CharSequence[] charSequenceArr) {
        super.setEntries(charSequenceArr);
        MaterialDialog materialDialog = this.mDialog;
        if (materialDialog != null) {
            materialDialog.setItems(charSequenceArr);
        }
    }

    private void init(Context context, AttributeSet attributeSet) {
        this.context = context;
        PrefUtil.setLayoutResource(context, this, attributeSet);
        if (Build.VERSION.SDK_INT <= 10) {
            setWidgetLayoutResource(0);
        }
    }

    @Override // android.preference.DialogPreference
    public Dialog getDialog() {
        return this.mDialog;
    }

    @Override // android.preference.DialogPreference
    protected void showDialog(Bundle bundle) {
        ArrayList arrayList = new ArrayList();
        for (String str : getValues()) {
            if (findIndexOfValue(str) >= 0) {
                arrayList.add(Integer.valueOf(findIndexOfValue(str)));
            }
        }
        MaterialDialog.Builder dismissListener = new MaterialDialog.Builder(this.context).title(getDialogTitle()).icon(getDialogIcon()).negativeText(getNegativeButtonText()).positiveText(getPositiveButtonText()).onAny(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.prefs.MaterialMultiSelectListPreference.2
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                switch (C06173.$SwitchMap$com$afollestad$materialdialogs$DialogAction[dialogAction.ordinal()]) {
                    case 1:
                        MaterialMultiSelectListPreference.this.onClick(materialDialog, -3);
                        return;
                    case 2:
                        MaterialMultiSelectListPreference.this.onClick(materialDialog, -2);
                        return;
                    default:
                        MaterialMultiSelectListPreference.this.onClick(materialDialog, -1);
                        return;
                }
            }
        }).items(getEntries()).itemsCallbackMultiChoice((Integer[]) arrayList.toArray(new Integer[arrayList.size()]), new MaterialDialog.ListCallbackMultiChoice() { // from class: com.afollestad.materialdialogs.prefs.MaterialMultiSelectListPreference.1
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackMultiChoice
            public boolean onSelection(MaterialDialog materialDialog, Integer[] numArr, CharSequence[] charSequenceArr) {
                MaterialMultiSelectListPreference.this.onClick(null, -1);
                materialDialog.dismiss();
                HashSet hashSet = new HashSet();
                for (Integer num : numArr) {
                    hashSet.add(MaterialMultiSelectListPreference.this.getEntryValues()[num.intValue()].toString());
                }
                if (!MaterialMultiSelectListPreference.this.callChangeListener(hashSet)) {
                    return true;
                }
                MaterialMultiSelectListPreference.this.setValues(hashSet);
                return true;
            }
        }).dismissListener(this);
        View onCreateDialogView = onCreateDialogView();
        if (onCreateDialogView != null) {
            onBindDialogView(onCreateDialogView);
            dismissListener.customView(onCreateDialogView, false);
        } else {
            dismissListener.content(getDialogMessage());
        }
        PrefUtil.registerOnActivityDestroyListener(this, this);
        this.mDialog = dismissListener.build();
        if (bundle != null) {
            this.mDialog.onRestoreInstanceState(bundle);
        }
        this.mDialog.show();
    }

    /* renamed from: com.afollestad.materialdialogs.prefs.MaterialMultiSelectListPreference$3 */
    /* loaded from: classes.dex */
    static /* synthetic */ class C06173 {
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

    @Override // android.preference.PreferenceManager.OnActivityDestroyListener, android.preference.DialogPreference
    public void onActivityDestroy() {
        super.onActivityDestroy();
        MaterialDialog materialDialog = this.mDialog;
        if (materialDialog != null && materialDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    @Override // android.preference.MultiSelectListPreference, android.preference.Preference, android.preference.DialogPreference
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

    @Override // android.preference.Preference, android.preference.DialogPreference
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
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.afollestad.materialdialogs.prefs.MaterialMultiSelectListPreference.SavedState.1
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
