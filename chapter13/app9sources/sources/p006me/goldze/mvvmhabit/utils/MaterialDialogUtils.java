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
import p006me.goldze.mvvmhabit.C0933R;

/* renamed from: me.goldze.mvvmhabit.utils.MaterialDialogUtils */
/* loaded from: classes.dex */
public class MaterialDialogUtils {
    public void showThemed(Context context, String str, String str2) {
        new MaterialDialog.Builder(context).title(str).content(str2).positiveText("agree").negativeText("disagree").positiveColorRes(C0933R.C0934color.white).negativeColorRes(C0933R.C0934color.white).titleGravity(GravityEnum.CENTER).titleColorRes(C0933R.C0934color.white).contentColorRes(17170443).backgroundColorRes(C0933R.C0934color.material_blue_grey_800).dividerColorRes(C0933R.C0934color.white).btnSelector(C0933R.C0935drawable.md_selector, DialogAction.POSITIVE).positiveColor(-1).negativeColorAttr(16842810).theme(Theme.DARK).autoDismiss(true).showListener(new DialogInterface.OnShowListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.3
            @Override // android.content.DialogInterface.OnShowListener
            public void onShow(DialogInterface dialogInterface) {
            }
        }).cancelListener(new DialogInterface.OnCancelListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.2
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
            }
        }).dismissListener(new DialogInterface.OnDismissListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.1
            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialogInterface) {
            }
        }).show();
    }

    public static MaterialDialog.Builder showIndeterminateProgressDialog(Context context, String str, boolean z) {
        return new MaterialDialog.Builder(context).title(str).progress(true, 0).progressIndeterminateStyle(z).canceledOnTouchOutside(false).backgroundColorRes(C0933R.C0934color.white).keyListener(new DialogInterface.OnKeyListener() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.4
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                keyEvent.getAction();
                return false;
            }
        });
    }

    public static MaterialDialog.Builder showBasicDialog(Context context, String str) {
        return new MaterialDialog.Builder(context).title(str).positiveText("确定").negativeText("取消");
    }

    public static MaterialDialog.Builder showBasicDialogNoTitle(Context context, String str) {
        return new MaterialDialog.Builder(context).content(str).positiveText("确定").negativeText("取消");
    }

    public static MaterialDialog.Builder showBasicDialogNoCancel(Context context, String str, String str2) {
        return new MaterialDialog.Builder(context).title(str).content(str2).positiveText("确定");
    }

    public static MaterialDialog.Builder showBasicDialog(Context context, String str, String str2) {
        return new MaterialDialog.Builder(context).title(str).content(str2).positiveText("确定").negativeText("取消");
    }

    public static MaterialDialog.Builder showBasicDialogPositive(Context context, String str, String str2) {
        return new MaterialDialog.Builder(context).title(str).content(str2).positiveText("复制").negativeText("取消");
    }

    public static MaterialDialog.Builder getSelectDialog(Context context, String str, String[] strArr) {
        MaterialDialog.Builder negativeText = new MaterialDialog.Builder(context).items(strArr).itemsColor(-12226906).negativeText("取消");
        if (!TextUtils.isEmpty(str)) {
            negativeText.title(str);
        }
        return negativeText;
    }

    public static MaterialDialog.Builder showBasicListDialog(Context context, String str, List list) {
        return new MaterialDialog.Builder(context).title(str).items(list).itemsCallback(new MaterialDialog.ListCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.5
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallback
            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
            }
        }).negativeText("取消");
    }

    public static MaterialDialog.Builder showSingleListDialog(Context context, String str, List list) {
        return new MaterialDialog.Builder(context).title(str).items(list).itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.6
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
            public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                return true;
            }
        }).positiveText("选择");
    }

    public static MaterialDialog.Builder showMultiListDialog(Context context, String str, List list) {
        return new MaterialDialog.Builder(context).title(str).items(list).itemsCallbackMultiChoice(new Integer[]{1, 3}, new MaterialDialog.ListCallbackMultiChoice() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.8
            @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackMultiChoice
            public boolean onSelection(MaterialDialog materialDialog, Integer[] numArr, CharSequence[] charSequenceArr) {
                return true;
            }
        }).onNeutral(new MaterialDialog.SingleButtonCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.7
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.clearSelectedIndices();
            }
        }).alwaysCallMultiChoiceCallback().positiveText(C0933R.string.md_choose_label).autoDismiss(false).neutralText("clear").itemsDisabledIndices(0, 1);
    }

    public static void showCustomDialog(Context context, String str, int i) {
        new MaterialDialog.Builder(context).title(str).customView(i, true).positiveText("确定").negativeText(17039360).onPositive(new MaterialDialog.SingleButtonCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.9
            @Override // com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            }
        }).build();
    }

    public static MaterialDialog.Builder showInputDialog(Context context, String str, String str2) {
        return new MaterialDialog.Builder(context).title(str).content(str2).inputType(8289).positiveText("确定").negativeText("取消").input((CharSequence) "hint", (CharSequence) "prefill", true, (MaterialDialog.InputCallback) new MaterialDialog.InputCallback() { // from class: me.goldze.mvvmhabit.utils.MaterialDialogUtils.10
            @Override // com.afollestad.materialdialogs.MaterialDialog.InputCallback
            public void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence) {
            }
        });
    }
}
