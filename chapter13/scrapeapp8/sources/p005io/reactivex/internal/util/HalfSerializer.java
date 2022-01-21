package p005io.reactivex.internal.util;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Subscriber;
import p005io.reactivex.Observer;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.util.HalfSerializer */
/* loaded from: classes.dex */
public final class HalfSerializer {
    private HalfSerializer() {
        throw new IllegalStateException("No instances!");
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> void onNext(Subscriber<? super T> subscriber, T value, AtomicInteger wip, AtomicThrowable error) {
        if (wip.get() == 0 && wip.compareAndSet(0, 1)) {
            subscriber.onNext(value);
            if (wip.decrementAndGet() != 0) {
                Throwable ex = error.terminate();
                if (ex != null) {
                    subscriber.onError(ex);
                } else {
                    subscriber.onComplete();
                }
            }
        }
    }

    public static void onError(Subscriber<?> subscriber, Throwable ex, AtomicInteger wip, AtomicThrowable error) {
        if (!error.addThrowable(ex)) {
            RxJavaPlugins.onError(ex);
        } else if (wip.getAndIncrement() == 0) {
            subscriber.onError(error.terminate());
        }
    }

    public static void onComplete(Subscriber<?> subscriber, AtomicInteger wip, AtomicThrowable error) {
        if (wip.getAndIncrement() == 0) {
            Throwable ex = error.terminate();
            if (ex != null) {
                subscriber.onError(ex);
            } else {
                subscriber.onComplete();
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> void onNext(Observer<? super T> observer, T value, AtomicInteger wip, AtomicThrowable error) {
        if (wip.get() == 0 && wip.compareAndSet(0, 1)) {
            observer.onNext(value);
            if (wip.decrementAndGet() != 0) {
                Throwable ex = error.terminate();
                if (ex != null) {
                    observer.onError(ex);
                } else {
                    observer.onComplete();
                }
            }
        }
    }

    public static void onError(Observer<?> observer, Throwable ex, AtomicInteger wip, AtomicThrowable error) {
        if (!error.addThrowable(ex)) {
            RxJavaPlugins.onError(ex);
        } else if (wip.getAndIncrement() == 0) {
            observer.onError(error.terminate());
        }
    }

    public static void onComplete(Observer<?> observer, AtomicInteger wip, AtomicThrowable error) {
        if (wip.getAndIncrement() == 0) {
            Throwable ex = error.terminate();
            if (ex != null) {
                observer.onError(ex);
            } else {
                observer.onComplete();
            }
        }
    }
}
