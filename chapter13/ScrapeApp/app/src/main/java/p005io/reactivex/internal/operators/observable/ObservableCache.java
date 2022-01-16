package p005io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.LinkedArrayList;
import p005io.reactivex.internal.util.NotificationLite;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.observable.ObservableCache */
/* loaded from: classes.dex */
public final class ObservableCache<T> extends AbstractObservableWithUpstream<T, T> {
    final AtomicBoolean once = new AtomicBoolean();
    final CacheState<T> state;

    public static <T> Observable<T> from(Observable<T> source) {
        return from(source, 16);
    }

    public static <T> Observable<T> from(Observable<T> source, int capacityHint) {
        ObjectHelper.verifyPositive(capacityHint, "capacityHint");
        return RxJavaPlugins.onAssembly(new ObservableCache(source, new CacheState<>(source, capacityHint)));
    }

    private ObservableCache(Observable<T> source, CacheState<T> state) {
        super(source);
        this.state = state;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> t) {
        ReplayDisposable<T> rp = new ReplayDisposable<>(t, this.state);
        t.onSubscribe(rp);
        this.state.addChild(rp);
        if (!this.once.get() && this.once.compareAndSet(false, true)) {
            this.state.connect();
        }
        rp.replay();
    }

    boolean isConnected() {
        return this.state.isConnected;
    }

    boolean hasObservers() {
        return this.state.observers.get().length != 0;
    }

    int cachedEventCount() {
        return this.state.size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableCache$CacheState */
    /* loaded from: classes.dex */
    public static final class CacheState<T> extends LinkedArrayList implements Observer<T> {
        static final ReplayDisposable[] EMPTY = new ReplayDisposable[0];
        static final ReplayDisposable[] TERMINATED = new ReplayDisposable[0];
        volatile boolean isConnected;
        final Observable<? extends T> source;
        boolean sourceDone;
        final AtomicReference<ReplayDisposable<T>[]> observers = new AtomicReference<>(EMPTY);
        final SequentialDisposable connection = new SequentialDisposable();

        CacheState(Observable<? extends T> source, int capacityHint) {
            super(capacityHint);
            this.source = source;
        }

        public boolean addChild(ReplayDisposable<T> p) {
            ReplayDisposable<T>[] a;
            ReplayDisposable<T>[] b;
            do {
                a = this.observers.get();
                if (a == TERMINATED) {
                    return false;
                }
                int n = a.length;
                b = new ReplayDisposable[n + 1];
                System.arraycopy(a, 0, b, 0, n);
                b[n] = p;
            } while (!this.observers.compareAndSet(a, b));
            return true;
        }

        public void removeChild(ReplayDisposable<T> p) {
            ReplayDisposable<T>[] a;
            ReplayDisposable<T>[] b;
            do {
                a = this.observers.get();
                int n = a.length;
                if (n != 0) {
                    int j = -1;
                    int i = 0;
                    while (true) {
                        if (i >= n) {
                            break;
                        } else if (a[i].equals(p)) {
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
                            ReplayDisposable<T>[] b2 = new ReplayDisposable[n - 1];
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

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.connection.update(d);
        }

        public void connect() {
            this.source.subscribe(this);
            this.isConnected = true;
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T t) {
            if (!this.sourceDone) {
                add(NotificationLite.next(t));
                for (ReplayDisposable<T> replayDisposable : this.observers.get()) {
                    replayDisposable.replay();
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            if (!this.sourceDone) {
                this.sourceDone = true;
                add(NotificationLite.error(e));
                this.connection.dispose();
                for (ReplayDisposable<T> replayDisposable : this.observers.getAndSet(TERMINATED)) {
                    replayDisposable.replay();
                }
            }
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            if (!this.sourceDone) {
                this.sourceDone = true;
                add(NotificationLite.complete());
                this.connection.dispose();
                for (ReplayDisposable<T> replayDisposable : this.observers.getAndSet(TERMINATED)) {
                    replayDisposable.replay();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.observable.ObservableCache$ReplayDisposable */
    /* loaded from: classes.dex */
    public static final class ReplayDisposable<T> extends AtomicInteger implements Disposable {
        private static final long serialVersionUID = 7058506693698832024L;
        volatile boolean cancelled;
        final Observer<? super T> child;
        Object[] currentBuffer;
        int currentIndexInBuffer;
        int index;
        final CacheState<T> state;

        ReplayDisposable(Observer<? super T> child, CacheState<T> state) {
            this.child = child;
            this.state = state;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.state.removeChild(this);
            }
        }

        public void replay() {
            if (getAndIncrement() == 0) {
                Observer<? super T> child = this.child;
                int missed = 1;
                while (!this.cancelled) {
                    int s = this.state.size();
                    if (s != 0) {
                        Object[] b = this.currentBuffer;
                        if (b == null) {
                            b = this.state.head();
                            this.currentBuffer = b;
                        }
                        int n = b.length - 1;
                        int j = this.index;
                        int k = this.currentIndexInBuffer;
                        while (j < s) {
                            if (!this.cancelled) {
                                if (k == n) {
                                    b = (Object[]) b[n];
                                    k = 0;
                                }
                                if (!NotificationLite.accept(b[k], child)) {
                                    k++;
                                    j++;
                                } else {
                                    return;
                                }
                            } else {
                                return;
                            }
                        }
                        if (!this.cancelled) {
                            this.index = j;
                            this.currentIndexInBuffer = k;
                            this.currentBuffer = b;
                        } else {
                            return;
                        }
                    }
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        return;
                    }
                }
            }
        }
    }
}
