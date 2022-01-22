package com.afollestad.materialdialogs.prefs;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.p003v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import java.lang.reflect.Field;

/* loaded from: classes.dex */
public class MaterialListPreference extends ListPreference {
    private Context context;
    private MaterialDialog dialog;

    public MaterialListPreference(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    @TargetApi(21)
    public MaterialListPreference(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context, attributeSet);
    }

    @TargetApi(21)
    public MaterialListPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        this.context = context;
        PrefUtil.setLayoutResource(context, this, attributeSet);
        if (Build.VERSION.SDK_INT <= 10) {
            setWidgetLayoutResource(0);
        }
    }

    @Override // android.preference.ListPreference
    public void setEntries(CharSequence[] charSequenceArr) {
        super.setEntries(charSequenceArr);
        MaterialDialog materialDialog = this.dialog;
        if (materialDialog != null) {
            materialDialog.setItems(charSequenceArr);
        }
    }

    @Override // android.preference.DialogPreference
    public Dialog getDialog() {
        return this.dialog;
    }

    public RecyclerView getRecyclerView() {
        if (getDialog() == null) {
            return null;
        }
        return ((MaterialDialog) getDialog()).getRecyclerView();
    }

    @Override // android.preference.DialogPreference
    protected void showDialog(Bundle bundle) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
        MaterialDialog.Builder itemsCallbackSingleChoice = new MaterialDialog.Builder(this.context).title(getDialogTitle()).icon(getDialogIcon()).dismissListener(this).onAny(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.prefs.MaterialListPreference.2
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                switch (C06133.$SwitchMap$com$afollestad$materialdialogs$DialogAction[dialogAction.ordinal()]) {
                    case 1:
                        MaterialListPreference.this.onClick(materialDialog, -3);
                        return;
                    case 2:
                        MaterialListPreference.this.onClick(materialDialog, -2);
                        return;
                    default:
                        MaterialListPreference.this.onClick(materialDialog, -1);
                        return;
                }
            }
        }).negativeText(getNegativeButtonText()).items(getEntries()).autoDismiss(true).itemsCallbackSingleChoice(findIndexOfValue(getValue()), new MaterialDialog.ListCallbackSingleChoice() { // from class: com.afollestad.materialdialogs.prefs.MaterialListPreference.1
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
            public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                MaterialListPreference.this.onClick(null, -1);
                if (i >= 0 && MaterialListPreference.this.getEntryValues() != null) {
                    try {
                        Field declaredField = ListPreference.class.getDeclaredField("mClickedDialogEntryIndex");
                        declaredField.setAccessible(true);
                        declaredField.set(MaterialListPreference.this, Integer.valueOf(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        View onCreateDialogView = onCreateDialogView();
        if (onCreateDialogView != null) {
            onBindDialogView(onCreateDialogView);
            itemsCallbackSingleChoice.customView(onCreateDialogView, false);
        } else {
            itemsCallbackSingleChoice.content(getDialogMessage());
        }
        PrefUtil.registerOnActivityDestroyListener(this, this);
        this.dialog = itemsCallbackSingleChoice.build();
        if (bundle != null) {
            this.dialog.onRestoreInstanceState(bundle);
        }
        onClick(this.dialog, -2);
        this.dialog.show();
    }

    /* renamed from: com.afollestad.materialdialogs.prefs.MaterialListPreference$3 */
    /* loaded from: classes.dex */
    static /* synthetic */ class C06133 {
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
        MaterialDialog materialDialog = this.dialog;
        if (materialDialog != null && materialDialog.isShowing()) {
            this.dialog.dismiss();
        }
    }

    @Override // android.preference.ListPreference, android.preference.Preference, android.preference.DialogPreference
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

    @Override // android.preference.ListPreference, android.preference.Preference, android.preference.DialogPreference
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
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.afollestad.materialdialogs.prefs.MaterialListPreference.SavedState.1
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
