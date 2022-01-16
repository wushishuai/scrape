package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableCache */
/* loaded from: classes.dex */
public final class CompletableCache extends Completable implements CompletableObserver {
    static final InnerCompletableCache[] EMPTY = new InnerCompletableCache[0];
    static final InnerCompletableCache[] TERMINATED = new InnerCompletableCache[0];
    Throwable error;
    final AtomicReference<InnerCompletableCache[]> observers = new AtomicReference<>(EMPTY);
    final AtomicBoolean once = new AtomicBoolean();
    final CompletableSource source;

    public CompletableCache(CompletableSource source) {
        this.source = source;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        InnerCompletableCache inner = new InnerCompletableCache(observer);
        observer.onSubscribe(inner);
        if (add(inner)) {
            if (inner.isDisposed()) {
                remove(inner);
            }
            if (this.once.compareAndSet(false, true)) {
                this.source.subscribe(this);
                return;
            }
            return;
        }
        Throwable ex = this.error;
        if (ex != null) {
            observer.onError(ex);
        } else {
            observer.onComplete();
        }
    }

    @Override // p005io.reactivex.CompletableObserver
    public void onSubscribe(Disposable d) {
    }

    @Override // p005io.reactivex.CompletableObserver
    public void onError(Throwable e) {
        this.error = e;
        InnerCompletableCache[] andSet = this.observers.getAndSet(TERMINATED);
        for (InnerCompletableCache inner : andSet) {
            if (!inner.get()) {
                inner.downstream.onError(e);
            }
        }
    }

    @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
    public void onComplete() {
        InnerCompletableCache[] andSet = this.observers.getAndSet(TERMINATED);
        for (InnerCompletableCache inner : andSet) {
            if (!inner.get()) {
                inner.downstream.onComplete();
            }
        }
    }

    boolean add(InnerCompletableCache inner) {
        InnerCompletableCache[] a;
        InnerCompletableCache[] b;
        do {
            a = this.observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            b = new InnerCompletableCache[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
        } while (!this.observers.compareAndSet(a, b));
        return true;
    }

    void remove(InnerCompletableCache inner) {
        InnerCompletableCache[] a;
        InnerCompletableCache[] b;
        do {
            a = this.observers.get();
            int n = a.length;
            if (n != 0) {
                int j = -1;
                int i = 0;
                while (true) {
                    if (i >= n) {
                        break;
                    } else if (a[i] == inner) {
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
                        InnerCompletableCache[] b2 = new InnerCompletableCache[n - 1];
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

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.completable.CompletableCache$InnerCompletableCache */
    /* loaded from: classes.dex */
    public final class InnerCompletableCache extends AtomicBoolean implements Disposable {
        private static final long serialVersionUID = 8943152917179642732L;
        final CompletableObserver downstream;

        InnerCompletableCache(CompletableObserver downstream) {
            this.downstream = downstream;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (compareAndSet(false, true)) {
                CompletableCache.this.remove(this);
            }
        }
    }
}
