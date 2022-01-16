package p005io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import p005io.reactivex.Flowable;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.flowable.FlowableFromObservable */
/* loaded from: classes.dex */
public final class FlowableFromObservable<T> extends Flowable<T> {
    private final Observable<T> upstream;

    public FlowableFromObservable(Observable<T> upstream) {
        this.upstream = upstream;
    }

    @Override // p005io.reactivex.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        this.upstream.subscribe(new SubscriberObserver(s));
    }

    /* renamed from: io.reactivex.internal.operators.flowable.FlowableFromObservable$SubscriberObserver */
    /* loaded from: classes.dex */
    static final class SubscriberObserver<T> implements Observer<T>, Subscription {
        final Subscriber<? super T> downstream;
        Disposable upstream;

        SubscriberObserver(Subscriber<? super T> s) {
            this.downstream = s;
        }

        @Override // p005io.reactivex.Observer
        public void onComplete() {
            this.downstream.onComplete();
        }

        @Override // p005io.reactivex.Observer
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.Observer
        public void onNext(T value) {
            this.downstream.onNext(value);
        }

        @Override // p005io.reactivex.Observer
        public void onSubscribe(Disposable d) {
            this.upstream = d;
            this.downstream.onSubscribe(this);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.dispose();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
        }
    }
}
