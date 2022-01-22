package p005io.reactivex.internal.observers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.observers.SubscriberCompletableObserver */
/* loaded from: classes.dex */
public final class SubscriberCompletableObserver<T> implements CompletableObserver, Subscription {
    final Subscriber<? super T> subscriber;
    Disposable upstream;

    @Override // org.reactivestreams.Subscription
    public void request(long j) {
    }

    public SubscriberCompletableObserver(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
    }

    @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
    public void onComplete() {
        this.subscriber.onComplete();
    }

    @Override // p005io.reactivex.CompletableObserver
    public void onError(Throwable th) {
        this.subscriber.onError(th);
    }

    @Override // p005io.reactivex.CompletableObserver
    public void onSubscribe(Disposable disposable) {
        if (DisposableHelper.validate(this.upstream, disposable)) {
            this.upstream = disposable;
            this.subscriber.onSubscribe(this);
        }
    }

    @Override // org.reactivestreams.Subscription
    public void cancel() {
        this.upstream.dispose();
    }
}
