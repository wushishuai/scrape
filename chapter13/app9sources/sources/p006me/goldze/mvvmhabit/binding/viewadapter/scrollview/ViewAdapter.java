package p006me.goldze.mvvmhabit.binding.viewadapter.scrollview;

import android.databinding.BindingAdapter;
import android.support.p000v4.widget.NestedScrollView;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.scrollview.ViewAdapter */
/* loaded from: classes.dex */
public final class ViewAdapter {
    @BindingAdapter({"onScrollChangeCommand"})
    public static void onScrollChangeCommand(NestedScrollView nestedScrollView, final BindingCommand<NestScrollDataWrapper> bindingCommand) {
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.scrollview.ViewAdapter.1
            @Override // android.support.p000v4.widget.NestedScrollView.OnScrollChangeListener
            public void onScrollChange(NestedScrollView nestedScrollView2, int i, int i2, int i3, int i4) {
                BindingCommand bindingCommand2 = BindingCommand.this;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute(new NestScrollDataWrapper(i, i2, i3, i4));
                }
            }
        });
    }

    @BindingAdapter({"onScrollChangeCommand"})
    public static void onScrollChangeCommand(final ScrollView scrollView, final BindingCommand<ScrollDataWrapper> bindingCommand) {
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.scrollview.ViewAdapter.2
            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                BindingCommand bindingCommand2 = BindingCommand.this;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute(new ScrollDataWrapper((float) scrollView.getScrollX(), (float) scrollView.getScrollY()));
                }
            }
        });
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.scrollview.ViewAdapter$ScrollDataWrapper */
    /* loaded from: classes.dex */
    public static class ScrollDataWrapper {
        public float scrollX;
        public float scrollY;

        public ScrollDataWrapper(float f, float f2) {
            this.scrollX = f;
            this.scrollY = f2;
        }
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.scrollview.ViewAdapter$NestScrollDataWrapper */
    /* loaded from: classes.dex */
    public static class NestScrollDataWrapper {
        public int oldScrollX;
        public int oldScrollY;
        public int scrollX;
        public int scrollY;

        public NestScrollDataWrapper(int i, int i2, int i3, int i4) {
            this.scrollX = i;
            this.scrollY = i2;
            this.oldScrollX = i3;
            this.oldScrollY = i4;
        }
    }
}
