package p006me.goldze.mvvmhabit.http.interceptor;

import java.io.IOException;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.BaseInterceptor */
/* loaded from: classes.dex */
public class BaseInterceptor implements Interceptor {
    private Map<String, String> headers;

    public BaseInterceptor(Map<String, String> map) {
        this.headers = map;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request.Builder newBuilder = chain.request().newBuilder();
        Map<String, String> map = this.headers;
        if (map != null && map.size() > 0) {
            for (String str : this.headers.keySet()) {
                newBuilder.addHeader(str, this.headers.get(str)).build();
            }
        }
        return chain.proceed(newBuilder.build());
    }
}
