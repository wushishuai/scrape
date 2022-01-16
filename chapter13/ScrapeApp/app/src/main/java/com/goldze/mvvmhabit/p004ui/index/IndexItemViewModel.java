package com.goldze.mvvmhabit.p004ui.index;

import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.p000v4.content.ContextCompat;
import com.goldze.mvvmhabit.C0691R;
import com.goldze.mvvmhabit.entity.MovieEntity;
import com.goldze.mvvmhabit.p004ui.detail.DetailFragment;
import p006me.goldze.mvvmhabit.base.ItemViewModel;
import p006me.goldze.mvvmhabit.binding.command.BindingAction;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;
import p006me.goldze.mvvmhabit.utils.ToastUtils;

/* renamed from: com.goldze.mvvmhabit.ui.index.IndexItemViewModel */
/* loaded from: classes.dex */
public class IndexItemViewModel extends ItemViewModel<IndexViewModel> {
    public Drawable drawableImg;
    public ObservableField<MovieEntity> entity = new ObservableField<>();
    public BindingCommand itemClick = new BindingCommand(new BindingAction() { // from class: com.goldze.mvvmhabit.ui.index.IndexItemViewModel.1
        @Override // p006me.goldze.mvvmhabit.binding.command.BindingAction
        public void call() {
            Bundle bundle = new Bundle();
            bundle.putParcelable("entity", IndexItemViewModel.this.entity.get());
            ((IndexViewModel) IndexItemViewModel.this.viewModel).startContainerActivity(DetailFragment.class.getCanonicalName(), bundle);
        }
    });
    public BindingCommand itemLongClick = new BindingCommand(new BindingAction() { // from class: com.goldze.mvvmhabit.ui.index.IndexItemViewModel.2
        @Override // p006me.goldze.mvvmhabit.binding.command.BindingAction
        public void call() {
            ToastUtils.showShort(IndexItemViewModel.this.entity.get().getName());
        }
    });

    public IndexItemViewModel(@NonNull IndexViewModel indexViewModel, MovieEntity movieEntity) {
        super(indexViewModel);
        this.entity.set(movieEntity);
        this.drawableImg = ContextCompat.getDrawable(indexViewModel.getApplication(), C0691R.mipmap.ic_launcher);
    }

    public int getPosition() {
        return ((IndexViewModel) this.viewModel).getItemPosition(this);
    }
}
