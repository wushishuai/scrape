package p006me.goldze.mvvmhabit.bus;

import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;

/* renamed from: me.goldze.mvvmhabit.bus.RxSubscriptions */
/* loaded from: classes.dex */
public class RxSubscriptions {
    private static CompositeDisposable mSubscriptions = new CompositeDisposable();

    public static boolean isDisposed() {
        return mSubscriptions.isDisposed();
    }

    public static void add(Disposable s) {
        if (s != null) {
            mSubscriptions.add(s);
        }
    }

    public static void remove(Disposable s) {
        if (s != null) {
            mSubscriptions.remove(s);
        }
    }

    public static void clear() {
        mSubscriptions.clear();
    }

    public static void dispose() {
        mSubscriptions.dispose();
    }
}
