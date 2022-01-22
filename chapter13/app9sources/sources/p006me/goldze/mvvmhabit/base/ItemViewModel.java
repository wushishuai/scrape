package p006me.goldze.mvvmhabit.base;

import android.support.annotation.NonNull;
import p006me.goldze.mvvmhabit.base.BaseViewModel;

/* renamed from: me.goldze.mvvmhabit.base.ItemViewModel */
/* loaded from: classes.dex */
public class ItemViewModel<VM extends BaseViewModel> {
    protected VM viewModel;

    public ItemViewModel(@NonNull VM vm) {
        this.viewModel = vm;
    }
}
