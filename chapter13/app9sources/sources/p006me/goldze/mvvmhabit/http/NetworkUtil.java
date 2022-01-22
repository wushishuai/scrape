package p006me.goldze.mvvmhabit.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

/* renamed from: me.goldze.mvvmhabit.http.NetworkUtil */
/* loaded from: classes.dex */
public class NetworkUtil {
    public static int NET_CNNT_BAIDU_OK = 1;
    public static int NET_CNNT_BAIDU_TIMEOUT = 2;
    public static int NET_ERROR = 4;
    public static int NET_NOT_PREPARE = 3;
    private static int TIMEOUT = 3000;
    public static String url = "http://www.baidu.com";

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetworkInfo;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService("connectivity");
        if (connectivityManager == null || (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) == null || !activeNetworkInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public static String getLocalIpAddress() {
        String str = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress nextElement = inetAddresses.nextElement();
                    if (!nextElement.isLoopbackAddress()) {
                        str = nextElement.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static int getNetState(Context context) {
        NetworkInfo activeNetworkInfo;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
            if (!(connectivityManager == null || (activeNetworkInfo = connectivityManager.getActiveNetworkInfo()) == null)) {
                if (!activeNetworkInfo.isAvailable() || !activeNetworkInfo.isConnected()) {
                    return NET_NOT_PREPARE;
                }
                if (!connectionNetwork()) {
                    return NET_CNNT_BAIDU_TIMEOUT;
                }
                return NET_CNNT_BAIDU_OK;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NET_ERROR;
    }

    private static boolean connectionNetwork() {
        Throwable th;
        HttpURLConnection httpURLConnection;
        HttpURLConnection httpURLConnection2 = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException unused) {
        } catch (Throwable th2) {
            th = th2;
        }
        try {
            httpURLConnection.setConnectTimeout(TIMEOUT);
            httpURLConnection.connect();
            if (httpURLConnection == null) {
                return true;
            }
            httpURLConnection.disconnect();
            return true;
        } catch (IOException unused2) {
            httpURLConnection2 = httpURLConnection;
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            httpURLConnection2 = httpURLConnection;
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
            throw th;
        }
    }

    public static boolean is3G(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.getType() == 0;
    }

    public static boolean isWifi(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.getType() == 1;
    }

    public static boolean is2G(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        if (activeNetworkInfo.getSubtype() == 2 || activeNetworkInfo.getSubtype() == 1 || activeNetworkInfo.getSubtype() == 4) {
            return true;
        }
        return false;
    }

    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        return (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || ((TelephonyManager) context.getSystemService("phone")).getNetworkType() == 3;
    }
}
