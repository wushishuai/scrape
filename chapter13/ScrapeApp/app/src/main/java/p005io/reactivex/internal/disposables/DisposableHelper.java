package p005io.reactivex.internal.disposables;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.ProtocolViolationException;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.disposables.DisposableHelper */
/* loaded from: classes.dex */
public enum DisposableHelper implements Disposable {
    DISPOSED;

    public static boolean isDisposed(Disposable d) {
        return d == DISPOSED;
    }

    public static boolean set(AtomicReference<Disposable> field, Disposable d) {
        Disposable current;
        do {
            current = field.get();
            if (current == DISPOSED) {
                if (d == null) {
                    return false;
                }
                d.dispose();
                return false;
            }
        } while (!field.compareAndSet(current, d));
        if (current == null) {
            return true;
        }
        current.dispose();
        return true;
    }

    public static boolean setOnce(AtomicReference<Disposable> field, Disposable d) {
        ObjectHelper.requireNonNull(d, "d is null");
        if (field.compareAndSet(null, d)) {
            return true;
        }
        d.dispose();
        if (field.get() == DISPOSED) {
            return false;
        }
        reportDisposableSet();
        return false;
    }

    public static boolean replace(AtomicReference<Disposable> field, Disposable d) {
        Disposable current;
        do {
            current = field.get();
            if (current == DISPOSED) {
                if (d == null) {
                    return false;
                }
                d.dispose();
                return false;
            }
        } while (!field.compareAndSet(current, d));
        return true;
    }

    public static boolean dispose(AtomicReference<Disposable> field) {
        Disposable current;
        Disposable current2 = field.get();
        Disposable d = DISPOSED;
        if (current2 == d || (current = field.getAndSet(d)) == d) {
            return false;
        }
        if (current == null) {
            return true;
        }
        current.dispose();
        return true;
    }

    public static boolean validate(Disposable current, Disposable next) {
        if (next == null) {
            RxJavaPlugins.onError(new NullPointerException("next is null"));
            return false;
        } else if (current == null) {
            return true;
        } else {
            next.dispose();
            reportDisposableSet();
            return false;
        }
    }

    public static void reportDisposableSet() {
        RxJavaPlugins.onError(new ProtocolViolationException("Disposable already set!"));
    }

    public static boolean trySet(AtomicReference<Disposable> field, Disposable d) {
        if (field.compareAndSet(null, d)) {
            return true;
        }
        if (field.get() != DISPOSED) {
            return false;
        }
        d.dispose();
        return false;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return true;
    }
}
