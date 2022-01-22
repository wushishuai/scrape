package p006me.majiajie.pagerbottomtabstrip.internal;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.p000v4.content.ContextCompat;
import android.support.p000v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;

/* renamed from: me.majiajie.pagerbottomtabstrip.internal.Utils */
/* loaded from: classes.dex */
public class Utils {
    public static Drawable tint(Drawable drawable, int i) {
        Drawable wrap = DrawableCompat.wrap(drawable);
        wrap.mutate();
        DrawableCompat.setTint(wrap, i);
        return wrap;
    }

    public static Drawable newDrawable(Drawable drawable) {
        Drawable.ConstantState constantState = drawable.getConstantState();
        return constantState != null ? constantState.newDrawable() : drawable;
    }

    public static int getColorPrimary(Context context) {
        int identifier = context.getResources().getIdentifier("colorPrimary", "attr", context.getPackageName());
        if (identifier == 0) {
            return -16738680;
        }
        return ContextCompat.getColor(context, getResourceId(context, identifier));
    }

    private static int getResourceId(Context context, int i) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(i, typedValue, true);
        return typedValue.resourceId;
    }
}
