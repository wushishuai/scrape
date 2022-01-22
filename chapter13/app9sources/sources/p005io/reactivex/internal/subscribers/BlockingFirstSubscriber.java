package p005io.reactivex.internal.subscribers;

import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.subscribers.BlockingFirstSubscriber */
/* loaded from: classes.dex */
public final class BlockingFirstSubscriber<T> extends BlockingBaseSubscriber<T> {
    @Override // org.reactivestreams.Subscriber
    public void onNext(T t) {
        if (this.value == null) {
            this.value = t;
            this.upstream.cancel();
            countDown();
        }
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable th) {
        if (this.value == null) {
            this.error = th;
        } else {
            RxJavaPlugins.onError(th);
        }
        countDown();
    }
}
