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

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (this.builder.getHeaders().size() > 0) {
            Headers headers = request.headers();
            Request.Builder newBuilder = request.newBuilder();
            newBuilder.headers(this.builder.getHeaders());
            for (String str : headers.names()) {
                newBuilder.addHeader(str, headers.get(str));
            }
            request = newBuilder.build();
        }
        if (!this.isDebug || this.builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }
        String str2 = null;
        MediaType contentType = request.body() != null ? request.body().contentType() : null;
        String subtype = contentType != null ? contentType.subtype() : null;
        if (subtype == null || (!subtype.contains("json") && !subtype.contains("xml") && !subtype.contains("plain") && !subtype.contains("html"))) {
            Printer.printFileRequest(this.builder, request);
        } else {
            Printer.printJsonRequest(this.builder, request);
        }
        long nanoTime = System.nanoTime();
        Response proceed = chain.proceed(request);
        List<String> encodedPathSegments = ((Request) request.tag()).url().encodedPathSegments();
        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - nanoTime);
        String headers2 = proceed.headers().toString();
        int code = proceed.code();
        boolean isSuccessful = proceed.isSuccessful();
        ResponseBody body = proceed.body();
        MediaType contentType2 = body.contentType();
        if (contentType2 != null) {
            str2 = contentType2.subtype();
        }
        if (str2 == null || (!str2.contains("json") && !str2.contains("xml") && !str2.contains("plain") && !str2.contains("html"))) {
            Printer.printFileResponse(this.builder, millis, isSuccessful, code, headers2, encodedPathSegments);
            return proceed;
        }
        String string = body.string();
        Printer.printJsonResponse(this.builder, millis, isSuccessful, code, headers2, Printer.getJsonString(string), encodedPathSegments);
        return proceed.newBuilder().body(ResponseBody.create(contentType2, string)).build();
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

        public String getTag(boolean z) {
            return z ? TextUtils.isEmpty(this.requestTag) ? TAG : this.requestTag : TextUtils.isEmpty(this.responseTag) ? TAG : this.responseTag;
        }

        public Logger getLogger() {
            return this.logger;
        }

        public Builder addHeader(String str, String str2) {
            this.builder.set(str, str2);
            return this;
        }

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder tag(String str) {
            TAG = str;
            return this;
        }

        public Builder request(String str) {
            this.requestTag = str;
            return this;
        }

        public Builder response(String str) {
            this.responseTag = str;
            return this;
        }

        public Builder loggable(boolean z) {
            this.isDebug = z;
            return this;
        }

        public Builder log(int i) {
            this.type = i;
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
