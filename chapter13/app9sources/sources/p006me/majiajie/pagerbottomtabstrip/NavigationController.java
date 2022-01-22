package p006me.majiajie.pagerbottomtabstrip;

import android.support.p000v4.view.ViewPager;
import p006me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;

/* renamed from: me.majiajie.pagerbottomtabstrip.NavigationController */
/* loaded from: classes.dex */
public class NavigationController implements ItemController, BottomLayoutController {
    private BottomLayoutController mBottomLayoutController;
    private ItemController mItemController;

    public NavigationController(BottomLayoutController bottomLayoutController, ItemController itemController) {
        this.mBottomLayoutController = bottomLayoutController;
        this.mItemController = itemController;
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setSelect(int i) {
        this.mItemController.setSelect(i);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setMessageNumber(int i, int i2) {
        this.mItemController.setMessageNumber(i, i2);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setHasMessage(int i, boolean z) {
        this.mItemController.setHasMessage(i, z);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void addTabItemSelectedListener(OnTabItemSelectedListener onTabItemSelectedListener) {
        this.mItemController.addTabItemSelectedListener(onTabItemSelectedListener);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public int getSelected() {
        return this.mItemController.getSelected();
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public int getItemCount() {
        return this.mItemController.getItemCount();
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public String getItemTitle(int i) {
        return this.mItemController.getItemTitle(i);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.BottomLayoutController
    public void setupWithViewPager(ViewPager viewPager) {
        this.mBottomLayoutController.setupWithViewPager(viewPager);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.BottomLayoutController
    public void hideBottomLayout() {
        this.mBottomLayoutController.hideBottomLayout();
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.BottomLayoutController
    public void showBottomLayout() {
        this.mBottomLayoutController.showBottomLayout();
    }
}
