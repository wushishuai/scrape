package p005io.reactivex.subjects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.subjects.MaybeSubject */
/* loaded from: classes.dex */
public final class MaybeSubject<T> extends Maybe<T> implements MaybeObserver<T> {
    static final MaybeDisposable[] EMPTY = new MaybeDisposable[0];
    static final MaybeDisposable[] TERMINATED = new MaybeDisposable[0];
    Throwable error;
    T value;
    final AtomicBoolean once = new AtomicBoolean();
    final AtomicReference<MaybeDisposable<T>[]> observers = new AtomicReference<>(EMPTY);

    @CheckReturnValue
    @NonNull
    public static <T> MaybeSubject<T> create() {
        return new MaybeSubject<>();
    }

    MaybeSubject() {
    }

    @Override // p005io.reactivex.MaybeObserver
    public void onSubscribe(Disposable disposable) {
        if (this.observers.get() == TERMINATED) {
            disposable.dispose();
        }
    }

    @Override // p005io.reactivex.MaybeObserver
    public void onSuccess(T t) {
        ObjectHelper.requireNonNull(t, "onSuccess called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.once.compareAndSet(false, true)) {
            this.value = t;
            for (MaybeDisposable<T> maybeDisposable : this.observers.getAndSet(TERMINATED)) {
                maybeDisposable.downstream.onSuccess(t);
            }
        }
    }

    @Override // p005io.reactivex.MaybeObserver
    public void onError(Throwable th) {
        ObjectHelper.requireNonNull(th, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.once.compareAndSet(false, true)) {
            this.error = th;
            for (MaybeDisposable<T> maybeDisposable : this.observers.getAndSet(TERMINATED)) {
                maybeDisposable.downstream.onError(th);
            }
            return;
        }
        RxJavaPlugins.onError(th);
    }

    @Override // p005io.reactivex.MaybeObserver
    public void onComplete() {
        if (this.once.compareAndSet(false, true)) {
            for (MaybeDisposable<T> maybeDisposable : this.observers.getAndSet(TERMINATED)) {
                maybeDisposable.downstream.onComplete();
            }
        }
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        MaybeDisposable<T> maybeDisposable = new MaybeDisposable<>(maybeObserver, this);
        maybeObserver.onSubscribe(maybeDisposable);
        if (!add(maybeDisposable)) {
            Throwable th = this.error;
            if (th != null) {
                maybeObserver.onError(th);
                return;
            }
            Object obj = (T) this.value;
            if (obj == null) {
                maybeObserver.onComplete();
            } else {
                maybeObserver.onSuccess(obj);
            }
        } else if (maybeDisposable.isDisposed()) {
            remove(maybeDisposable);
        }
    }

    boolean add(MaybeDisposable<T> maybeDisposable) {
        MaybeDisposable<T>[] maybeDisposableArr;
        MaybeDisposable<T>[] maybeDisposableArr2;
        do {
            maybeDisposableArr = this.observers.get();
            if (maybeDisposableArr == TERMINATED) {
                return false;
            }
            int length = maybeDisposableArr.length;
            maybeDisposableArr2 = new MaybeDisposable[length + 1];
            System.arraycopy(maybeDisposableArr, 0, maybeDisposableArr2, 0, length);
            maybeDisposableArr2[length] = maybeDisposable;
        } while (!this.observers.compareAndSet(maybeDisposableArr, maybeDisposableArr2));
        return true;
    }

    void remove(MaybeDisposable<T> maybeDisposable) {
        MaybeDisposable<T>[] maybeDisposableArr;
        MaybeDisposable<T>[] maybeDisposableArr2;
        do {
            maybeDisposableArr = this.observers.get();
            int length = maybeDisposableArr.length;
            if (length != 0) {
                int i = -1;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (maybeDisposableArr[i2] == maybeDisposable) {
                        i = i2;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (i >= 0) {
                    if (length == 1) {
                        maybeDisposableArr2 = EMPTY;
                    } else {
                        MaybeDisposable<T>[] maybeDisposableArr3 = new MaybeDisposable[length - 1];
                        System.arraycopy(maybeDisposableArr, 0, maybeDisposableArr3, 0, i);
                        System.arraycopy(maybeDisposableArr, i + 1, maybeDisposableArr3, i, (length - i) - 1);
                        maybeDisposableArr2 = maybeDisposableArr3;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.observers.compareAndSet(maybeDisposableArr, maybeDisposableArr2));
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

    public boolean hasComplete() {
        return this.observers.get() == TERMINATED && this.value == null && this.error == null;
    }

    public boolean hasObservers() {
        return this.observers.get().length != 0;
    }

    int observerCount() {
        return this.observers.get().length;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.subjects.MaybeSubject$MaybeDisposable */
    /* loaded from: classes.dex */
    public static final class MaybeDisposable<T> extends AtomicReference<MaybeSubject<T>> implements Disposable {
        private static final long serialVersionUID = -7650903191002190468L;
        final MaybeObserver<? super T> downstream;

        MaybeDisposable(MaybeObserver<? super T> maybeObserver, MaybeSubject<T> maybeSubject) {
            this.downstream = maybeObserver;
            lazySet(maybeSubject);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            MaybeSubject<T> andSet = getAndSet(null);
            if (andSet != null) {
                andSet.remove(this);
            }
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return get() == null;
        }
    }
}
