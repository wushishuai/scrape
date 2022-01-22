package p006me.tatarka.bindingcollectionadapter2;

import android.databinding.BindingAdapter;
import android.support.p003v7.widget.RecyclerView;
import java.util.List;
import p006me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import p006me.tatarka.bindingcollectionadapter2.LayoutManagers;

/* renamed from: me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapters */
/* loaded from: classes.dex */
public class BindingRecyclerViewAdapters {
    @BindingAdapter(requireAll = false, value = {"itemBinding", "items", "adapter", "itemIds", "viewHolder"})
    public static <T> void setAdapter(RecyclerView recyclerView, ItemBinding<T> itemBinding, List<T> list, BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter, BindingRecyclerViewAdapter.ItemIds<? super T> itemIds, BindingRecyclerViewAdapter.ViewHolderFactory viewHolderFactory) {
        if (itemBinding != null) {
            BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter2 = (BindingRecyclerViewAdapter) recyclerView.getAdapter();
            if (bindingRecyclerViewAdapter == null) {
                bindingRecyclerViewAdapter = bindingRecyclerViewAdapter2 == null ? new BindingRecyclerViewAdapter<>() : bindingRecyclerViewAdapter2;
            }
            bindingRecyclerViewAdapter.setItemBinding(itemBinding);
            bindingRecyclerViewAdapter.setItems(list);
            bindingRecyclerViewAdapter.setItemIds(itemIds);
            bindingRecyclerViewAdapter.setViewHolderFactory(viewHolderFactory);
            if (bindingRecyclerViewAdapter2 != bindingRecyclerViewAdapter) {
                recyclerView.setAdapter(bindingRecyclerViewAdapter);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("itemBinding must not be null");
    }

    @BindingAdapter({"layoutManager"})
    public static void setLayoutManager(RecyclerView recyclerView, LayoutManagers.LayoutManagerFactory layoutManagerFactory) {
        recyclerView.setLayoutManager(layoutManagerFactory.create(recyclerView));
    }
}
