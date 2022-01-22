package p005io.reactivex.internal.util;

import p005io.reactivex.Observer;

/* renamed from: io.reactivex.internal.util.ObservableQueueDrain */
/* loaded from: classes.dex */
public interface ObservableQueueDrain<T, U> {
    void accept(Observer<? super U> observer, T t);

    boolean cancelled();

    boolean done();

    boolean enter();

    Throwable error();

    int leave(int i);
}
