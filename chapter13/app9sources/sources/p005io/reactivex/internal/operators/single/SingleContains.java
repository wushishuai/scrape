package p005io.reactivex.internal.operators.single;

import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiPredicate;

/* renamed from: io.reactivex.internal.operators.single.SingleContains */
/* loaded from: classes.dex */
public final class SingleContains<T> extends Single<Boolean> {
    final BiPredicate<Object, Object> comparer;
    final SingleSource<T> source;
    final Object value;

    public SingleContains(SingleSource<T> singleSource, Object obj, BiPredicate<Object, Object> biPredicate) {
        this.source = singleSource;
        this.value = obj;
        this.comparer = biPredicate;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Boolean> singleObserver) {
        this.source.subscribe(new ContainsSingleObserver(singleObserver));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleContains$ContainsSingleObserver */
    /* loaded from: classes.dex */
    final class ContainsSingleObserver implements SingleObserver<T> {
        private final SingleObserver<? super Boolean> downstream;

        ContainsSingleObserver(SingleObserver<? super Boolean> singleObserver) {
            this.downstream = singleObserver;
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            this.downstream.onSubscribe(disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            try {
                this.downstream.onSuccess(Boolean.valueOf(SingleContains.this.comparer.test(t, SingleContains.this.value)));
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.downstream.onError(th);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }
    }
}
