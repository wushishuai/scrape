package com.afollestad.materialdialogs.folderselector;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.p000v4.app.ActivityCompat;
import android.support.p000v4.app.DialogFragment;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentActivity;
import android.support.p003v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.commons.C0592R;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/* loaded from: classes.dex */
public class FolderChooserDialog extends DialogFragment implements MaterialDialog.ListCallback {
    private static final String DEFAULT_TAG = "[MD_FOLDER_SELECTOR]";
    private FolderCallback callback;
    private boolean canGoUp = false;
    private File[] parentContents;
    private File parentFolder;

    /* loaded from: classes.dex */
    public interface FolderCallback {
        void onFolderChooserDismissed(@NonNull FolderChooserDialog folderChooserDialog);

        void onFolderSelection(@NonNull FolderChooserDialog folderChooserDialog, @NonNull File file);
    }

    String[] getContentsArray() {
        File[] fileArr = this.parentContents;
        if (fileArr == null) {
            return this.canGoUp ? new String[]{getBuilder().goUpLabel} : new String[0];
        }
        int length = fileArr.length;
        boolean z = this.canGoUp;
        String[] strArr = new String[length + (z ? 1 : 0)];
        if (z) {
            strArr[0] = getBuilder().goUpLabel;
        }
        for (int i = 0; i < this.parentContents.length; i++) {
            strArr[this.canGoUp ? i + 1 : i] = this.parentContents[i].getName();
        }
        return strArr;
    }

    File[] listFiles() {
        File[] listFiles = this.parentFolder.listFiles();
        ArrayList arrayList = new ArrayList();
        if (listFiles == null) {
            return null;
        }
        for (File file : listFiles) {
            if (file.isDirectory()) {
                arrayList.add(file);
            }
        }
        Collections.sort(arrayList, new FolderSorter());
        return (File[]) arrayList.toArray(new File[arrayList.size()]);
    }

