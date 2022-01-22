package p005io.reactivex.internal.operators.completable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.disposables.CompositeDisposable;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.internal.util.AtomicThrowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.completable.CompletableMerge */
/* loaded from: classes.dex */
public final class CompletableMerge extends Completable {
    final boolean delayErrors;
    final int maxConcurrency;
    final Publisher<? extends CompletableSource> source;

    public CompletableMerge(Publisher<? extends CompletableSource> publisher, int i, boolean z) {
        this.source = publisher;
        this.maxConcurrency = i;
        this.delayErrors = z;
    }

    @Override // p005io.reactivex.Completable
    public void subscribeActual(CompletableObserver completableObserver) {
        this.source.subscribe(new CompletableMergeSubscriber(completableObserver, this.maxConcurrency, this.delayErrors));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableMerge$CompletableMergeSubscriber */
    /* loaded from: classes.dex */
    static final class CompletableMergeSubscriber extends AtomicInteger implements FlowableSubscriber<CompletableSource>, Disposable {
        private static final long serialVersionUID = -2108443387387077490L;
        final boolean delayErrors;
        final CompletableObserver downstream;
        final int maxConcurrency;
        Subscription upstream;
        final CompositeDisposable set = new CompositeDisposable();
        final AtomicThrowable error = new AtomicThrowable();

        CompletableMergeSubscriber(CompletableObserver completableObserver, int i, boolean z) {
            this.downstream = completableObserver;
            this.maxConcurrency = i;
            this.delayErrors = z;
            lazySet(1);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.upstream.cancel();
            this.set.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.set.isDisposed();
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
                int i = this.maxConcurrency;
                if (i == Integer.MAX_VALUE) {
                    subscription.request(Long.MAX_VALUE);
                } else {
                    subscription.request((long) i);
                }
            }
        }

        public void onNext(CompletableSource completableSource) {
            getAndIncrement();
            MergeInnerObserver mergeInnerObserver = new MergeInnerObserver();
            this.set.add(mergeInnerObserver);
            completableSource.subscribe(mergeInnerObserver);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (!this.delayErrors) {
                this.set.dispose();
                if (!this.error.addThrowable(th)) {
                    RxJavaPlugins.onError(th);
                } else if (getAndSet(0) > 0) {
                    this.downstream.onError(this.error.terminate());
                }
            } else if (!this.error.addThrowable(th)) {
                RxJavaPlugins.onError(th);
            } else if (decrementAndGet() == 0) {
                this.downstream.onError(this.error.terminate());
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (decrementAndGet() != 0) {
                return;
            }
            if (this.error.get() != null) {
                this.downstream.onError(this.error.terminate());
            } else {
                this.downstream.onComplete();
            }
        }

        void innerError(MergeInnerObserver mergeInnerObserver, Throwable th) {
            this.set.delete(mergeInnerObserver);
            if (!this.delayErrors) {
                this.upstream.cancel();
                this.set.dispose();
                if (!this.error.addThrowable(th)) {
                    RxJavaPlugins.onError(th);
                } else if (getAndSet(0) > 0) {
                    this.downstream.onError(this.error.terminate());
                }
            } else if (!this.error.addThrowable(th)) {
                RxJavaPlugins.onError(th);
            } else if (decrementAndGet() == 0) {
                this.downstream.onError(this.error.terminate());
            } else if (this.maxConcurrency != Integer.MAX_VALUE) {
                this.upstream.request(1);
            }
        }

        void innerComplete(MergeInnerObserver mergeInnerObserver) {
            this.set.delete(mergeInnerObserver);
            if (decrementAndGet() == 0) {
                Throwable th = this.error.get();
                if (th != null) {
                    this.downstream.onError(th);
                } else {
                    this.downstream.onComplete();
                }
            } else if (this.maxConcurrency != Integer.MAX_VALUE) {
                this.upstream.request(1);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: io.reactivex.internal.operators.completable.CompletableMerge$CompletableMergeSubscriber$MergeInnerObserver */
        /* loaded from: classes.dex */
        public final class MergeInnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
            private static final long serialVersionUID = 251330541679988317L;

            MergeInnerObserver() {
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(this, disposable);
            }

            @Override // p005io.reactivex.CompletableObserver
            public void onError(Throwable th) {
                CompletableMergeSubscriber.this.innerError(this, th);
            }

            @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
            public void onComplete() {
                CompletableMergeSubscriber.this.innerComplete(this);
            }

            @Override // p005io.reactivex.disposables.Disposable
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }

            @Override // p005io.reactivex.disposables.Disposable
            public void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
