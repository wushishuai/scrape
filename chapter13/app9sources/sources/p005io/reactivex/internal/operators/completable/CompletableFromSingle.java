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

    public CompletableFromSingle(SingleSource<T> singleSource) {
        this.single = singleSource;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        this.single.subscribe(new CompletableFromSingleObserver(completableObserver));
    }

    /* renamed from: io.reactivex.internal.operators.completable.CompletableFromSingle$CompletableFromSingleObserver */
    /* loaded from: classes.dex */
    static final class CompletableFromSingleObserver<T> implements SingleObserver<T> {

        /* renamed from: co */
        final CompletableObserver f112co;

        CompletableFromSingleObserver(CompletableObserver completableObserver) {
            this.f112co = completableObserver;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.f112co.onError(th);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            this.f112co.onSubscribe(disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            this.f112co.onComplete();
        }
    }
}
