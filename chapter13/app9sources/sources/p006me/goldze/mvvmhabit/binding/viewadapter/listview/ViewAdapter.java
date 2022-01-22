package p006me.goldze.mvvmhabit.binding.viewadapter.listview;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.concurrent.TimeUnit;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.subjects.PublishSubject;
import p006me.goldze.mvvmhabit.binding.command.BindingCommand;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.listview.ViewAdapter */
/* loaded from: classes.dex */
public final class ViewAdapter {
    @BindingAdapter(requireAll = false, value = {"onScrollChangeCommand", "onScrollStateChangedCommand"})
    public static void onScrollChangeCommand(ListView listView, final BindingCommand<ListViewScrollDataWrapper> bindingCommand, final BindingCommand<Integer> bindingCommand2) {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.listview.ViewAdapter.1
            private int scrollState;

            @Override // android.widget.AbsListView.OnScrollListener
            public void onScrollStateChanged(AbsListView absListView, int i) {
                this.scrollState = i;
                BindingCommand bindingCommand3 = BindingCommand.this;
                if (bindingCommand3 != null) {
                    bindingCommand3.execute(Integer.valueOf(i));
                }
            }

            @Override // android.widget.AbsListView.OnScrollListener
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                BindingCommand bindingCommand3 = bindingCommand;
                if (bindingCommand3 != null) {
                    bindingCommand3.execute(new ListViewScrollDataWrapper(this.scrollState, i, i2, i3));
                }
            }
        });
    }

    @BindingAdapter(requireAll = false, value = {"onItemClickCommand"})
    public static void onItemClickCommand(ListView listView, final BindingCommand<Integer> bindingCommand) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: me.goldze.mvvmhabit.binding.viewadapter.listview.ViewAdapter.2
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                BindingCommand bindingCommand2 = BindingCommand.this;
                if (bindingCommand2 != null) {
                    bindingCommand2.execute(Integer.valueOf(i));
                }
            }
        });
    }

    @BindingAdapter({"onLoadMoreCommand"})
    public static void onLoadMoreCommand(ListView listView, BindingCommand<Integer> bindingCommand) {
        listView.setOnScrollListener(new OnScrollListener(listView, bindingCommand));
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.listview.ViewAdapter$OnScrollListener */
    /* loaded from: classes.dex */
    public static class OnScrollListener implements AbsListView.OnScrollListener {
        private ListView listView;
        private PublishSubject<Integer> methodInvoke = PublishSubject.create();
        private BindingCommand<Integer> onLoadMoreCommand;

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        public OnScrollListener(ListView listView, final BindingCommand<Integer> bindingCommand) {
            this.onLoadMoreCommand = bindingCommand;
            this.listView = listView;
            this.methodInvoke.throttleFirst(1, TimeUnit.SECONDS).subscribe(new Consumer<Integer>() { // from class: me.goldze.mvvmhabit.binding.viewadapter.listview.ViewAdapter.OnScrollListener.1
                public void accept(Integer num) throws Exception {
                    bindingCommand.execute(num);
                }
            });
        }

        @Override // android.widget.AbsListView.OnScrollListener
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            if (i + i2 >= i3 && i3 != 0 && i3 != this.listView.getHeaderViewsCount() + this.listView.getFooterViewsCount() && this.onLoadMoreCommand != null) {
                this.methodInvoke.onNext(Integer.valueOf(i3));
            }
        }
    }

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.listview.ViewAdapter$ListViewScrollDataWrapper */
    /* loaded from: classes.dex */
    public static class ListViewScrollDataWrapper {
        public int firstVisibleItem;
        public int scrollState;
        public int totalItemCount;
        public int visibleItemCount;

        public ListViewScrollDataWrapper(int i, int i2, int i3, int i4) {
            this.firstVisibleItem = i2;
            this.visibleItemCount = i3;
            this.totalItemCount = i4;
            this.scrollState = i;
        }
    }
}
