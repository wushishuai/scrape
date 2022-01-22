package p005io.reactivex.internal.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import p005io.reactivex.functions.Function;

/* renamed from: io.reactivex.internal.util.SorterFunction */
/* loaded from: classes.dex */
public final class SorterFunction<T> implements Function<List<T>, List<T>> {
    final Comparator<? super T> comparator;

    @Override // p005io.reactivex.functions.Function
    public /* bridge */ /* synthetic */ Object apply(Object obj) throws Exception {
        return apply((List) ((List) obj));
    }

    public SorterFunction(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    public List<T> apply(List<T> list) throws Exception {
        Collections.sort(list, this.comparator);
        return list;
    }
}
