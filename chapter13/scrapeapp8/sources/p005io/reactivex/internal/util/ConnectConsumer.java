package p005io.reactivex.internal.util;

import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Consumer;

/* renamed from: io.reactivex.internal.util.ConnectConsumer */
/* loaded from: classes.dex */
public final class ConnectConsumer implements Consumer<Disposable> {
    public Disposable disposable;

    public void accept(Disposable t) throws Exception {
        this.disposable = t;
    }
}
