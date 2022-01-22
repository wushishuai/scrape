package p005io.reactivex.internal.subscriptions;

import java.util.concurrent.atomic.AtomicReferenceArray;
import org.reactivestreams.Subscription;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.subscriptions.ArrayCompositeSubscription */
/* loaded from: classes.dex */
public final class ArrayCompositeSubscription extends AtomicReferenceArray<Subscription> implements Disposable {
    private static final long serialVersionUID = 2746389416410565408L;

    public ArrayCompositeSubscription(int i) {
        super(i);
    }

    public boolean setResource(int i, Subscription subscription) {
        Subscription subscription2;
        do {
            subscription2 = get(i);
            if (subscription2 == SubscriptionHelper.CANCELLED) {
                if (subscription == null) {
                    return false;
                }
                subscription.cancel();
                return false;
            }
        } while (!compareAndSet(i, subscription2, subscription));
        if (subscription2 == null) {
            return true;
        }
        subscription2.cancel();
        return true;
    }

    public Subscription replaceResource(int i, Subscription subscription) {
        Subscription subscription2;
        do {
            subscription2 = get(i);
            if (subscription2 == SubscriptionHelper.CANCELLED) {
                if (subscription == null) {
                    return null;
                }
                subscription.cancel();
                return null;
            }
        } while (!compareAndSet(i, subscription2, subscription));
        return subscription2;
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
        Subscription andSet;
        if (get(0) != SubscriptionHelper.CANCELLED) {
            int length = length();
            for (int i = 0; i < length; i++) {
                if (!(get(i) == SubscriptionHelper.CANCELLED || (andSet = getAndSet(i, SubscriptionHelper.CANCELLED)) == SubscriptionHelper.CANCELLED || andSet == null)) {
                    andSet.cancel();
                }
            }
        }
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return get(0) == SubscriptionHelper.CANCELLED;
    }
}
