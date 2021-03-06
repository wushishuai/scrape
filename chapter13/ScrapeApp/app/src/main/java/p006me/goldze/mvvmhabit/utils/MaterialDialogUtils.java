package p006me.goldze.mvvmhabit.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import java.util.List;
import p006me.goldze.mvvmhabit.C0934R;

/* renamed from: me.goldze.mvvmhabit.utils.MaterialDialogUtils */
/* loaded from: classes.dex */
public class MaterialDialogUtils {
    public void showThemed(Context context, String title, String content) {
        new MaterialDialog.Builder(context).title(title).content(content).positiveText("agree").negativeText("disagree").positiveColorRes(C0934R.C0935color.white).negativeColorRes(C0934R.C0935color.white).titleGravity(GravityEnum.CENTER).titleColorRes(C0934R.C0935color.white).contentColorRes(17170443).backgroundColorRes(C0934R.C0935color.material_blue_grey_800).dividerColorRes(C0934R.C0935color.white).btnSelector(C0934R.C0936drawable.md_selector, DialogAction.POSITIVE).positiveColor(-1).negativeColorAttr(16842810).theme(Theme.DARK).autoDismiss(true).showListener(new DialogInterface.OnShowListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.3
            @Override // android.content.DialogInterface.OnShowListener
            public void onShow(DialogInterface dialog) {
            }
        }).cancelListener(new DialogInterface.OnCancelListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.2
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialog) {
            }
        }).dismissListener(new DialogInterface.OnDismissListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.1
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialog) {
            }
        }).show();
    }

    public static MaterialDialog.Builder showIndeterminateProgressDialog(Context context, String content, boolean horizontal) {
        return new MaterialDialog.Builder(context).title(content).progress(true, 0).progressIndeterminateStyle(horizontal).canceledOnTouchOutside(false).backgroundColorRes(C0934R.C0935color.white).keyListener(new DialogInterface.OnKeyListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.4
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == 0) {
                }
                return false;
            }
        });
    }

    public static MaterialDialog.Builder showBasicDialog(Context context, String content) {
        return new MaterialDialog.Builder(context).title(content).positiveText("??????").negativeText("??????");
    }

    public static MaterialDialog.Builder showBasicDialogNoTitle(Context context, String content) {
        return new MaterialDialog.Builder(context).content(content).positiveText("??????").negativeText("??????");
    }

    public static MaterialDialog.Builder showBasicDialogNoCancel(Context context, String title, String content) {
        return new MaterialDialog.Builder(context).title(title).content(content).positiveText("??????");
    }

    public static MaterialDialog.Builder showBasicDialog(Context context, String title, String content) {
        return new MaterialDialog.Builder(context).title(title).content(content).positiveText("??????").negativeText("??????");
    }

    public static MaterialDialog.Builder showBasicDialogPositive(Context context, String title, String content) {
        return new MaterialDialog.Builder(context).title(title).content(content).positiveText("??????").negativeText("??????");
    }

    public static MaterialDialog.Builder getSelectDialog(Context context, String title, String[] arrays) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context).items(arrays).itemsColor(-12226906).negativeText("??????");
        if (!TextUtils.isEmpty(title)) {
            builder.title(title);
        }
        return builder;
    }

    public static MaterialDialog.Builder showBasicListDialog(Context context, String title, List content) {
        return new MaterialDialog.Builder(context).title(title).items(content).itemsCallback(new MaterialDialog.ListCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.5
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallback
            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
            }
        }).negativeText("??????");
    }

    public static MaterialDialog.Builder showSingleListDialog(Context context, String title, List content) {
        return new MaterialDialog.Builder(context).title(title).items(content).itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.6
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                return true;
            }
        }).positiveText("??????");
    }

    public static MaterialDialog.Builder showMultiListDialog(Context context, String title, List content) {
        return new MaterialDialog.Builder(context).title(title).items(content).itemsCallbackMultiChoice(new Integer[]{1, 3}, new MaterialDialog.ListCallbackMultiChoice() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.8
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackMultiChoice
            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                return true;
            }
        }).onNeutral(new MaterialDialog.SingleButtonCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.7
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.clearSelectedIndices();
            }
        }).alwaysCallMultiChoiceCallback().positiveText(C0934R.string.md_choose_label).autoDismiss(false).neutralText("clear").itemsDisabledIndices(0, 1);
    }

    public static void showCustomDialog(Context context, String title, int content) {
        new MaterialDialog.Builder(context).title(title).customView(content, true).positiveText("??????").negativeText(17039360).onPositive(new MaterialDialog.SingleButtonCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.9
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            }
        }).build();
    }

    public static MaterialDialog.Builder showInputDialog(Context context, String title, String content) {
        return new MaterialDialog.Builder(context).title(title).content(content).inputType(8289).positiveText("??????").negativeText("??????").input((CharSequence) "hint", (CharSequence) "prefill", true, (MaterialDialog.InputCallback) new MaterialDialog.InputCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.10
            @Override // com.afollestad.materialdialogs.MaterialDialog.InputCallback
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            }
        });
    }
}
