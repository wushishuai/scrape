package p005io.reactivex.subjects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.subjects.SingleSubject */
/* loaded from: classes.dex */
public final class SingleSubject<T> extends Single<T> implements SingleObserver<T> {
    static final SingleDisposable[] EMPTY = new SingleDisposable[0];
    static final SingleDisposable[] TERMINATED = new SingleDisposable[0];
    Throwable error;
    T value;
    final AtomicBoolean once = new AtomicBoolean();
    final AtomicReference<SingleDisposable<T>[]> observers = new AtomicReference<>(EMPTY);

    @CheckReturnValue
    @NonNull
    public static <T> SingleSubject<T> create() {
        return new SingleSubject<>();
    }

    SingleSubject() {
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSubscribe(@NonNull Disposable d) {
        if (this.observers.get() == TERMINATED) {
            d.dispose();
        }
    }

    @Override // p005io.reactivex.SingleObserver
    public void onSuccess(@NonNull T value) {
        ObjectHelper.requireNonNull(value, "onSuccess called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.once.compareAndSet(false, true)) {
            this.value = value;
            for (SingleDisposable<T> md : this.observers.getAndSet(TERMINATED)) {
                md.downstream.onSuccess(value);
            }
        }
    }

    @Override // p005io.reactivex.SingleObserver
    public void onError(@NonNull Throwable e) {
        ObjectHelper.requireNonNull(e, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.once.compareAndSet(false, true)) {
            this.error = e;
            for (SingleDisposable<T> md : this.observers.getAndSet(TERMINATED)) {
                md.downstream.onError(e);
            }
            return;
        }
        RxJavaPlugins.onError(e);
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(@NonNull SingleObserver<? super T> observer) {
        SingleDisposable<T> md = new SingleDisposable<>(observer, this);
        observer.onSubscribe(md);
        if (!add(md)) {
            Throwable ex = this.error;
            if (ex != null) {
                observer.onError(ex);
            } else {
                observer.onSuccess((T) this.value);
            }
        } else if (md.isDisposed()) {
            remove(md);
        }
    }

    boolean add(@NonNull SingleDisposable<T> inner) {
        SingleDisposable<T>[] a;
        SingleDisposable<T>[] b;
        do {
            a = this.observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            b = new SingleDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = inner;
        } while (!this.observers.compareAndSet(a, b));
        return true;
    }

    void remove(@NonNull SingleDisposable<T> inner) {
        SingleDisposable<T>[] a;
        SingleDisposable<T>[] b;
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
                        SingleDisposable<T>[] b2 = new SingleDisposable[n - 1];
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

    @Nullable
    public T getValue() {
        if (this.observers.get() == TERMINATED) {
            return this.value;
        }
        return null;
    }

    public boolean hasValue() {
        return this.observers.get() == TERMINATED && this.value != null;
    }

    @Nullable
    public Throwable getThrowable() {
        if (this.observers.get() == TERMINATED) {
            return this.error;
        }
        return null;
    }

    public boolean hasThrowable() {
        return this.observers.get() == TERMINATED && this.error != null;
    }

    public boolean hasObservers() {
        return this.observers.get().length != 0;
    }

    int observerCount() {
        return this.observers.get().length;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.subjects.SingleSubject$SingleDisposable */
    /* loaded from: classes.dex */
    public static final class SingleDisposable<T> extends AtomicReference<SingleSubject<T>> implements Disposable {
        private static final long serialVersionUID = -7650903191002190468L;
        final SingleObserver<? super T> downstream;

        SingleDisposable(SingleObserver<? super T> actual, SingleSubject<T> parent) {
            this.downstream = actual;
            lazySet(parent);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            SingleSubject<T> parent = getAndSet(null);
            if (parent != null) {
                parent.remove(this);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() == null;
        }
    }
}
