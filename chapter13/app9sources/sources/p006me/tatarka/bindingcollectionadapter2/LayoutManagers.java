package p006me.tatarka.bindingcollectionadapter2;

import android.support.p003v7.widget.GridLayoutManager;
import android.support.p003v7.widget.LinearLayoutManager;
import android.support.p003v7.widget.RecyclerView;
import android.support.p003v7.widget.StaggeredGridLayoutManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/* renamed from: me.tatarka.bindingcollectionadapter2.LayoutManagers */
/* loaded from: classes.dex */
public class LayoutManagers {

    /* renamed from: me.tatarka.bindingcollectionadapter2.LayoutManagers$LayoutManagerFactory */
    /* loaded from: classes.dex */
    public interface LayoutManagerFactory {
        RecyclerView.LayoutManager create(RecyclerView recyclerView);
    }

    @Retention(RetentionPolicy.SOURCE)
    /* renamed from: me.tatarka.bindingcollectionadapter2.LayoutManagers$Orientation */
    /* loaded from: classes.dex */
    public @interface Orientation {
    }

    protected LayoutManagers() {
    }

    public static LayoutManagerFactory linear() {
        return new LayoutManagerFactory() { // from class: me.tatarka.bindingcollectionadapter2.LayoutManagers.1
            @Override // p006me.tatarka.bindingcollectionadapter2.LayoutManagers.LayoutManagerFactory
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new LinearLayoutManager(recyclerView.getContext());
            }
        };
    }

    public static LayoutManagerFactory linear(final int i, final boolean z) {
        return new LayoutManagerFactory() { // from class: me.tatarka.bindingcollectionadapter2.LayoutManagers.2
            @Override // p006me.tatarka.bindingcollectionadapter2.LayoutManagers.LayoutManagerFactory
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new LinearLayoutManager(recyclerView.getContext(), i, z);
            }
        };
    }

    public static LayoutManagerFactory grid(final int i) {
        return new LayoutManagerFactory() { // from class: me.tatarka.bindingcollectionadapter2.LayoutManagers.3
            @Override // p006me.tatarka.bindingcollectionadapter2.LayoutManagers.LayoutManagerFactory
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new GridLayoutManager(recyclerView.getContext(), i);
            }
        };
    }

    public static LayoutManagerFactory grid(final int i, final int i2, final boolean z) {
        return new LayoutManagerFactory() { // from class: me.tatarka.bindingcollectionadapter2.LayoutManagers.4
            @Override // p006me.tatarka.bindingcollectionadapter2.LayoutManagers.LayoutManagerFactory
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new GridLayoutManager(recyclerView.getContext(), i, i2, z);
            }
        };
    }

    public static LayoutManagerFactory staggeredGrid(final int i, final int i2) {
        return new LayoutManagerFactory() { // from class: me.tatarka.bindingcollectionadapter2.LayoutManagers.5
            @Override // p006me.tatarka.bindingcollectionadapter2.LayoutManagers.LayoutManagerFactory
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new StaggeredGridLayoutManager(i, i2);
            }
        };
    }
}
