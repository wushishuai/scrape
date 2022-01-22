package p006me.goldze.mvvmhabit.http;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import p005io.reactivex.Observable;
import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.schedulers.Schedulers;
import p006me.goldze.mvvmhabit.http.download.DownLoadSubscriber;
import p006me.goldze.mvvmhabit.http.download.ProgressCallBack;
import p006me.goldze.mvvmhabit.http.interceptor.ProgressInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/* renamed from: me.goldze.mvvmhabit.http.DownLoadManager */
/* loaded from: classes.dex */
public class DownLoadManager {
    private static DownLoadManager instance;
    private static Retrofit retrofit;

    /* renamed from: me.goldze.mvvmhabit.http.DownLoadManager$ApiService */
    /* loaded from: classes.dex */
    private interface ApiService {
        @Streaming
        @GET
        Observable<ResponseBody> download(@Url String str);
    }

    private DownLoadManager() {
        buildNetWork();
    }

    public static DownLoadManager getInstance() {
        if (instance == null) {
            instance = new DownLoadManager();
        }
        return instance;
    }

    public void load(String str, final ProgressCallBack progressCallBack) {
        ((ApiService) retrofit.create(ApiService.class)).download(str).subscribeOn(Schedulers.m27io()).observeOn(Schedulers.m27io()).doOnNext(new Consumer<ResponseBody>() { // from class: me.goldze.mvvmhabit.http.DownLoadManager.1
            public void accept(ResponseBody responseBody) throws Exception {
                progressCallBack.saveFile(responseBody);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new DownLoadSubscriber(progressCallBack));
    }

    private void buildNetWork() {
        retrofit = new Retrofit.Builder().client(new OkHttpClient.Builder().addInterceptor(new ProgressInterceptor()).connectTimeout(20, TimeUnit.SECONDS).build()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).baseUrl(NetworkUtil.url).build();
    }
}
