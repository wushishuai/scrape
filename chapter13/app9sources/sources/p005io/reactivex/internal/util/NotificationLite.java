package p005io.reactivex.internal.util;

import java.io.Serializable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.util.NotificationLite */
/* loaded from: classes.dex */
public enum NotificationLite {
    COMPLETE;

    /* JADX WARN: Multi-variable type inference failed */
    public static <T> T getValue(Object obj) {
        return obj;
    }

    public static <T> Object next(T t) {
        return t;
    }

    @Override // java.lang.Enum, java.lang.Object
    public String toString() {
        return "NotificationLite.Complete";
    }

    /* renamed from: io.reactivex.internal.util.NotificationLite$ErrorNotification */
    /* loaded from: classes.dex */
    public static final class ErrorNotification implements Serializable {
        private static final long serialVersionUID = -8759979445933046293L;

        /* renamed from: e */
        final Throwable f199e;

        ErrorNotification(Throwable th) {
            this.f199e = th;
        }

        @Override // java.lang.Object
        public String toString() {
            return "NotificationLite.Error[" + this.f199e + "]";
        }

        @Override // java.lang.Object
        public int hashCode() {
            return this.f199e.hashCode();
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            if (obj instanceof ErrorNotification) {
                return ObjectHelper.equals(this.f199e, ((ErrorNotification) obj).f199e);
            }
            return false;
        }
    }

    /* renamed from: io.reactivex.internal.util.NotificationLite$SubscriptionNotification */
    /* loaded from: classes.dex */
    public static final class SubscriptionNotification implements Serializable {
        private static final long serialVersionUID = -1322257508628817540L;
        final Subscription upstream;

        SubscriptionNotification(Subscription subscription) {
            this.upstream = subscription;
        }

        @Override // java.lang.Object
        public String toString() {
            return "NotificationLite.Subscription[" + this.upstream + "]";
        }
    }

    /* renamed from: io.reactivex.internal.util.NotificationLite$DisposableNotification */
    /* loaded from: classes.dex */
    public static final class DisposableNotification implements Serializable {
        private static final long serialVersionUID = -7482590109178395495L;
        final Disposable upstream;

        DisposableNotification(Disposable disposable) {
            this.upstream = disposable;
        }

        @Override // java.lang.Object
        public String toString() {
            return "NotificationLite.Disposable[" + this.upstream + "]";
        }
    }

    public static Object complete() {
        return COMPLETE;
    }

    public static Object error(Throwable th) {
        return new ErrorNotification(th);
    }

    public static Object subscription(Subscription subscription) {
        return new SubscriptionNotification(subscription);
    }

    public static Object disposable(Disposable disposable) {
        return new DisposableNotification(disposable);
    }

    public static boolean isComplete(Object obj) {
        return obj == COMPLETE;
    }

    public static boolean isError(Object obj) {
        return obj instanceof ErrorNotification;
    }

    public static boolean isSubscription(Object obj) {
        return obj instanceof SubscriptionNotification;
    }

    public static boolean isDisposable(Object obj) {
        return obj instanceof DisposableNotification;
    }

    public static Throwable getError(Object obj) {
        return ((ErrorNotification) obj).f199e;
    }

    public static Subscription getSubscription(Object obj) {
        return ((SubscriptionNotification) obj).upstream;
    }

    public static Disposable getDisposable(Object obj) {
        return ((DisposableNotification) obj).upstream;
    }

    public static <T> boolean accept(Object obj, Subscriber<? super T> subscriber) {
        if (obj == COMPLETE) {
            subscriber.onComplete();
            return true;
        } else if (obj instanceof ErrorNotification) {
            subscriber.onError(((ErrorNotification) obj).f199e);
            return true;
        } else {
            subscriber.onNext(obj);
            return false;
        }
    }

    public static <T> boolean accept(Object obj, Observer<? super T> observer) {
        if (obj == COMPLETE) {
            observer.onComplete();
            return true;
        } else if (obj instanceof ErrorNotification) {
            observer.onError(((ErrorNotification) obj).f199e);
            return true;
        } else {
            observer.onNext(obj);
            return false;
        }
    }

    public static <T> boolean acceptFull(Object obj, Subscriber<? super T> subscriber) {
        if (obj == COMPLETE) {
            subscriber.onComplete();
            return true;
        } else if (obj instanceof ErrorNotification) {
            subscriber.onError(((ErrorNotification) obj).f199e);
            return true;
        } else if (obj instanceof SubscriptionNotification) {
            subscriber.onSubscribe(((SubscriptionNotification) obj).upstream);
            return false;
        } else {
            subscriber.onNext(obj);
            return false;
        }
    }

    public static <T> boolean acceptFull(Object obj, Observer<? super T> observer) {
        if (obj == COMPLETE) {
            observer.onComplete();
            return true;
        } else if (obj instanceof ErrorNotification) {
            observer.onError(((ErrorNotification) obj).f199e);
            return true;
        } else if (obj instanceof DisposableNotification) {
            observer.onSubscribe(((DisposableNotification) obj).upstream);
            return false;
        } else {
            observer.onNext(obj);
            return false;
        }
    }
}
