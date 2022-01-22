package com.goldze.mvvmhabit.utils;

/* loaded from: classes.dex */
public class NativeUtils {
    public static native String encrypt(String str, int i);

    static {
        System.loadLibrary("native");
    }
}
