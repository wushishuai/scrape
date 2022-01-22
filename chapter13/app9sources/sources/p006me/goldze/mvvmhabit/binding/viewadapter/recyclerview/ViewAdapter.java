package p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview;

import android.databinding.BindingAdapter;
import android.support.p003v7.widget.LinearLayoutManager;
import android.support.p003v7.widget.RecyclerView;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.subjects.PublishSubject;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;
import p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.ViewAdapter */
/* loaded from: classes.dex */
public class ViewAdapter {
    @BindingAdapter({"lineManager"})
    public static void setLineManager(RecyclerView recyclerView, LineManagers.LineManagerFactory lineManagerFactory) {
        recyclerView.addItemDecoration(lineManagerFactory.create(recyclerView));
    }

    @BindingAdapter(requireAll = false, value = {"onScrollChangeCommand", "onScrollStateChangedCommand"})
    public static void onScrollChangeCommand(RecyclerView recyclerView, final BindingCommand<ScrollDataWrapper> bindingCommand, final BindingCommand<Integer> bindingCommand2) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.ViewAdapter.1
            private int state;

            @Override // android.support.p003v7.widget.RecyclerView.OnScrollListener
            public void onScrolled(RecyclerView recyclerView2, int i, int i2) {
                super.onScrolled(recyclerView2, i, i2);
                BindingCommand bindingCommand3 = BindingCommand.this;
                if (bindingCommand3 != null) {
                    bindingCommand3.execute(new ScrollDataWrapper((float) i, (float) i2, this.state));
                }
            }

            @Override // android.support.p003v7.widget.RecyclerView.OnScrollListener
            public void onScrollStateChanged(RecyclerView recyclerView2, int i) {
                super.onScrollStateChanged(recyclerView2, i);
                this.state = i;
                BindingCommand bindingCommand3 = bindingCommand2;
                if (bindingCommand3 != null) {
                    bindingCommand3.execute(Integer.valueOf(i));
                }
            }
        });
    }

    @BindingAdapter({"onLoadMoreCommand"})
    public static void onLoadMoreCommand(RecyclerView recyclerView, BindingCommand<Integer> bindingCommand) {
        recyclerView.addOnScrollListener(new OnScrollListener(bindingCommand));
    }

    @BindingAdapter({"itemAnimator"})
    public static void setItemAnimator(RecyclerView recyclerView, RecyclerView.ItemAnimator itemAnimator) {
        recyclerView.setItemAnimator(itemAnimator);
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.ViewAdapter$OnScrollListener */
    /* loaded from: classes.dex */
    public static class OnScrollListener extends RecyclerView.OnScrollListener {
        private PublishSubject<Integer> methodInvoke = PublishSubject.create();
        private BindingCommand<Integer> onLoadMoreCommand;

        public OnScrollListener(final BindingCommand<Integer> bindingCommand) {
            this.onLoadMoreCommand = bindingCommand;
            this.methodInvoke.throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Integer>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.ViewAdapter.OnScrollListener.1
                public void accept(Integer num) throws Exception {
                    bindingCommand.execute(num);
                }
            });
        }

        @Override // android.support.p003v7.widget.RecyclerView.OnScrollListener
        public void onScrolled(RecyclerView recyclerView, int i, int i2) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int childCount = linearLayoutManager.getChildCount();
            if (childCount + linearLayoutManager.findFirstVisibleItemPosition() >= linearLayoutManager.getItemCount() && this.onLoadMoreCommand != null) {
                this.methodInvoke.onNext(Integer.valueOf(recyclerView.getAdapter().getItemCount()));
            }
        }

        @Override // android.support.p003v7.widget.RecyclerView.OnScrollListener
        public void onScrollStateChanged(RecyclerView recyclerView, int i) {
            super.onScrollStateChanged(recyclerView, i);
        }
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.ViewAdapter$ScrollDataWrapper */
    /* loaded from: classes.dex */
    public static class ScrollDataWrapper {
        public float scrollX;
        public float scrollY;
        public int state;

        public ScrollDataWrapper(float f, float f2, int i) {
            this.scrollX = f;
            this.scrollY = f2;
            this.state = i;
        }
    }
}
