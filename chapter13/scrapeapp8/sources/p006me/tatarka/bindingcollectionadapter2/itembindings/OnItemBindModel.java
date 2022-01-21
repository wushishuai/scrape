package p006me.tatarka.bindingcollectionadapter2.itembindings;

import p006me.tatarka.bindingcollectionadapter2.ItemBinding;
import p006me.tatarka.bindingcollectionadapter2.OnItemBind;
import p006me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel;

/* renamed from: me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel */
/* loaded from: classes.dex */
public class OnItemBindModel<T extends ItemBindingModel> implements OnItemBind<T> {
    /* JADX WARN: Multi-variable type inference failed */
    @Override // p006me.tatarka.bindingcollectionadapter2.OnItemBind
    public /* bridge */ /* synthetic */ void onItemBind(ItemBinding itemBinding, int i, Object obj) {
        onItemBind(itemBinding, i, (int) ((ItemBindingModel) obj));
    }

    public void onItemBind(ItemBinding itemBinding, int position, T item) {
        item.onItemBind(itemBinding);
    }
}
