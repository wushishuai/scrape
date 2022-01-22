package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleFlatMapCompletable */
/* loaded from: classes.dex */
public final class SingleFlatMapCompletable<T> extends Completable {
    final Function<? super T, ? extends CompletableSource> mapper;
    final SingleSource<T> source;

    public SingleFlatMapCompletable(SingleSource<T> singleSource, Function<? super T, ? extends CompletableSource> function) {
        this.source = singleSource;
        this.mapper = function;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        FlatMapCompletableObserver flatMapCompletableObserver = new FlatMapCompletableObserver(completableObserver, this.mapper);
        completableObserver.onSubscribe(flatMapCompletableObserver);
        this.source.subscribe(flatMapCompletableObserver);
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleFlatMapCompletable$FlatMapCompletableObserver */
    /* loaded from: classes.dex */
    static final class FlatMapCompletableObserver<T> extends AtomicReference<Disposable> implements SingleObserver<T>, CompletableObserver, Disposable {
        private static final long serialVersionUID = -2177128922851101253L;
        final CompletableObserver downstream;
        final Function<? super T, ? extends CompletableSource> mapper;

        FlatMapCompletableObserver(CompletableObserver completableObserver, Function<? super T, ? extends CompletableSource> function) {
            this.downstream = completableObserver;
            this.mapper = function;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.replace(this, disposable);
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            try {
                CompletableSource completableSource = (CompletableSource) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null CompletableSource");
                if (!isDisposed()) {
                    completableSource.subscribe(this);
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                onError(th);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.CompletableObserver, p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
        }
    }
}
