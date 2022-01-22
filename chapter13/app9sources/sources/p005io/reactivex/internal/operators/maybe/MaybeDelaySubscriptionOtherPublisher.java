package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeDelaySubscriptionOtherPublisher */
/* loaded from: classes.dex */
public final class MaybeDelaySubscriptionOtherPublisher<T, U> extends AbstractMaybeWithUpstream<T, T> {
    final Publisher<U> other;

    public MaybeDelaySubscriptionOtherPublisher(MaybeSource<T> maybeSource, Publisher<U> publisher) {
        super(maybeSource);
        this.other = publisher;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.other.subscribe(new OtherSubscriber(maybeObserver, this.source));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeDelaySubscriptionOtherPublisher$OtherSubscriber */
    /* loaded from: classes.dex */
    static final class OtherSubscriber<T> implements FlowableSubscriber<Object>, Disposable {
        final DelayMaybeObserver<T> main;
        MaybeSource<T> source;
        Subscription upstream;

        OtherSubscriber(MaybeObserver<? super T> maybeObserver, MaybeSource<T> maybeSource) {
            this.main = new DelayMaybeObserver<>(maybeObserver);
            this.source = maybeSource;
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.main.downstream.onSubscribe(this);
                subscription.request(Long.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(Object obj) {
            if (this.upstream != SubscriptionHelper.CANCELLED) {
                this.upstream.cancel();
                this.upstream = SubscriptionHelper.CANCELLED;
                subscribeNext();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.upstream != SubscriptionHelper.CANCELLED) {
                this.upstream = SubscriptionHelper.CANCELLED;
                this.main.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.upstream != SubscriptionHelper.CANCELLED) {
                this.upstream = SubscriptionHelper.CANCELLED;
                subscribeNext();
            }
        }

        void subscribeNext() {
            MaybeSource<T> maybeSource = this.source;
            this.source = null;
            maybeSource.subscribe(this.main);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(this.main.get());
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.cancel();
            this.upstream = SubscriptionHelper.CANCELLED;
            DisposableHelper.dispose(this.main);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.maybe.MaybeDelaySubscriptionOtherPublisher$DelayMaybeObserver */
    /* loaded from: classes.dex */
    public static final class DelayMaybeObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T> {
        private static final long serialVersionUID = 706635022205076709L;
        final MaybeObserver<? super T> downstream;

        DelayMaybeObserver(MaybeObserver<? super T> maybeObserver) {
            this.downstream = maybeObserver;
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            this.downstream.onSuccess(t);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
        }
    }
}
