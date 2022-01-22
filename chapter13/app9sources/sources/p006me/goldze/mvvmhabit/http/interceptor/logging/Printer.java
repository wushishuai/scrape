package p006me.goldze.mvvmhabit.http.interceptor.logging;

import android.text.TextUtils;
import java.io.IOException;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.Request;
import okio.Buffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import p006me.goldze.mvvmhabit.http.interceptor.logging.LoggingInterceptor;

/* renamed from: me.goldze.mvvmhabit.http.interceptor.logging.Printer */
/* loaded from: classes.dex */
class Printer {
    private static final String BODY_TAG = "Body:";
    private static final String CENTER_LINE = "├ ";
    private static final String CORNER_BOTTOM = "└ ";
    private static final String CORNER_UP = "┌ ";
    private static final String DEFAULT_LINE = "│ ";
    private static final String END_LINE = "└───────────────────────────────────────────────────────────────────────────────────────";
    private static final String HEADERS_TAG = "Headers:";
    private static final int JSON_INDENT = 3;
    private static final String METHOD_TAG = "Method: @";

    /* renamed from: N */
    private static final String f207N = "\n";
    private static final String[] OMITTED_REQUEST;
    private static final String[] OMITTED_RESPONSE;
    private static final String RECEIVED_TAG = "Received in: ";
    private static final String REQUEST_UP_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────";
    private static final String RESPONSE_UP_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────";
    private static final String STATUS_CODE_TAG = "Status Code: ";

    /* renamed from: T */
    private static final String f208T = "\t";
    private static final String URL_TAG = "URL: ";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

    static {
        String str = LINE_SEPARATOR;
        OMITTED_RESPONSE = new String[]{str, "Omitted response body"};
        OMITTED_REQUEST = new String[]{str, "Omitted request body"};
    }

    protected Printer() {
        throw new UnsupportedOperationException();
    }

