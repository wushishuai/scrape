package p006me.goldze.mvvmhabit.base;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.p000v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.materialdialogs.MaterialDialog;
import com.trello.rxlifecycle2.components.support.RxFragment;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import p006me.goldze.mvvmhabit.base.BaseViewModel;
import p006me.goldze.mvvmhabit.bus.Messenger;
import p006me.goldze.mvvmhabit.utils.MaterialDialogUtils;

/* renamed from: me.goldze.mvvmhabit.base.BaseFragment */
/* loaded from: classes.dex */
public abstract class BaseFragment<V extends ViewDataBinding, VM extends BaseViewModel> extends RxFragment implements IBaseView {
    protected V binding;
    private MaterialDialog dialog;
    protected VM viewModel;
    private int viewModelId;

    public abstract int initContentView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle);

    @Override // p006me.goldze.mvvmhabit.base.IBaseView
    public void initData() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseView
    public void initParam() {
    }

    public abstract int initVariableId();

    public VM initViewModel() {
        return null;
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseView
    public void initViewObservable() {
    }

    public boolean isBackPressed() {
        return false;
    }

    @Override // com.trello.rxlifecycle2.components.support.RxFragment, android.support.p000v4.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initParam();
    }

    @Override // com.trello.rxlifecycle2.components.support.RxFragment, android.support.p000v4.app.Fragment
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // android.support.p000v4.app.Fragment
    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        this.binding = (V) DataBindingUtil.inflate(layoutInflater, initContentView(layoutInflater, viewGroup, bundle), viewGroup, false);
        return this.binding.getRoot();
    }

    @Override // com.trello.rxlifecycle2.components.support.RxFragment, android.support.p000v4.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        Messenger.getDefault().unregister(this.viewModel);
        VM vm = this.viewModel;
        if (vm != null) {
            vm.removeRxBus();
        }
        V v = this.binding;
        if (v != null) {
            v.unbind();
        }
    }

    @Override // com.trello.rxlifecycle2.components.support.RxFragment, android.support.p000v4.app.Fragment
    public void onViewCreated(View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
        initViewDataBinding();
        registorUIChangeLiveDataCallBack();
        initData();
        initViewObservable();
        this.viewModel.registerRxBus();
    }

    private void initViewDataBinding() {
        Class<BaseViewModel> cls;
        this.viewModelId = initVariableId();
        this.viewModel = initViewModel();
        if (this.viewModel == null) {
            Type genericSuperclass = getClass().getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                cls = (Class) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[1];
            } else {
                cls = BaseViewModel.class;
            }
            this.viewModel = (VM) ((BaseViewModel) createViewModel(this, cls));
        }
        this.binding.setVariable(this.viewModelId, this.viewModel);
        this.binding.setLifecycleOwner(this);
        getLifecycle().addObserver(this.viewModel);
        this.viewModel.injectLifecycleProvider(this);
    }

    protected void registorUIChangeLiveDataCallBack() {
        this.viewModel.getUC().getShowDialogEvent().observe(this, new Observer<String>() { // from class: me.goldze.mvvmhabit.base.BaseFragment.1
            public void onChanged(@Nullable String str) {
                BaseFragment.this.showDialog(str);
            }
        });
        this.viewModel.getUC().getDismissDialogEvent().observe(this, new Observer<Void>() { // from class: me.goldze.mvvmhabit.base.BaseFragment.2
            public void onChanged(@Nullable Void r1) {
                BaseFragment.this.dismissDialog();
            }
        });
        this.viewModel.getUC().getStartActivityEvent().observe(this, new Observer<Map<String, Object>>() { // from class: me.goldze.mvvmhabit.base.BaseFragment.3
            public void onChanged(@Nullable Map<String, Object> map) {
                BaseFragment.this.startActivity((Class) map.get(BaseViewModel.ParameterField.CLASS), (Bundle) map.get(BaseViewModel.ParameterField.BUNDLE));
            }
        });
        this.viewModel.getUC().getStartContainerActivityEvent().observe(this, new Observer<Map<String, Object>>() { // from class: me.goldze.mvvmhabit.base.BaseFragment.4
            public void onChanged(@Nullable Map<String, Object> map) {
                BaseFragment.this.startContainerActivity((String) map.get(BaseViewModel.ParameterField.CANONICAL_NAME), (Bundle) map.get(BaseViewModel.ParameterField.BUNDLE));
            }
        });
        this.viewModel.getUC().getFinishEvent().observe(this, new Observer<Void>() { // from class: me.goldze.mvvmhabit.base.BaseFragment.5
            public void onChanged(@Nullable Void r1) {
                BaseFragment.this.getActivity().finish();
            }
        });
        this.viewModel.getUC().getOnBackPressedEvent().observe(this, new Observer<Void>() { // from class: me.goldze.mvvmhabit.base.BaseFragment.6
            public void onChanged(@Nullable Void r1) {
                BaseFragment.this.getActivity().onBackPressed();
            }
        });
    }

    public void showDialog(String str) {
        MaterialDialog materialDialog = this.dialog;
        if (materialDialog != null) {
            this.dialog = materialDialog.getBuilder().title(str).build();
            this.dialog.show();
            return;
        }
        this.dialog = MaterialDialogUtils.showIndeterminateProgressDialog(getActivity(), str, true).show();
    }

    public void dismissDialog() {
        MaterialDialog materialDialog = this.dialog;
        if (materialDialog != null && materialDialog.isShowing()) {
            this.dialog.dismiss();
        }
    }

    public void startActivity(Class<?> cls) {
        startActivity(new Intent(getContext(), cls));
    }

    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(getContext(), cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void startContainerActivity(String str) {
        startContainerActivity(str, null);
    }

    public void startContainerActivity(String str, Bundle bundle) {
        Intent intent = new Intent(getContext(), ContainerActivity.class);
        intent.putExtra(ContainerActivity.FRAGMENT, str);
        if (bundle != null) {
            intent.putExtra(ContainerActivity.BUNDLE, bundle);
        }
        startActivity(intent);
    }

    public void refreshLayout() {
        VM vm = this.viewModel;
        if (vm != null) {
            this.binding.setVariable(this.viewModelId, vm);
        }
    }

    public <T extends ViewModel> T createViewModel(Fragment fragment, Class<T> cls) {
        return (T) ViewModelProviders.m61of(fragment).get(cls);
    }
}
