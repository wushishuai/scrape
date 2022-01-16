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
    public void setSelect(int index) {
        this.mItemController.setSelect(index);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setMessageNumber(int index, int number) {
        this.mItemController.setMessageNumber(index, number);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void setHasMessage(int index, boolean hasMessage) {
        this.mItemController.setHasMessage(index, hasMessage);
    }

    @Override // p006me.majiajie.pagerbottomtabstrip.ItemController
    public void addTabItemSelectedListener(OnTabItemSelectedListener listener) {
        this.mItemController.addTabItemSelectedListener(listener);
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
    public String getItemTitle(int index) {
        return this.mItemController.getItemTitle(index);
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
