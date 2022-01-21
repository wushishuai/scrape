package p005io.reactivex.internal.subscriptions;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscription;
import p005io.reactivex.exceptions.ProtocolViolationException;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.util.BackpressureHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.subscriptions.SubscriptionHelper */
/* loaded from: classes.dex */
public enum SubscriptionHelper implements Subscription {
    CANCELLED;

    @Override // org.reactivestreams.Subscription
    public void request(long n) {
    }

    @Override // org.reactivestreams.Subscription
    public void cancel() {
    }

    public static boolean validate(Subscription current, Subscription next) {
        if (next == null) {
            RxJavaPlugins.onError(new NullPointerException("next is null"));
            return false;
        } else if (current == null) {
            return true;
        } else {
            next.cancel();
            reportSubscriptionSet();
            return false;
        }
    }

    public static void reportSubscriptionSet() {
        RxJavaPlugins.onError(new ProtocolViolationException("Subscription already set!"));
    }

    public static boolean validate(long n) {
        if (n > 0) {
            return true;
        }
        RxJavaPlugins.onError(new IllegalArgumentException("n > 0 required but it was " + n));
        return false;
    }

    public static void reportMoreProduced(long n) {
        RxJavaPlugins.onError(new ProtocolViolationException("More produced than requested: " + n));
    }

    public static boolean isCancelled(Subscription s) {
        return s == CANCELLED;
    }

    public static boolean set(AtomicReference<Subscription> field, Subscription s) {
        Subscription current;
        do {
            current = field.get();
            if (current == CANCELLED) {
                if (s == null) {
                    return false;
                }
                s.cancel();
                return false;
            }
        } while (!field.compareAndSet(current, s));
        if (current == null) {
            return true;
        }
        current.cancel();
        return true;
    }

    public static boolean setOnce(AtomicReference<Subscription> field, Subscription s) {
        ObjectHelper.requireNonNull(s, "s is null");
        if (field.compareAndSet(null, s)) {
            return true;
        }
        s.cancel();
        if (field.get() == CANCELLED) {
            return false;
        }
        reportSubscriptionSet();
        return false;
    }

    public static boolean replace(AtomicReference<Subscription> field, Subscription s) {
        Subscription current;
        do {
            current = field.get();
            if (current == CANCELLED) {
                if (s == null) {
                    return false;
                }
                s.cancel();
                return false;
            }
        } while (!field.compareAndSet(current, s));
        return true;
    }

    public static boolean cancel(AtomicReference<Subscription> field) {
        Subscription current;
        Subscription current2 = field.get();
        SubscriptionHelper subscriptionHelper = CANCELLED;
        if (current2 == subscriptionHelper || (current = field.getAndSet(subscriptionHelper)) == CANCELLED) {
            return false;
        }
        if (current == null) {
            return true;
        }
        current.cancel();
        return true;
    }

    public static boolean deferredSetOnce(AtomicReference<Subscription> field, AtomicLong requested, Subscription s) {
        if (!setOnce(field, s)) {
            return false;
        }
        long r = requested.getAndSet(0);
        if (r == 0) {
            return true;
        }
        s.request(r);
        return true;
    }

    public static void deferredRequest(AtomicReference<Subscription> field, AtomicLong requested, long n) {
        Subscription s = field.get();
        if (s != null) {
            s.request(n);
        } else if (validate(n)) {
            BackpressureHelper.add(requested, n);
            Subscription s2 = field.get();
            if (s2 != null) {
                long r = requested.getAndSet(0);
                if (r != 0) {
                    s2.request(r);
                }
            }
        }
    }

    public static boolean setOnce(AtomicReference<Subscription> field, Subscription s, long request) {
        if (!setOnce(field, s)) {
            return false;
        }
        s.request(request);
        return true;
    }
}
