package com.lcodecore.tkrefreshlayout.utils;

import android.content.Context;

/* loaded from: classes.dex */
public class DensityUtil {
    public static int dp2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        return (int) ((pxValue / context.getResources().getDisplayMetrics().density) + 0.5f);
    }
}
