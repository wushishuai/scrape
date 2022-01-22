package p006me.goldze.mvvmhabit.utils;

import android.text.TextUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* renamed from: me.goldze.mvvmhabit.utils.KLog */
/* loaded from: classes.dex */
public class KLog {

    /* renamed from: A */
    private static final int f209A = 6;

    /* renamed from: D */
    private static final int f210D = 2;
    private static final String DEFAULT_MESSAGE = "execute";

    /* renamed from: E */
    private static final int f211E = 5;

    /* renamed from: I */
    private static final int f212I = 3;
    private static boolean IS_SHOW_LOG = false;
    private static final int JSON = 7;
    private static final int JSON_INDENT = 4;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /* renamed from: V */
    private static final int f213V = 1;

    /* renamed from: W */
    private static final int f214W = 4;

    public static void init(boolean z) {
        IS_SHOW_LOG = z;
    }

    /* renamed from: v */
    public static void m14v() {
        printLog(1, null, DEFAULT_MESSAGE);
    }

    /* renamed from: v */
    public static void m13v(Object obj) {
        printLog(1, null, obj);
    }

    /* renamed from: v */
    public static void m12v(String str, String str2) {
        printLog(1, str, str2);
    }

    /* renamed from: d */
    public static void m23d() {
        printLog(2, null, DEFAULT_MESSAGE);
    }

    /* renamed from: d */
    public static void m22d(Object obj) {
        printLog(2, null, obj);
    }

    /* renamed from: d */
    public static void m21d(String str, Object obj) {
        printLog(2, str, obj);
    }

    /* renamed from: i */
    public static void m17i() {
        printLog(3, null, DEFAULT_MESSAGE);
    }

    /* renamed from: i */
    public static void m16i(Object obj) {
        printLog(3, null, obj);
    }

    /* renamed from: i */
    public static void m15i(String str, Object obj) {
        printLog(3, str, obj);
    }

    /* renamed from: w */
    public static void m11w() {
        printLog(4, null, DEFAULT_MESSAGE);
    }

    /* renamed from: w */
    public static void m10w(Object obj) {
        printLog(4, null, obj);
    }

    /* renamed from: w */
    public static void m9w(String str, Object obj) {
        printLog(4, str, obj);
    }

    /* renamed from: e */
    public static void m20e() {
        printLog(5, null, DEFAULT_MESSAGE);
    }

    /* renamed from: e */
    public static void m19e(Object obj) {
        printLog(5, null, obj);
    }

    /* renamed from: e */
    public static void m18e(String str, Object obj) {
        printLog(5, str, obj);
    }

    /* renamed from: a */
    public static void m26a() {
        printLog(6, null, DEFAULT_MESSAGE);
    }

    /* renamed from: a */
    public static void m25a(Object obj) {
        printLog(6, null, obj);
    }

    /* renamed from: a */
    public static void m24a(String str, Object obj) {
        printLog(6, str, obj);
    }

    public static void json(String str) {
        printLog(7, null, str);
    }

    public static void json(String str, String str2) {
        printLog(7, str, str2);
    }

    private static void printLog(int i, String str, Object obj) {
        String str2;
        if (IS_SHOW_LOG) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String fileName = stackTrace[4].getFileName();
            String methodName = stackTrace[4].getMethodName();
            int lineNumber = stackTrace[4].getLineNumber();
            if (str == null) {
                str = fileName;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[ (");
            sb.append(fileName);
            sb.append(":");
            sb.append(lineNumber);
            sb.append(")#");
            sb.append(methodName.substring(0, 1).toUpperCase() + methodName.substring(1));
            sb.append(" ] ");
            if (obj == null) {
                str2 = "Log with null Object";
            } else {
                str2 = obj.toString();
            }
            if (!(str2 == null || i == 7)) {
                sb.append(str2);
            }
            String sb2 = sb.toString();
            switch (i) {
                case 1:
                    Log.v(str, sb2);
                    return;
                case 2:
                    Log.d(str, sb2);
                    return;
                case 3:
                    Log.i(str, sb2);
                    return;
                case 4:
                    Log.w(str, sb2);
                    return;
                case 5:
                    Log.e(str, sb2);
                    return;
                case 6:
                    Log.wtf(str, sb2);
                    return;
                case 7:
                    if (TextUtils.isEmpty(str2)) {
                        Log.d(str, "Empty or Null json content");
                        return;
                    }
                    String str3 = null;
                    try {
                        if (str2.startsWith("{")) {
                            str3 = new JSONObject(str2).toString(4);
                        } else if (str2.startsWith("[")) {
                            str3 = new JSONArray(str2).toString(4);
                        }
                        printLine(str, true);
                        String[] split = (sb2 + LINE_SEPARATOR + str3).split(LINE_SEPARATOR);
                        StringBuilder sb3 = new StringBuilder();
                        for (String str4 : split) {
                            sb3.append("║ ");
                            sb3.append(str4);
                            sb3.append(LINE_SEPARATOR);
                        }
                        if (sb3.toString().length() > 3200) {
                            Log.w(str, "jsonContent.length = " + sb3.toString().length());
                            int length = sb3.toString().length() / 3200;
                            int i2 = 0;
                            while (i2 <= length) {
                                int i3 = i2 + 1;
                                int i4 = i3 * 3200;
                                if (i4 >= sb3.toString().length()) {
                                    Log.w(str, sb3.toString().substring(i2 * 3200));
                                } else {
                                    Log.w(str, sb3.toString().substring(i2 * 3200, i4));
                                }
                                i2 = i3;
                            }
                        } else {
                            Log.w(str, sb3.toString());
                        }
                        printLine(str, false);
                        return;
                    } catch (JSONException e) {
                        m18e(str, e.getCause().getMessage() + "\n" + str2);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    private static void printLine(String str, boolean z) {
        if (z) {
            Log.w(str, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.w(str, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }
}
