package com.goldze.mvvmhabit.p004ui.index;

import android.annotation.SuppressLint;
import android.app.Application;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import com.goldze.mvvmhabit.C0690R;
import com.goldze.mvvmhabit.data.MainRepository;
import com.goldze.mvvmhabit.data.source.HttpResponse;
import com.goldze.mvvmhabit.entity.MovieEntity;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.Consumer;
import p006me.goldze.mvvmhabit.base.BaseViewModel;
import p006me.goldze.mvvmhabit.binding.command.BindingAction;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;
import p006me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import p006me.goldze.mvvmhabit.http.ResponseThrowable;
import p006me.goldze.mvvmhabit.utils.RxUtils;
import p006me.goldze.mvvmhabit.utils.ToastUtils;
import p006me.tatarka.bindingcollectionadapter2.ItemBinding;

/* renamed from: com.goldze.mvvmhabit.ui.index.IndexViewModel */
/* loaded from: classes.dex */
public class IndexViewModel extends BaseViewModel<MainRepository> {
    public SingleLiveEvent<IndexItemViewModel> deleteItemLiveData = new SingleLiveEvent<>();

    /* renamed from: uc */
    public UIChangeObservable f67uc = new UIChangeObservable();
    private int page = 1;
    private int limit = 10;
    private int count = this.limit;
    public ObservableList<IndexItemViewModel> observableList = new ObservableArrayList();
    public ItemBinding<IndexItemViewModel> itemBinding = ItemBinding.m8of(2, C0690R.layout.item);
    public BindingCommand onRefreshCommand = new BindingCommand(new BindingAction() { // from class: com.goldze.mvvmhabit.ui.index.IndexViewModel.1
        @Override // p006me.goldze.mvvmhabit.binding.command.BindingAction
        public void call() {
            IndexViewModel.this.requestNetWork();
        }
    });
    public BindingCommand onLoadMoreCommand = new BindingCommand(new BindingAction() { // from class: com.goldze.mvvmhabit.ui.index.IndexViewModel.2
        @Override // p006me.goldze.mvvmhabit.binding.command.BindingAction
        public void call() {
            if (IndexViewModel.this.page > IndexViewModel.this.count / IndexViewModel.this.limit) {
                IndexViewModel.this.f67uc.finishLoadMore.call();
            } else {
                IndexViewModel.this.requestNetWork();
            }
        }
    });

    /* renamed from: com.goldze.mvvmhabit.ui.index.IndexViewModel$UIChangeObservable */
    /* loaded from: classes.dex */
    public class UIChangeObservable {
        public SingleLiveEvent finishRefreshing = new SingleLiveEvent();
        public SingleLiveEvent finishLoadMore = new SingleLiveEvent();

        public UIChangeObservable() {
            IndexViewModel.this = r1;
        }
    }

    public IndexViewModel(@NonNull Application application, MainRepository mainRepository) {
        super(application, mainRepository);
    }

    @SuppressLint({"CheckResult"})
    public void requestNetWork() {
        ((MainRepository) this.model).index(this.page, this.limit).compose(RxUtils.schedulersTransformer()).compose(RxUtils.exceptionTransformer()).doOnSubscribe(this).doOnSubscribe(new Consumer<Disposable>() { // from class: com.goldze.mvvmhabit.ui.index.IndexViewModel.6
            public void accept(Disposable disposable) throws Exception {
            }
        }).subscribe(new Consumer<HttpResponse<MovieEntity>>() { // from class: com.goldze.mvvmhabit.ui.index.IndexViewModel.3
            public void accept(HttpResponse<MovieEntity> httpResponse) throws Exception {
                if (httpResponse.getCount() > 0) {
                    IndexViewModel.this.count = httpResponse.getCount();
                }
                if (httpResponse.getResults().size() > 0) {
                    for (MovieEntity movieEntity : httpResponse.getResults()) {
                        IndexViewModel.this.observableList.add(new IndexItemViewModel(IndexViewModel.this, movieEntity));
                    }
                    IndexViewModel.this.page++;
                    IndexViewModel.this.f67uc.finishLoadMore.call();
                    return;
                }
                IndexViewModel.this.f67uc.finishLoadMore.call();
                ToastUtils.showShort("????????????");
            }
        }, new Consumer<Throwable>() { // from class: com.goldze.mvvmhabit.ui.index.IndexViewModel.4
            public void accept(Throwable th) throws Exception {
                IndexViewModel.this.f67uc.finishLoadMore.call();
                if (th instanceof ResponseThrowable) {
                    ToastUtils.showShort(((ResponseThrowable) th).message);
                }
            }
        }, new Action() { // from class: com.goldze.mvvmhabit.ui.index.IndexViewModel.5
            @Override // p005io.reactivex.functions.Action
            public void run() throws Exception {
                IndexViewModel.this.f67uc.finishLoadMore.call();
            }
        });
    }

    public int getItemPosition(IndexItemViewModel indexItemViewModel) {
        return this.observableList.indexOf(indexItemViewModel);
    }

    @Override // p006me.goldze.mvvmhabit.base.BaseViewModel, p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onDestroy() {
        super.onDestroy();
    }
}
