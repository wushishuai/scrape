package p006me.goldze.mvvmhabit.binding.viewadapter.viewgroup;

import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import p006me.tatarka.bindingcollectionadapter2.ItemBinding;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.viewgroup.ViewAdapter */
/* loaded from: classes.dex */
public final class ViewAdapter {
    @BindingAdapter({"itemView", "observableList"})
    public static void addViews(ViewGroup viewGroup, ItemBinding itemBinding, ObservableList<IBindingItemViewModel> observableList) {
        if (!(observableList == null || observableList.isEmpty())) {
            viewGroup.removeAllViews();
            for (IBindingItemViewModel iBindingItemViewModel : observableList) {
                ViewDataBinding inflate = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), itemBinding.layoutRes(), viewGroup, true);
                inflate.setVariable(itemBinding.variableId(), iBindingItemViewModel);
                iBindingItemViewModel.injecDataBinding(inflate);
            }
        }
    }
}
