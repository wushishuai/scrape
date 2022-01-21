package p006me.goldze.mvvmhabit.http.interceptor;

import android.content.Context;
import java.io.IOException;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import p006me.goldze.mvvmhabit.http.NetworkUtil;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.CacheInterceptor */
/* loaded from: classes.dex */
public class CacheInterceptor implements Interceptor {
    private Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (NetworkUtil.isNetworkAvailable(this.context)) {
            Response.Builder removeHeader = chain.proceed(request).newBuilder().removeHeader("Pragma").removeHeader("Cache-Control");
            return removeHeader.header("Cache-Control", "public, max-age=60").build();
        }
        Response.Builder removeHeader2 = chain.proceed(request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build()).newBuilder().removeHeader("Pragma").removeHeader("Cache-Control");
        return removeHeader2.header("Cache-Control", "public, only-if-cached, max-stale=259200").build();
    }
}
