package p006me.goldze.mvvmhabit.binding.viewadapter.swiperefresh;

import android.databinding.BindingAdapter;
import android.support.p000v4.widget.SwipeRefreshLayout;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.swiperefresh.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter({"onRefreshCommand"})
    public static void onRefreshCommand(SwipeRefreshLayout swipeRefreshLayout, final BindingCommand bindingCommand) {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.swiperefresh.ViewAdapter.1
            @Override // android.support.p000v4.widget.SwipeRefreshLayout.OnRefreshListener
            public void onRefresh() {
                BindingCommand bindingCommand2 = BindingCommand.this;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute();
                }
            }
        });
    }

    @BindingAdapter({"refreshing"})
    public static void setRefreshing(SwipeRefreshLayout swipeRefreshLayout, boolean z) {
        swipeRefreshLayout.setRefreshing(z);
    }
}