    @Override // android.support.p000v4.app.DialogFragment
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            return new MaterialDialog.Builder(getActivity()).title(C0592R.string.md_error_label).content(C0592R.string.md_storage_perm_error).positiveText(17039370).build();
        }
        if (getArguments() == null || !getArguments().containsKey("builder")) {
            throw new IllegalStateException("You must create a FolderChooserDialog using the Builder.");
        }
        if (!getArguments().containsKey("current_path")) {
            getArguments().putString("current_path", getBuilder().initialPath);
        }
        this.parentFolder = new File(getArguments().getString("current_path"));
        checkIfCanGoUp();
        this.parentContents = listFiles();
        MaterialDialog.Builder negativeText = new MaterialDialog.Builder(getActivity()).typeface(getBuilder().mediumFont, getBuilder().regularFont).title(this.parentFolder.getAbsolutePath()).items(getContentsArray()).itemsCallback(this).onPositive(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.folderselector.FolderChooserDialog.2
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.dismiss();
                FolderCallback folderCallback = FolderChooserDialog.this.callback;
                FolderChooserDialog folderChooserDialog = FolderChooserDialog.this;
                folderCallback.onFolderSelection(folderChooserDialog, folderChooserDialog.parentFolder);
            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.folderselector.FolderChooserDialog.1
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.dismiss();
            }
        }).autoDismiss(false).positiveText(getBuilder().chooseButton).negativeText(getBuilder().cancelButton);
        if (getBuilder().allowNewFolder) {
            negativeText.neutralText(getBuilder().newFolderButton);
            negativeText.onNeutral(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.folderselector.FolderChooserDialog.3
                @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    FolderChooserDialog.this.createNewFolder();
                }
            });
        }
        if ("/".equals(getBuilder().initialPath)) {
            this.canGoUp = false;
        }
        return negativeText.build();
    }

    @Override // android.support.p000v4.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        FolderCallback folderCallback = this.callback;
        if (folderCallback != null) {
            folderCallback.onFolderChooserDismissed(this);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createNewFolder() {
        new MaterialDialog.Builder(getActivity()).title(getBuilder().newFolderButton).input(0, 0, false, (MaterialDialog.InputCallback) new MaterialDialog.InputCallback() { // from class: com.afollestad.materialdialogs.folderselector.FolderChooserDialog.4
            @Override // com.afollestad.materialdialogs.MaterialDialog.InputCallback
            public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {
                File file = new File(FolderChooserDialog.this.parentFolder, charSequence.toString());
                if (!file.mkdir()) {
                    Toast.makeText(FolderChooserDialog.this.getActivity(), "Unable to create folder " + file.getAbsolutePath() + ", make sure you have the WRITE_EXTERNAL_STORAGE permission or root permissions.", 1).show();
                    return;
                }
                FolderChooserDialog.this.reload();
            }
        }).show();
    }

    @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallback
    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
        boolean z = true;
        if (!this.canGoUp || i != 0) {
            File[] fileArr = this.parentContents;
            if (this.canGoUp) {
                i--;
            }
            this.parentFolder = fileArr[i];
            this.canGoUp = true;
            if (this.parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                this.parentFolder = Environment.getExternalStorageDirectory();
            }
        } else {
            this.parentFolder = this.parentFolder.getParentFile();
            if (this.parentFolder.getAbsolutePath().equals("/storage/emulated")) {
                this.parentFolder = this.parentFolder.getParentFile();
            }
            if (this.parentFolder.getParent() == null) {
                z = false;
            }
            this.canGoUp = z;
        }
        reload();
    }

    private void checkIfCanGoUp() {
        try {
            boolean z = true;
            if (this.parentFolder.getPath().split("/").length <= 1) {
                z = false;
            }
            this.canGoUp = z;
        } catch (IndexOutOfBoundsException unused) {
            this.canGoUp = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void reload() {
        this.parentContents = listFiles();
        MaterialDialog materialDialog = (MaterialDialog) getDialog();
        materialDialog.setTitle(this.parentFolder.getAbsolutePath());
        getArguments().putString("current_path", this.parentFolder.getAbsolutePath());
        materialDialog.setItems(getContentsArray());
    }

    @Override // android.support.p000v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (FolderCallback) activity;
    }

    public void show(FragmentActivity fragmentActivity) {
        String str = getBuilder().tag;
        Fragment findFragmentByTag = fragmentActivity.getSupportFragmentManager().findFragmentByTag(str);
        if (findFragmentByTag != null) {
            ((DialogFragment) findFragmentByTag).dismiss();
            fragmentActivity.getSupportFragmentManager().beginTransaction().remove(findFragmentByTag).commit();
        }
        show(fragmentActivity.getSupportFragmentManager(), str);
    }

    @NonNull
    private Builder getBuilder() {
        return (Builder) getArguments().getSerializable("builder");
    }

    /* loaded from: classes.dex */
    public static class Builder implements Serializable {
        boolean allowNewFolder;
        @NonNull
        final transient AppCompatActivity context;
        @Nullable
        String mediumFont;
        @StringRes
        int newFolderButton;
        @Nullable
        String regularFont;
        String tag;
        @StringRes
        int chooseButton = C0592R.string.md_choose_label;
        @StringRes
        int cancelButton = 17039360;
        String goUpLabel = "...";
        String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        public <ActivityType extends AppCompatActivity & FolderCallback> Builder(@NonNull ActivityType activitytype) {
            this.context = activitytype;
        }

        @NonNull
        public Builder typeface(@Nullable String str, @Nullable String str2) {
            this.mediumFont = str;
            this.regularFont = str2;
            return this;
        }

        @NonNull
        public Builder chooseButton(@StringRes int i) {
            this.chooseButton = i;
            return this;
        }

        @NonNull
        public Builder cancelButton(@StringRes int i) {
            this.cancelButton = i;
            return this;
        }

        @NonNull
        public Builder goUpLabel(String str) {
            this.goUpLabel = str;
            return this;
        }

        @NonNull
        public Builder allowNewFolder(boolean z, @StringRes int i) {
            this.allowNewFolder = z;
            if (i == 0) {
                i = C0592R.string.new_folder;
            }
            this.newFolderButton = i;
            return this;
        }

        @NonNull
        public Builder initialPath(@Nullable String str) {
            if (str == null) {
                str = File.separator;
            }
            this.initialPath = str;
            return this;
        }

        @NonNull
        public Builder tag(@Nullable String str) {
            if (str == null) {
                str = FolderChooserDialog.DEFAULT_TAG;
            }
            this.tag = str;
            return this;
        }

        @NonNull
        public FolderChooserDialog build() {
            FolderChooserDialog folderChooserDialog = new FolderChooserDialog();
            Bundle bundle = new Bundle();
            bundle.putSerializable("builder", this);
            folderChooserDialog.setArguments(bundle);
            return folderChooserDialog;
        }

        @NonNull
        public FolderChooserDialog show() {
            FolderChooserDialog build = build();
            build.show(this.context);
            return build;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class FolderSorter implements Comparator<File> {
        private FolderSorter() {
        }

        public int compare(File file, File file2) {
            return file.getName().compareTo(file2.getName());
        }
    }
}
