package p005io.reactivex.internal.util;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.FlowableSubscriber;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.Observer;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.util.EmptyComponent */
/* loaded from: classes.dex */
public enum EmptyComponent implements FlowableSubscriber<Object>, Observer<Object>, MaybeObserver<Object>, SingleObserver<Object>, CompletableObserver, Subscription, Disposable {
    INSTANCE;

    @Override // org.reactivestreams.Subscription
    public void cancel() {
    }

    @Override // p005io.reactivex.disposables.Disposable
    public void dispose() {
    }

    @Override // p005io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return true;
    }

    @Override // org.reactivestreams.Subscriber
    public void onComplete() {
    }

    @Override // org.reactivestreams.Subscriber
    public void onNext(Object obj) {
    }

    @Override // p005io.reactivex.MaybeObserver
    public void onSuccess(Object obj) {
    }

    @Override // org.reactivestreams.Subscription
    public void request(long j) {
    }

    public static <T> Subscriber<T> asSubscriber() {
        return INSTANCE;
    }

    public static <T> Observer<T> asObserver() {
        return INSTANCE;
    }

    @Override // p005io.reactivex.Observer
    public void onSubscribe(Disposable disposable) {
        disposable.dispose();
    }

    @Override // p005io.reactivex.FlowableSubscriber, org.reactivestreams.Subscriber
    public void onSubscribe(Subscription subscription) {
        subscription.cancel();
    }

    @Override // org.reactivestreams.Subscriber
    public void onError(Throwable th) {
        RxJavaPlugins.onError(th);
    }
}
