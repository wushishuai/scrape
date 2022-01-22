package com.afollestad.materialdialogs;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.p003v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.util.DialogUtils;

/* loaded from: classes.dex */
public class DefaultRvAdapter extends RecyclerView.Adapter<DefaultVH> {
    private InternalListCallback callback;
    private final MaterialDialog dialog;
    private final GravityEnum itemGravity;
    @LayoutRes
    private final int layout;

    /* loaded from: classes.dex */
    public interface InternalListCallback {
        boolean onItemSelected(MaterialDialog materialDialog, View view, int i, CharSequence charSequence, boolean z);
    }

    public DefaultRvAdapter(MaterialDialog materialDialog, @LayoutRes int i) {
        this.dialog = materialDialog;
        this.layout = i;
        this.itemGravity = materialDialog.builder.itemsGravity;
    }

    public void setCallback(InternalListCallback internalListCallback) {
        this.callback = internalListCallback;
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public DefaultVH onCreateViewHolder(ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(this.layout, viewGroup, false);
        DialogUtils.setBackgroundCompat(inflate, this.dialog.getListSelector());
        return new DefaultVH(inflate, this);
    }

    public void onBindViewHolder(DefaultVH defaultVH, int i) {
        View view = defaultVH.itemView;
        boolean isIn = DialogUtils.isIn(Integer.valueOf(i), this.dialog.builder.disabledIndices);
        int adjustAlpha = isIn ? DialogUtils.adjustAlpha(this.dialog.builder.itemColor, 0.4f) : this.dialog.builder.itemColor;
        defaultVH.itemView.setEnabled(!isIn);
        switch (this.dialog.listType) {
            case SINGLE:
                RadioButton radioButton = (RadioButton) defaultVH.control;
                boolean z = this.dialog.builder.selectedIndex == i;
                if (this.dialog.builder.choiceWidgetColor != null) {
                    MDTintHelper.setTint(radioButton, this.dialog.builder.choiceWidgetColor);
                } else {
                    MDTintHelper.setTint(radioButton, this.dialog.builder.widgetColor);
                }
                radioButton.setChecked(z);
                radioButton.setEnabled(!isIn);
                break;
            case MULTI:
                CheckBox checkBox = (CheckBox) defaultVH.control;
                boolean contains = this.dialog.selectedIndicesList.contains(Integer.valueOf(i));
                if (this.dialog.builder.choiceWidgetColor != null) {
                    MDTintHelper.setTint(checkBox, this.dialog.builder.choiceWidgetColor);
                } else {
                    MDTintHelper.setTint(checkBox, this.dialog.builder.widgetColor);
                }
                checkBox.setChecked(contains);
                checkBox.setEnabled(!isIn);
                break;
        }
        defaultVH.title.setText(this.dialog.builder.items.get(i));
        defaultVH.title.setTextColor(adjustAlpha);
        this.dialog.setTypeface(defaultVH.title, this.dialog.builder.regularFont);
        ViewGroup viewGroup = (ViewGroup) view;
        setupGravity(viewGroup);
        if (this.dialog.builder.itemIds != null) {
            if (i < this.dialog.builder.itemIds.length) {
                view.setId(this.dialog.builder.itemIds[i]);
            } else {
                view.setId(-1);
            }
        }
        if (Build.VERSION.SDK_INT >= 21 && viewGroup.getChildCount() == 2) {
            if (viewGroup.getChildAt(0) instanceof CompoundButton) {
                viewGroup.getChildAt(0).setBackground(null);
            } else if (viewGroup.getChildAt(1) instanceof CompoundButton) {
                viewGroup.getChildAt(1).setBackground(null);
            }
        }
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        if (this.dialog.builder.items != null) {
            return this.dialog.builder.items.size();
        }
        return 0;
    }

    @TargetApi(17)
    private void setupGravity(ViewGroup viewGroup) {
        ((LinearLayout) viewGroup).setGravity(this.itemGravity.getGravityInt() | 16);
        if (viewGroup.getChildCount() != 2) {
            return;
        }
        if (this.itemGravity == GravityEnum.END && !isRTL() && (viewGroup.getChildAt(0) instanceof CompoundButton)) {
            View view = (CompoundButton) viewGroup.getChildAt(0);
            viewGroup.removeView(view);
            TextView textView = (TextView) viewGroup.getChildAt(0);
            viewGroup.removeView(textView);
            textView.setPadding(textView.getPaddingRight(), textView.getPaddingTop(), textView.getPaddingLeft(), textView.getPaddingBottom());
            viewGroup.addView(textView);
            viewGroup.addView(view);
        } else if (this.itemGravity == GravityEnum.START && isRTL() && (viewGroup.getChildAt(1) instanceof CompoundButton)) {
            View view2 = (CompoundButton) viewGroup.getChildAt(1);
            viewGroup.removeView(view2);
            TextView textView2 = (TextView) viewGroup.getChildAt(0);
            viewGroup.removeView(textView2);
            textView2.setPadding(textView2.getPaddingRight(), textView2.getPaddingTop(), textView2.getPaddingRight(), textView2.getPaddingBottom());
            viewGroup.addView(view2);
            viewGroup.addView(textView2);
        }
    }

    @TargetApi(17)
    private boolean isRTL() {
        if (Build.VERSION.SDK_INT >= 17 && this.dialog.getBuilder().getContext().getResources().getConfiguration().getLayoutDirection() == 1) {
            return true;
        }
        return false;
    }

    /* loaded from: classes.dex */
    public static class DefaultVH extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final DefaultRvAdapter adapter;
        final CompoundButton control;
        final TextView title;

        DefaultVH(View view, DefaultRvAdapter defaultRvAdapter) {
            super(view);
            this.control = (CompoundButton) view.findViewById(C0582R.C0585id.md_control);
            this.title = (TextView) view.findViewById(C0582R.C0585id.md_title);
            this.adapter = defaultRvAdapter;
            view.setOnClickListener(this);
            if (defaultRvAdapter.dialog.builder.listLongCallback != null) {
                view.setOnLongClickListener(this);
            }
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.adapter.callback != null && getAdapterPosition() != -1) {
                this.adapter.callback.onItemSelected(this.adapter.dialog, view, getAdapterPosition(), (this.adapter.dialog.builder.items == null || getAdapterPosition() >= this.adapter.dialog.builder.items.size()) ? null : this.adapter.dialog.builder.items.get(getAdapterPosition()), false);
            }
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            if (this.adapter.callback == null || getAdapterPosition() == -1) {
                return false;
            }
            return this.adapter.callback.onItemSelected(this.adapter.dialog, view, getAdapterPosition(), (this.adapter.dialog.builder.items == null || getAdapterPosition() >= this.adapter.dialog.builder.items.size()) ? null : this.adapter.dialog.builder.items.get(getAdapterPosition()), true);
        }
    }
}
