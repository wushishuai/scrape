package p006me.tatarka.bindingcollectionadapter2;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.lang.ref.WeakReference;
import java.util.List;

/* renamed from: me.tatarka.bindingcollectionadapter2.BindingListViewAdapter */
/* loaded from: classes.dex */
public class BindingListViewAdapter<T> extends BaseAdapter implements BindingCollectionAdapter<T> {
    @NonNull
    private final WeakReferenceOnListChangedCallback<T> callback = new WeakReferenceOnListChangedCallback<>(this);
    private int dropDownItemLayout;
    private LayoutInflater inflater;
    private ItemBinding<T> itemBinding;
    private ItemIds<? super T> itemIds;
    private ItemIsEnabled<? super T> itemIsEnabled;
    private final int itemTypeCount;
    private List<T> items;
    private int[] layouts;

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingListViewAdapter$ItemIds */
    /* loaded from: classes.dex */
    public interface ItemIds<T> {
        long getItemId(int i, T t);
    }

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingListViewAdapter$ItemIsEnabled */
    /* loaded from: classes.dex */
    public interface ItemIsEnabled<T> {
        boolean isEnabled(int i, T t);
    }

    public BindingListViewAdapter(int i) {
        this.itemTypeCount = i;
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public void setItemBinding(ItemBinding<T> itemBinding) {
        this.itemBinding = itemBinding;
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public ItemBinding<T> getItemBinding() {
        return this.itemBinding;
    }

    public void setDropDownItemLayout(int i) {
        this.dropDownItemLayout = i;
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public void setItems(@Nullable List<T> list) {
        List<T> list2 = this.items;
        if (list2 != list) {
            if (list2 instanceof ObservableList) {
                ((ObservableList) list2).removeOnListChangedCallback(this.callback);
            }
            if (list instanceof ObservableList) {
                ((ObservableList) list).addOnListChangedCallback(this.callback);
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
    public ViewDataBinding onCreateBinding(LayoutInflater layoutInflater, int i, ViewGroup viewGroup) {
        return DataBindingUtil.inflate(layoutInflater, i, viewGroup, false);
    }

    @Override // p006me.tatarka.bindingcollectionadapter2.BindingCollectionAdapter
    public void onBindBinding(ViewDataBinding viewDataBinding, int i, int i2, int i3, T t) {
        if (this.itemBinding.bind(viewDataBinding, t)) {
            viewDataBinding.executePendingBindings();
        }
    }

    public void setItemIds(@Nullable ItemIds<? super T> itemIds) {
        this.itemIds = itemIds;
    }

    public void setItemIsEnabled(@Nullable ItemIsEnabled<? super T> itemIsEnabled) {
        this.itemIsEnabled = itemIsEnabled;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        List<T> list = this.items;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override // android.widget.Adapter
    public T getItem(int i) {
        return this.items.get(i);
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        ItemIds<? super T> itemIds = this.itemIds;
        return itemIds == null ? (long) i : itemIds.getItemId(i, (T) this.items.get(i));
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int i) {
        ItemIsEnabled<? super T> itemIsEnabled = this.itemIsEnabled;
        return itemIsEnabled == null || itemIsEnabled.isEnabled(i, (T) this.items.get(i));
    }

    @Override // android.widget.Adapter
    public final View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        ViewDataBinding viewDataBinding;
        if (this.inflater == null) {
            this.inflater = LayoutInflater.from(viewGroup.getContext());
        }
        int i2 = this.layouts[getItemViewType(i)];
        if (view == null) {
            viewDataBinding = onCreateBinding(this.inflater, i2, viewGroup);
        } else {
            viewDataBinding = DataBindingUtil.getBinding(view);
        }
        onBindBinding(viewDataBinding, this.itemBinding.variableId(), i2, i, this.items.get(i));
        return viewDataBinding.getRoot();
    }

    @Override // android.widget.BaseAdapter, android.widget.SpinnerAdapter
    public final View getDropDownView(int i, View view, ViewGroup viewGroup) {
        ViewDataBinding viewDataBinding;
        if (this.inflater == null) {
            this.inflater = LayoutInflater.from(viewGroup.getContext());
        }
        int i2 = this.dropDownItemLayout;
        if (i2 == 0) {
            return super.getDropDownView(i, view, viewGroup);
        }
        if (view == null) {
            viewDataBinding = onCreateBinding(this.inflater, i2, viewGroup);
        } else {
            viewDataBinding = DataBindingUtil.getBinding(view);
        }
        onBindBinding(viewDataBinding, this.itemBinding.variableId(), i2, i, this.items.get(i));
        return viewDataBinding.getRoot();
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getItemViewType(int i) {
        ensureLayoutsInit();
        this.itemBinding.onItemBind(i, this.items.get(i));
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = this.layouts;
            if (i2 < iArr.length) {
                int layoutRes = this.itemBinding.layoutRes();
                int[] iArr2 = this.layouts;
                if (layoutRes == iArr2[i2]) {
                    return i2;
                }
                if (iArr2[i2] == 0) {
                    i3 = i2;
                }
                i2++;
            } else {
                iArr[i3] = this.itemBinding.layoutRes();
                return i3;
            }
        }
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return this.itemIds != null;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getViewTypeCount() {
        return ensureLayoutsInit();
    }

    private int ensureLayoutsInit() {
        int i = this.itemTypeCount;
        if (this.layouts == null) {
            this.layouts = new int[i];
        }
        return i;
    }

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingListViewAdapter$WeakReferenceOnListChangedCallback */
    /* loaded from: classes.dex */
    private static class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback<ObservableList<T>> {
        final WeakReference<BindingListViewAdapter<T>> adapterRef;

        WeakReferenceOnListChangedCallback(BindingListViewAdapter<T> bindingListViewAdapter) {
            this.adapterRef = new WeakReference<>(bindingListViewAdapter);
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onChanged(ObservableList observableList) {
            BindingListViewAdapter<T> bindingListViewAdapter = this.adapterRef.get();
            if (bindingListViewAdapter != null) {
                Utils.ensureChangeOnMainThread();
                bindingListViewAdapter.notifyDataSetChanged();
            }
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeChanged(ObservableList observableList, int i, int i2) {
            onChanged(observableList);
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeInserted(ObservableList observableList, int i, int i2) {
            onChanged(observableList);
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeMoved(ObservableList observableList, int i, int i2, int i3) {
            onChanged(observableList);
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onItemRangeRemoved(ObservableList observableList, int i, int i2) {
            onChanged(observableList);
        }
    }
}
