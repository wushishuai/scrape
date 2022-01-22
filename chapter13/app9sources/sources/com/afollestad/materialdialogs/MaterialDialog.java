package com.afollestad.materialdialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.p000v4.content.res.ResourcesCompat;
import android.support.p003v7.widget.GridLayoutManager;
import android.support.p003v7.widget.LinearLayoutManager;
import android.support.p003v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import com.afollestad.materialdialogs.DefaultRvAdapter;
import com.afollestad.materialdialogs.internal.MDButton;
import com.afollestad.materialdialogs.internal.MDRootLayout;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.materialdialogs.util.RippleHelper;
import com.afollestad.materialdialogs.util.TypefaceHelper;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public class MaterialDialog extends DialogBase implements View.OnClickListener, DefaultRvAdapter.InternalListCallback {
    protected final Builder builder;
    CheckBox checkBoxPrompt;
    protected TextView content;
    FrameLayout customViewFrame;
    private final Handler handler = new Handler();
    protected ImageView icon;
    EditText input;
    TextView inputMinMax;
    ListType listType;
    MDButton negativeButton;
    MDButton neutralButton;
    MDButton positiveButton;
    ProgressBar progressBar;
    TextView progressLabel;
    TextView progressMinMax;
    RecyclerView recyclerView;
    List<Integer> selectedIndicesList;
    protected TextView title;
    View titleFrame;

    /* loaded from: classes.dex */
    public interface InputCallback {
        void onInput(@NonNull MaterialDialog materialDialog, CharSequence charSequence);
    }

    /* loaded from: classes.dex */
    public interface ListCallback {
        void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence);
    }

    /* loaded from: classes.dex */
    public interface ListCallbackMultiChoice {
        boolean onSelection(MaterialDialog materialDialog, Integer[] numArr, CharSequence[] charSequenceArr);
    }

    /* loaded from: classes.dex */
    public interface ListCallbackSingleChoice {
        boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence);
    }

    /* loaded from: classes.dex */
    public interface ListLongCallback {
        boolean onLongSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence);
    }

    /* loaded from: classes.dex */
    public interface SingleButtonCallback {
        void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction);
    }

    @Override // com.afollestad.materialdialogs.DialogBase, android.app.Dialog
    public /* bridge */ /* synthetic */ View findViewById(int i) {
        return super.findViewById(i);
    }

    @Override // com.afollestad.materialdialogs.DialogBase, android.app.Dialog
    @Deprecated
    public /* bridge */ /* synthetic */ void setContentView(int i) throws IllegalAccessError {
        super.setContentView(i);
    }

    @Override // com.afollestad.materialdialogs.DialogBase, android.app.Dialog
    @Deprecated
    public /* bridge */ /* synthetic */ void setContentView(@NonNull View view) throws IllegalAccessError {
        super.setContentView(view);
    }

    @Override // com.afollestad.materialdialogs.DialogBase, android.app.Dialog
    @Deprecated
    public /* bridge */ /* synthetic */ void setContentView(@NonNull View view, ViewGroup.LayoutParams layoutParams) throws IllegalAccessError {
        super.setContentView(view, layoutParams);
    }

    @SuppressLint({"InflateParams"})
    protected MaterialDialog(Builder builder) {
        super(builder.context, DialogInit.getTheme(builder));
        this.builder = builder;
        this.view = (MDRootLayout) LayoutInflater.from(builder.context).inflate(DialogInit.getInflateLayout(builder), (ViewGroup) null);
        DialogInit.init(this);
    }

    public final Builder getBuilder() {
        return this.builder;
    }

    public final void setTypeface(TextView textView, Typeface typeface) {
        if (typeface != null) {
            textView.setPaintFlags(textView.getPaintFlags() | 128);
            textView.setTypeface(typeface);
        }
    }

    @Nullable
    public Object getTag() {
        return this.builder.tag;
    }

    public final void checkIfListInitScroll() {
        RecyclerView recyclerView = this.recyclerView;
        if (recyclerView != null) {
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.afollestad.materialdialogs.MaterialDialog.1
                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    final int i;
                    if (Build.VERSION.SDK_INT < 16) {
                        MaterialDialog.this.recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        MaterialDialog.this.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    if (MaterialDialog.this.listType == ListType.SINGLE || MaterialDialog.this.listType == ListType.MULTI) {
                        if (MaterialDialog.this.listType == ListType.SINGLE) {
                            if (MaterialDialog.this.builder.selectedIndex >= 0) {
                                i = MaterialDialog.this.builder.selectedIndex;
                            } else {
                                return;
                            }
                        } else if (MaterialDialog.this.selectedIndicesList != null && MaterialDialog.this.selectedIndicesList.size() != 0) {
                            Collections.sort(MaterialDialog.this.selectedIndicesList);
                            i = MaterialDialog.this.selectedIndicesList.get(0).intValue();
                        } else {
                            return;
                        }
                        MaterialDialog.this.recyclerView.post(new Runnable() { // from class: com.afollestad.materialdialogs.MaterialDialog.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                MaterialDialog.this.recyclerView.requestFocus();
                                MaterialDialog.this.builder.layoutManager.scrollToPosition(i);
                            }
                        });
                    }
                }
            });
        }
    }

    public final void invalidateList() {
        if (this.recyclerView != null) {
            if ((this.builder.items != null && this.builder.items.size() != 0) || this.builder.adapter != null) {
                if (this.builder.layoutManager == null) {
                    this.builder.layoutManager = new LinearLayoutManager(getContext());
                }
                this.recyclerView.setLayoutManager(this.builder.layoutManager);
                this.recyclerView.setAdapter(this.builder.adapter);
                if (this.listType != null) {
                    ((DefaultRvAdapter) this.builder.adapter).setCallback(this);
                }
            }
        }
    }

    @Override // com.afollestad.materialdialogs.DefaultRvAdapter.InternalListCallback
    public boolean onItemSelected(MaterialDialog materialDialog, View view, int i, CharSequence charSequence, boolean z) {
        boolean z2 = false;
        if (!view.isEnabled()) {
            return false;
        }
        ListType listType = this.listType;
        if (listType == null || listType == ListType.REGULAR) {
            if (this.builder.autoDismiss) {
                dismiss();
            }
            if (!z && this.builder.listCallback != null) {
                this.builder.listCallback.onSelection(this, view, i, this.builder.items.get(i));
            }
            if (z && this.builder.listLongCallback != null) {
                return this.builder.listLongCallback.onLongSelection(this, view, i, this.builder.items.get(i));
            }
        } else if (this.listType == ListType.MULTI) {
            CheckBox checkBox = (CheckBox) view.findViewById(C0582R.C0585id.md_control);
            if (!checkBox.isEnabled()) {
                return false;
            }
            if (!this.selectedIndicesList.contains(Integer.valueOf(i))) {
                this.selectedIndicesList.add(Integer.valueOf(i));
                if (!this.builder.alwaysCallMultiChoiceCallback) {
                    checkBox.setChecked(true);
                } else if (sendMultiChoiceCallback()) {
                    checkBox.setChecked(true);
                } else {
                    this.selectedIndicesList.remove(Integer.valueOf(i));
                }
            } else {
                this.selectedIndicesList.remove(Integer.valueOf(i));
                if (!this.builder.alwaysCallMultiChoiceCallback) {
                    checkBox.setChecked(false);
                } else if (sendMultiChoiceCallback()) {
                    checkBox.setChecked(false);
                } else {
                    this.selectedIndicesList.add(Integer.valueOf(i));
                }
            }
        } else if (this.listType == ListType.SINGLE) {
            RadioButton radioButton = (RadioButton) view.findViewById(C0582R.C0585id.md_control);
            if (!radioButton.isEnabled()) {
                return false;
            }
            int i2 = this.builder.selectedIndex;
            if (this.builder.autoDismiss && this.builder.positiveText == null) {
                dismiss();
                this.builder.selectedIndex = i;
                sendSingleChoiceCallback(view);
            } else if (this.builder.alwaysCallSingleChoiceCallback) {
                this.builder.selectedIndex = i;
                z2 = sendSingleChoiceCallback(view);
                this.builder.selectedIndex = i2;
            } else {
                z2 = true;
            }
            if (z2) {
                this.builder.selectedIndex = i;
                radioButton.setChecked(true);
                this.builder.adapter.notifyItemChanged(i2);
                this.builder.adapter.notifyItemChanged(i);
            }
        }
        return true;
    }

    public final Drawable getListSelector() {
        if (this.builder.listSelector != 0) {
            return ResourcesCompat.getDrawable(this.builder.context.getResources(), this.builder.listSelector, null);
        }
        Drawable resolveDrawable = DialogUtils.resolveDrawable(this.builder.context, C0582R.attr.md_list_selector);
        if (resolveDrawable != null) {
            return resolveDrawable;
        }
        return DialogUtils.resolveDrawable(getContext(), C0582R.attr.md_list_selector);
    }

    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }

    public boolean isPromptCheckBoxChecked() {
        CheckBox checkBox = this.checkBoxPrompt;
        return checkBox != null && checkBox.isChecked();
    }

    public void setPromptCheckBoxChecked(boolean z) {
        CheckBox checkBox = this.checkBoxPrompt;
        if (checkBox != null) {
            checkBox.setChecked(z);
        }
    }

    public Drawable getButtonSelector(DialogAction dialogAction, boolean z) {
        if (!z) {
            switch (dialogAction) {
                case NEUTRAL:
                    if (this.builder.btnSelectorNeutral != 0) {
                        return ResourcesCompat.getDrawable(this.builder.context.getResources(), this.builder.btnSelectorNeutral, null);
                    }
                    Drawable resolveDrawable = DialogUtils.resolveDrawable(this.builder.context, C0582R.attr.md_btn_neutral_selector);
                    if (resolveDrawable != null) {
                        return resolveDrawable;
                    }
                    Drawable resolveDrawable2 = DialogUtils.resolveDrawable(getContext(), C0582R.attr.md_btn_neutral_selector);
                    if (Build.VERSION.SDK_INT >= 21) {
                        RippleHelper.applyColor(resolveDrawable2, this.builder.buttonRippleColor);
                    }
                    return resolveDrawable2;
                case NEGATIVE:
                    if (this.builder.btnSelectorNegative != 0) {
                        return ResourcesCompat.getDrawable(this.builder.context.getResources(), this.builder.btnSelectorNegative, null);
                    }
                    Drawable resolveDrawable3 = DialogUtils.resolveDrawable(this.builder.context, C0582R.attr.md_btn_negative_selector);
                    if (resolveDrawable3 != null) {
                        return resolveDrawable3;
                    }
                    Drawable resolveDrawable4 = DialogUtils.resolveDrawable(getContext(), C0582R.attr.md_btn_negative_selector);
                    if (Build.VERSION.SDK_INT >= 21) {
                        RippleHelper.applyColor(resolveDrawable4, this.builder.buttonRippleColor);
                    }
                    return resolveDrawable4;
                default:
                    if (this.builder.btnSelectorPositive != 0) {
                        return ResourcesCompat.getDrawable(this.builder.context.getResources(), this.builder.btnSelectorPositive, null);
                    }
                    Drawable resolveDrawable5 = DialogUtils.resolveDrawable(this.builder.context, C0582R.attr.md_btn_positive_selector);
                    if (resolveDrawable5 != null) {
                        return resolveDrawable5;
                    }
                    Drawable resolveDrawable6 = DialogUtils.resolveDrawable(getContext(), C0582R.attr.md_btn_positive_selector);
                    if (Build.VERSION.SDK_INT >= 21) {
                        RippleHelper.applyColor(resolveDrawable6, this.builder.buttonRippleColor);
                    }
                    return resolveDrawable6;
            }
        } else if (this.builder.btnSelectorStacked != 0) {
            return ResourcesCompat.getDrawable(this.builder.context.getResources(), this.builder.btnSelectorStacked, null);
        } else {
            Drawable resolveDrawable7 = DialogUtils.resolveDrawable(this.builder.context, C0582R.attr.md_btn_stacked_selector);
            if (resolveDrawable7 != null) {
                return resolveDrawable7;
            }
            return DialogUtils.resolveDrawable(getContext(), C0582R.attr.md_btn_stacked_selector);
        }
    }

    private boolean sendSingleChoiceCallback(View view) {
        if (this.builder.listCallbackSingleChoice == null) {
            return false;
        }
        CharSequence charSequence = null;
        if (this.builder.selectedIndex >= 0 && this.builder.selectedIndex < this.builder.items.size()) {
            charSequence = this.builder.items.get(this.builder.selectedIndex);
        }
        return this.builder.listCallbackSingleChoice.onSelection(this, view, this.builder.selectedIndex, charSequence);
    }

    private boolean sendMultiChoiceCallback() {
        if (this.builder.listCallbackMultiChoice == null) {
            return false;
        }
        Collections.sort(this.selectedIndicesList);
        ArrayList arrayList = new ArrayList();
        for (Integer num : this.selectedIndicesList) {
            if (num.intValue() >= 0 && num.intValue() <= this.builder.items.size() - 1) {
                arrayList.add(this.builder.items.get(num.intValue()));
            }
        }
        ListCallbackMultiChoice listCallbackMultiChoice = this.builder.listCallbackMultiChoice;
        List<Integer> list = this.selectedIndicesList;
        return listCallbackMultiChoice.onSelection(this, (Integer[]) list.toArray(new Integer[list.size()]), (CharSequence[]) arrayList.toArray(new CharSequence[arrayList.size()]));
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        DialogAction dialogAction = (DialogAction) view.getTag();
        switch (dialogAction) {
            case NEUTRAL:
                if (this.builder.callback != null) {
                    this.builder.callback.onAny(this);
                    this.builder.callback.onNeutral(this);
                }
                if (this.builder.onNeutralCallback != null) {
                    this.builder.onNeutralCallback.onClick(this, dialogAction);
                }
                if (this.builder.autoDismiss) {
                    dismiss();
                    break;
                }
                break;
            case NEGATIVE:
                if (this.builder.callback != null) {
                    this.builder.callback.onAny(this);
                    this.builder.callback.onNegative(this);
                }
                if (this.builder.onNegativeCallback != null) {
                    this.builder.onNegativeCallback.onClick(this, dialogAction);
                }
                if (this.builder.autoDismiss) {
                    cancel();
                    break;
                }
                break;
            case POSITIVE:
                if (this.builder.callback != null) {
                    this.builder.callback.onAny(this);
                    this.builder.callback.onPositive(this);
                }
                if (this.builder.onPositiveCallback != null) {
                    this.builder.onPositiveCallback.onClick(this, dialogAction);
                }
                if (!this.builder.alwaysCallSingleChoiceCallback) {
                    sendSingleChoiceCallback(view);
                }
                if (!this.builder.alwaysCallMultiChoiceCallback) {
                    sendMultiChoiceCallback();
                }
                if (!(this.builder.inputCallback == null || this.input == null || this.builder.alwaysCallInputCallback)) {
                    this.builder.inputCallback.onInput(this, this.input.getText());
                }
                if (this.builder.autoDismiss) {
                    dismiss();
                    break;
                }
                break;
        }
        if (this.builder.onAnyCallback != null) {
            this.builder.onAnyCallback.onClick(this, dialogAction);
        }
    }

    @Override // android.app.Dialog
    @UiThread
    public void show() {
        try {
            super.show();
        } catch (WindowManager.BadTokenException unused) {
            throw new DialogException("Bad window token, you cannot show a dialog before an Activity is created or after it's hidden.");
        }
    }

    public final MDButton getActionButton(@NonNull DialogAction dialogAction) {
        switch (dialogAction) {
            case NEUTRAL:
                return this.neutralButton;
            case NEGATIVE:
                return this.negativeButton;
            default:
                return this.positiveButton;
        }
    }

    public final View getView() {
        return this.view;
    }

    @Nullable
    public final EditText getInputEditText() {
        return this.input;
    }

    public final TextView getTitleView() {
        return this.title;
    }

    public ImageView getIconView() {
        return this.icon;
    }

    @Nullable
    public final TextView getContentView() {
        return this.content;
    }

    @Nullable
    public final View getCustomView() {
        return this.builder.customView;
    }

    @UiThread
    public final void setActionButton(@NonNull DialogAction dialogAction, CharSequence charSequence) {
        int i = 8;
        switch (dialogAction) {
            case NEUTRAL:
                this.builder.neutralText = charSequence;
                this.neutralButton.setText(charSequence);
                MDButton mDButton = this.neutralButton;
                if (charSequence != null) {
                    i = 0;
                }
                mDButton.setVisibility(i);
                return;
            case NEGATIVE:
                this.builder.negativeText = charSequence;
                this.negativeButton.setText(charSequence);
                MDButton mDButton2 = this.negativeButton;
                if (charSequence != null) {
                    i = 0;
                }
                mDButton2.setVisibility(i);
                return;
            default:
                this.builder.positiveText = charSequence;
                this.positiveButton.setText(charSequence);
                MDButton mDButton3 = this.positiveButton;
                if (charSequence != null) {
                    i = 0;
                }
                mDButton3.setVisibility(i);
                return;
        }
    }

    public final void setActionButton(DialogAction dialogAction, @StringRes int i) {
        setActionButton(dialogAction, getContext().getText(i));
    }

    public final boolean hasActionButtons() {
        return numberOfActionButtons() > 0;
    }

    public final int numberOfActionButtons() {
        int i = (this.builder.positiveText == null || this.positiveButton.getVisibility() != 0) ? 0 : 1;
        if (this.builder.neutralText != null && this.neutralButton.getVisibility() == 0) {
            i++;
        }
        return (this.builder.negativeText == null || this.negativeButton.getVisibility() != 0) ? i : i + 1;
    }

    @Override // android.app.Dialog
    @UiThread
    public final void setTitle(CharSequence charSequence) {
        this.title.setText(charSequence);
    }

    @Override // android.app.Dialog
    @UiThread
    public final void setTitle(@StringRes int i) {
        setTitle(this.builder.context.getString(i));
    }

    @UiThread
    public final void setTitle(@StringRes int i, @Nullable Object... objArr) {
        setTitle(this.builder.context.getString(i, objArr));
    }

    @UiThread
    public void setIcon(@DrawableRes int i) {
        this.icon.setImageResource(i);
        this.icon.setVisibility(i != 0 ? 0 : 8);
    }

    @UiThread
    public void setIcon(Drawable drawable) {
        this.icon.setImageDrawable(drawable);
        this.icon.setVisibility(drawable != null ? 0 : 8);
    }

    @UiThread
    public void setIconAttribute(@AttrRes int i) {
        setIcon(DialogUtils.resolveDrawable(this.builder.context, i));
    }

    @UiThread
    public final void setContent(CharSequence charSequence) {
        this.content.setText(charSequence);
        this.content.setVisibility(TextUtils.isEmpty(charSequence) ? 8 : 0);
    }

    @UiThread
    public final void setContent(@StringRes int i) {
        setContent(this.builder.context.getString(i));
    }

    @UiThread
    public final void setContent(@StringRes int i, @Nullable Object... objArr) {
        setContent(this.builder.context.getString(i, objArr));
    }

    @Nullable
    public final ArrayList<CharSequence> getItems() {
        return this.builder.items;
    }

    @UiThread
    public final void setItems(CharSequence... charSequenceArr) {
        if (this.builder.adapter != null) {
            if (charSequenceArr != null) {
                this.builder.items = new ArrayList<>(charSequenceArr.length);
                Collections.addAll(this.builder.items, charSequenceArr);
            } else {
                this.builder.items = null;
            }
            if (this.builder.adapter instanceof DefaultRvAdapter) {
                notifyItemsChanged();
                return;
            }
            throw new IllegalStateException("When using a custom adapter, setItems() cannot be used. Set items through the adapter instead.");
        }
        throw new IllegalStateException("This MaterialDialog instance does not yet have an adapter set to it. You cannot use setItems().");
    }

    @UiThread
    public final void notifyItemInserted(@IntRange(from = 0, m54to = 2147483647L) int i) {
        this.builder.adapter.notifyItemInserted(i);
    }

    @UiThread
    public final void notifyItemChanged(@IntRange(from = 0, m54to = 2147483647L) int i) {
        this.builder.adapter.notifyItemChanged(i);
    }

    @UiThread
    public final void notifyItemsChanged() {
        this.builder.adapter.notifyDataSetChanged();
    }

    public final int getCurrentProgress() {
        ProgressBar progressBar = this.progressBar;
        if (progressBar == null) {
            return -1;
        }
        return progressBar.getProgress();
    }

    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    public final void incrementProgress(int i) {
        setProgress(getCurrentProgress() + i);
    }

    public final void setProgress(int i) {
        if (this.builder.progress <= -2) {
            Log.w("MaterialDialog", "Calling setProgress(int) on an indeterminate progress dialog has no effect!");
            return;
        }
        this.progressBar.setProgress(i);
        this.handler.post(new Runnable() { // from class: com.afollestad.materialdialogs.MaterialDialog.2
            @Override // java.lang.Runnable
            public void run() {
                if (MaterialDialog.this.progressLabel != null) {
                    MaterialDialog.this.progressLabel.setText(MaterialDialog.this.builder.progressPercentFormat.format((double) (((float) MaterialDialog.this.getCurrentProgress()) / ((float) MaterialDialog.this.getMaxProgress()))));
                }
                if (MaterialDialog.this.progressMinMax != null) {
                    MaterialDialog.this.progressMinMax.setText(String.format(MaterialDialog.this.builder.progressNumberFormat, Integer.valueOf(MaterialDialog.this.getCurrentProgress()), Integer.valueOf(MaterialDialog.this.getMaxProgress())));
                }
            }
        });
    }

    public final boolean isIndeterminateProgress() {
        return this.builder.indeterminateProgress;
    }

    public final int getMaxProgress() {
        ProgressBar progressBar = this.progressBar;
        if (progressBar == null) {
            return -1;
        }
        return progressBar.getMax();
    }

    public final void setMaxProgress(int i) {
        if (this.builder.progress > -2) {
            this.progressBar.setMax(i);
            return;
        }
        throw new IllegalStateException("Cannot use setMaxProgress() on this dialog.");
    }

    public final void setProgressPercentFormat(NumberFormat numberFormat) {
        this.builder.progressPercentFormat = numberFormat;
        setProgress(getCurrentProgress());
    }

    public final void setProgressNumberFormat(String str) {
        this.builder.progressNumberFormat = str;
        setProgress(getCurrentProgress());
    }

    public final boolean isCancelled() {
        return !isShowing();
    }

    public int getSelectedIndex() {
        if (this.builder.listCallbackSingleChoice != null) {
            return this.builder.selectedIndex;
        }
        return -1;
    }

    @UiThread
    public void setSelectedIndex(int i) {
        Builder builder = this.builder;
        builder.selectedIndex = i;
        if (builder.adapter == null || !(this.builder.adapter instanceof DefaultRvAdapter)) {
            throw new IllegalStateException("You can only use setSelectedIndex() with the default adapter implementation.");
        }
        this.builder.adapter.notifyDataSetChanged();
    }

    @Nullable
    public Integer[] getSelectedIndices() {
        if (this.builder.listCallbackMultiChoice == null) {
            return null;
        }
        List<Integer> list = this.selectedIndicesList;
        return (Integer[]) list.toArray(new Integer[list.size()]);
    }

    @UiThread
    public void setSelectedIndices(@NonNull Integer[] numArr) {
        this.selectedIndicesList = new ArrayList(Arrays.asList(numArr));
        if (this.builder.adapter == null || !(this.builder.adapter instanceof DefaultRvAdapter)) {
            throw new IllegalStateException("You can only use setSelectedIndices() with the default adapter implementation.");
        }
        this.builder.adapter.notifyDataSetChanged();
    }

    public void clearSelectedIndices() {
        clearSelectedIndices(true);
    }

    public void clearSelectedIndices(boolean z) {
        ListType listType = this.listType;
        if (listType == null || listType != ListType.MULTI) {
            throw new IllegalStateException("You can only use clearSelectedIndices() with multi choice list dialogs.");
        } else if (this.builder.adapter == null || !(this.builder.adapter instanceof DefaultRvAdapter)) {
            throw new IllegalStateException("You can only use clearSelectedIndices() with the default adapter implementation.");
        } else {
            List<Integer> list = this.selectedIndicesList;
            if (list != null) {
                list.clear();
            }
            this.builder.adapter.notifyDataSetChanged();
            if (z && this.builder.listCallbackMultiChoice != null) {
                sendMultiChoiceCallback();
            }
        }
    }

    public void selectAllIndices() {
        selectAllIndices(true);
    }

    public void selectAllIndices(boolean z) {
        ListType listType = this.listType;
        if (listType == null || listType != ListType.MULTI) {
            throw new IllegalStateException("You can only use selectAllIndices() with multi choice list dialogs.");
        } else if (this.builder.adapter == null || !(this.builder.adapter instanceof DefaultRvAdapter)) {
            throw new IllegalStateException("You can only use selectAllIndices() with the default adapter implementation.");
        } else {
            if (this.selectedIndicesList == null) {
                this.selectedIndicesList = new ArrayList();
            }
            for (int i = 0; i < this.builder.adapter.getItemCount(); i++) {
                if (!this.selectedIndicesList.contains(Integer.valueOf(i))) {
                    this.selectedIndicesList.add(Integer.valueOf(i));
                }
            }
            this.builder.adapter.notifyDataSetChanged();
            if (z && this.builder.listCallbackMultiChoice != null) {
                sendMultiChoiceCallback();
            }
        }
    }

    @Override // com.afollestad.materialdialogs.DialogBase, android.content.DialogInterface.OnShowListener
    public final void onShow(DialogInterface dialogInterface) {
        if (this.input != null) {
            DialogUtils.showKeyboard(this, this.builder);
            if (this.input.getText().length() > 0) {
                EditText editText = this.input;
                editText.setSelection(editText.getText().length());
            }
        }
        super.onShow(dialogInterface);
    }

    public void setInternalInputCallback() {
        EditText editText = this.input;
        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() { // from class: com.afollestad.materialdialogs.MaterialDialog.3
                @Override // android.text.TextWatcher
                public void afterTextChanged(Editable editable) {
                }

                @Override // android.text.TextWatcher
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override // android.text.TextWatcher
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    int length = charSequence.toString().length();
                    boolean z = false;
                    if (!MaterialDialog.this.builder.inputAllowEmpty) {
                        if (length == 0) {
                            z = true;
                        }
                        MaterialDialog.this.getActionButton(DialogAction.POSITIVE).setEnabled(!z);
                    }
                    MaterialDialog.this.invalidateInputMinMaxIndicator(length, z);
                    if (MaterialDialog.this.builder.alwaysCallInputCallback) {
                        MaterialDialog.this.builder.inputCallback.onInput(MaterialDialog.this, charSequence);
                    }
                }
            });
        }
    }

    public void invalidateInputMinMaxIndicator(int i, boolean z) {
        if (this.inputMinMax != null) {
            boolean z2 = false;
            if (this.builder.inputMaxLength > 0) {
                this.inputMinMax.setText(String.format(Locale.getDefault(), "%d/%d", Integer.valueOf(i), Integer.valueOf(this.builder.inputMaxLength)));
                this.inputMinMax.setVisibility(0);
            } else {
                this.inputMinMax.setVisibility(8);
            }
            if ((z && i == 0) || ((this.builder.inputMaxLength > 0 && i > this.builder.inputMaxLength) || i < this.builder.inputMinLength)) {
                z2 = true;
            }
            int i2 = z2 ? this.builder.inputRangeErrorColor : this.builder.contentColor;
            int i3 = z2 ? this.builder.inputRangeErrorColor : this.builder.widgetColor;
            if (this.builder.inputMaxLength > 0) {
                this.inputMinMax.setTextColor(i2);
            }
            MDTintHelper.setTint(this.input, i3);
            getActionButton(DialogAction.POSITIVE).setEnabled(!z2);
        }
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        if (this.input != null) {
            DialogUtils.hideKeyboard(this, this.builder);
        }
        super.dismiss();
    }

    /* loaded from: classes.dex */
    public enum ListType {
        REGULAR,
        SINGLE,
        MULTI;

        public static int getLayoutForType(ListType listType) {
            switch (listType) {
                case REGULAR:
                    return C0582R.layout.md_listitem;
                case SINGLE:
                    return C0582R.layout.md_listitem_singlechoice;
                case MULTI:
                    return C0582R.layout.md_listitem_multichoice;
                default:
                    throw new IllegalArgumentException("Not a valid list type");
            }
        }
    }

    /* loaded from: classes.dex */
    public static class DialogException extends WindowManager.BadTokenException {
        DialogException(String str) {
            super(str);
        }
    }

    /* loaded from: classes.dex */
    public static class Builder {
        protected RecyclerView.Adapter<?> adapter;
        protected boolean alwaysCallInputCallback;
        protected int backgroundColor;
        @DrawableRes
        protected int btnSelectorNegative;
        @DrawableRes
        protected int btnSelectorNeutral;
        @DrawableRes
        protected int btnSelectorPositive;
        @DrawableRes
        protected int btnSelectorStacked;
        protected GravityEnum btnStackedGravity;
        protected int buttonRippleColor;
        protected GravityEnum buttonsGravity;
        protected ButtonCallback callback;
        protected DialogInterface.OnCancelListener cancelListener;
        protected CharSequence checkBoxPrompt;
        protected boolean checkBoxPromptInitiallyChecked;
        protected CompoundButton.OnCheckedChangeListener checkBoxPromptListener;
        protected ColorStateList choiceWidgetColor;
        protected CharSequence content;
        protected GravityEnum contentGravity;
        protected final Context context;
        protected View customView;
        protected DialogInterface.OnDismissListener dismissListener;
        protected int dividerColor;
        protected Drawable icon;
        protected boolean indeterminateIsHorizontalProgress;
        protected boolean indeterminateProgress;
        protected boolean inputAllowEmpty;
        protected InputCallback inputCallback;
        protected CharSequence inputHint;
        protected CharSequence inputPrefill;
        protected int itemColor;
        protected int[] itemIds;
        protected ArrayList<CharSequence> items;
        protected GravityEnum itemsGravity;
        protected DialogInterface.OnKeyListener keyListener;
        protected RecyclerView.LayoutManager layoutManager;
        protected boolean limitIconToDefaultSize;
        protected ColorStateList linkColor;
        protected ListCallback listCallback;
        protected ListCallbackMultiChoice listCallbackMultiChoice;
        protected ListCallbackSingleChoice listCallbackSingleChoice;
        protected ListLongCallback listLongCallback;
        @DrawableRes
        protected int listSelector;
        protected Typeface mediumFont;
        protected ColorStateList negativeColor;
        protected boolean negativeFocus;
        protected CharSequence negativeText;
        protected ColorStateList neutralColor;
        protected boolean neutralFocus;
        protected CharSequence neutralText;
        protected SingleButtonCallback onAnyCallback;
        protected SingleButtonCallback onNegativeCallback;
        protected SingleButtonCallback onNeutralCallback;
        protected SingleButtonCallback onPositiveCallback;
        protected ColorStateList positiveColor;
        protected boolean positiveFocus;
        protected CharSequence positiveText;
        protected String progressNumberFormat;
        protected NumberFormat progressPercentFormat;
        protected Typeface regularFont;
        protected DialogInterface.OnShowListener showListener;
        protected boolean showMinMax;
        protected StackingBehavior stackingBehavior;
        protected Object tag;
        protected Theme theme;
        protected CharSequence title;
        protected GravityEnum titleGravity;
        protected int widgetColor;
        protected boolean wrapCustomViewInScroll;
        protected int titleColor = -1;
        protected int contentColor = -1;
        protected boolean alwaysCallMultiChoiceCallback = false;
        protected boolean alwaysCallSingleChoiceCallback = false;
        protected boolean cancelable = true;
        protected boolean canceledOnTouchOutside = true;
        protected float contentLineSpacingMultiplier = 1.2f;
        protected int selectedIndex = -1;
        protected Integer[] selectedIndices = null;
        protected Integer[] disabledIndices = null;
        protected boolean autoDismiss = true;
        protected int maxIconSize = -1;
        protected int progress = -2;
        protected int progressMax = 0;
        protected int inputType = -1;
        protected int inputMinLength = -1;
        protected int inputMaxLength = -1;
        protected int inputRangeErrorColor = 0;
        protected boolean titleColorSet = false;
        protected boolean contentColorSet = false;
        protected boolean itemColorSet = false;
        protected boolean positiveColorSet = false;
        protected boolean neutralColorSet = false;
        protected boolean negativeColorSet = false;
        protected boolean widgetColorSet = false;
        protected boolean dividerColorSet = false;

        public Builder(@NonNull Context context) {
            this.titleGravity = GravityEnum.START;
            this.contentGravity = GravityEnum.START;
            this.btnStackedGravity = GravityEnum.END;
            this.itemsGravity = GravityEnum.START;
            this.buttonsGravity = GravityEnum.START;
            this.buttonRippleColor = 0;
            this.theme = Theme.LIGHT;
            this.context = context;
            this.widgetColor = DialogUtils.resolveColor(context, C0582R.attr.colorAccent, DialogUtils.getColor(context, C0582R.C0583color.md_material_blue_600));
            if (Build.VERSION.SDK_INT >= 21) {
                this.widgetColor = DialogUtils.resolveColor(context, 16843829, this.widgetColor);
            }
            this.positiveColor = DialogUtils.getActionTextStateList(context, this.widgetColor);
            this.negativeColor = DialogUtils.getActionTextStateList(context, this.widgetColor);
            this.neutralColor = DialogUtils.getActionTextStateList(context, this.widgetColor);
            this.linkColor = DialogUtils.getActionTextStateList(context, DialogUtils.resolveColor(context, C0582R.attr.md_link_color, this.widgetColor));
            this.buttonRippleColor = DialogUtils.resolveColor(context, C0582R.attr.md_btn_ripple_color, DialogUtils.resolveColor(context, C0582R.attr.colorControlHighlight, Build.VERSION.SDK_INT >= 21 ? DialogUtils.resolveColor(context, 16843820) : 0));
            this.progressPercentFormat = NumberFormat.getPercentInstance();
            this.progressNumberFormat = "%1d/%2d";
            this.theme = DialogUtils.isColorDark(DialogUtils.resolveColor(context, 16842806)) ? Theme.LIGHT : Theme.DARK;
            checkSingleton();
            this.titleGravity = DialogUtils.resolveGravityEnum(context, C0582R.attr.md_title_gravity, this.titleGravity);
            this.contentGravity = DialogUtils.resolveGravityEnum(context, C0582R.attr.md_content_gravity, this.contentGravity);
            this.btnStackedGravity = DialogUtils.resolveGravityEnum(context, C0582R.attr.md_btnstacked_gravity, this.btnStackedGravity);
            this.itemsGravity = DialogUtils.resolveGravityEnum(context, C0582R.attr.md_items_gravity, this.itemsGravity);
            this.buttonsGravity = DialogUtils.resolveGravityEnum(context, C0582R.attr.md_buttons_gravity, this.buttonsGravity);
            try {
                typeface(DialogUtils.resolveString(context, C0582R.attr.md_medium_font), DialogUtils.resolveString(context, C0582R.attr.md_regular_font));
            } catch (Throwable unused) {
            }
            if (this.mediumFont == null) {
                try {
                    if (Build.VERSION.SDK_INT >= 21) {
                        this.mediumFont = Typeface.create("sans-serif-medium", 0);
                    } else {
                        this.mediumFont = Typeface.create("sans-serif", 1);
                    }
                } catch (Throwable unused2) {
                    this.mediumFont = Typeface.DEFAULT_BOLD;
                }
            }
            if (this.regularFont == null) {
                try {
                    this.regularFont = Typeface.create("sans-serif", 0);
                } catch (Throwable unused3) {
                    this.regularFont = Typeface.SANS_SERIF;
                    if (this.regularFont == null) {
                        this.regularFont = Typeface.DEFAULT;
                    }
                }
            }
        }

        public final Context getContext() {
            return this.context;
        }

        public final int getItemColor() {
            return this.itemColor;
        }

        public final Typeface getRegularFont() {
            return this.regularFont;
        }

        private void checkSingleton() {
            if (ThemeSingleton.get(false) != null) {
                ThemeSingleton themeSingleton = ThemeSingleton.get();
                if (themeSingleton.darkTheme) {
                    this.theme = Theme.DARK;
                }
                if (themeSingleton.titleColor != 0) {
                    this.titleColor = themeSingleton.titleColor;
                }
                if (themeSingleton.contentColor != 0) {
                    this.contentColor = themeSingleton.contentColor;
                }
                if (themeSingleton.positiveColor != null) {
                    this.positiveColor = themeSingleton.positiveColor;
                }
                if (themeSingleton.neutralColor != null) {
                    this.neutralColor = themeSingleton.neutralColor;
                }
                if (themeSingleton.negativeColor != null) {
                    this.negativeColor = themeSingleton.negativeColor;
                }
                if (themeSingleton.itemColor != 0) {
                    this.itemColor = themeSingleton.itemColor;
                }
                if (themeSingleton.icon != null) {
                    this.icon = themeSingleton.icon;
                }
                if (themeSingleton.backgroundColor != 0) {
                    this.backgroundColor = themeSingleton.backgroundColor;
                }
                if (themeSingleton.dividerColor != 0) {
                    this.dividerColor = themeSingleton.dividerColor;
                }
                if (themeSingleton.btnSelectorStacked != 0) {
                    this.btnSelectorStacked = themeSingleton.btnSelectorStacked;
                }
                if (themeSingleton.listSelector != 0) {
                    this.listSelector = themeSingleton.listSelector;
                }
                if (themeSingleton.btnSelectorPositive != 0) {
                    this.btnSelectorPositive = themeSingleton.btnSelectorPositive;
                }
                if (themeSingleton.btnSelectorNeutral != 0) {
                    this.btnSelectorNeutral = themeSingleton.btnSelectorNeutral;
                }
                if (themeSingleton.btnSelectorNegative != 0) {
                    this.btnSelectorNegative = themeSingleton.btnSelectorNegative;
                }
                if (themeSingleton.widgetColor != 0) {
                    this.widgetColor = themeSingleton.widgetColor;
                }
                if (themeSingleton.linkColor != null) {
                    this.linkColor = themeSingleton.linkColor;
                }
                this.titleGravity = themeSingleton.titleGravity;
                this.contentGravity = themeSingleton.contentGravity;
                this.btnStackedGravity = themeSingleton.btnStackedGravity;
                this.itemsGravity = themeSingleton.itemsGravity;
                this.buttonsGravity = themeSingleton.buttonsGravity;
            }
        }

        public Builder title(@StringRes int i) {
            title(this.context.getText(i));
            return this;
        }

        public Builder title(@NonNull CharSequence charSequence) {
            this.title = charSequence;
            return this;
        }

        public Builder titleGravity(@NonNull GravityEnum gravityEnum) {
            this.titleGravity = gravityEnum;
            return this;
        }

        public Builder buttonRippleColor(@ColorInt int i) {
            this.buttonRippleColor = i;
            return this;
        }

        public Builder buttonRippleColorRes(@ColorRes int i) {
            return buttonRippleColor(DialogUtils.getColor(this.context, i));
        }

        public Builder buttonRippleColorAttr(@AttrRes int i) {
            return buttonRippleColor(DialogUtils.resolveColor(this.context, i));
        }

        public Builder titleColor(@ColorInt int i) {
            this.titleColor = i;
            this.titleColorSet = true;
            return this;
        }

        public Builder titleColorRes(@ColorRes int i) {
            return titleColor(DialogUtils.getColor(this.context, i));
        }

        public Builder titleColorAttr(@AttrRes int i) {
            return titleColor(DialogUtils.resolveColor(this.context, i));
        }

        public Builder typeface(@Nullable Typeface typeface, @Nullable Typeface typeface2) {
            this.mediumFont = typeface;
            this.regularFont = typeface2;
            return this;
        }

        public Builder typeface(@Nullable String str, @Nullable String str2) {
            if (str != null && !str.trim().isEmpty()) {
                this.mediumFont = TypefaceHelper.get(this.context, str);
                if (this.mediumFont == null) {
                    throw new IllegalArgumentException("No font asset found for \"" + str + "\"");
                }
            }
            if (str2 != null && !str2.trim().isEmpty()) {
                this.regularFont = TypefaceHelper.get(this.context, str2);
                if (this.regularFont == null) {
                    throw new IllegalArgumentException("No font asset found for \"" + str2 + "\"");
                }
            }
            return this;
        }

        public Builder icon(@NonNull Drawable drawable) {
            this.icon = drawable;
            return this;
        }

        public Builder iconRes(@DrawableRes int i) {
            this.icon = ResourcesCompat.getDrawable(this.context.getResources(), i, null);
            return this;
        }

        public Builder iconAttr(@AttrRes int i) {
            this.icon = DialogUtils.resolveDrawable(this.context, i);
            return this;
        }

        public Builder content(@StringRes int i) {
            return content(i, false);
        }

        public Builder content(@StringRes int i, boolean z) {
            CharSequence text = this.context.getText(i);
            if (z) {
                text = Html.fromHtml(text.toString().replace("\n", "<br/>"));
            }
            return content(text);
        }

        public Builder content(@NonNull CharSequence charSequence) {
            if (this.customView == null) {
                this.content = charSequence;
                return this;
            }
            throw new IllegalStateException("You cannot set content() when you're using a custom view.");
        }

        public Builder content(@StringRes int i, Object... objArr) {
            return content(Html.fromHtml(String.format(this.context.getString(i), objArr).replace("\n", "<br/>")));
        }

        public Builder contentColor(@ColorInt int i) {
            this.contentColor = i;
            this.contentColorSet = true;
            return this;
        }

        public Builder contentColorRes(@ColorRes int i) {
            contentColor(DialogUtils.getColor(this.context, i));
            return this;
        }

        public Builder contentColorAttr(@AttrRes int i) {
            contentColor(DialogUtils.resolveColor(this.context, i));
            return this;
        }

        public Builder contentGravity(@NonNull GravityEnum gravityEnum) {
            this.contentGravity = gravityEnum;
            return this;
        }

        public Builder contentLineSpacing(float f) {
            this.contentLineSpacingMultiplier = f;
            return this;
        }

        public Builder items(@NonNull Collection collection) {
            if (collection.size() > 0) {
                CharSequence[] charSequenceArr = new CharSequence[collection.size()];
                int i = 0;
                for (Object obj : collection) {
                    charSequenceArr[i] = obj.toString();
                    i++;
                }
                items(charSequenceArr);
            } else if (collection.size() == 0) {
                this.items = new ArrayList<>();
            }
            return this;
        }

        public Builder items(@ArrayRes int i) {
            items(this.context.getResources().getTextArray(i));
            return this;
        }

        public Builder items(@NonNull CharSequence... charSequenceArr) {
            if (this.customView == null) {
                this.items = new ArrayList<>();
                Collections.addAll(this.items, charSequenceArr);
                return this;
            }
            throw new IllegalStateException("You cannot set items() when you're using a custom view.");
        }

        public Builder itemsCallback(@NonNull ListCallback listCallback) {
            this.listCallback = listCallback;
            this.listCallbackSingleChoice = null;
            this.listCallbackMultiChoice = null;
            return this;
        }

        public Builder itemsLongCallback(@NonNull ListLongCallback listLongCallback) {
            this.listLongCallback = listLongCallback;
            this.listCallbackSingleChoice = null;
            this.listCallbackMultiChoice = null;
            return this;
        }

        public Builder itemsColor(@ColorInt int i) {
            this.itemColor = i;
            this.itemColorSet = true;
            return this;
        }

        public Builder itemsColorRes(@ColorRes int i) {
            return itemsColor(DialogUtils.getColor(this.context, i));
        }

        public Builder itemsColorAttr(@AttrRes int i) {
            return itemsColor(DialogUtils.resolveColor(this.context, i));
        }

        public Builder itemsGravity(@NonNull GravityEnum gravityEnum) {
            this.itemsGravity = gravityEnum;
            return this;
        }

        public Builder itemsIds(@NonNull int[] iArr) {
            this.itemIds = iArr;
            return this;
        }

        public Builder itemsIds(@ArrayRes int i) {
            return itemsIds(this.context.getResources().getIntArray(i));
        }

        public Builder buttonsGravity(@NonNull GravityEnum gravityEnum) {
            this.buttonsGravity = gravityEnum;
            return this;
        }

        public Builder itemsCallbackSingleChoice(int i, @NonNull ListCallbackSingleChoice listCallbackSingleChoice) {
            this.selectedIndex = i;
            this.listCallback = null;
            this.listCallbackSingleChoice = listCallbackSingleChoice;
            this.listCallbackMultiChoice = null;
            return this;
        }

        public Builder alwaysCallSingleChoiceCallback() {
            this.alwaysCallSingleChoiceCallback = true;
            return this;
        }

        public Builder itemsCallbackMultiChoice(@Nullable Integer[] numArr, @NonNull ListCallbackMultiChoice listCallbackMultiChoice) {
            this.selectedIndices = numArr;
            this.listCallback = null;
            this.listCallbackSingleChoice = null;
            this.listCallbackMultiChoice = listCallbackMultiChoice;
            return this;
        }

        public Builder itemsDisabledIndices(@Nullable Integer... numArr) {
            this.disabledIndices = numArr;
            return this;
        }

        public Builder alwaysCallMultiChoiceCallback() {
            this.alwaysCallMultiChoiceCallback = true;
            return this;
        }

        public Builder positiveText(@StringRes int i) {
            if (i == 0) {
                return this;
            }
            positiveText(this.context.getText(i));
            return this;
        }

        public Builder positiveText(@NonNull CharSequence charSequence) {
            this.positiveText = charSequence;
            return this;
        }

        public Builder positiveColor(@ColorInt int i) {
            return positiveColor(DialogUtils.getActionTextStateList(this.context, i));
        }

        public Builder positiveColorRes(@ColorRes int i) {
            return positiveColor(DialogUtils.getActionTextColorStateList(this.context, i));
        }

        public Builder positiveColorAttr(@AttrRes int i) {
            return positiveColor(DialogUtils.resolveActionTextColorStateList(this.context, i, null));
        }

        public Builder positiveColor(@NonNull ColorStateList colorStateList) {
            this.positiveColor = colorStateList;
            this.positiveColorSet = true;
            return this;
        }

        public Builder positiveFocus(boolean z) {
            this.positiveFocus = z;
            return this;
        }

        public Builder neutralText(@StringRes int i) {
            return i == 0 ? this : neutralText(this.context.getText(i));
        }

        public Builder neutralText(@NonNull CharSequence charSequence) {
            this.neutralText = charSequence;
            return this;
        }

        public Builder negativeColor(@ColorInt int i) {
            return negativeColor(DialogUtils.getActionTextStateList(this.context, i));
        }

        public Builder negativeColorRes(@ColorRes int i) {
            return negativeColor(DialogUtils.getActionTextColorStateList(this.context, i));
        }

        public Builder negativeColorAttr(@AttrRes int i) {
            return negativeColor(DialogUtils.resolveActionTextColorStateList(this.context, i, null));
        }

        public Builder negativeColor(@NonNull ColorStateList colorStateList) {
            this.negativeColor = colorStateList;
            this.negativeColorSet = true;
            return this;
        }

        public Builder negativeText(@StringRes int i) {
            return i == 0 ? this : negativeText(this.context.getText(i));
        }

        public Builder negativeText(@NonNull CharSequence charSequence) {
            this.negativeText = charSequence;
            return this;
        }

        public Builder negativeFocus(boolean z) {
            this.negativeFocus = z;
            return this;
        }

        public Builder neutralColor(@ColorInt int i) {
            return neutralColor(DialogUtils.getActionTextStateList(this.context, i));
        }

        public Builder neutralColorRes(@ColorRes int i) {
            return neutralColor(DialogUtils.getActionTextColorStateList(this.context, i));
        }

        public Builder neutralColorAttr(@AttrRes int i) {
            return neutralColor(DialogUtils.resolveActionTextColorStateList(this.context, i, null));
        }

        public Builder neutralColor(@NonNull ColorStateList colorStateList) {
            this.neutralColor = colorStateList;
            this.neutralColorSet = true;
            return this;
        }

        public Builder neutralFocus(boolean z) {
            this.neutralFocus = z;
            return this;
        }

        public Builder linkColor(@ColorInt int i) {
            return linkColor(DialogUtils.getActionTextStateList(this.context, i));
        }

        public Builder linkColorRes(@ColorRes int i) {
            return linkColor(DialogUtils.getActionTextColorStateList(this.context, i));
        }

        public Builder linkColorAttr(@AttrRes int i) {
            return linkColor(DialogUtils.resolveActionTextColorStateList(this.context, i, null));
        }

        public Builder linkColor(@NonNull ColorStateList colorStateList) {
            this.linkColor = colorStateList;
            return this;
        }

        public Builder listSelector(@DrawableRes int i) {
            this.listSelector = i;
            return this;
        }

        public Builder btnSelectorStacked(@DrawableRes int i) {
            this.btnSelectorStacked = i;
            return this;
        }

        public Builder btnSelector(@DrawableRes int i) {
            this.btnSelectorPositive = i;
            this.btnSelectorNeutral = i;
            this.btnSelectorNegative = i;
            return this;
        }

        public Builder btnSelector(@DrawableRes int i, @NonNull DialogAction dialogAction) {
            switch (dialogAction) {
                case NEUTRAL:
                    this.btnSelectorNeutral = i;
                    break;
                case NEGATIVE:
                    this.btnSelectorNegative = i;
                    break;
                default:
                    this.btnSelectorPositive = i;
                    break;
            }
            return this;
        }

        public Builder btnStackedGravity(@NonNull GravityEnum gravityEnum) {
            this.btnStackedGravity = gravityEnum;
            return this;
        }

        public Builder checkBoxPrompt(@NonNull CharSequence charSequence, boolean z, @Nullable CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
            this.checkBoxPrompt = charSequence;
            this.checkBoxPromptInitiallyChecked = z;
            this.checkBoxPromptListener = onCheckedChangeListener;
            return this;
        }

        public Builder checkBoxPromptRes(@StringRes int i, boolean z, @Nullable CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
            return checkBoxPrompt(this.context.getResources().getText(i), z, onCheckedChangeListener);
        }

        public Builder customView(@LayoutRes int i, boolean z) {
            return customView(LayoutInflater.from(this.context).inflate(i, (ViewGroup) null), z);
        }

        public Builder customView(@NonNull View view, boolean z) {
            if (this.content != null) {
                throw new IllegalStateException("You cannot use customView() when you have content set.");
            } else if (this.items != null) {
                throw new IllegalStateException("You cannot use customView() when you have items set.");
            } else if (this.inputCallback != null) {
                throw new IllegalStateException("You cannot use customView() with an input dialog");
            } else if (this.progress > -2 || this.indeterminateProgress) {
                throw new IllegalStateException("You cannot use customView() with a progress dialog");
            } else {
                if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) view.getParent()).removeView(view);
                }
                this.customView = view;
                this.wrapCustomViewInScroll = z;
                return this;
            }
        }

        public Builder progress(boolean z, int i) {
            if (this.customView == null) {
                if (z) {
                    this.indeterminateProgress = true;
                    this.progress = -2;
                } else {
                    this.indeterminateIsHorizontalProgress = false;
                    this.indeterminateProgress = false;
                    this.progress = -1;
                    this.progressMax = i;
                }
                return this;
            }
            throw new IllegalStateException("You cannot set progress() when you're using a custom view.");
        }

        public Builder progress(boolean z, int i, boolean z2) {
            this.showMinMax = z2;
            return progress(z, i);
        }

        public Builder progressNumberFormat(@NonNull String str) {
            this.progressNumberFormat = str;
            return this;
        }

        public Builder progressPercentFormat(@NonNull NumberFormat numberFormat) {
            this.progressPercentFormat = numberFormat;
            return this;
        }

        public Builder progressIndeterminateStyle(boolean z) {
            this.indeterminateIsHorizontalProgress = z;
            return this;
        }

        public Builder widgetColor(@ColorInt int i) {
            this.widgetColor = i;
            this.widgetColorSet = true;
            return this;
        }

        public Builder widgetColorRes(@ColorRes int i) {
            return widgetColor(DialogUtils.getColor(this.context, i));
        }

        public Builder widgetColorAttr(@AttrRes int i) {
            return widgetColor(DialogUtils.resolveColor(this.context, i));
        }

        public Builder choiceWidgetColor(@Nullable ColorStateList colorStateList) {
            this.choiceWidgetColor = colorStateList;
            return this;
        }

        public Builder dividerColor(@ColorInt int i) {
            this.dividerColor = i;
            this.dividerColorSet = true;
            return this;
        }

        public Builder dividerColorRes(@ColorRes int i) {
            return dividerColor(DialogUtils.getColor(this.context, i));
        }

        public Builder dividerColorAttr(@AttrRes int i) {
            return dividerColor(DialogUtils.resolveColor(this.context, i));
        }

        public Builder backgroundColor(@ColorInt int i) {
            this.backgroundColor = i;
            return this;
        }

        public Builder backgroundColorRes(@ColorRes int i) {
            return backgroundColor(DialogUtils.getColor(this.context, i));
        }

        public Builder backgroundColorAttr(@AttrRes int i) {
            return backgroundColor(DialogUtils.resolveColor(this.context, i));
        }

        public Builder callback(@NonNull ButtonCallback buttonCallback) {
            this.callback = buttonCallback;
            return this;
        }

        public Builder onPositive(@NonNull SingleButtonCallback singleButtonCallback) {
            this.onPositiveCallback = singleButtonCallback;
            return this;
        }

        public Builder onNegative(@NonNull SingleButtonCallback singleButtonCallback) {
            this.onNegativeCallback = singleButtonCallback;
            return this;
        }

        public Builder onNeutral(@NonNull SingleButtonCallback singleButtonCallback) {
            this.onNeutralCallback = singleButtonCallback;
            return this;
        }

        public Builder onAny(@NonNull SingleButtonCallback singleButtonCallback) {
            this.onAnyCallback = singleButtonCallback;
            return this;
        }

        public Builder theme(@NonNull Theme theme) {
            this.theme = theme;
            return this;
        }

        public Builder cancelable(boolean z) {
            this.cancelable = z;
            this.canceledOnTouchOutside = z;
            return this;
        }

        public Builder canceledOnTouchOutside(boolean z) {
            this.canceledOnTouchOutside = z;
            return this;
        }

        public Builder autoDismiss(boolean z) {
            this.autoDismiss = z;
            return this;
        }

        public Builder adapter(@NonNull RecyclerView.Adapter<?> adapter, @Nullable RecyclerView.LayoutManager layoutManager) {
            if (this.customView != null) {
                throw new IllegalStateException("You cannot set adapter() when you're using a custom view.");
            } else if (layoutManager == null || (layoutManager instanceof LinearLayoutManager) || (layoutManager instanceof GridLayoutManager)) {
                this.adapter = adapter;
                this.layoutManager = layoutManager;
                return this;
            } else {
                throw new IllegalStateException("You can currently only use LinearLayoutManager and GridLayoutManager with this library.");
            }
        }

        public Builder limitIconToDefaultSize() {
            this.limitIconToDefaultSize = true;
            return this;
        }

        public Builder maxIconSize(int i) {
            this.maxIconSize = i;
            return this;
        }

        public Builder maxIconSizeRes(@DimenRes int i) {
            return maxIconSize((int) this.context.getResources().getDimension(i));
        }

        public Builder showListener(@NonNull DialogInterface.OnShowListener onShowListener) {
            this.showListener = onShowListener;
            return this;
        }

        public Builder dismissListener(@NonNull DialogInterface.OnDismissListener onDismissListener) {
            this.dismissListener = onDismissListener;
            return this;
        }

        public Builder cancelListener(@NonNull DialogInterface.OnCancelListener onCancelListener) {
            this.cancelListener = onCancelListener;
            return this;
        }

        public Builder keyListener(@NonNull DialogInterface.OnKeyListener onKeyListener) {
            this.keyListener = onKeyListener;
            return this;
        }

        public Builder stackingBehavior(@NonNull StackingBehavior stackingBehavior) {
            this.stackingBehavior = stackingBehavior;
            return this;
        }

        public Builder input(@Nullable CharSequence charSequence, @Nullable CharSequence charSequence2, boolean z, @NonNull InputCallback inputCallback) {
            if (this.customView == null) {
                this.inputCallback = inputCallback;
                this.inputHint = charSequence;
                this.inputPrefill = charSequence2;
                this.inputAllowEmpty = z;
                return this;
            }
            throw new IllegalStateException("You cannot set content() when you're using a custom view.");
        }

        public Builder input(@Nullable CharSequence charSequence, @Nullable CharSequence charSequence2, @NonNull InputCallback inputCallback) {
            return input(charSequence, charSequence2, true, inputCallback);
        }

        public Builder input(@StringRes int i, @StringRes int i2, boolean z, @NonNull InputCallback inputCallback) {
            CharSequence charSequence;
            CharSequence charSequence2 = null;
            if (i == 0) {
                charSequence = null;
            } else {
                charSequence = this.context.getText(i);
            }
            if (i2 != 0) {
                charSequence2 = this.context.getText(i2);
            }
            return input(charSequence, charSequence2, z, inputCallback);
        }

        public Builder input(@StringRes int i, @StringRes int i2, @NonNull InputCallback inputCallback) {
            return input(i, i2, true, inputCallback);
        }

        public Builder inputType(int i) {
            this.inputType = i;
            return this;
        }

        public Builder inputRange(@IntRange(from = 0, m54to = 2147483647L) int i, @IntRange(from = -1, m54to = 2147483647L) int i2) {
            return inputRange(i, i2, 0);
        }

        public Builder inputRange(@IntRange(from = 0, m54to = 2147483647L) int i, @IntRange(from = -1, m54to = 2147483647L) int i2, @ColorInt int i3) {
            if (i >= 0) {
                this.inputMinLength = i;
                this.inputMaxLength = i2;
                if (i3 == 0) {
                    this.inputRangeErrorColor = DialogUtils.getColor(this.context, C0582R.C0583color.md_edittext_error);
                } else {
                    this.inputRangeErrorColor = i3;
                }
                if (this.inputMinLength > 0) {
                    this.inputAllowEmpty = false;
                }
                return this;
            }
            throw new IllegalArgumentException("Min length for input dialogs cannot be less than 0.");
        }

        public Builder inputRangeRes(@IntRange(from = 0, m54to = 2147483647L) int i, @IntRange(from = -1, m54to = 2147483647L) int i2, @ColorRes int i3) {
            return inputRange(i, i2, DialogUtils.getColor(this.context, i3));
        }

        public Builder alwaysCallInputCallback() {
            this.alwaysCallInputCallback = true;
            return this;
        }

        public Builder tag(@Nullable Object obj) {
            this.tag = obj;
            return this;
        }

        @UiThread
        public MaterialDialog build() {
            return new MaterialDialog(this);
        }

        @UiThread
        public MaterialDialog show() {
            MaterialDialog build = build();
            build.show();
            return build;
        }
    }

    @Deprecated
    /* loaded from: classes.dex */
    public static abstract class ButtonCallback {
        @Deprecated
        public void onAny(MaterialDialog materialDialog) {
        }

        @Deprecated
        public void onNegative(MaterialDialog materialDialog) {
        }

        @Deprecated
        public void onNeutral(MaterialDialog materialDialog) {
        }

        @Deprecated
        public void onPositive(MaterialDialog materialDialog) {
        }

        protected final Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public final boolean equals(Object obj) {
            return super.equals(obj);
        }

        protected final void finalize() throws Throwable {
            super.finalize();
        }

        public final int hashCode() {
            return super.hashCode();
        }

        public final String toString() {
            return super.toString();
        }
    }
}
