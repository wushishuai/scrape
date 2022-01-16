package p005io.reactivex.internal.operators.single;

import org.reactivestreams.Subscriber;
import p005io.reactivex.Flowable;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.subscriptions.DeferredScalarSubscription;

/* renamed from: io.reactivex.internal.operators.single.SingleToFlowable */
/* loaded from: classes.dex */
public final class SingleToFlowable<T> extends Flowable<T> {
    final SingleSource<? extends T> source;

    public SingleToFlowable(SingleSource<? extends T> source) {
        this.source = source;
    }

    @Override // p005io.reactivex.Flowable
    public void subscribeActual(Subscriber<? super T> s) {
        this.source.subscribe(new SingleToFlowableObserver(s));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleToFlowable$SingleToFlowableObserver */
    /* loaded from: classes.dex */
    static final class SingleToFlowableObserver<T> extends DeferredScalarSubscription<T> implements SingleObserver<T> {
        private static final long serialVersionUID = 187782011903685568L;
        Disposable upstream;

        SingleToFlowableObserver(Subscriber<? super T> downstream) {
            super(downstream);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            complete(value);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.downstream.onError(e);
        }

        @Override // p005io.reactivex.internal.subscriptions.DeferredScalarSubscription, org.reactivestreams.Subscription
        public void cancel() {
            super.cancel();
            this.upstream.dispose();
        }
    }
}
