package com.goldze.mvvmhabit.utils;

import android.content.Context;
import android.text.TextUtils;
import com.goldze.mvvmhabit.utils.HttpsUtils;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.android.schedulers.AndroidSchedulers;
import p005io.reactivex.schedulers.Schedulers;
import p006me.goldze.mvvmhabit.http.cookie.CookieJarImpl;
import p006me.goldze.mvvmhabit.http.cookie.store.PersistentCookieStore;
import p006me.goldze.mvvmhabit.http.interceptor.BaseInterceptor;
import p006me.goldze.mvvmhabit.http.interceptor.CacheInterceptor;
import p006me.goldze.mvvmhabit.http.interceptor.logging.Level;
import p006me.goldze.mvvmhabit.http.interceptor.logging.LoggingInterceptor;
import p006me.goldze.mvvmhabit.utils.KLog;
import p006me.goldze.mvvmhabit.utils.Utils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/* loaded from: classes.dex */
public class RetrofitClient {
    private static final int CACHE_TIMEOUT = 10485760;
    private static final int DEFAULT_TIMEOUT = 20;
    public static String baseUrl = "https://app9.scrape.center";
    private static Context mContext = Utils.getContext();
    private static OkHttpClient okHttpClient;
    private static Retrofit retrofit;
    private Cache cache;
    private File httpCacheDirectory;

    /* loaded from: classes.dex */
    private static class SingletonHolder {
        private static RetrofitClient INSTANCE = new RetrofitClient();

        private SingletonHolder() {
        }
    }

    public static RetrofitClient getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private RetrofitClient() {
        this(baseUrl, null);
    }

    private RetrofitClient(String str, Map<String, String> map) {
        this.cache = null;
        str = TextUtils.isEmpty(str) ? baseUrl : str;
        if (this.httpCacheDirectory == null) {
            this.httpCacheDirectory = new File(mContext.getCacheDir(), "goldze_cache");
        }
        try {
            if (this.cache == null) {
                this.cache = new Cache(this.httpCacheDirectory, 10485760);
            }
        } catch (Exception e) {
            KLog.m18e("Could not create http cache", e);
        }
        HttpsUtils.SSLParams sslSocketFactory = HttpsUtils.getSslSocketFactory();
        okHttpClient = new OkHttpClient.Builder().cookieJar(new CookieJarImpl(new PersistentCookieStore(mContext))).addInterceptor(new BaseInterceptor(map)).addInterceptor(new CacheInterceptor(mContext)).sslSocketFactory(sslSocketFactory.sSLSocketFactory, sslSocketFactory.trustManager).addInterceptor(new LoggingInterceptor.Builder().loggable(false).setLevel(Level.BASIC).log(4).request("Request").response("HttpResponse").addHeader("log-header", "I am the log request header.").build()).connectTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).connectionPool(new ConnectionPool(8, 15, TimeUnit.SECONDS)).build();
        retrofit = new Retrofit.Builder().client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).baseUrl(str).build();
    }

    public <T> T create(Class<T> cls) {
        if (cls != null) {
            return (T) retrofit.create(cls);
        }
        throw new RuntimeException("Api service is null!");
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T execute(Observable<T> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.m27io()).unsubscribeOn(Schedulers.m27io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
        return null;
    }
}
