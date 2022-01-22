package p006me.tatarka.bindingcollectionadapter2;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.p003v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;
import java.util.List;

/* renamed from: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter */
/* loaded from: classes.dex */
public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements BindingCollectionAdapter<T> {
    private static final Object DATA_INVALIDATION = new Object();
    private final WeakReferenceOnListChangedCallback<T> callback = new WeakReferenceOnListChangedCallback<>(this);
    private LayoutInflater inflater;
    private ItemBinding<T> itemBinding;
    private ItemIds<? super T> itemIds;
    private List<T> items;
    @Nullable
    private RecyclerView recyclerView;
    private ViewHolderFactory viewHolderFactory;

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter$ItemIds */
    /* loaded from: classes.dex */
    public interface ItemIds<T> {
        long getItemId(int i, T t);
    }

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter$ViewHolderFactory */
    /* loaded from: classes.dex */
    public interface ViewHolderFactory {
        RecyclerView.ViewHolder createViewHolder(ViewDataBinding viewDataBinding);
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public void setItemBinding(ItemBinding<T> itemBinding) {
        this.itemBinding = itemBinding;
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public ItemBinding<T> getItemBinding() {
        return this.itemBinding;
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public void setItems(@Nullable List<T> list) {
        List<T> list2 = this.items;
        if (list2 != list) {
            if (this.recyclerView != null) {
                if (list2 instanceof ObservableList) {
                    ((ObservableList) list2).removeOnListChangedCallback(this.callback);
                }
                if (list instanceof ObservableList) {
                    ((ObservableList) list).addOnListChangedCallback(this.callback);
                }
            }
            this.items = list;
            notifyDataSetChanged();
        }
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public T getAdapterItem(int i) {
        return this.items.get(i);
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public ViewDataBinding onCreateBinding(LayoutInflater layoutInflater, @LayoutRes int i, ViewGroup viewGroup) {
        return DataBindingUtil.inflate(layoutInflater, i, viewGroup, false);
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public void onBindBinding(ViewDataBinding viewDataBinding, int i, @LayoutRes int i2, int i3, T t) {
        if (this.itemBinding.bind(viewDataBinding, t)) {
            viewDataBinding.executePendingBindings();
        }
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        List<T> list;
        if (this.recyclerView == null && (list = this.items) != null && (list instanceof ObservableList)) {
            ((ObservableList) list).addOnListChangedCallback(this.callback);
        }
        this.recyclerView = recyclerView;
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        List<T> list;
        if (!(this.recyclerView == null || (list = this.items) == null || !(list instanceof ObservableList))) {
            ((ObservableList) list).removeOnListChangedCallback(this.callback);
        }
        this.recyclerView = null;
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (this.inflater == null) {
            this.inflater = LayoutInflater.from(viewGroup.getContext());
        }
        ViewDataBinding onCreateBinding = onCreateBinding(this.inflater, i, viewGroup);
        final RecyclerView.ViewHolder onCreateViewHolder = onCreateViewHolder(onCreateBinding);
        onCreateBinding.addOnRebindCallback(new OnRebindCallback() { // from class: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter.1
            @Override // android.databinding.OnRebindCallback
            public boolean onPreBind(ViewDataBinding viewDataBinding) {
                return BindingRecyclerViewAdapter.this.recyclerView != null && BindingRecyclerViewAdapter.this.recyclerView.isComputingLayout();
            }

            @Override // android.databinding.OnRebindCallback
            public void onCanceled(ViewDataBinding viewDataBinding) {
                int adapterPosition;
                if (BindingRecyclerViewAdapter.this.recyclerView != null && !BindingRecyclerViewAdapter.this.recyclerView.isComputingLayout() && (adapterPosition = onCreateViewHolder.getAdapterPosition()) != -1) {
                    BindingRecyclerViewAdapter.this.notifyItemChanged(adapterPosition, BindingRecyclerViewAdapter.DATA_INVALIDATION);
                }
            }
        });
        return onCreateViewHolder;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewDataBinding viewDataBinding) {
        ViewHolderFactory viewHolderFactory = this.viewHolderFactory;
        if (viewHolderFactory != null) {
            return viewHolderFactory.createViewHolder(viewDataBinding);
        }
        return new BindingViewHolder(viewDataBinding);
    }

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter$BindingViewHolder */
    /* loaded from: classes.dex */
    public static class BindingViewHolder extends RecyclerView.ViewHolder {
        public BindingViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
        }
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public final void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        onBindBinding(DataBindingUtil.getBinding(viewHolder.itemView), this.itemBinding.variableId(), this.itemBinding.layoutRes(), i, this.items.get(i));
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i, List<Object> list) {
        if (isForDataBinding(list)) {
            DataBindingUtil.getBinding(viewHolder.itemView).executePendingBindings();
        } else {
            super.onBindViewHolder(viewHolder, i, list);
        }
    }

    private boolean isForDataBinding(List<Object> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != DATA_INVALIDATION) {
                return false;
            }
        }
        return true;
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        this.itemBinding.onItemBind(i, this.items.get(i));
        return this.itemBinding.layoutRes();
    }

    public void setItemIds(@Nullable ItemIds<? super T> itemIds) {
        if (this.itemIds != itemIds) {
            this.itemIds = itemIds;
            setHasStableIds(itemIds != null);
        }
    }

    public void setViewHolderFactory(@Nullable ViewHolderFactory viewHolderFactory) {
        this.viewHolderFactory = viewHolderFactory;
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public int getItemCount() {
        List<T> list = this.items;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override // android.support.p003v7.widget.RecyclerView.Adapter
    public long getItemId(int i) {
        ItemIds<? super T> itemIds = this.itemIds;
        return itemIds == null ? (long) i : itemIds.getItemId(i, (T) this.items.get(i));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter$WeakReferenceOnListChangedCallback */
    /* loaded from: classes.dex */
    public static class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback<ObservableList<T>> {
        final WeakReference<BindingRecyclerViewAdapter<T>> adapterRef;

        WeakReferenceOnListChangedCallback(BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter) {
            this.adapterRef = new WeakReference<>(bindingRecyclerViewAdapter);
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onChanged(ObservableList observableList) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter = this.adapterRef.get();
            if (bindingRecyclerViewAdapter != null) {
                Utils.ensureChangeOnMainThread();
                bindingRecyclerViewAdapter.notifyDataSetChanged();
            }
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeChanged(ObservableList observableList, int i, int i2) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter = this.adapterRef.get();
            if (bindingRecyclerViewAdapter != null) {
                Utils.ensureChangeOnMainThread();
                bindingRecyclerViewAdapter.notifyItemRangeChanged(i, i2);
            }
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeInserted(ObservableList observableList, int i, int i2) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter = this.adapterRef.get();
            if (bindingRecyclerViewAdapter != null) {
                Utils.ensureChangeOnMainThread();
                bindingRecyclerViewAdapter.notifyItemRangeInserted(i, i2);
            }
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeMoved(ObservableList observableList, int i, int i2, int i3) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter = this.adapterRef.get();
            if (bindingRecyclerViewAdapter != null) {
                Utils.ensureChangeOnMainThread();
                for (int i4 = 0; i4 < i3; i4++) {
                    bindingRecyclerViewAdapter.notifyItemMoved(i + i4, i2 + i4);
                }
            }
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeRemoved(ObservableList observableList, int i, int i2) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter = this.adapterRef.get();
            if (bindingRecyclerViewAdapter != null) {
                Utils.ensureChangeOnMainThread();
                bindingRecyclerViewAdapter.notifyItemRangeRemoved(i, i2);
            }
        }
    }
}
