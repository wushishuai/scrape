package p005io.reactivex.subjects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.annotations.CheckReturnValue;
import p005io.reactivex.annotations.NonNull;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.subjects.CompletableSubject */
/* loaded from: classes.dex */
public final class CompletableSubject extends Completable implements CompletableObserver {
    static final CompletableDisposable[] EMPTY = new CompletableDisposable[0];
    static final CompletableDisposable[] TERMINATED = new CompletableDisposable[0];
    Throwable error;
    final AtomicBoolean once = new AtomicBoolean();
    final AtomicReference<CompletableDisposable[]> observers = new AtomicReference<>(EMPTY);

    @CheckReturnValue
    @NonNull
    public static CompletableSubject create() {
        return new CompletableSubject();
    }

    CompletableSubject() {
    }

    @Override // p005io.reactivex.CompletableObserver
    public void onSubscribe(Disposable disposable) {
        if (this.observers.get() == TERMINATED) {
            disposable.dispose();
        }
    }

    @Override // p005io.reactivex.CompletableObserver
    public void onError(Throwable th) {
        ObjectHelper.requireNonNull(th, "onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        if (this.once.compareAndSet(false, true)) {
            this.error = th;
            for (CompletableDisposable completableDisposable : this.observers.getAndSet(TERMINATED)) {
                completableDisposable.downstream.onError(th);
            }
            return;
        }
        RxJavaPlugins.onError(th);
    }

    @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
    public void onComplete() {
        if (this.once.compareAndSet(false, true)) {
            for (CompletableDisposable completableDisposable : this.observers.getAndSet(TERMINATED)) {
                completableDisposable.downstream.onComplete();
            }
        }
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        CompletableDisposable completableDisposable = new CompletableDisposable(completableObserver, this);
        completableObserver.onSubscribe(completableDisposable);
        if (!add(completableDisposable)) {
            Throwable th = this.error;
            if (th != null) {
                completableObserver.onError(th);
            } else {
                completableObserver.onComplete();
            }
        } else if (completableDisposable.isDisposed()) {
            remove(completableDisposable);
        }
    }

    boolean add(CompletableDisposable completableDisposable) {
        CompletableDisposable[] completableDisposableArr;
        CompletableDisposable[] completableDisposableArr2;
        do {
            completableDisposableArr = this.observers.get();
            if (completableDisposableArr == TERMINATED) {
                return false;
            }
            int length = completableDisposableArr.length;
            completableDisposableArr2 = new CompletableDisposable[length + 1];
            System.arraycopy(completableDisposableArr, 0, completableDisposableArr2, 0, length);
            completableDisposableArr2[length] = completableDisposable;
        } while (!this.observers.compareAndSet(completableDisposableArr, completableDisposableArr2));
        return true;
    }

    void remove(CompletableDisposable completableDisposable) {
        CompletableDisposable[] completableDisposableArr;
        CompletableDisposable[] completableDisposableArr2;
        do {
            completableDisposableArr = this.observers.get();
            int length = completableDisposableArr.length;
            if (length != 0) {
                int i = -1;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (completableDisposableArr[i2] == completableDisposable) {
                        i = i2;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (i >= 0) {
                    if (length == 1) {
                        completableDisposableArr2 = EMPTY;
                    } else {
                        CompletableDisposable[] completableDisposableArr3 = new CompletableDisposable[length - 1];
                        System.arraycopy(completableDisposableArr, 0, completableDisposableArr3, 0, i);
                        System.arraycopy(completableDisposableArr, i + 1, completableDisposableArr3, i, (length - i) - 1);
                        completableDisposableArr2 = completableDisposableArr3;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        } while (!this.observers.compareAndSet(completableDisposableArr, completableDisposableArr2));
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
        return this.observers.get() == TERMINATED && this.error == null;
    }

    public boolean hasObservers() {
        return this.observers.get().length != 0;
    }

    int observerCount() {
        return this.observers.get().length;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.subjects.CompletableSubject$CompletableDisposable */
    /* loaded from: classes.dex */
    public static final class CompletableDisposable extends AtomicReference<CompletableSubject> implements Disposable {
        private static final long serialVersionUID = -7650903191002190468L;
        final CompletableObserver downstream;

        CompletableDisposable(CompletableObserver completableObserver, CompletableSubject completableSubject) {
            this.downstream = completableObserver;
            lazySet(completableSubject);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            CompletableSubject andSet = getAndSet(null);
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
