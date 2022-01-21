package p005io.reactivex.internal.util;

import java.util.concurrent.CountDownLatch;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.Consumer;

/* renamed from: io.reactivex.internal.util.BlockingIgnoringReceiver */
/* loaded from: classes.dex */
public final class BlockingIgnoringReceiver extends CountDownLatch implements Consumer<Throwable>, Action {
    public Throwable error;

    public BlockingIgnoringReceiver() {
        super(1);
    }

    public void accept(Throwable e) {
        this.error = e;
        countDown();
    }

    @Override // p005io.reactivex.functions.Action
    public void run() {
        countDown();
    }
}
