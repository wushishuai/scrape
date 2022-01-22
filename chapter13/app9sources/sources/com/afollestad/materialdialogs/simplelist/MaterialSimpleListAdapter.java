package com.afollestad.materialdialogs.simplelist;

import android.graphics.PorterDuff;
import android.support.p003v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.commons.C0592R;
import com.afollestad.materialdialogs.internal.MDAdapter;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class MaterialSimpleListAdapter extends RecyclerView.Adapter<SimpleListVH> implements MDAdapter {
    private Callback callback;
    private MaterialDialog dialog;
    private List<MaterialSimpleListItem> items = new ArrayList(4);

    /* loaded from: classes.dex */
    public interface Callback {
        void onMaterialListItemSelected(MaterialDialog materialDialog, int i, MaterialSimpleListItem materialSimpleListItem);
    }

    public MaterialSimpleListAdapter(Callback callback) {
        this.callback = callback;
    }

    public void add(MaterialSimpleListItem materialSimpleListItem) {
        this.items.add(materialSimpleListItem);
        notifyItemInserted(this.items.size() - 1);
    }

    public void clear() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public MaterialSimpleListItem getItem(int i) {
        return this.items.get(i);
    }

    @Override // com.afollestad.materialdialogs.internal.MDAdapter
    public void setDialog(MaterialDialog materialDialog) {
        this.dialog = materialDialog;
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public SimpleListVH onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new SimpleListVH(LayoutInflater.from(viewGroup.getContext()).inflate(C0592R.layout.md_simplelist_item, viewGroup, false), this);
    }

    public void onBindViewHolder(SimpleListVH simpleListVH, int i) {
        if (this.dialog != null) {
            MaterialSimpleListItem materialSimpleListItem = this.items.get(i);
            if (materialSimpleListItem.getIcon() != null) {
                simpleListVH.icon.setImageDrawable(materialSimpleListItem.getIcon());
                simpleListVH.icon.setPadding(materialSimpleListItem.getIconPadding(), materialSimpleListItem.getIconPadding(), materialSimpleListItem.getIconPadding(), materialSimpleListItem.getIconPadding());
                simpleListVH.icon.getBackground().setColorFilter(materialSimpleListItem.getBackgroundColor(), PorterDuff.Mode.SRC_ATOP);
            } else {
                simpleListVH.icon.setVisibility(8);
            }
            simpleListVH.title.setTextColor(this.dialog.getBuilder().getItemColor());
            simpleListVH.title.setText(materialSimpleListItem.getContent());
            this.dialog.setTypeface(simpleListVH.title, this.dialog.getBuilder().getRegularFont());
        }
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.items.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class SimpleListVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final MaterialSimpleListAdapter adapter;
        final ImageView icon;
        final TextView title;

        SimpleListVH(View view, MaterialSimpleListAdapter materialSimpleListAdapter) {
            super(view);
            this.icon = (ImageView) view.findViewById(16908294);
            this.title = (TextView) view.findViewById(16908310);
            this.adapter = materialSimpleListAdapter;
            view.setOnClickListener(this);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            if (this.adapter.callback != null) {
                this.adapter.callback.onMaterialListItemSelected(this.adapter.dialog, getAdapterPosition(), this.adapter.getItem(getAdapterPosition()));
            }
        }
    }
}
