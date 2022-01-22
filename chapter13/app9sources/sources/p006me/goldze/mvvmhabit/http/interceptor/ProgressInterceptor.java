package p006me.goldze.mvvmhabit.http.interceptor;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;
import p006me.goldze.mvvmhabit.http.download.ProgressResponseBody;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.ProgressInterceptor */
/* loaded from: classes.dex */
public class ProgressInterceptor implements Interceptor {
    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response proceed = chain.proceed(chain.request());
        return proceed.newBuilder().body(new ProgressResponseBody(proceed.body())).build();
    }
}
