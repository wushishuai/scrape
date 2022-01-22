package p006me.goldze.mvvmhabit.http.cookie.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

/* renamed from: me.goldze.mvvmhabit.http.cookie.store.PersistentCookieStore */
/* loaded from: classes.dex */
public class PersistentCookieStore implements CookieStore {
    private static final String COOKIE_NAME_PREFIX = "cookie_";
    private static final String COOKIE_PREFS = "habit_cookie";
    private static final String LOG_TAG = "PersistentCookieStore";
    private final SharedPreferences cookiePrefs;
    private final HashMap<String, ConcurrentHashMap<String, Cookie>> cookies = new HashMap<>();

    public PersistentCookieStore(Context context) {
        Cookie decodeCookie;
        this.cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        for (Map.Entry<String, ?> entry : this.cookiePrefs.getAll().entrySet()) {
            if (entry.getValue() != null && !entry.getKey().startsWith(COOKIE_NAME_PREFIX)) {
                String[] split = TextUtils.split((String) entry.getValue(), ",");
                for (String str : split) {
                    String string = this.cookiePrefs.getString(COOKIE_NAME_PREFIX + str, null);
                    if (!(string == null || (decodeCookie = decodeCookie(string)) == null)) {
                        if (!this.cookies.containsKey(entry.getKey())) {
                            this.cookies.put(entry.getKey(), new ConcurrentHashMap<>());
                        }
                        this.cookies.get(entry.getKey()).put(str, decodeCookie);
                    }
                }
            }
        }
    }

    private String getCookieToken(Cookie cookie) {
        return cookie.name() + "@" + cookie.domain();
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public List<Cookie> loadCookie(HttpUrl httpUrl) {
        ArrayList arrayList = new ArrayList();
        if (this.cookies.containsKey(httpUrl.host())) {
            for (Cookie cookie : this.cookies.get(httpUrl.host()).values()) {
                if (isCookieExpired(cookie)) {
                    removeCookie(httpUrl, cookie);
                } else {
                    arrayList.add(cookie);
                }
            }
        }
        return arrayList;
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public void saveCookie(HttpUrl httpUrl, List<Cookie> list) {
        if (!this.cookies.containsKey(httpUrl.host())) {
            this.cookies.put(httpUrl.host(), new ConcurrentHashMap<>());
        }
        for (Cookie cookie : list) {
            if (isCookieExpired(cookie)) {
                removeCookie(httpUrl, cookie);
            } else {
                saveCookie(httpUrl, cookie, getCookieToken(cookie));
            }
        }
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public void saveCookie(HttpUrl httpUrl, Cookie cookie) {
        if (!this.cookies.containsKey(httpUrl.host())) {
            this.cookies.put(httpUrl.host(), new ConcurrentHashMap<>());
        }
        if (isCookieExpired(cookie)) {
            removeCookie(httpUrl, cookie);
        } else {
            saveCookie(httpUrl, cookie, getCookieToken(cookie));
        }
    }

    private void saveCookie(HttpUrl httpUrl, Cookie cookie, String str) {
        this.cookies.get(httpUrl.host()).put(str, cookie);
        SharedPreferences.Editor edit = this.cookiePrefs.edit();
        edit.putString(httpUrl.host(), TextUtils.join(",", this.cookies.get(httpUrl.host()).keySet()));
        edit.putString(COOKIE_NAME_PREFIX + str, encodeCookie(new SerializableHttpCookie(cookie)));
        edit.apply();
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public boolean removeCookie(HttpUrl httpUrl, Cookie cookie) {
        String cookieToken = getCookieToken(cookie);
        if (!this.cookies.containsKey(httpUrl.host()) || !this.cookies.get(httpUrl.host()).containsKey(cookieToken)) {
            return false;
        }
        this.cookies.get(httpUrl.host()).remove(cookieToken);
        SharedPreferences.Editor edit = this.cookiePrefs.edit();
        SharedPreferences sharedPreferences = this.cookiePrefs;
        if (sharedPreferences.contains(COOKIE_NAME_PREFIX + cookieToken)) {
            edit.remove(COOKIE_NAME_PREFIX + cookieToken);
        }
        edit.putString(httpUrl.host(), TextUtils.join(",", this.cookies.get(httpUrl.host()).keySet()));
        edit.apply();
        return true;
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public boolean removeCookie(HttpUrl httpUrl) {
        if (!this.cookies.containsKey(httpUrl.host())) {
            return false;
        }
        Set<String> keySet = this.cookies.get(httpUrl.host()).keySet();
        SharedPreferences.Editor edit = this.cookiePrefs.edit();
        for (String str : keySet) {
            SharedPreferences sharedPreferences = this.cookiePrefs;
            if (sharedPreferences.contains(COOKIE_NAME_PREFIX + str)) {
                edit.remove(COOKIE_NAME_PREFIX + str);
            }
        }
        edit.remove(httpUrl.host()).apply();
        this.cookies.remove(httpUrl.host());
        return true;
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public boolean removeAllCookie() {
        this.cookiePrefs.edit().clear().apply();
        this.cookies.clear();
        return true;
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public List<Cookie> getAllCookie() {
        ArrayList arrayList = new ArrayList();
        for (String str : this.cookies.keySet()) {
            arrayList.addAll(this.cookies.get(str).values());
        }
        return arrayList;
    }

    @Override // p006me.goldze.mvvmhabit.http.cookie.store.CookieStore
    public List<Cookie> getCookie(HttpUrl httpUrl) {
        ArrayList arrayList = new ArrayList();
        ConcurrentHashMap<String, Cookie> concurrentHashMap = this.cookies.get(httpUrl.host());
        if (concurrentHashMap != null) {
            arrayList.addAll(concurrentHashMap.values());
        }
        return arrayList;
    }

    private String encodeCookie(SerializableHttpCookie serializableHttpCookie) {
        if (serializableHttpCookie == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(byteArrayOutputStream).writeObject(serializableHttpCookie);
            return byteArrayToHexString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in encodeCookie", e);
            return null;
        }
    }

    private Cookie decodeCookie(String str) {
        try {
            return ((SerializableHttpCookie) new ObjectInputStream(new ByteArrayInputStream(hexStringToByteArray(str))).readObject()).getCookie();
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in decodeCookie", e);
            return null;
        } catch (ClassNotFoundException e2) {
            Log.d(LOG_TAG, "ClassNotFoundException in decodeCookie", e2);
            return null;
        }
    }

    private String byteArrayToHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
            int i = b & 255;
            if (i < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(i));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    private byte[] hexStringToByteArray(String str) {
        int length = str.length();
        byte[] bArr = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return bArr;
    }
}
