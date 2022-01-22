package p006me.goldze.mvvmhabit.binding.viewadapter.viewpager;

import android.databinding.BindingAdapter;
import android.support.p000v4.view.ViewPager;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.viewpager.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter(requireAll = false, value = {"onPageScrolledCommand", "onPageSelectedCommand", "onPageScrollStateChangedCommand"})
    public static void onScrollChangeCommand(ViewPager viewPager, final BindingCommand<ViewPagerDataWrapper> bindingCommand, final BindingCommand<Integer> bindingCommand2, final BindingCommand<Integer> bindingCommand3) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.viewpager.ViewAdapter.1
            private int state;

            @Override // android.support.p000v4.view.ViewPager.OnPageChangeListener
            public void onPageScrolled(int i, float f, int i2) {
                BindingCommand bindingCommand4 = BindingCommand.this;
                if (bindingCommand4 != null) {
                    bindingCommand4.execute(new ViewPagerDataWrapper((float) i, f, i2, this.state));
                }
            }

            @Override // android.support.p000v4.view.ViewPager.OnPageChangeListener
            public void onPageSelected(int i) {
                BindingCommand bindingCommand4 = bindingCommand2;
                if (bindingCommand4 != null) {
                    bindingCommand4.execute(Integer.valueOf(i));
                }
            }

            @Override // android.support.p000v4.view.ViewPager.OnPageChangeListener
            public void onPageScrollStateChanged(int i) {
                this.state = i;
                BindingCommand bindingCommand4 = bindingCommand3;
                if (bindingCommand4 != null) {
                    bindingCommand4.execute(Integer.valueOf(i));
                }
            }
        });
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.viewpager.ViewAdapter$ViewPagerDataWrapper */
    /* loaded from: classes.dex */
    public static class ViewPagerDataWrapper {
        public float position;
        public float positionOffset;
        public int positionOffsetPixels;
        public int state;

        public ViewPagerDataWrapper(float f, float f2, int i, int i2) {
            this.positionOffset = f2;
            this.position = f;
            this.positionOffsetPixels = i;
            this.state = i2;
        }
    }
}
