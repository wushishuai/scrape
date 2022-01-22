package p005io.reactivex.internal.operators.single;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.single.SingleTakeUntil */
/* loaded from: classes.dex */
public final class SingleTakeUntil<T, U> extends Single<T> {
    final Publisher<U> other;
    final SingleSource<T> source;

    public SingleTakeUntil(SingleSource<T> singleSource, Publisher<U> publisher) {
        this.source = singleSource;
        this.other = publisher;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        TakeUntilMainObserver takeUntilMainObserver = new TakeUntilMainObserver(singleObserver);
        singleObserver.onSubscribe(takeUntilMainObserver);
        this.other.subscribe(takeUntilMainObserver.other);
        this.source.subscribe(takeUntilMainObserver);
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleTakeUntil$TakeUntilMainObserver */
    /* loaded from: classes.dex */
    static final class TakeUntilMainObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T>, Disposable {
        private static final long serialVersionUID = -622603812305745221L;
        final SingleObserver<? super T> downstream;
        final TakeUntilOtherSubscriber other = new TakeUntilOtherSubscriber(this);

        TakeUntilMainObserver(SingleObserver<? super T> singleObserver) {
            this.downstream = singleObserver;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
            this.other.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.other.dispose();
            if (getAndSet(DisposableHelper.DISPOSED) != DisposableHelper.DISPOSED) {
                this.downstream.onSuccess(t);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.other.dispose();
            if (get() == DisposableHelper.DISPOSED || getAndSet(DisposableHelper.DISPOSED) == DisposableHelper.DISPOSED) {
                RxJavaPlugins.onError(th);
            } else {
                this.downstream.onError(th);
            }
        }

        void otherError(Throwable th) {
            Disposable andSet;
            if (get() == DisposableHelper.DISPOSED || (andSet = getAndSet(DisposableHelper.DISPOSED)) == DisposableHelper.DISPOSED) {
                RxJavaPlugins.onError(th);
                return;
            }
            if (andSet != null) {
                andSet.dispose();
            }
            this.downstream.onError(th);
        }
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleTakeUntil$TakeUntilOtherSubscriber */
    /* loaded from: classes.dex */
    static final class TakeUntilOtherSubscriber extends AtomicReference<Subscription> implements FlowableSubscriber<Object> {
        private static final long serialVersionUID = 5170026210238877381L;
        final TakeUntilMainObserver<?> parent;

        TakeUntilOtherSubscriber(TakeUntilMainObserver<?> takeUntilMainObserver) {
            this.parent = takeUntilMainObserver;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            SubscriptionHelper.setOnce(this, subscription, Long.MAX_VALUE);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object obj) {
            if (SubscriptionHelper.cancel(this)) {
                this.parent.otherError(new CancellationException());
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            this.parent.otherError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (get() != SubscriptionHelper.CANCELLED) {
                lazySet(SubscriptionHelper.CANCELLED);
                this.parent.otherError(new CancellationException());
            }
        }

        public void dispose() {
            SubscriptionHelper.cancel(this);
        }
    }
}
