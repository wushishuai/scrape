package p006me.goldze.mvvmhabit.http.interceptor.logging;

import android.text.TextUtils;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.logging.LoggingInterceptor */
/* loaded from: classes.dex */
public class LoggingInterceptor implements Interceptor {
    private Builder builder;
    private boolean isDebug;

    private LoggingInterceptor(Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug;
    }

    /* JADX INFO: Multiple debug info for r3v9 okhttp3.ResponseBody: [D('bodyString' java.lang.String), D('body' okhttp3.ResponseBody)] */
    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        String subtype;
        Request request = chain.request();
        if (this.builder.getHeaders().size() > 0) {
            Headers headers = request.headers();
            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.headers(this.builder.getHeaders());
            for (String name : headers.names()) {
                requestBuilder.addHeader(name, headers.get(name));
            }
            request = requestBuilder.build();
        }
        if (!this.isDebug || this.builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }
        MediaType rContentType = null;
        if (request.body() != null) {
            rContentType = request.body().contentType();
        }
        String rSubtype = null;
        if (rContentType != null) {
            rSubtype = rContentType.subtype();
        }
        if (rSubtype == null || (!rSubtype.contains("json") && !rSubtype.contains("xml") && !rSubtype.contains("plain") && !rSubtype.contains("html"))) {
            Printer.printFileRequest(this.builder, request);
        } else {
            Printer.printJsonRequest(this.builder, request);
        }
        long st = System.nanoTime();
        Response response = chain.proceed(request);
        List<String> segmentList = ((Request) request.tag()).url().encodedPathSegments();
        long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);
        String header = response.headers().toString();
        int code = response.code();
        boolean isSuccessful = response.isSuccessful();
        ResponseBody responseBody = response.body();
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            subtype = contentType.subtype();
        } else {
            subtype = null;
        }
        if (subtype != null && (subtype.contains("json") || subtype.contains("xml") || subtype.contains("plain") || subtype.contains("html"))) {
            String bodyString = responseBody.string();
            Printer.printJsonResponse(this.builder, chainMs, isSuccessful, code, header, Printer.getJsonString(bodyString), segmentList);
            return response.newBuilder().body(ResponseBody.create(contentType, bodyString)).build();
        }
        Printer.printFileResponse(this.builder, chainMs, isSuccessful, code, header, segmentList);
        return response;
    }

    /* renamed from: me.goldze.mvvmhabit.http.interceptor.logging.LoggingInterceptor$Builder */
    /* loaded from: classes.dex */
    public static class Builder {
        private static String TAG = "LoggingI";
        private boolean isDebug;
        private Logger logger;
        private String requestTag;
        private String responseTag;
        private int type = 4;
        private Level level = Level.BASIC;
        private Headers.Builder builder = new Headers.Builder();

        public int getType() {
            return this.type;
        }

        public Level getLevel() {
            return this.level;
        }

        Headers getHeaders() {
            return this.builder.build();
        }

        public String getTag(boolean isRequest) {
            return isRequest ? TextUtils.isEmpty(this.requestTag) ? TAG : this.requestTag : TextUtils.isEmpty(this.responseTag) ? TAG : this.responseTag;
        }

        public Logger getLogger() {
            return this.logger;
        }

        public Builder addHeader(String name, String value) {
            this.builder.set(name, value);
            return this;
        }

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder tag(String tag) {
            TAG = tag;
            return this;
        }

        public Builder request(String tag) {
            this.requestTag = tag;
            return this;
        }

        public Builder response(String tag) {
            this.responseTag = tag;
            return this;
        }

        public Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder log(int type) {
            this.type = type;
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }
    }
}
