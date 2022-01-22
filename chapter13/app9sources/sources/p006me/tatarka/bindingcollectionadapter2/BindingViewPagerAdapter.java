package p006me.tatarka.bindingcollectionadapter2;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.p000v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.lang.ref.WeakReference;
import java.util.List;

/* renamed from: me.tatarka.bindingcollectionadapter2.BindingViewPagerAdapter */
/* loaded from: classes.dex */
public class BindingViewPagerAdapter<T> extends PagerAdapter implements BindingCollectionAdapter<T> {
    private final WeakReferenceOnListChangedCallback<T> callback = new WeakReferenceOnListChangedCallback<>(this);
    private LayoutInflater inflater;
    private ItemBinding<T> itemBinding;
    private List<T> items;
    private PageTitles<T> pageTitles;

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingViewPagerAdapter$PageTitles */
    /* loaded from: classes.dex */
    public interface PageTitles<T> {
        CharSequence getPageTitle(int i, T t);
    }

    @Override // android.support.p000v4.view.PagerAdapter
    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
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

    public void setPageTitles(@Nullable PageTitles<T> pageTitles) {
        this.pageTitles = pageTitles;
    }

    @Override // android.support.p000v4.view.PagerAdapter
    public int getCount() {
        List<T> list = this.items;
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override // android.support.p000v4.view.PagerAdapter
    public CharSequence getPageTitle(int i) {
        PageTitles<T> pageTitles = this.pageTitles;
        if (pageTitles == null) {
            return null;
        }
        return pageTitles.getPageTitle(i, this.items.get(i));
    }

    @Override // android.support.p000v4.view.PagerAdapter
    public Object instantiateItem(ViewGroup viewGroup, int i) {
        if (this.inflater == null) {
            this.inflater = LayoutInflater.from(viewGroup.getContext());
        }
        T t = this.items.get(i);
        this.itemBinding.onItemBind(i, t);
        ViewDataBinding onCreateBinding = onCreateBinding(this.inflater, this.itemBinding.layoutRes(), viewGroup);
        onBindBinding(onCreateBinding, this.itemBinding.variableId(), this.itemBinding.layoutRes(), i, t);
        viewGroup.addView(onCreateBinding.getRoot());
        onCreateBinding.getRoot().setTag(t);
        return onCreateBinding.getRoot();
    }

    @Override // android.support.p000v4.view.PagerAdapter
    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        viewGroup.removeView((View) obj);
    }

    @Override // android.support.p000v4.view.PagerAdapter
    public int getItemPosition(Object obj) {
        Object tag = ((View) obj).getTag();
        if (this.items == null) {
            return -2;
        }
        for (int i = 0; i < this.items.size(); i++) {
            if (tag == this.items.get(i)) {
                return i;
            }
        }
        return -2;
    }

    /* renamed from: me.tatarka.bindingcollectionadapter2.BindingViewPagerAdapter$WeakReferenceOnListChangedCallback */
    /* loaded from: classes.dex */
    private static class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback<ObservableList<T>> {
        final WeakReference<BindingViewPagerAdapter<T>> adapterRef;

        WeakReferenceOnListChangedCallback(BindingViewPagerAdapter<T> bindingViewPagerAdapter) {
            this.adapterRef = new WeakReference<>(bindingViewPagerAdapter);
        }

        @Override // android.databinding.ObservableList.OnListChangedCallback
        public void onChanged(ObservableList observableList) {
            BindingViewPagerAdapter<T> bindingViewPagerAdapter = this.adapterRef.get();
            if (bindingViewPagerAdapter != null) {
                Utils.ensureChangeOnMainThread();
                bindingViewPagerAdapter.notifyDataSetChanged();
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
