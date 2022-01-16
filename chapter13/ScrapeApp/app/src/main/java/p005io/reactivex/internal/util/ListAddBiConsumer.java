package p005io.reactivex.internal.util;

import java.util.List;
import p005io.reactivex.functions.BiFunction;

/* renamed from: io.reactivex.internal.util.ListAddBiConsumer */
/* loaded from: classes.dex */
public enum ListAddBiConsumer implements BiFunction<List, Object, List> {
    INSTANCE;

    public static <T> BiFunction<List<T>, T, List<T>> instance() {
        return INSTANCE;
    }

    public List apply(List t1, Object t2) throws Exception {
        t1.add(t2);
        return t1;
    }
}
