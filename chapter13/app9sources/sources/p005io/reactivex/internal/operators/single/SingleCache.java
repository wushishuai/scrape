package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.single.SingleCache */
/* loaded from: classes.dex */
public final class SingleCache<T> extends Single<T> implements SingleObserver<T> {
    static final CacheDisposable[] EMPTY = new CacheDisposable[0];
    static final CacheDisposable[] TERMINATED = new CacheDisposable[0];
    Throwable error;
    final SingleSource<? extends T> source;
    T value;
    final AtomicInteger wip = new AtomicInteger();
    final AtomicReference<CacheDisposable<T>[]> observers = new AtomicReference<>(EMPTY);

    @Override // p005io.reactivex.SingleObserver
    public void onSubscribe(Disposable disposable) {
    }

    public SingleCache(SingleSource<? extends T> singleSource) {
        this.source = singleSource;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        CacheDisposable<T> cacheDisposable = new CacheDisposable<>(singleObserver, this);
        singleObserver.onSubscribe(cacheDisposable);
        if (add(cacheDisposable)) {
            if (cacheDisposable.isDisposed()) {
                remove(cacheDisposable);
            }
            if (this.wip.getAndIncrement() == 0) {
                this.source.subscribe(this);
                return;
            }
            return;
        }
        Throwable th = this.error;
        if (th != null) {
            singleObserver.onError(th);
        } else {
            singleObserver.onSuccess((T) this.value);
        }
    }

    boolean add(CacheDisposable<T> cacheDisposable) {
        CacheDisposable<T>[] cacheDisposableArr;
        CacheDisposable<T>[] cacheDisposableArr2;
        do {
            cacheDisposableArr = this.observers.get();
            if (cacheDisposableArr == TERMINATED) {
                return false;
            }
            int length = cacheDisposableArr.length;
            cacheDisposableArr2 = new CacheDisposable[length + 1];
            System.arraycopy(cacheDisposableArr, 0, cacheDisposableArr2, 0, length);
            cacheDisposableArr2[length] = cacheDisposable;
        } while (!this.observers.compareAndSet(cacheDisposableArr, cacheDisposableArr2));
        return true;
    }

    void remove(CacheDisposable<T> cacheDisposable) {
        CacheDisposable<T>[] cacheDisposableArr;
        CacheDisposable<T>[] cacheDisposableArr2;
        do {
            cacheDisposableArr = this.observers.get();
            int length = cacheDisposableArr.length;
            if (length != 0) {
                int i = -1;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (cacheDisposableArr[i2] == cacheDisposable) {
                        i = i2;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (i >= 0) {
                    if (length == 1) {
                        cacheDisposableArr2 = EMPTY;
                    } else {
                        CacheDisposable<T>[] cacheDisposableArr3 = new CacheDisposable[length - 1];
                        System.arraycopy(cacheDisposableArr, 0, cacheDisposableArr3, 0, i);
                        System.arraycopy(cacheDisposableArr, i + 1, cacheDisposableArr3, i, (length - i) - 1);
                        cacheDisposableArr2 = cacheDisposableArr3;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.observers.compareAndSet(cacheDisposableArr, cacheDisposableArr2));
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSuccess(T t) {
        this.value = t;
        CacheDisposable<T>[] andSet = this.observers.getAndSet(TERMINATED);
        for (CacheDisposable<T> cacheDisposable : andSet) {
            if (!cacheDisposable.isDisposed()) {
                cacheDisposable.downstream.onSuccess(t);
            }
        }
    }

    @Override // p005io.reactivex.SingleObserver
    public void onError(Throwable th) {
        this.error = th;
        CacheDisposable<T>[] andSet = this.observers.getAndSet(TERMINATED);
        for (CacheDisposable<T> cacheDisposable : andSet) {
            if (!cacheDisposable.isDisposed()) {
                cacheDisposable.downstream.onError(th);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.single.SingleCache$CacheDisposable */
    /* loaded from: classes.dex */
    public static final class CacheDisposable<T> extends AtomicBoolean implements Disposable {
        private static final long serialVersionUID = 7514387411091976596L;
        final SingleObserver<? super T> downstream;
        final SingleCache<T> parent;

        CacheDisposable(SingleObserver<? super T> singleObserver, SingleCache<T> singleCache) {
            this.downstream = singleObserver;
            this.parent = singleCache;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (compareAndSet(false, true)) {
                this.parent.remove(this);
            }
        }
    }
}
