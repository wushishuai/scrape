package p005io.reactivex.internal.operators.flowable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import p005io.reactivex.functions.Action;
import p005io.reactivex.functions.Consumer;
import p005io.reactivex.internal.functions.Functions;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.subscribers.BlockingSubscriber;
import p005io.reactivex.internal.subscribers.BoundedSubscriber;
import p005io.reactivex.internal.subscribers.LambdaSubscriber;
import p005io.reactivex.internal.util.BlockingHelper;
import p005io.reactivex.internal.util.BlockingIgnoringReceiver;
import p005io.reactivex.internal.util.ExceptionHelper;
import p005io.reactivex.internal.util.NotificationLite;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableBlockingSubscribe */
/* loaded from: classes.dex */
public final class FlowableBlockingSubscribe {
    private FlowableBlockingSubscribe() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> void subscribe(Publisher<? extends T> o, Subscriber<? super T> subscriber) {
        Object v;
        BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
        BlockingSubscriber<T> bs = new BlockingSubscriber<>(queue);
        o.subscribe(bs);
        do {
            try {
                if (!bs.isCancelled()) {
                    v = queue.poll();
                    if (v == null) {
                        if (!bs.isCancelled()) {
                            BlockingHelper.verifyNonBlocking();
                            v = queue.take();
                        } else {
                            return;
                        }
                    }
                    if (bs.isCancelled() || v == BlockingSubscriber.TERMINATED) {
                        return;
                    }
                } else {
                    return;
                }
            } catch (InterruptedException e) {
                bs.cancel();
                subscriber.onError(e);
                return;
            }
        } while (!NotificationLite.acceptFull(v, subscriber));
    }

    public static <T> void subscribe(Publisher<? extends T> o) {
        BlockingIgnoringReceiver callback = new BlockingIgnoringReceiver();
        LambdaSubscriber<T> ls = new LambdaSubscriber<>(Functions.emptyConsumer(), callback, callback, Functions.REQUEST_MAX);
        o.subscribe(ls);
        BlockingHelper.awaitForComplete(callback, ls);
        Throwable e = callback.error;
        if (e != null) {
            throw ExceptionHelper.wrapOrThrow(e);
        }
    }

    public static <T> void subscribe(Publisher<? extends T> o, Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
        ObjectHelper.requireNonNull(onNext, "onNext is null");
        ObjectHelper.requireNonNull(onError, "onError is null");
        ObjectHelper.requireNonNull(onComplete, "onComplete is null");
        subscribe(o, new LambdaSubscriber(onNext, onError, onComplete, Functions.REQUEST_MAX));
    }

    public static <T> void subscribe(Publisher<? extends T> o, Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete, int bufferSize) {
        ObjectHelper.requireNonNull(onNext, "onNext is null");
        ObjectHelper.requireNonNull(onError, "onError is null");
        ObjectHelper.requireNonNull(onComplete, "onComplete is null");
        ObjectHelper.verifyPositive(bufferSize, "number > 0 required");
        subscribe(o, new BoundedSubscriber(onNext, onError, onComplete, Functions.boundedConsumer(bufferSize), bufferSize));
    }
}
