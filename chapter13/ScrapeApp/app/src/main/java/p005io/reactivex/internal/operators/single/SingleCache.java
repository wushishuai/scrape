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

    public SingleCache(SingleSource<? extends T> source) {
        this.source = source;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        CacheDisposable<T> d = new CacheDisposable<>(observer, this);
        observer.onSubscribe(d);
        if (add(d)) {
            if (d.isDisposed()) {
                remove(d);
            }
            if (this.wip.getAndIncrement() == 0) {
                this.source.subscribe(this);
                return;
            }
            return;
        }
        Throwable ex = this.error;
        if (ex != null) {
            observer.onError(ex);
        } else {
            observer.onSuccess((T) this.value);
        }
    }

    boolean add(CacheDisposable<T> observer) {
        CacheDisposable<T>[] a;
        CacheDisposable<T>[] b;
        do {
            a = this.observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            b = new CacheDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = observer;
        } while (!this.observers.compareAndSet(a, b));
        return true;
    }

    void remove(CacheDisposable<T> observer) {
        CacheDisposable<T>[] a;
        CacheDisposable<T>[] b;
        do {
            a = this.observers.get();
            int n = a.length;
            if (n != 0) {
                int j = -1;
                int i = 0;
                while (true) {
                    if (i >= n) {
                        break;
                    } else if (a[i] == observer) {
                        j = i;
                        break;
                    } else {
                        i++;
                    }
                }
                if (j >= 0) {
                    if (n == 1) {
                        b = EMPTY;
                    } else {
                        CacheDisposable<T>[] b2 = new CacheDisposable[n - 1];
                        System.arraycopy(a, 0, b2, 0, j);
                        System.arraycopy(a, j + 1, b2, j, (n - j) - 1);
                        b = b2;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.observers.compareAndSet(a, b));
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSubscribe(Disposable d) {
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSuccess(T value) {
        this.value = value;
        CacheDisposable<T>[] andSet = this.observers.getAndSet(TERMINATED);
        for (CacheDisposable<T> d : andSet) {
            if (!d.isDisposed()) {
                d.downstream.onSuccess(value);
            }
        }
    }

    @Override // p005io.reactivex.SingleObserver
    public void onError(Throwable e) {
        this.error = e;
        CacheDisposable<T>[] andSet = this.observers.getAndSet(TERMINATED);
        for (CacheDisposable<T> d : andSet) {
            if (!d.isDisposed()) {
                d.downstream.onError(e);
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

        CacheDisposable(SingleObserver<? super T> actual, SingleCache<T> parent) {
            this.downstream = actual;
            this.parent = parent;
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