    private static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str) || f207N.equals(str) || f208T.equals(str) || TextUtils.isEmpty(str.trim());
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void printJsonRequest(LoggingInterceptor.Builder builder, Request request) {
        String str = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request);
        String tag = builder.getTag(true);
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, REQUEST_UP_LINE);
        }
        logLines(builder.getType(), tag, new String[]{URL_TAG + request.url()}, builder.getLogger(), false);
        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()), builder.getLogger(), true);
        if (request.body() instanceof FormBody) {
            StringBuilder sb = new StringBuilder();
            FormBody formBody = (FormBody) request.body();
            if (!(formBody == null || formBody.size() == 0)) {
                for (int i = 0; i < formBody.size(); i++) {
                    sb.append(formBody.encodedName(i) + "=" + formBody.encodedValue(i) + "&");
                }
                sb.delete(sb.length() - 1, sb.length());
                logLines(builder.getType(), tag, new String[]{sb.toString()}, builder.getLogger(), true);
            }
        }
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, str.split(LINE_SEPARATOR), builder.getLogger(), true);
        }
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, END_LINE);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void printJsonResponse(LoggingInterceptor.Builder builder, long j, boolean z, int i, String str, String str2, List<String> list) {
        String str3 = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + getJsonString(str2);
        String tag = builder.getTag(false);
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, RESPONSE_UP_LINE);
        }
        logLines(builder.getType(), tag, getResponse(str, j, i, z, builder.getLevel(), list), builder.getLogger(), true);
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, str3.split(LINE_SEPARATOR), builder.getLogger(), true);
        }
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, END_LINE);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void printFileRequest(LoggingInterceptor.Builder builder, Request request) {
        String tag = builder.getTag(true);
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, REQUEST_UP_LINE);
        }
        int type = builder.getType();
        logLines(type, tag, new String[]{URL_TAG + request.url()}, builder.getLogger(), false);
        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()), builder.getLogger(), true);
        if (request.body() instanceof FormBody) {
            StringBuilder sb = new StringBuilder();
            FormBody formBody = (FormBody) request.body();
            if (!(formBody == null || formBody.size() == 0)) {
                for (int i = 0; i < formBody.size(); i++) {
                    sb.append(formBody.encodedName(i) + "=" + formBody.encodedValue(i) + "&");
                }
                sb.delete(sb.length() - 1, sb.length());
                logLines(builder.getType(), tag, new String[]{sb.toString()}, builder.getLogger(), true);
            }
        }
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, OMITTED_REQUEST, builder.getLogger(), true);
        }
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, END_LINE);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void printFileResponse(LoggingInterceptor.Builder builder, long j, boolean z, int i, String str, List<String> list) {
        String tag = builder.getTag(false);
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, RESPONSE_UP_LINE);
        }
        logLines(builder.getType(), tag, getResponse(str, j, i, z, builder.getLevel(), list), builder.getLogger(), true);
        logLines(builder.getType(), tag, OMITTED_RESPONSE, builder.getLogger(), true);
        if (builder.getLogger() == null) {
            C0985I.log(builder.getType(), tag, END_LINE);
        }
    }

    private static String[] getRequest(Request request, Level level) {
        String str;
        String headers = request.headers().toString();
        boolean z = level == Level.HEADERS || level == Level.BASIC;
        StringBuilder sb = new StringBuilder();
        sb.append(METHOD_TAG);
        sb.append(request.method());
        sb.append(DOUBLE_SEPARATOR);
        if (!isEmpty(headers) && z) {
            str = HEADERS_TAG + LINE_SEPARATOR + dotHeaders(headers);
        } else {
            str = "";
        }
        sb.append(str);
        return sb.toString().split(LINE_SEPARATOR);
    }

    private static String[] getResponse(String str, long j, int i, boolean z, Level level, List<String> list) {
        String str2;
        String str3;
        boolean z2 = level == Level.HEADERS || level == Level.BASIC;
        String slashSegments = slashSegments(list);
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(slashSegments)) {
            str2 = slashSegments + " - ";
        } else {
            str2 = "";
        }
        sb.append(str2);
        sb.append("is success : ");
        sb.append(z);
        sb.append(" - ");
        sb.append(RECEIVED_TAG);
        sb.append(j);
        sb.append("ms");
        sb.append(DOUBLE_SEPARATOR);
        sb.append(STATUS_CODE_TAG);
        sb.append(i);
        sb.append(DOUBLE_SEPARATOR);
        if (isEmpty(str)) {
            str3 = "";
        } else if (z2) {
            str3 = HEADERS_TAG + LINE_SEPARATOR + dotHeaders(str);
        } else {
            str3 = "";
        }
        sb.append(str3);
        return sb.toString().split(LINE_SEPARATOR);
    }

    private static String slashSegments(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String str : list) {
            sb.append("/");
            sb.append(str);
        }
        return sb.toString();
    }

    private static String dotHeaders(String str) {
        String str2;
        String[] split = str.split(LINE_SEPARATOR);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        if (split.length > 1) {
            while (i < split.length) {
                if (i == 0) {
                    str2 = CORNER_UP;
                } else {
                    str2 = i == split.length - 1 ? CORNER_BOTTOM : CENTER_LINE;
                }
                sb.append(str2);
                sb.append(split[i]);
                sb.append(f207N);
                i++;
            }
        } else {
            int length = split.length;
            while (i < length) {
                String str3 = split[i];
                sb.append("─ ");
                sb.append(str3);
                sb.append(f207N);
                i++;
            }
        }
        return sb.toString();
    }

    private static void logLines(int i, String str, String[] strArr, Logger logger, boolean z) {
        for (String str2 : strArr) {
            int length = str2.length();
            int i2 = z ? 110 : length;
            int i3 = 0;
            while (i3 <= length / i2) {
                int i4 = i3 * i2;
                i3++;
                int i5 = i3 * i2;
                if (i5 > str2.length()) {
                    i5 = str2.length();
                }
                if (logger == null) {
                    C0985I.log(i, str, DEFAULT_LINE + str2.substring(i4, i5));
                } else {
                    logger.log(i, str, str2.substring(i4, i5));
                }
            }
        }
    }

    private static String bodyToString(Request request) {
        try {
            Request build = request.newBuilder().build();
            Buffer buffer = new Buffer();
            if (build.body() == null) {
                return "";
            }
            build.body().writeTo(buffer);
            return getJsonString(buffer.readUtf8());
        } catch (IOException e) {
            return "{\"err\": \"" + e.getMessage() + "\"}";
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static String getJsonString(String str) {
        try {
            if (str.startsWith("{")) {
                str = new JSONObject(str).toString(3);
            } else if (str.startsWith("[")) {
                str = new JSONArray(str).toString(3);
            }
        } catch (JSONException unused) {
        }
        return str;
    }
}
