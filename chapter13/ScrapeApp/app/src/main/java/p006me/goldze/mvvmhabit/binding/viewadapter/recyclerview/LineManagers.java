package p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview;

import android.support.p003v7.widget.RecyclerView;
import p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview.DividerLine;

/* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers */
/* loaded from: classes.dex */
public class LineManagers {

    /* renamed from: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers$LineManagerFactory */
    /* loaded from: classes.dex */
    public interface LineManagerFactory {
        RecyclerView.ItemDecoration create(RecyclerView recyclerView);
    }

    protected LineManagers() {
    }

    public static LineManagerFactory both() {
        return new LineManagerFactory() { // from class: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers.1
            @Override // p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers.LineManagerFactory
            public RecyclerView.ItemDecoration create(RecyclerView recyclerView) {
                return new DividerLine(recyclerView.getContext(), DividerLine.LineDrawMode.BOTH);
            }
        };
    }

    public static LineManagerFactory horizontal() {
        return new LineManagerFactory() { // from class: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers.2
            @Override // p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers.LineManagerFactory
            public RecyclerView.ItemDecoration create(RecyclerView recyclerView) {
                return new DividerLine(recyclerView.getContext(), DividerLine.LineDrawMode.HORIZONTAL);
            }
        };
    }

    public static LineManagerFactory vertical() {
        return new LineManagerFactory() { // from class: me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers.3
            @Override // p006me.goldze.mvvmhabit.binding.viewadapter.recyclerview.LineManagers.LineManagerFactory
            public RecyclerView.ItemDecoration create(RecyclerView recyclerView) {
                return new DividerLine(recyclerView.getContext(), DividerLine.LineDrawMode.VERTICAL);
            }
        };
    }
}
