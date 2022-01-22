package p005io.reactivex.internal.operators.parallel;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiFunction;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.fuseable.ConditionalSubscriber;
import p005io.reactivex.internal.subscriptions.SubscriptionHelper;
import p005io.reactivex.parallel.ParallelFailureHandling;
import p005io.reactivex.parallel.ParallelFlowable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.parallel.ParallelDoOnNextTry */
/* loaded from: classes.dex */
public final class ParallelDoOnNextTry<T> extends ParallelFlowable<T> {
    final BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler;
    final Consumer<? super T> onNext;
    final ParallelFlowable<T> source;

    public ParallelDoOnNextTry(ParallelFlowable<T> parallelFlowable, Consumer<? super T> consumer, BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> biFunction) {
        this.source = parallelFlowable;
        this.onNext = consumer;
        this.errorHandler = biFunction;
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public void subscribe(Subscriber<? super T>[] subscriberArr) {
        if (validate(subscriberArr)) {
            int length = subscriberArr.length;
            Subscriber<? super T>[] subscriberArr2 = new Subscriber[length];
            for (int i = 0; i < length; i++) {
                Subscriber<? super T> subscriber = subscriberArr[i];
                if (subscriber instanceof ConditionalSubscriber) {
                    subscriberArr2[i] = new ParallelDoOnNextConditionalSubscriber((ConditionalSubscriber) subscriber, this.onNext, this.errorHandler);
                } else {
                    subscriberArr2[i] = new ParallelDoOnNextSubscriber(subscriber, this.onNext, this.errorHandler);
                }
            }
            this.source.subscribe(subscriberArr2);
        }
    }

    @Override // p005io.reactivex.parallel.ParallelFlowable
    public int parallelism() {
        return this.source.parallelism();
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelDoOnNextTry$ParallelDoOnNextSubscriber */
    /* loaded from: classes.dex */
    static final class ParallelDoOnNextSubscriber<T> implements ConditionalSubscriber<T>, Subscription {
        boolean done;
        final Subscriber<? super T> downstream;
        final BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler;
        final Consumer<? super T> onNext;
        Subscription upstream;

        ParallelDoOnNextSubscriber(Subscriber<? super T> subscriber, Consumer<? super T> consumer, BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> biFunction) {
            this.downstream = subscriber;
            this.onNext = consumer;
            this.errorHandler = biFunction;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!tryOnNext(t)) {
                this.upstream.request(1);
            }
        }

        @Override // p005io.reactivex.internal.fuseable.ConditionalSubscriber
        public boolean tryOnNext(T t) {
            if (this.done) {
                return false;
            }
            long j = 0;
            while (true) {
                try {
                    this.onNext.accept(t);
                    this.downstream.onNext(t);
                    return true;
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    try {
                        j++;
                        switch ((ParallelFailureHandling) ObjectHelper.requireNonNull(this.errorHandler.apply(Long.valueOf(j), th), "The errorHandler returned a null item")) {
                            case RETRY:
                                break;
                            case SKIP:
                                return false;
                            case STOP:
                                cancel();
                                onComplete();
                                return false;
                            default:
                                cancel();
                                onError(th);
                                return false;
                        }
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        cancel();
                        onError(new CompositeException(th, th2));
                        return false;
                    }
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
            }
        }
    }

    /* renamed from: io.reactivex.internal.operators.parallel.ParallelDoOnNextTry$ParallelDoOnNextConditionalSubscriber */
    /* loaded from: classes.dex */
    static final class ParallelDoOnNextConditionalSubscriber<T> implements ConditionalSubscriber<T>, Subscription {
        boolean done;
        final ConditionalSubscriber<? super T> downstream;
        final BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler;
        final Consumer<? super T> onNext;
        Subscription upstream;

        ParallelDoOnNextConditionalSubscriber(ConditionalSubscriber<? super T> conditionalSubscriber, Consumer<? super T> consumer, BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> biFunction) {
            this.downstream = conditionalSubscriber;
            this.onNext = consumer;
            this.errorHandler = biFunction;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long j) {
            this.upstream.request(j);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }

        @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription subscription) {
            if (SubscriptionHelper.validate(this.upstream, subscription)) {
                this.upstream = subscription;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!tryOnNext(t) && !this.done) {
                this.upstream.request(1);
            }
        }

        @Override // p005io.reactivex.internal.fuseable.ConditionalSubscriber
        public boolean tryOnNext(T t) {
            if (this.done) {
                return false;
            }
            long j = 0;
            while (true) {
                try {
                    this.onNext.accept(t);
                    return this.downstream.tryOnNext(t);
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    try {
                        j++;
                        switch ((ParallelFailureHandling) ObjectHelper.requireNonNull(this.errorHandler.apply(Long.valueOf(j), th), "The errorHandler returned a null item")) {
                            case RETRY:
                                break;
                            case SKIP:
                                return false;
                            case STOP:
                                cancel();
                                onComplete();
                                return false;
                            default:
                                cancel();
                                onError(th);
                                return false;
                        }
                    } catch (Throwable th2) {
                        Exceptions.throwIfFatal(th2);
                        cancel();
                        onError(new CompositeException(th, th2));
                        return false;
                    }
                }
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable th) {
            if (this.done) {
                RxJavaPlugins.onError(th);
                return;
            }
            this.done = true;
            this.downstream.onError(th);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.downstream.onComplete();
            }
        }
    }
}
