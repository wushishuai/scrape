package me.goldze.mvvmhabit.http.interceptor;

import java.io.IOException;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
/* loaded from: classes.dex */
public class BaseInterceptor implements Interceptor {
    private Map<String, String> headers;

    public BaseInterceptor(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        Map<String, String> map = this.headers;
        if (map != null && map.size() > 0) {
            for (String headerKey : this.headers.keySet()) {
                builder.addHeader(headerKey, this.headers.get(headerKey)).build();
            }
        }
        return chain.proceed(builder.build());
    }
}
