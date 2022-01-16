package p005io.reactivex.internal.operators.completable;

import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;

/* renamed from: io.reactivex.internal.operators.completable.CompletableFromSingle */
/* loaded from: classes.dex */
public final class CompletableFromSingle<T> extends Completable {
    final SingleSource<T> single;

    public CompletableFromSingle(SingleSource<T> single) {
        this.single = single;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver observer) {
        this.single.subscribe(new CompletableFromSingleObserver(observer));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableFromSingle$CompletableFromSingleObserver */
    /* loaded from: classes.dex */
    static final class CompletableFromSingleObserver<T> implements SingleObserver<T> {

        /* renamed from: co */
        final CompletableObserver f112co;

        CompletableFromSingleObserver(CompletableObserver co) {
            this.f112co = co;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable e) {
            this.f112co.onError(e);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable d) {
            this.f112co.onSubscribe(d);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T value) {
            this.f112co.onComplete();
        }
    }
}
