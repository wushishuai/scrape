package p005io.reactivex.observers;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.Observer;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.QueueDisposable;
import p005io.reactivex.internal.util.ExceptionHelper;

/* renamed from: io.reactivex.observers.TestObserver */
/* loaded from: classes.dex */
public class TestObserver<T> extends BaseTestConsumer<T, TestObserver<T>> implements Observer<T>, Disposable, MaybeObserver<T>, SingleObserver<T>, CompletableObserver {
    private final Observer<? super T> downstream;

    /* renamed from: qd */
    private QueueDisposable<T> f200qd;
    private final AtomicReference<Disposable> upstream;

    /* renamed from: io.reactivex.observers.TestObserver$EmptyObserver */
    /* loaded from: classes.dex */
    enum EmptyObserver implements Observer<Object> {
        INSTANCE;

        @Override // p005io.reactivex.Observer
        public void onComplete() {
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable th) {
        }

        @Override // p005io.reactivex.Observer
        public void onNext(Object obj) {
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable disposable) {
        }
    }

    public static <T> TestObserver<T> create() {
        return new TestObserver<>();
    }

    public static <T> TestObserver<T> create(Observer<? super T> observer) {
        return new TestObserver<>(observer);
    }

    public TestObserver() {
        this(EmptyObserver.INSTANCE);
    }

    public TestObserver(Observer<? super T> observer) {
        this.upstream = new AtomicReference<>();
        this.downstream = observer;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        this.lastThread = Thread.currentThread();
        if (disposable == null) {
            this.errors.add(new NullPointerException("onSubscribe received a null Subscription"));
        } else if (!this.upstream.compareAndSet(null, disposable)) {
            disposable.dispose();
            if (this.upstream.get() != DisposableHelper.DISPOSED) {
                this.errors.add(new IllegalStateException("onSubscribe received multiple subscriptions: " + disposable));
            }
        } else {
            if (this.initialFusionMode != 0 && (disposable instanceof QueueDisposable)) {
                this.f200qd = (QueueDisposable) disposable;
                int requestFusion = this.f200qd.requestFusion(this.initialFusionMode);
                this.establishedFusionMode = requestFusion;
                if (requestFusion == 1) {
                    this.checkSubscriptionOnce = true;
                    this.lastThread = Thread.currentThread();
                    while (true) {
                        try {
                            T poll = this.f200qd.poll();
                            if (poll != null) {
                                this.values.add(poll);
                            } else {
                                this.completions++;
                                this.upstream.lazySet(DisposableHelper.DISPOSED);
                                return;
                            }
                        } catch (Throwable th) {
                            this.errors.add(th);
                            return;
                        }
                    }
                }
            }
            this.downstream.onSubscribe(disposable);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onNext(T t) {
        if (!this.checkSubscriptionOnce) {
            this.checkSubscriptionOnce = true;
            if (this.upstream.get() == null) {
                this.errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        this.lastThread = Thread.currentThread();
        if (this.establishedFusionMode == 2) {
            while (true) {
                try {
                    T poll = this.f200qd.poll();
                    if (poll != null) {
                        this.values.add(poll);
                    } else {
                        return;
                    }
                } catch (Throwable th) {
                    this.errors.add(th);
                    this.f200qd.dispose();
                    return;
                }
            }
        } else {
            this.values.add(t);
            if (t == null) {
                this.errors.add(new NullPointerException("onNext received a null value"));
            }
            this.downstream.onNext(t);
        }
    }

    @Override // p005io.reactivex.Observer
    public void onError(Throwable th) {
        if (!this.checkSubscriptionOnce) {
            this.checkSubscriptionOnce = true;
            if (this.upstream.get() == null) {
                this.errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        try {
            this.lastThread = Thread.currentThread();
            if (th == null) {
                this.errors.add(new NullPointerException("onError received a null Throwable"));
            } else {
                this.errors.add(th);
            }
            this.downstream.onError(th);
        } finally {
            this.done.countDown();
        }
    }

    @Override // p005io.reactivex.Observer
    public void onComplete() {
        if (!this.checkSubscriptionOnce) {
            this.checkSubscriptionOnce = true;
            if (this.upstream.get() == null) {
                this.errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        try {
            this.lastThread = Thread.currentThread();
            this.completions++;
            this.downstream.onComplete();
        } finally {
            this.done.countDown();
        }
    }

    public final boolean isCancelled() {
        return isDisposed();
    }

    public final void cancel() {
        dispose();
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final void dispose() {
        DisposableHelper.dispose(this.upstream);
    }

    @Override // p005io.reactivex.disposables.Disposable
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(this.upstream.get());
    }

    public final boolean hasSubscription() {
        return this.upstream.get() != null;
    }

    @Override // p005io.reactivex.observers.BaseTestConsumer
    public final TestObserver<T> assertSubscribed() {
        if (this.upstream.get() != null) {
            return this;
        }
        throw fail("Not subscribed!");
    }

    @Override // p005io.reactivex.observers.BaseTestConsumer
    public final TestObserver<T> assertNotSubscribed() {
        if (this.upstream.get() != null) {
            throw fail("Subscribed!");
        } else if (this.errors.isEmpty()) {
            return this;
        } else {
            throw fail("Not subscribed but errors found");
        }
    }

    public final TestObserver<T> assertOf(Consumer<? super TestObserver<T>> consumer) {
        try {
            consumer.accept(this);
            return this;
        } catch (Throwable th) {
            throw ExceptionHelper.wrapOrThrow(th);
        }
    }

    final TestObserver<T> setInitialFusionMode(int i) {
        this.initialFusionMode = i;
        return this;
    }

    final TestObserver<T> assertFusionMode(int i) {
        int i2 = this.establishedFusionMode;
        if (i2 == i) {
            return this;
        }
        if (this.f200qd != null) {
            throw new AssertionError("Fusion mode different. Expected: " + fusionModeToString(i) + ", actual: " + fusionModeToString(i2));
        }
        throw fail("Upstream is not fuseable");
    }

    static String fusionModeToString(int i) {
        switch (i) {
            case 0:
                return "NONE";
            case 1:
                return "SYNC";
            case 2:
                return "ASYNC";
            default:
                return "Unknown(" + i + ")";
        }
    }

    final TestObserver<T> assertFuseable() {
        if (this.f200qd != null) {
            return this;
        }
        throw new AssertionError("Upstream is not fuseable.");
    }

    final TestObserver<T> assertNotFuseable() {
        if (this.f200qd == null) {
            return this;
        }
        throw new AssertionError("Upstream is fuseable.");
    }

    @Override // p005io.reactivex.MaybeObserver
    public void onSuccess(T t) {
        onNext(t);
        onComplete();
    }
}
