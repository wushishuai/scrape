package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.SequentialDisposable;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.internal.util.NotificationLite;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeConcatArray */
/* loaded from: classes.dex */
public final class MaybeConcatArray<T> extends Flowable<T> {
    final MaybeSource<? extends T>[] sources;

    public MaybeConcatArray(MaybeSource<? extends T>[] sources) {
        this.sources = sources;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        ConcatMaybeObserver<T> parent = new ConcatMaybeObserver<>(s, this.sources);
        s.onSubscribe(parent);
        parent.drain();
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeConcatArray$ConcatMaybeObserver */
    /* loaded from: classes.dex */
    static final class ConcatMaybeObserver<T> extends AtomicInteger implements MaybeObserver<T>, Subscription {
        private static final long serialVersionUID = 3520831347801429610L;
        final Subscriber<? super T> downstream;
        int index;
        long produced;
        final MaybeSource<? extends T>[] sources;
        final AtomicLong requested = new AtomicLong();
        final SequentialDisposable disposables = new SequentialDisposable();
        final AtomicReference<Object> current = new AtomicReference<>(NotificationLite.COMPLETE);

        ConcatMaybeObserver(Subscriber<? super T> actual, MaybeSource<? extends T>[] sources) {
            this.downstream = actual;
            this.sources = sources;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.disposables.dispose();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable d) {
            this.disposables.replace(d);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T value) {
            this.current.lazySet(value);
            drain();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.current.lazySet(NotificationLite.COMPLETE);
            drain();
        }

        void drain() {
            boolean goNextSource;
            if (getAndIncrement() == 0) {
                AtomicReference<Object> c = this.current;
                Subscriber<? super T> a = this.downstream;
                Disposable cancelled = this.disposables;
                while (!cancelled.isDisposed()) {
                    Object o = c.get();
                    if (o != null) {
                        if (o != NotificationLite.COMPLETE) {
                            long p = this.produced;
                            if (p != this.requested.get()) {
                                this.produced = 1 + p;
                                c.lazySet(null);
                                goNextSource = true;
                                a.onNext(o);
                            } else {
                                goNextSource = false;
                            }
                        } else {
                            c.lazySet(null);
                            goNextSource = true;
                        }
                        if (goNextSource && !cancelled.isDisposed()) {
                            int i = this.index;
                            MaybeSource<? extends T>[] maybeSourceArr = this.sources;
                            if (i == maybeSourceArr.length) {
                                a.onComplete();
                                return;
                            } else {
                                this.index = i + 1;
                                maybeSourceArr[i].subscribe(this);
                            }
                        }
                    }
                    if (decrementAndGet() == 0) {
                        return;
                    }
                }
                c.lazySet(null);
            }
        }
    }
}
