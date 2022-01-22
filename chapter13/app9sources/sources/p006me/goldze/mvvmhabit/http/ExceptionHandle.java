package p006me.goldze.mvvmhabit.http;

import android.net.ParseException;
import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import retrofit2.HttpException;

/* renamed from: me.goldze.mvvmhabit.http.ExceptionHandle */
/* loaded from: classes.dex */
public class ExceptionHandle {
    private static final int FORBIDDEN = 403;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int UNAUTHORIZED = 401;

    public static ResponseThrowable handleException(Throwable th) {
        if (th instanceof HttpException) {
            ResponseThrowable responseThrowable = new ResponseThrowable(th, 1003);
            int code = ((HttpException) th).code();
            if (code == UNAUTHORIZED) {
                responseThrowable.message = "操作未授权";
            } else if (code == REQUEST_TIMEOUT) {
                responseThrowable.message = "服务器执行超时";
            } else if (code == INTERNAL_SERVER_ERROR) {
                responseThrowable.message = "服务器内部错误";
            } else if (code != SERVICE_UNAVAILABLE) {
                switch (code) {
                    case FORBIDDEN /* 403 */:
                        responseThrowable.message = "请求被拒绝";
                        break;
                    case NOT_FOUND /* 404 */:
                        responseThrowable.message = "资源不存在";
                        break;
                    default:
                        responseThrowable.message = "网络错误";
                        break;
                }
            } else {
                responseThrowable.message = "服务器不可用";
            }
            return responseThrowable;
        } else if ((th instanceof JsonParseException) || (th instanceof JSONException) || (th instanceof ParseException) || (th instanceof MalformedJsonException)) {
            ResponseThrowable responseThrowable2 = new ResponseThrowable(th, 1001);
            responseThrowable2.message = "解析错误";
            return responseThrowable2;
        } else if (th instanceof ConnectException) {
            ResponseThrowable responseThrowable3 = new ResponseThrowable(th, 1002);
            responseThrowable3.message = "连接失败";
            return responseThrowable3;
        } else if (th instanceof SSLException) {
            ResponseThrowable responseThrowable4 = new ResponseThrowable(th, ERROR.SSL_ERROR);
            responseThrowable4.message = "证书验证失败";
            return responseThrowable4;
        } else if (th instanceof ConnectTimeoutException) {
            ResponseThrowable responseThrowable5 = new ResponseThrowable(th, 1006);
            responseThrowable5.message = "连接超时";
            return responseThrowable5;
        } else if (th instanceof SocketTimeoutException) {
            ResponseThrowable responseThrowable6 = new ResponseThrowable(th, 1006);
            responseThrowable6.message = "连接超时";
            return responseThrowable6;
        } else if (th instanceof UnknownHostException) {
            ResponseThrowable responseThrowable7 = new ResponseThrowable(th, 1006);
            responseThrowable7.message = "主机地址未知";
            return responseThrowable7;
        } else {
            ResponseThrowable responseThrowable8 = new ResponseThrowable(th, 1000);
            responseThrowable8.message = "未知错误";
            return responseThrowable8;
        }
    }

    /* renamed from: me.goldze.mvvmhabit.http.ExceptionHandle$ERROR */
    /* loaded from: classes.dex */
    class ERROR {
        public static final int HTTP_ERROR = 1003;
        public static final int NETWORD_ERROR = 1002;
        public static final int PARSE_ERROR = 1001;
        public static final int SSL_ERROR = 1005;
        public static final int TIMEOUT_ERROR = 1006;
        public static final int UNKNOWN = 1000;

        ERROR() {
        }
    }
}
