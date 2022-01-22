package p005io.reactivex.disposables;

import org.reactivestreams.Subscription;
import p005io.reactivex.annotations.NonNull;

/* renamed from: io.reactivex.disposables.SubscriptionDisposable */
/* loaded from: classes.dex */
final class SubscriptionDisposable extends ReferenceDisposable<Subscription> {
    private static final long serialVersionUID = -707001650852963139L;

    /* JADX INFO: Access modifiers changed from: package-private */
    public SubscriptionDisposable(Subscription subscription) {
        super(subscription);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onDisposed(@NonNull Subscription subscription) {
        subscription.cancel();
    }
}
