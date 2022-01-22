package com.afollestad.materialdialogs.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.afollestad.materialdialogs.commons.C0592R;
import java.lang.reflect.Method;

/* loaded from: classes.dex */
class PrefUtil {
    private PrefUtil() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void setLayoutResource(@NonNull Context context, @NonNull Preference preference, @Nullable AttributeSet attributeSet) {
        boolean z;
        boolean z2 = false;
        if (attributeSet != null) {
            for (int i = 0; i < attributeSet.getAttributeCount(); i++) {
                if (((XmlResourceParser) attributeSet).getAttributeNamespace(0).equals("http://schemas.android.com/apk/res/android") && attributeSet.getAttributeName(i).equals("layout")) {
                    z = true;
                    break;
                }
            }
        }
        z = false;
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, C0592R.styleable.Preference, 0, 0);
            try {
                z2 = obtainStyledAttributes.getBoolean(C0592R.styleable.Preference_useStockLayout, false);
            } finally {
                obtainStyledAttributes.recycle();
            }
        }
        if (!(z || z2)) {
            preference.setLayoutResource(C0592R.layout.md_preference_custom);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void registerOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener onActivityDestroyListener) {
        try {
            PreferenceManager preferenceManager = preference.getPreferenceManager();
            Method declaredMethod = preferenceManager.getClass().getDeclaredMethod("registerOnActivityDestroyListener", PreferenceManager.OnActivityDestroyListener.class);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(preferenceManager, onActivityDestroyListener);
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void unregisterOnActivityDestroyListener(@NonNull Preference preference, @NonNull PreferenceManager.OnActivityDestroyListener onActivityDestroyListener) {
        try {
            PreferenceManager preferenceManager = preference.getPreferenceManager();
            Method declaredMethod = preferenceManager.getClass().getDeclaredMethod("unregisterOnActivityDestroyListener", PreferenceManager.OnActivityDestroyListener.class);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(preferenceManager, onActivityDestroyListener);
        } catch (Exception unused) {
        }
    }
}
