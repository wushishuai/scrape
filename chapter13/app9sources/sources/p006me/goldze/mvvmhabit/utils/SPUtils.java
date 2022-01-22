package p006me.goldze.mvvmhabit.utils;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* renamed from: me.goldze.mvvmhabit.utils.SPUtils */
/* loaded from: classes.dex */
public final class SPUtils {
    private static Map<String, SPUtils> sSPMap = new HashMap();

    /* renamed from: sp */
    private SharedPreferences f215sp;

    public static SPUtils getInstance() {
        return getInstance("");
    }

    public static SPUtils getInstance(String str) {
        if (isSpace(str)) {
            str = "spUtils";
        }
        SPUtils sPUtils = sSPMap.get(str);
        if (sPUtils != null) {
            return sPUtils;
        }
        SPUtils sPUtils2 = new SPUtils(str);
        sSPMap.put(str, sPUtils2);
        return sPUtils2;
    }

    private SPUtils(String str) {
        this.f215sp = Utils.getContext().getSharedPreferences(str, 0);
    }

    public void put(@NonNull String str, @NonNull String str2) {
        this.f215sp.edit().putString(str, str2).apply();
    }

    public String getString(@NonNull String str) {
        return getString(str, "");
    }

    public String getString(@NonNull String str, @NonNull String str2) {
        return this.f215sp.getString(str, str2);
    }

    public void put(@NonNull String str, int i) {
        this.f215sp.edit().putInt(str, i).apply();
    }

    public int getInt(@NonNull String str) {
        return getInt(str, -1);
    }

    public int getInt(@NonNull String str, int i) {
        return this.f215sp.getInt(str, i);
    }

    public void put(@NonNull String str, long j) {
        this.f215sp.edit().putLong(str, j).apply();
    }

    public long getLong(@NonNull String str) {
        return getLong(str, -1);
    }

    public long getLong(@NonNull String str, long j) {
        return this.f215sp.getLong(str, j);
    }

    public void put(@NonNull String str, float f) {
        this.f215sp.edit().putFloat(str, f).apply();
    }

    public float getFloat(@NonNull String str) {
        return getFloat(str, -1.0f);
    }

    public float getFloat(@NonNull String str, float f) {
        return this.f215sp.getFloat(str, f);
    }

    public void put(@NonNull String str, boolean z) {
        this.f215sp.edit().putBoolean(str, z).apply();
    }

    public boolean getBoolean(@NonNull String str) {
        return getBoolean(str, false);
    }

    public boolean getBoolean(@NonNull String str, boolean z) {
        return this.f215sp.getBoolean(str, z);
    }

    public void put(@NonNull String str, @NonNull Set<String> set) {
        this.f215sp.edit().putStringSet(str, set).apply();
    }

    public Set<String> getStringSet(@NonNull String str) {
        return getStringSet(str, Collections.emptySet());
    }

    public Set<String> getStringSet(@NonNull String str, @NonNull Set<String> set) {
        return this.f215sp.getStringSet(str, set);
    }

    public Map<String, ?> getAll() {
        return this.f215sp.getAll();
    }

    public boolean contains(@NonNull String str) {
        return this.f215sp.contains(str);
    }

    public void remove(@NonNull String str) {
        this.f215sp.edit().remove(str).apply();
    }

    public void clear() {
        this.f215sp.edit().clear().apply();
    }

    private static boolean isSpace(String str) {
        if (str == null) {
            return true;
        }
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
