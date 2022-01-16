package p006me.goldze.mvvmhabit.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.graphics.drawable.PathInterpolatorCompat;
import android.telephony.TelephonyManager;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/* renamed from: me.goldze.mvvmhabit.http.NetworkUtil */
/* loaded from: classes.dex */
public class NetworkUtil {
    public static String url = "http://www.baidu.com";
    public static int NET_CNNT_BAIDU_OK = 1;
    public static int NET_CNNT_BAIDU_TIMEOUT = 2;
    public static int NET_NOT_PREPARE = 3;
    public static int NET_ERROR = 4;
    private static int TIMEOUT = PathInterpolatorCompat.MAX_NUM_POINTS;

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info;
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService("connectivity");
        if (manager == null || (info = manager.getActiveNetworkInfo()) == null || !info.isAvailable()) {
            return false;
        }
        return true;
    }

    public static String getLocalIpAddress() {
        String ret = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ret = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public static int getNetState(Context context) {
        NetworkInfo networkinfo;
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService("connectivity");
            if (!(connectivity == null || (networkinfo = connectivity.getActiveNetworkInfo()) == null)) {
                if (!networkinfo.isAvailable() || !networkinfo.isConnected()) {
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

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0025, code lost:
        if (r1 == null) goto L_0x002a;
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0027, code lost:
        r1.disconnect();
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x002c, code lost:
        return r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:5:0x0019, code lost:
        if (r1 != null) goto L_0x0027;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static boolean connectionNetwork() {
        /*
            r0 = 0
            r1 = 0
            java.net.URL r2 = new java.net.URL     // Catch: IOException -> 0x0024, all -> 0x001c
            java.lang.String r3 = p006me.goldze.mvvmhabit.http.NetworkUtil.url     // Catch: IOException -> 0x0024, all -> 0x001c
            r2.<init>(r3)     // Catch: IOException -> 0x0024, all -> 0x001c
            java.net.URLConnection r2 = r2.openConnection()     // Catch: IOException -> 0x0024, all -> 0x001c
            java.net.HttpURLConnection r2 = (java.net.HttpURLConnection) r2     // Catch: IOException -> 0x0024, all -> 0x001c
            r1 = r2
            int r2 = p006me.goldze.mvvmhabit.http.NetworkUtil.TIMEOUT     // Catch: IOException -> 0x0024, all -> 0x001c
            r1.setConnectTimeout(r2)     // Catch: IOException -> 0x0024, all -> 0x001c
            r1.connect()     // Catch: IOException -> 0x0024, all -> 0x001c
            r0 = 1
            if (r1 == 0) goto L_0x002a
            goto L_0x0027
        L_0x001c:
            r2 = move-exception
            if (r1 == 0) goto L_0x0022
            r1.disconnect()
        L_0x0022:
            r1 = 0
            throw r2
        L_0x0024:
            r2 = move-exception
            if (r1 == 0) goto L_0x002a
        L_0x0027:
            r1.disconnect()
        L_0x002a:
            r1 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: p006me.goldze.mvvmhabit.http.NetworkUtil.connectionNetwork():boolean");
    }

    public static boolean is3G(Context context) {
        NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo == null || activeNetInfo.getType() != 0) {
            return false;
        }
        return true;
    }

    public static boolean isWifi(Context context) {
        NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo == null || activeNetInfo.getType() != 1) {
            return false;
        }
        return true;
    }

    public static boolean is2G(Context context) {
        NetworkInfo activeNetInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetInfo == null) {
            return false;
        }
        if (activeNetInfo.getSubtype() == 2 || activeNetInfo.getSubtype() == 1 || activeNetInfo.getSubtype() == 4) {
            return true;
        }
        return false;
    }

    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager mgrConn = (ConnectivityManager) context.getSystemService("connectivity");
        return (mgrConn.getActiveNetworkInfo() != null && mgrConn.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED) || ((TelephonyManager) context.getSystemService("phone")).getNetworkType() == 3;
    }
}
