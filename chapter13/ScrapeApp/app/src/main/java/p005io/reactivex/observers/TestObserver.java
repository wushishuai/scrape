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

    public static <T> TestObserver<T> create() {
        return new TestObserver<>();
    }

    public static <T> TestObserver<T> create(Observer<? super T> delegate) {
        return new TestObserver<>(delegate);
    }

    public TestObserver() {
        this(EmptyObserver.INSTANCE);
    }

    public TestObserver(Observer<? super T> downstream) {
        this.upstream = new AtomicReference<>();
        this.downstream = downstream;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable d) {
        this.lastThread = Thread.currentThread();
        if (d == null) {
            this.errors.add(new NullPointerException("onSubscribe received a null Subscription"));
        } else if (!this.upstream.compareAndSet(null, d)) {
            d.dispose();
            if (this.upstream.get() != DisposableHelper.DISPOSED) {
                this.errors.add(new IllegalStateException("onSubscribe received multiple subscriptions: " + d));
            }
        } else {
            if (this.initialFusionMode != 0 && (d instanceof QueueDisposable)) {
                this.f200qd = (QueueDisposable) d;
                int m = this.f200qd.requestFusion(this.initialFusionMode);
                this.establishedFusionMode = m;
                if (m == 1) {
                    this.checkSubscriptionOnce = true;
                    this.lastThread = Thread.currentThread();
                    while (true) {
                        try {
                            T t = this.f200qd.poll();
                            if (t != null) {
                                this.values.add(t);
                            } else {
                                this.completions++;
                                this.upstream.lazySet(DisposableHelper.DISPOSED);
                                return;
                            }
                        } catch (Throwable ex) {
                            this.errors.add(ex);
                            return;
                        }
                    }
                }
            }
            this.downstream.onSubscribe(d);
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
                    T t2 = this.f200qd.poll();
                    if (t2 != null) {
                        this.values.add(t2);
                    } else {
                        return;
                    }
                } catch (Throwable ex) {
                    this.errors.add(ex);
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
    public void onError(Throwable t) {
        if (!this.checkSubscriptionOnce) {
            this.checkSubscriptionOnce = true;
            if (this.upstream.get() == null) {
                this.errors.add(new IllegalStateException("onSubscribe not called in proper order"));
            }
        }
        try {
            this.lastThread = Thread.currentThread();
            if (t == null) {
                this.errors.add(new NullPointerException("onError received a null Throwable"));
            } else {
                this.errors.add(t);
            }
            this.downstream.onError(t);
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

    public final TestObserver<T> assertOf(Consumer<? super TestObserver<T>> check) {
        try {
            check.accept(this);
            return this;
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    final TestObserver<T> setInitialFusionMode(int mode) {
        this.initialFusionMode = mode;
        return this;
    }

    final TestObserver<T> assertFusionMode(int mode) {
        int m = this.establishedFusionMode;
        if (m == mode) {
            return this;
        }
        if (this.f200qd != null) {
            throw new AssertionError("Fusion mode different. Expected: " + fusionModeToString(mode) + ", actual: " + fusionModeToString(m));
        }
        throw fail("Upstream is not fuseable");
    }

    static String fusionModeToString(int mode) {
        switch (mode) {
            case 0:
                return "NONE";
            case 1:
                return "SYNC";
            case 2:
                return "ASYNC";
            default:
                return "Unknown(" + mode + ")";
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
    public void onSuccess(T value) {
        onNext(value);
        onComplete();
    }

    /* renamed from: io.reactivex.observers.TestObserver$EmptyObserver */
    /* loaded from: classes.dex */
    enum EmptyObserver implements Observer<Object> {
        INSTANCE;

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
        }

        @Override // p005io.reactivex.Observer
        public void onNext(Object t) {
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable t) {
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
        }
    }
}
