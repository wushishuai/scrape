package p006me.tatarka.bindingcollectionadapter2;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.support.p000v4.view.ViewPager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.WrapperListAdapter;
import java.util.List;
import p006me.tatarka.bindingcollectionadapter2.BindingListViewAdapter;
import p006me.tatarka.bindingcollectionadapter2.BindingViewPagerAdapter;

/* renamed from: me.tatarka.bindingcollectionadapter2.BindingCollectionAdapters */
/* loaded from: classes.dex */
public class BindingCollectionAdapters {
    @BindingAdapter(requireAll = false, value = {"itemBinding", "itemTypeCount", "items", "adapter", "itemDropDownLayout", "itemIds", "itemIsEnabled"})
    public static <T> void setAdapter(AdapterView adapterView, ItemBinding<T> itemBinding, Integer num, List list, BindingListViewAdapter<T> bindingListViewAdapter, int i, BindingListViewAdapter.ItemIds<? super T> itemIds, BindingListViewAdapter.ItemIsEnabled<? super T> itemIsEnabled) {
        if (itemBinding != null) {
            BindingListViewAdapter<T> bindingListViewAdapter2 = (BindingListViewAdapter) unwrapAdapter(adapterView.getAdapter());
            if (bindingListViewAdapter == null) {
                if (bindingListViewAdapter2 == null) {
                    bindingListViewAdapter = new BindingListViewAdapter<>(num != null ? num.intValue() : 1);
                } else {
                    bindingListViewAdapter = bindingListViewAdapter2;
                }
            }
            bindingListViewAdapter.setItemBinding(itemBinding);
            bindingListViewAdapter.setDropDownItemLayout(i);
            bindingListViewAdapter.setItems(list);
            bindingListViewAdapter.setItemIds(itemIds);
            bindingListViewAdapter.setItemIsEnabled(itemIsEnabled);
            if (bindingListViewAdapter2 != bindingListViewAdapter) {
                adapterView.setAdapter(bindingListViewAdapter);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("onItemBind must not be null");
    }

    private static Adapter unwrapAdapter(Adapter adapter) {
        return adapter instanceof WrapperListAdapter ? unwrapAdapter(((WrapperListAdapter) adapter).getWrappedAdapter()) : adapter;
    }

    @BindingAdapter(requireAll = false, value = {"itemBinding", "items", "adapter", "pageTitles"})
    public static <T> void setAdapter(ViewPager viewPager, ItemBinding<T> itemBinding, List list, BindingViewPagerAdapter<T> bindingViewPagerAdapter, BindingViewPagerAdapter.PageTitles<T> pageTitles) {
        if (itemBinding != null) {
            BindingViewPagerAdapter<T> bindingViewPagerAdapter2 = (BindingViewPagerAdapter) viewPager.getAdapter();
            if (bindingViewPagerAdapter == null) {
                bindingViewPagerAdapter = bindingViewPagerAdapter2 == null ? new BindingViewPagerAdapter<>() : bindingViewPagerAdapter2;
            }
            bindingViewPagerAdapter.setItemBinding(itemBinding);
            bindingViewPagerAdapter.setItems(list);
            bindingViewPagerAdapter.setPageTitles(pageTitles);
            if (bindingViewPagerAdapter2 != bindingViewPagerAdapter) {
                viewPager.setAdapter(bindingViewPagerAdapter);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("onItemBind must not be null");
    }

    @BindingConversion
    public static <T> ItemBinding<T> toItemBinding(OnItemBind<T> onItemBind) {
        return ItemBinding.m7of(onItemBind);
    }
}
