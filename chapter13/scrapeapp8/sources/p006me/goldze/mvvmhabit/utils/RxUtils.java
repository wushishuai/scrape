package p006me.goldze.mvvmhabit.utils;

import android.content.Context;
import android.support.p000v4.app.Fragment;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.LifecycleTransformer;
import p005io.reactivex.Observable;
import p005io.reactivex.ObservableSource;
import p005io.reactivex.ObservableTransformer;
import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.functions.Function;
import p005io.reactivex.schedulers.Schedulers;
import p006me.goldze.mvvmhabit.http.BaseResponse;
import p006me.goldze.mvvmhabit.http.ExceptionHandle;

/* renamed from: me.goldze.mvvmhabit.utils.RxUtils */
/* loaded from: classes.dex */
public class RxUtils {
    public static <T> LifecycleTransformer<T> bindToLifecycle(@NonNull Context lifecycle) {
        if (lifecycle instanceof LifecycleProvider) {
            return ((LifecycleProvider) lifecycle).bindToLifecycle();
        }
        throw new IllegalArgumentException("context not the LifecycleProvider type");
    }

    public static LifecycleTransformer bindToLifecycle(@NonNull Fragment lifecycle) {
        if (lifecycle instanceof LifecycleProvider) {
            return ((LifecycleProvider) lifecycle).bindToLifecycle();
        }
        throw new IllegalArgumentException("fragment not the LifecycleProvider type");
    }

    public static LifecycleTransformer bindToLifecycle(@NonNull LifecycleProvider lifecycle) {
        return lifecycle.bindToLifecycle();
    }

    public static ObservableTransformer schedulersTransformer() {
        return new ObservableTransformer() { // from class: me.goldze.mvvmhabit.utils.RxUtils.1
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource apply(Observable upstream) {
                return upstream.subscribeOn(Schedulers.m27io()).observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static ObservableTransformer exceptionTransformer() {
        return new ObservableTransformer() { // from class: me.goldze.mvvmhabit.utils.RxUtils.2
            @Override // p005io.reactivex.ObservableTransformer
            public ObservableSource apply(Observable observable) {
                return observable.onErrorResumeNext(new HttpResponseFunc());
            }
        };
    }

    /* renamed from: me.goldze.mvvmhabit.utils.RxUtils$HttpResponseFunc */
    /* loaded from: classes.dex */
    private static class HttpResponseFunc<T> implements Function<Throwable, Observable<T>> {
        private HttpResponseFunc() {
        }

        public Observable<T> apply(Throwable t) {
            return Observable.error(ExceptionHandle.handleException(t));
        }
    }

    /* renamed from: me.goldze.mvvmhabit.utils.RxUtils$HandleFuc */
    /* loaded from: classes.dex */
    private static class HandleFuc<T> implements Function<BaseResponse<T>, T> {
        private HandleFuc() {
        }

        @Override // p005io.reactivex.functions.Function
        public /* bridge */ /* synthetic */ Object apply(Object obj) throws Exception {
            return apply((BaseResponse) ((BaseResponse) obj));
        }

        public T apply(BaseResponse<T> response) {
            if (response.isOk()) {
                return response.getResult();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(response.getCode());
            sb.append("");
            sb.append(response.getMessage());
            throw new RuntimeException(!"".equals(sb.toString()) ? response.getMessage() : "");
        }
    }
}
