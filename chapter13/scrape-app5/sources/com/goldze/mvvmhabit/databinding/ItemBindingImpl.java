package com.goldze.mvvmhabit.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.databinding.adapters.ImageViewBindingAdapter;
import android.databinding.adapters.TextViewBindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.goldze.mvvmhabit.entity.MovieEntity;
import com.goldze.mvvmhabit.ui.index.IndexItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.viewadapter.view.ViewAdapter;
/* loaded from: classes.dex */
public class ItemBindingImpl extends ItemBinding {
    @Nullable
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    @Nullable
    private static final SparseIntArray sViewsWithIds = null;
    private long mDirtyFlags;
    @NonNull
    private final ImageView mboundView1;

    public ItemBindingImpl(@Nullable DataBindingComponent dataBindingComponent, @NonNull View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 5, sIncludes, sViewsWithIds));
    }

    private ItemBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 1, (LinearLayout) objArr[0], (TextView) objArr[3], (TextView) objArr[4], (TextView) objArr[2]);
        this.mDirtyFlags = -1;
        this.item.setTag(null);
        this.mboundView1 = (ImageView) objArr[1];
        this.mboundView1.setTag(null);
        this.tvDescription.setTag(null);
        this.tvScore.setTag(null);
        this.tvTitle.setTag(null);
        setRootTag(view);
        invalidateAll();
    }

    @Override // android.databinding.ViewDataBinding
    public void invalidateAll() {
        synchronized (this) {
            this.mDirtyFlags = 4;
        }
        requestRebind();
    }

    @Override // android.databinding.ViewDataBinding
    public boolean hasPendingBindings() {
        synchronized (this) {
            if (this.mDirtyFlags != 0) {
                return true;
            }
            return false;
        }
    }

    @Override // android.databinding.ViewDataBinding
    public boolean setVariable(int i, @Nullable Object obj) {
        if (2 != i) {
            return false;
        }
        setViewModel((IndexItemViewModel) obj);
        return true;
    }

    @Override // com.goldze.mvvmhabit.databinding.ItemBinding
    public void setViewModel(@Nullable IndexItemViewModel indexItemViewModel) {
        this.mViewModel = indexItemViewModel;
        synchronized (this) {
            this.mDirtyFlags |= 2;
        }
        notifyPropertyChanged(2);
        super.requestRebind();
    }

    @Override // android.databinding.ViewDataBinding
    protected boolean onFieldChange(int i, Object obj, int i2) {
        if (i != 0) {
            return false;
        }
        return onChangeViewModelEntity((ObservableField) obj, i2);
    }

    private boolean onChangeViewModelEntity(ObservableField<MovieEntity> observableField, int i) {
        if (i != 0) {
            return false;
        }
        synchronized (this) {
            this.mDirtyFlags |= 1;
        }
        return true;
    }

    @Override // android.databinding.ViewDataBinding
    protected void executeBindings() {
        long j;
        String str;
        String str2;
        Drawable drawable;
        BindingCommand bindingCommand;
        String str3;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0;
        }
        IndexItemViewModel indexItemViewModel = this.mViewModel;
        long j2 = 7 & j;
        String str4 = null;
        if (j2 != 0) {
            if ((j & 6) == 0 || indexItemViewModel == null) {
                bindingCommand = null;
                drawable = null;
            } else {
                bindingCommand = indexItemViewModel.itemClick;
                drawable = indexItemViewModel.drawableImg;
            }
            ObservableField<MovieEntity> observableField = indexItemViewModel != null ? indexItemViewModel.entity : null;
            updateRegistration(0, observableField);
            MovieEntity movieEntity = observableField != null ? observableField.get() : null;
            if (movieEntity != null) {
                str4 = movieEntity.getCover();
                str2 = movieEntity.getName();
                str = movieEntity.getScore();
                str3 = movieEntity.getCategories();
            } else {
                str3 = null;
                str2 = null;
                str = null;
            }
        } else {
            str3 = null;
            bindingCommand = null;
            drawable = null;
            str2 = null;
            str = null;
        }
        if ((j & 6) != 0) {
            ViewAdapter.onClickCommand(this.item, bindingCommand, false);
            ImageViewBindingAdapter.setImageDrawable(this.mboundView1, drawable);
        }
        if (j2 != 0) {
            me.goldze.mvvmhabit.binding.viewadapter.image.ViewAdapter.setImageUri(this.mboundView1, str4, 0);
            TextViewBindingAdapter.setText(this.tvDescription, str3);
            TextViewBindingAdapter.setText(this.tvScore, str);
            TextViewBindingAdapter.setText(this.tvTitle, str2);
        }
    }
}
