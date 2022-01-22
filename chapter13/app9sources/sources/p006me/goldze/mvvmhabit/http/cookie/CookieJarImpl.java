package p006me.goldze.mvvmhabit.http.cookie;

import java.util.List;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import p006me.goldze.mvvmhabit.http.cookie.store.CookieStore;

/* renamed from: me.goldze.mvvmhabit.http.cookie.CookieJarImpl */
/* loaded from: classes.dex */
public class CookieJarImpl implements CookieJar {
    private CookieStore cookieStore;

    public CookieJarImpl(CookieStore cookieStore) {
        if (cookieStore != null) {
            this.cookieStore = cookieStore;
            return;
        }
        throw new IllegalArgumentException("cookieStore can not be null!");
    }

    @Override // okhttp3.CookieJar
    public synchronized void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
        this.cookieStore.saveCookie(httpUrl, list);
    }

    @Override // okhttp3.CookieJar
    public synchronized List<Cookie> loadForRequest(HttpUrl httpUrl) {
        return this.cookieStore.loadCookie(httpUrl);
    }

    public CookieStore getCookieStore() {
        return this.cookieStore;
    }
}
