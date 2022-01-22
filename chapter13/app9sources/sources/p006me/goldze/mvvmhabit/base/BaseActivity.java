package p006me.goldze.mvvmhabit.base;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.p000v4.app.FragmentActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import p006me.goldze.mvvmhabit.base.BaseViewModel;
import p006me.goldze.mvvmhabit.bus.Messenger;
import p006me.goldze.mvvmhabit.utils.MaterialDialogUtils;

/* renamed from: me.goldze.mvvmhabit.base.BaseActivity */
/* loaded from: classes.dex */
public abstract class BaseActivity<V extends ViewDataBinding, VM extends BaseViewModel> extends RxAppCompatActivity implements IBaseView {
    protected V binding;
    private MaterialDialog dialog;
    protected VM viewModel;
    private int viewModelId;

    public abstract int initContentView(Bundle bundle);

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

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.trello.rxlifecycle2.components.support.RxAppCompatActivity, android.support.p003v7.app.AppCompatActivity, android.support.p000v4.app.FragmentActivity, android.support.p000v4.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initParam();
        initViewDataBinding(bundle);
        registorUIChangeLiveDataCallBack();
        initData();
        initViewObservable();
        this.viewModel.registerRxBus();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.trello.rxlifecycle2.components.support.RxAppCompatActivity, android.support.p003v7.app.AppCompatActivity, android.support.p000v4.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
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

    private void initViewDataBinding(Bundle bundle) {
        Class<BaseViewModel> cls;
        this.binding = (V) DataBindingUtil.setContentView(this, initContentView(bundle));
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

    public void refreshLayout() {
        VM vm = this.viewModel;
        if (vm != null) {
            this.binding.setVariable(this.viewModelId, vm);
        }
    }

    protected void registorUIChangeLiveDataCallBack() {
        this.viewModel.getUC().getShowDialogEvent().observe(this, new Observer<String>() { // from class: me.goldze.mvvmhabit.base.BaseActivity.1
            public void onChanged(@Nullable String str) {
                BaseActivity.this.showDialog(str);
            }
        });
        this.viewModel.getUC().getDismissDialogEvent().observe(this, new Observer<Void>() { // from class: me.goldze.mvvmhabit.base.BaseActivity.2
            public void onChanged(@Nullable Void r1) {
                BaseActivity.this.dismissDialog();
            }
        });
        this.viewModel.getUC().getStartActivityEvent().observe(this, new Observer<Map<String, Object>>() { // from class: me.goldze.mvvmhabit.base.BaseActivity.3
            public void onChanged(@Nullable Map<String, Object> map) {
                BaseActivity.this.startActivity((Class) map.get(BaseViewModel.ParameterField.CLASS), (Bundle) map.get(BaseViewModel.ParameterField.BUNDLE));
            }
        });
        this.viewModel.getUC().getStartContainerActivityEvent().observe(this, new Observer<Map<String, Object>>() { // from class: me.goldze.mvvmhabit.base.BaseActivity.4
            public void onChanged(@Nullable Map<String, Object> map) {
                BaseActivity.this.startContainerActivity((String) map.get(BaseViewModel.ParameterField.CANONICAL_NAME), (Bundle) map.get(BaseViewModel.ParameterField.BUNDLE));
            }
        });
        this.viewModel.getUC().getFinishEvent().observe(this, new Observer<Void>() { // from class: me.goldze.mvvmhabit.base.BaseActivity.5
            public void onChanged(@Nullable Void r1) {
                BaseActivity.this.finish();
            }
        });
        this.viewModel.getUC().getOnBackPressedEvent().observe(this, new Observer<Void>() { // from class: me.goldze.mvvmhabit.base.BaseActivity.6
            public void onChanged(@Nullable Void r1) {
                BaseActivity.this.onBackPressed();
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
        this.dialog = MaterialDialogUtils.showIndeterminateProgressDialog(this, str, true).show();
    }

    public void dismissDialog() {
        MaterialDialog materialDialog = this.dialog;
        if (materialDialog != null && materialDialog.isShowing()) {
            this.dialog.dismiss();
        }
    }

    public void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    public void startContainerActivity(String str) {
        startContainerActivity(str, null);
    }

    public void startContainerActivity(String str, Bundle bundle) {
        Intent intent = new Intent(this, ContainerActivity.class);
        intent.putExtra(ContainerActivity.FRAGMENT, str);
        if (bundle != null) {
            intent.putExtra(ContainerActivity.BUNDLE, bundle);
        }
        startActivity(intent);
    }

    public <T extends ViewModel> T createViewModel(FragmentActivity fragmentActivity, Class<T> cls) {
        return (T) ViewModelProviders.m59of(fragmentActivity).get(cls);
    }
}
