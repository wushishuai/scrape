package io.reactivex.internal.disposables;

import io.reactivex.disposables.Disposable;
import java.util.concurrent.atomic.AtomicReferenceArray;
/* loaded from: classes.dex */
public final class ArrayCompositeDisposable extends AtomicReferenceArray<Disposable> implements Disposable {
    private static final long serialVersionUID = 2746389416410565408L;

    public ArrayCompositeDisposable(int capacity) {
        super(capacity);
    }

    public boolean setResource(int index, Disposable resource) {
        Disposable o;
        do {
            o = get(index);
            if (o == DisposableHelper.DISPOSED) {
                resource.dispose();
                return false;
            }
        } while (!compareAndSet(index, o, resource));
        if (o == null) {
            return true;
        }
        o.dispose();
        return true;
    }

    public Disposable replaceResource(int index, Disposable resource) {
        Disposable o;
        do {
            o = get(index);
            if (o == DisposableHelper.DISPOSED) {
                resource.dispose();
                return null;
            }
        } while (!compareAndSet(index, o, resource));
        return o;
    }

    @Override // io.reactivex.disposables.Disposable
    public void dispose() {
        Disposable o;
        if (get(0) != DisposableHelper.DISPOSED) {
            int s = length();
            for (int i = 0; i < s; i++) {
                if (!(get(i) == DisposableHelper.DISPOSED || (o = getAndSet(i, DisposableHelper.DISPOSED)) == DisposableHelper.DISPOSED || o == null)) {
                    o.dispose();
                }
            }
        }
    }

    @Override // io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return get(0) == DisposableHelper.DISPOSED;
    }
}
