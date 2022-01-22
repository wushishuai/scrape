package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Scheduler;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.flowables.ConnectableFlowable;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.disposables.ResettableConnectable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;
import p005io.reactivex.schedulers.Schedulers;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableRefCount */
/* loaded from: classes.dex */
public final class FlowableRefCount<T> extends Flowable<T> {
    RefConnection connection;

    /* renamed from: n */
    final int f126n;
    final Scheduler scheduler;
    final ConnectableFlowable<T> source;
    final long timeout;
    final TimeUnit unit;

    public FlowableRefCount(ConnectableFlowable<T> connectableFlowable) {
        this(connectableFlowable, 1, 0, TimeUnit.NANOSECONDS, Schedulers.trampoline());
    }

    public FlowableRefCount(ConnectableFlowable<T> connectableFlowable, int i, long j, TimeUnit timeUnit, Scheduler scheduler) {
        this.source = connectableFlowable;
        this.f126n = i;
        this.timeout = j;
        this.unit = timeUnit;
        this.scheduler = scheduler;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        RefConnection refConnection;
        boolean z;
        synchronized (this) {
            refConnection = this.connection;
            if (refConnection == null) {
                refConnection = new RefConnection(this);
                this.connection = refConnection;
            }
            long j = refConnection.subscriberCount;
            if (j == 0 && refConnection.timer != null) {
                refConnection.timer.dispose();
            }
            long j2 = j + 1;
            refConnection.subscriberCount = j2;
            z = true;
            if (refConnection.connected || j2 != ((long) this.f126n)) {
                z = false;
            } else {
                refConnection.connected = true;
            }
        }
        this.source.subscribe((FlowableSubscriber) new RefCountSubscriber(subscriber, this, refConnection));
        if (z) {
            this.source.connect(refConnection);
        }
    }

    void cancel(RefConnection refConnection) {
        synchronized (this) {
            if (this.connection != null && this.connection == refConnection) {
                long j = refConnection.subscriberCount - 1;
                refConnection.subscriberCount = j;
                if (j == 0 && refConnection.connected) {
                    if (this.timeout == 0) {
                        timeout(refConnection);
                        return;
                    }
                    SequentialDisposable sequentialDisposable = new SequentialDisposable();
                    refConnection.timer = sequentialDisposable;
                    sequentialDisposable.replace(this.scheduler.scheduleDirect(refConnection, this.timeout, this.unit));
                }
            }
        }
    }

    void terminated(RefConnection refConnection) {
        synchronized (this) {
            if (this.connection != null && this.connection == refConnection) {
                this.connection = null;
                if (refConnection.timer != null) {
                    refConnection.timer.dispose();
                }
            }
            long j = refConnection.subscriberCount - 1;
            refConnection.subscriberCount = j;
            if (j == 0) {
                if (this.source instanceof Disposable) {
                    ((Disposable) this.source).dispose();
                } else if (this.source instanceof ResettableConnectable) {
                    ((ResettableConnectable) this.source).resetIf(refConnection.get());
                }
            }
        }
    }

    void timeout(RefConnection refConnection) {
        synchronized (this) {
            if (refConnection.subscriberCount == 0 && refConnection == this.connection) {
                this.connection = null;
                Disposable disposable = refConnection.get();
                DisposableHelper.dispose(refConnection);
                if (this.source instanceof Disposable) {
                    ((Disposable) this.source).dispose();
                } else if (this.source instanceof ResettableConnectable) {
                    ((ResettableConnectable) this.source).resetIf(disposable);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRefCount$RefConnection */
    /* loaded from: classes.dex */
    public static final class RefConnection extends AtomicReference<Disposable> implements Runnable, Consumer<Disposable> {
        private static final long serialVersionUID = -4552101107598366241L;
        boolean connected;
        final FlowableRefCount<?> parent;
        long subscriberCount;
        Disposable timer;

        RefConnection(FlowableRefCount<?> flowableRefCount) {
            this.parent = flowableRefCount;
        }

        @Override // java.lang.Runnable
        public void run() {
            this.parent.timeout(this);
        }

        public void accept(Disposable disposable) throws Exception {
            DisposableHelper.replace(this, disposable);
        }
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableRefCount$RefCountSubscriber */
    /* loaded from: classes.dex */
    static final class RefCountSubscriber<T> extends AtomicBoolean implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = -7419642935409022375L;
        final RefConnection connection;
        final Subscriber<? super T> downstream;
        final FlowableRefCount<T> parent;
        Subscription upstream;

        RefCountSubscriber(Subscriber<? super T> subscriber, FlowableRefCount<T> flowableRefCount, RefConnection refConnection) {
            this.downstream = subscriber;
            this.parent = flowableRefCount;
            this.connection = refConnection;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.downstream.onNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (compareAndSet(false, true)) {
                this.parent.terminated(this.connection);
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (compareAndSet(false, true)) {
                this.parent.terminated(this.connection);
                this.downstream.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
            if (compareAndSet(false, true)) {
                this.parent.cancel(this.connection);
            }
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }
    }
}
