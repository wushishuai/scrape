package p006me.goldze.mvvmhabit.base;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.trello.rxlifecycle2.LifecycleProvider;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Consumer;
import p006me.goldze.mvvmhabit.base.BaseModel;
import p006me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

/* renamed from: me.goldze.mvvmhabit.base.BaseViewModel */
/* loaded from: classes.dex */
public class BaseViewModel<M extends BaseModel> extends AndroidViewModel implements IBaseViewModel, Consumer<Disposable> {
    private WeakReference<LifecycleProvider> lifecycle;
    private CompositeDisposable mCompositeDisposable;
    protected M model;

    /* renamed from: uc */
    private BaseViewModel<M>.UIChangeLiveData f205uc;

    /* renamed from: me.goldze.mvvmhabit.base.BaseViewModel$ParameterField */
    /* loaded from: classes.dex */
    public static final class ParameterField {
        public static String BUNDLE = "BUNDLE";
        public static String CANONICAL_NAME = "CANONICAL_NAME";
        public static String CLASS = "CLASS";
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onAny(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onCreate() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onDestroy() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onPause() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onResume() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onStart() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void onStop() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void registerRxBus() {
    }

    @Override // p006me.goldze.mvvmhabit.base.IBaseViewModel
    public void removeRxBus() {
    }

    public BaseViewModel(@NonNull Application application) {
        this(application, null);
    }

    public BaseViewModel(@NonNull Application application, M m) {
        super(application);
        this.model = m;
        this.mCompositeDisposable = new CompositeDisposable();
    }

    protected void addSubscribe(Disposable disposable) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }
        this.mCompositeDisposable.add(disposable);
    }

    public void injectLifecycleProvider(LifecycleProvider lifecycleProvider) {
        this.lifecycle = new WeakReference<>(lifecycleProvider);
    }

    public LifecycleProvider getLifecycleProvider() {
        return this.lifecycle.get();
    }

    public BaseViewModel<M>.UIChangeLiveData getUC() {
        if (this.f205uc == null) {
            this.f205uc = new UIChangeLiveData();
        }
        return this.f205uc;
    }

    public void showDialog() {
        showDialog("请稍后...");
    }

    public void showDialog(String str) {
        ((UIChangeLiveData) this.f205uc).showDialogEvent.postValue(str);
    }

    public void dismissDialog() {
        ((UIChangeLiveData) this.f205uc).dismissDialogEvent.call();
    }

    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }

    public void startActivity(Class<?> cls, Bundle bundle) {
        HashMap hashMap = new HashMap();
        hashMap.put(ParameterField.CLASS, cls);
        if (bundle != null) {
            hashMap.put(ParameterField.BUNDLE, bundle);
        }
        ((UIChangeLiveData) this.f205uc).startActivityEvent.postValue(hashMap);
    }

    public void startContainerActivity(String str) {
        startContainerActivity(str, null);
    }

    public void startContainerActivity(String str, Bundle bundle) {
        HashMap hashMap = new HashMap();
        hashMap.put(ParameterField.CANONICAL_NAME, str);
        if (bundle != null) {
            hashMap.put(ParameterField.BUNDLE, bundle);
        }
        ((UIChangeLiveData) this.f205uc).startContainerActivityEvent.postValue(hashMap);
    }

    public void finish() {
        ((UIChangeLiveData) this.f205uc).finishEvent.call();
    }

    public void onBackPressed() {
        ((UIChangeLiveData) this.f205uc).onBackPressedEvent.call();
    }

    @Override // android.arch.lifecycle.ViewModel
    public void onCleared() {
        super.onCleared();
        M m = this.model;
        if (m != null) {
            m.onCleared();
        }
        CompositeDisposable compositeDisposable = this.mCompositeDisposable;
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    public void accept(Disposable disposable) throws Exception {
        addSubscribe(disposable);
    }

    /* renamed from: me.goldze.mvvmhabit.base.BaseViewModel$UIChangeLiveData */
    /* loaded from: classes.dex */
    public final class UIChangeLiveData extends SingleLiveEvent {
        private SingleLiveEvent<Void> dismissDialogEvent;
        private SingleLiveEvent<Void> finishEvent;
        private SingleLiveEvent<Void> onBackPressedEvent;
        private SingleLiveEvent<String> showDialogEvent;
        private SingleLiveEvent<Map<String, Object>> startActivityEvent;
        private SingleLiveEvent<Map<String, Object>> startContainerActivityEvent;

        public UIChangeLiveData() {
            BaseViewModel.this = r1;
        }

        public SingleLiveEvent<String> getShowDialogEvent() {
            SingleLiveEvent<String> createLiveData = createLiveData(this.showDialogEvent);
            this.showDialogEvent = createLiveData;
            return createLiveData;
        }

        public SingleLiveEvent<Void> getDismissDialogEvent() {
            SingleLiveEvent<Void> createLiveData = createLiveData(this.dismissDialogEvent);
            this.dismissDialogEvent = createLiveData;
            return createLiveData;
        }

        public SingleLiveEvent<Map<String, Object>> getStartActivityEvent() {
            SingleLiveEvent<Map<String, Object>> createLiveData = createLiveData(this.startActivityEvent);
            this.startActivityEvent = createLiveData;
            return createLiveData;
        }

        public SingleLiveEvent<Map<String, Object>> getStartContainerActivityEvent() {
            SingleLiveEvent<Map<String, Object>> createLiveData = createLiveData(this.startContainerActivityEvent);
            this.startContainerActivityEvent = createLiveData;
            return createLiveData;
        }

        public SingleLiveEvent<Void> getFinishEvent() {
            SingleLiveEvent<Void> createLiveData = createLiveData(this.finishEvent);
            this.finishEvent = createLiveData;
            return createLiveData;
        }

        public SingleLiveEvent<Void> getOnBackPressedEvent() {
            SingleLiveEvent<Void> createLiveData = createLiveData(this.onBackPressedEvent);
            this.onBackPressedEvent = createLiveData;
            return createLiveData;
        }

        private <T> SingleLiveEvent<T> createLiveData(SingleLiveEvent<T> singleLiveEvent) {
            return singleLiveEvent == null ? new SingleLiveEvent<>() : singleLiveEvent;
        }

        @Override // p006me.goldze.mvvmhabit.bus.event.SingleLiveEvent, android.arch.lifecycle.LiveData
        public void observe(LifecycleOwner lifecycleOwner, Observer observer) {
            super.observe(lifecycleOwner, observer);
        }
    }
}
