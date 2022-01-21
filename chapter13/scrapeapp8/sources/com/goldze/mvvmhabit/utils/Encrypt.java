package com.goldze.mvvmhabit.utils;

import android.text.TextUtils;
import java.util.List;

/* loaded from: classes.dex */
public class Encrypt {
    public static String encrypt(List<String> strings, int offset) {
        return NativeUtils.encrypt(TextUtils.join("", strings), offset);
    }
}
