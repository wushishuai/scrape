package com.goldze.mvvmhabit.utils;

import android.text.TextUtils;
import java.util.List;

/* loaded from: classes.dex */
public class Encrypt {
    public static String encrypt(List<String> list, int i) {
        return NativeUtils.encrypt(TextUtils.join("", list), i);
    }
}
