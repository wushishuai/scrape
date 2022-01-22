package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.Callable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscriptions.EmptySubscription;
import p005io.reactivex.internal.subscriptions.ScalarSubscription;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableScalarXMap */
/* loaded from: classes.dex */
public final class FlowableScalarXMap {
    private FlowableScalarXMap() {
        throw new IllegalStateException("No instances!");
    }

    public static <T, R> boolean tryScalarXMapSubscribe(Publisher<T> publisher, Subscriber<? super R> subscriber, Function<? super T, ? extends Publisher<? extends R>> function) {
        if (!(publisher instanceof Callable)) {
            return false;
        }
        try {
            Object obj = (Object) ((Callable) publisher).call();
            if (obj == 0) {
                EmptySubscription.complete(subscriber);
                return true;
            }
            try {
                Publisher publisher2 = (Publisher) ObjectHelper.requireNonNull(function.apply(obj), "The mapper returned a null Publisher");
                if (publisher2 instanceof Callable) {
                    try {
                        Object call = ((Callable) publisher2).call();
                        if (call == null) {
                            EmptySubscription.complete(subscriber);
                            return true;
                        }
                        subscriber.onSubscribe(new ScalarSubscription(subscriber, call));
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        EmptySubscription.error(th, subscriber);
                        return true;
                    }
                } else {
                    publisher2.subscribe(subscriber);
                }
                return true;
            } catch (Throwable th2) {
                Exceptions.throwIfFatal(th2);
                EmptySubscription.error(th2, subscriber);
                return true;
            }
        } catch (Throwable th3) {
            Exceptions.throwIfFatal(th3);
            EmptySubscription.error(th3, subscriber);
            return true;
        }
    }

    public static <T, U> Flowable<U> scalarXMap(T t, Function<? super T, ? extends Publisher<? extends U>> function) {
        return RxJavaPlugins.onAssembly(new ScalarXMapFlowable(t, function));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableScalarXMap$ScalarXMapFlowable */
    /* loaded from: classes.dex */
    public static final class ScalarXMapFlowable<T, R> extends Flowable<R> {
        final Function<? super T, ? extends Publisher<? extends R>> mapper;
        final T value;

        ScalarXMapFlowable(T t, Function<? super T, ? extends Publisher<? extends R>> function) {
            this.value = t;
            this.mapper = function;
        }

        @Override // p005io.reactivex.Flowable
        public void subscribeActual(Subscriber<? super R> subscriber) {
            try {
                Publisher publisher = (Publisher) ObjectHelper.requireNonNull(this.mapper.apply((T) this.value), "The mapper returned a null Publisher");
                if (publisher instanceof Callable) {
                    try {
                        Object call = ((Callable) publisher).call();
                        if (call == null) {
                            EmptySubscription.complete(subscriber);
                        } else {
                            subscriber.onSubscribe(new ScalarSubscription(subscriber, call));
                        }
                    } catch (Throwable th) {
                        Exceptions.throwIfFatal(th);
                        EmptySubscription.error(th, subscriber);
                    }
                } else {
                    publisher.subscribe(subscriber);
                }
            } catch (Throwable th2) {
                EmptySubscription.error(th2, subscriber);
            }
        }
    }
}
