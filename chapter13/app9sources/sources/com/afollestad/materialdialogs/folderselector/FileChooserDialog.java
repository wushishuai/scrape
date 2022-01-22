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
import android.support.p000v4.app.DialogFragment;
import android.support.p000v4.app.Fragment;
import android.support.p000v4.app.FragmentActivity;
import android.support.p003v7.app.AppCompatActivity;
import android.support.v13.app.ActivityCompat;
import android.view.View;
import android.webkit.MimeTypeMap;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.commons.C0592R;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/* loaded from: classes.dex */
public class FileChooserDialog extends DialogFragment implements MaterialDialog.ListCallback {
    private static final String DEFAULT_TAG = "[MD_FILE_SELECTOR]";
    private FileCallback callback;
    private boolean canGoUp = true;
    private File[] parentContents;
    private File parentFolder;

    /* loaded from: classes.dex */
    public interface FileCallback {
        void onFileChooserDismissed(@NonNull FileChooserDialog fileChooserDialog);

        void onFileSelection(@NonNull FileChooserDialog fileChooserDialog, @NonNull File file);
    }

    CharSequence[] getContentsArray() {
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

    File[] listFiles(@Nullable String str, @Nullable String[] strArr) {
        boolean z;
        File[] listFiles = this.parentFolder.listFiles();
        ArrayList arrayList = new ArrayList();
        if (listFiles == null) {
            return null;
        }
        MimeTypeMap singleton = MimeTypeMap.getSingleton();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                arrayList.add(file);
            } else if (strArr != null) {
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        z = false;
                        break;
                    }
                    if (file.getName().toLowerCase().endsWith(strArr[i].toLowerCase())) {
                        z = true;
                        break;
                    }
                    i++;
                }
                if (z) {
                    arrayList.add(file);
                }
            } else if (str != null && fileIsMimeType(file, str, singleton)) {
                arrayList.add(file);
            }
        }
        Collections.sort(arrayList, new FileSorter());
        return (File[]) arrayList.toArray(new File[arrayList.size()]);
    }

    boolean fileIsMimeType(File file, String str, MimeTypeMap mimeTypeMap) {
        int lastIndexOf;
        if (str == null || str.equals("*/*")) {
            return true;
        }
        String uri = file.toURI().toString();
        int lastIndexOf2 = uri.lastIndexOf(46);
        if (lastIndexOf2 == -1) {
            return false;
        }
        String substring = uri.substring(lastIndexOf2 + 1);
        if (substring.endsWith("json")) {
            return str.startsWith("application/json");
        }
        String mimeTypeFromExtension = mimeTypeMap.getMimeTypeFromExtension(substring);
        if (mimeTypeFromExtension == null) {
            return false;
        }
        if (mimeTypeFromExtension.equals(str)) {
            return true;
        }
        int lastIndexOf3 = str.lastIndexOf(47);
        if (lastIndexOf3 == -1) {
            return false;
        }
        String substring2 = str.substring(0, lastIndexOf3);
        if (str.substring(lastIndexOf3 + 1).equals("*") && (lastIndexOf = mimeTypeFromExtension.lastIndexOf(47)) != -1 && mimeTypeFromExtension.substring(0, lastIndexOf).equals(substring2)) {
            return true;
        }
        return false;
    }

    @Override // android.support.p000v4.app.DialogFragment
    @NonNull
    public Dialog onCreateDialog(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            return new MaterialDialog.Builder(getActivity()).title(C0592R.string.md_error_label).content(C0592R.string.md_storage_perm_error).positiveText(17039370).build();
        }
        if (getArguments() == null || !getArguments().containsKey("builder")) {
            throw new IllegalStateException("You must create a FileChooserDialog using the Builder.");
        }
        if (!getArguments().containsKey("current_path")) {
            getArguments().putString("current_path", getBuilder().initialPath);
        }
        this.parentFolder = new File(getArguments().getString("current_path"));
        checkIfCanGoUp();
        this.parentContents = listFiles(getBuilder().mimeType, getBuilder().extensions);
        return new MaterialDialog.Builder(getActivity()).title(this.parentFolder.getAbsolutePath()).typeface(getBuilder().mediumFont, getBuilder().regularFont).items(getContentsArray()).itemsCallback(this).onNegative(new MaterialDialog.SingleButtonCallback() { // from class: com.afollestad.materialdialogs.folderselector.FileChooserDialog.1
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.dismiss();
            }
        }).autoDismiss(false).negativeText(getBuilder().cancelButton).build();
    }

    @Override // android.support.p000v4.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        FileCallback fileCallback = this.callback;
        if (fileCallback != null) {
            fileCallback.onFileChooserDismissed(this);
        }
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
        if (this.parentFolder.isFile()) {
            this.callback.onFileSelection(this, this.parentFolder);
            dismiss();
            return;
        }
        this.parentContents = listFiles(getBuilder().mimeType, getBuilder().extensions);
        MaterialDialog materialDialog2 = (MaterialDialog) getDialog();
        materialDialog2.setTitle(this.parentFolder.getAbsolutePath());
        getArguments().putString("current_path", this.parentFolder.getAbsolutePath());
        materialDialog2.setItems(getContentsArray());
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

    @Override // android.support.p000v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (FileCallback) activity;
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
    public String getInitialPath() {
        return getBuilder().initialPath;
    }

    @NonNull
    private Builder getBuilder() {
        return (Builder) getArguments().getSerializable("builder");
    }

    /* loaded from: classes.dex */
    public static class Builder implements Serializable {
        @NonNull
        final transient AppCompatActivity context;
        String[] extensions;
        @Nullable
        String mediumFont;
        @Nullable
        String regularFont;
        String tag;
        @StringRes
        int cancelButton = 17039360;
        String initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String mimeType = null;
        String goUpLabel = "...";

        public <ActivityType extends AppCompatActivity & FileCallback> Builder(@NonNull ActivityType activitytype) {
            this.context = activitytype;
        }

        @NonNull
        public Builder typeface(@Nullable String str, @Nullable String str2) {
            this.mediumFont = str;
            this.regularFont = str2;
            return this;
        }

        @NonNull
        public Builder cancelButton(@StringRes int i) {
            this.cancelButton = i;
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
        public Builder mimeType(@Nullable String str) {
            this.mimeType = str;
            return this;
        }

        @NonNull
        public Builder extensionsFilter(@Nullable String... strArr) {
            this.extensions = strArr;
            return this;
        }

        @NonNull
        public Builder tag(@Nullable String str) {
            if (str == null) {
                str = FileChooserDialog.DEFAULT_TAG;
            }
            this.tag = str;
            return this;
        }

        @NonNull
        public Builder goUpLabel(String str) {
            this.goUpLabel = str;
            return this;
        }

        @NonNull
        public FileChooserDialog build() {
            FileChooserDialog fileChooserDialog = new FileChooserDialog();
            Bundle bundle = new Bundle();
            bundle.putSerializable("builder", this);
            fileChooserDialog.setArguments(bundle);
            return fileChooserDialog;
        }

        @NonNull
        public FileChooserDialog show() {
            FileChooserDialog build = build();
            build.show(this.context);
            return build;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class FileSorter implements Comparator<File> {
        private FileSorter() {
        }

        public int compare(File file, File file2) {
            if (file.isDirectory() && !file2.isDirectory()) {
                return -1;
            }
            if (file.isDirectory() || !file2.isDirectory()) {
                return file.getName().compareTo(file2.getName());
            }
            return 1;
        }
    }
}
