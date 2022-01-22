package p005io.reactivex.internal.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import p005io.reactivex.functions.BiFunction;

/* renamed from: io.reactivex.internal.util.MergerBiFunction */
/* loaded from: classes.dex */
public final class MergerBiFunction<T> implements BiFunction<List<T>, List<T>, List<T>> {
    final Comparator<? super T> comparator;

    @Override // p005io.reactivex.functions.BiFunction
    public /* bridge */ /* synthetic */ Object apply(Object obj, Object obj2) throws Exception {
        return apply((List) ((List) obj), (List) ((List) obj2));
    }

    public MergerBiFunction(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    public List<T> apply(List<T> list, List<T> list2) throws Exception {
        int size = list.size() + list2.size();
        if (size == 0) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(size);
        Iterator<T> it = list.iterator();
        Iterator<T> it2 = list2.iterator();
        T next = it.hasNext() ? it.next() : null;
        T next2 = it2.hasNext() ? it2.next() : null;
        while (next != null && next2 != null) {
            if (this.comparator.compare(next, next2) < 0) {
                arrayList.add(next);
                next = it.hasNext() ? it.next() : null;
            } else {
                arrayList.add(next2);
                next2 = it2.hasNext() ? it2.next() : null;
            }
        }
        if (next != null) {
            arrayList.add(next);
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
        } else {
            arrayList.add(next2);
            while (it2.hasNext()) {
                arrayList.add(it2.next());
            }
        }
        return arrayList;
    }
}
