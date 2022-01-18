package com.bumptech.glide.load.resource.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.content.res.ResourcesCompat;
import android.support.p003v7.content.res.AppCompatResources;
import android.support.p003v7.view.ContextThemeWrapper;

/* loaded from: classes.dex */
public final class DrawableDecoderCompat {
    private static volatile boolean shouldCallAppCompatResources = true;

    private DrawableDecoderCompat() {
    }

    public static Drawable getDrawable(Context ourContext, Context targetContext, @DrawableRes int id) {
        return getDrawable(ourContext, targetContext, id, null);
    }

    public static Drawable getDrawable(Context ourContext, @DrawableRes int id, @Nullable Resources.Theme theme) {
        return getDrawable(ourContext, ourContext, id, theme);
    }

    private static Drawable getDrawable(Context ourContext, Context targetContext, @DrawableRes int id, @Nullable Resources.Theme theme) {
        try {
            if (shouldCallAppCompatResources) {
                return loadDrawableV7(targetContext, id, theme);
            }
        } catch (Resources.NotFoundException e) {
        } catch (IllegalStateException e2) {
            if (!ourContext.getPackageName().equals(targetContext.getPackageName())) {
                return ContextCompat.getDrawable(targetContext, id);
            }
            throw e2;
        } catch (NoClassDefFoundError e3) {
            shouldCallAppCompatResources = false;
        }
        return loadDrawableV4(targetContext, id, theme != null ? theme : targetContext.getTheme());
    }

    private static Drawable loadDrawableV7(Context context, @DrawableRes int id, @Nullable Resources.Theme theme) {
        return AppCompatResources.getDrawable(theme != null ? new ContextThemeWrapper(context, theme) : context, id);
    }

    private static Drawable loadDrawableV4(Context context, @DrawableRes int id, @Nullable Resources.Theme theme) {
        return ResourcesCompat.getDrawable(context.getResources(), id, theme);
    }
}