package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.observers.ResumeSingleObserver;

/* renamed from: io.reactivex.internal.operators.single.SingleDelayWithCompletable */
/* loaded from: classes.dex */
public final class SingleDelayWithCompletable<T> extends Single<T> {
    final CompletableSource other;
    final SingleSource<T> source;

    public SingleDelayWithCompletable(SingleSource<T> singleSource, CompletableSource completableSource) {
        this.source = singleSource;
        this.other = completableSource;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super T> singleObserver) {
        this.other.subscribe(new OtherObserver(singleObserver, this.source));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleDelayWithCompletable$OtherObserver */
    /* loaded from: classes.dex */
    static final class OtherObserver<T> extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
        private static final long serialVersionUID = -8565274649390031272L;
        final SingleObserver<? super T> downstream;
        final SingleSource<T> source;

        OtherObserver(SingleObserver<? super T> singleObserver, SingleSource<T> singleSource) {
            this.downstream = singleObserver;
            this.source = singleSource;
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.setOnce(this, disposable)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.CompletableObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.source.subscribe(new ResumeSingleObserver(this, this.downstream));
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
