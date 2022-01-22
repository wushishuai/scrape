package p006me.majiajie.pagerbottomtabstrip;

import p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

/* renamed from: me.majiajie.pagerbottomtabstrip.ItemController */
/* loaded from: classes.dex */
public interface ItemController {
    void addTabItemSelectedListener(OnTabItemSelectedListener onTabItemSelectedListener);

    int getItemCount();

    String getItemTitle(int i);

    int getSelected();

    void setHasMessage(int i, boolean z);

    void setMessageNumber(int i, int i2);

    void setSelect(int i);
}
