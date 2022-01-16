package com.goldze.mvvmhabit.databinding;

import android.databinding.DataBindingComponent;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.databinding.adapters.TextViewBindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.goldze.mvvmhabit.C0691R;
import com.goldze.mvvmhabit.entity.MovieEntity;
import com.goldze.mvvmhabit.p004ui.detail.DetailViewModel;
import p006me.goldze.mvvmhabit.binding.viewadapter.image.ViewAdapter;

/* loaded from: classes.dex */
public class FragmentDetailBindingImpl extends FragmentDetailBinding {
    @Nullable
    private static final ViewDataBinding.IncludedLayouts sIncludes = null;
    @Nullable
    private static final SparseIntArray sViewsWithIds = new SparseIntArray();
    private long mDirtyFlags;

    static {
        sViewsWithIds.put(C0691R.C0694id.categories_key, 8);
        sViewsWithIds.put(C0691R.C0694id.score_key, 9);
        sViewsWithIds.put(C0691R.C0694id.minute_key, 10);
        sViewsWithIds.put(C0691R.C0694id.published_at_key, 11);
        sViewsWithIds.put(C0691R.C0694id.drama_key, 12);
    }

    public FragmentDetailBindingImpl(@Nullable DataBindingComponent dataBindingComponent, @NonNull View view) {
        this(dataBindingComponent, view, mapBindings(dataBindingComponent, view, 13, sIncludes, sViewsWithIds));
    }

    private FragmentDetailBindingImpl(DataBindingComponent dataBindingComponent, View view, Object[] objArr) {
        super(dataBindingComponent, view, 1, (TextView) objArr[8], (TextView) objArr[3], (ImageView) objArr[1], (ScrollView) objArr[0], (TextView) objArr[12], (TextView) objArr[7], (TextView) objArr[10], (TextView) objArr[5], (TextView) objArr[11], (TextView) objArr[6], (TextView) objArr[9], (TextView) objArr[4], (TextView) objArr[2]);
        this.mDirtyFlags = -1;
        this.categoriesValue.setTag(null);
        this.cover.setTag(null);
        this.detail.setTag(null);
        this.dramaValue.setTag(null);
        this.minuteValue.setTag(null);
        this.publishedAtValue.setTag(null);
        this.scoreValue.setTag(null);
        this.title.setTag(null);
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
        setViewModel((DetailViewModel) obj);
        return true;
    }

    @Override // com.goldze.mvvmhabit.databinding.FragmentDetailBinding
    public void setViewModel(@Nullable DetailViewModel detailViewModel) {
        this.mViewModel = detailViewModel;
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
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        String str9;
        synchronized (this) {
            j = this.mDirtyFlags;
            this.mDirtyFlags = 0;
        }
        DetailViewModel detailViewModel = this.mViewModel;
        long j2 = j & 7;
        String str10 = null;
        if (j2 != 0) {
            ObservableField<MovieEntity> observableField = detailViewModel != null ? detailViewModel.entity : null;
            updateRegistration(0, observableField);
            MovieEntity movieEntity = observableField != null ? observableField.get() : null;
            if (movieEntity != null) {
                str10 = movieEntity.getPublishedAt();
                str5 = movieEntity.getCover();
                str4 = movieEntity.getDrama();
                str8 = movieEntity.getName();
                str2 = movieEntity.getScore();
                str7 = movieEntity.getCategories();
                str9 = movieEntity.getMinute();
            } else {
                str9 = null;
                str5 = null;
                str4 = null;
                str8 = null;
                str2 = null;
                str7 = null;
            }
            str6 = str9 + "分钟";
            str3 = str10;
            str10 = str7;
            str = str8;
        } else {
            str6 = null;
            str5 = null;
            str4 = null;
            str3 = null;
            str2 = null;
            str = null;
        }
        if (j2 != 0) {
            TextViewBindingAdapter.setText(this.categoriesValue, str10);
            ViewAdapter.setImageUri(this.cover, str5, C0691R.mipmap.ic_launcher_round);
            TextViewBindingAdapter.setText(this.dramaValue, str4);
            TextViewBindingAdapter.setText(this.minuteValue, str6);
            TextViewBindingAdapter.setText(this.publishedAtValue, str3);
            TextViewBindingAdapter.setText(this.scoreValue, str2);
            TextViewBindingAdapter.setText(this.title, str);
        }
    }
}
