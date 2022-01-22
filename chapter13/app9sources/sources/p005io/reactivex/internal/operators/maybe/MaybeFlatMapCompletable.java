package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Completable;
import p005io.reactivex.CompletableObserver;
import p005io.reactivex.CompletableSource;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeFlatMapCompletable */
/* loaded from: classes.dex */
public final class MaybeFlatMapCompletable<T> extends Completable {
    final Function<? super T, ? extends CompletableSource> mapper;
    final MaybeSource<T> source;

    public MaybeFlatMapCompletable(MaybeSource<T> maybeSource, Function<? super T, ? extends CompletableSource> function) {
        this.source = maybeSource;
        this.mapper = function;
    }

    @Override // p005io.reactivex.Completable
    protected void subscribeActual(CompletableObserver completableObserver) {
        FlatMapCompletableObserver flatMapCompletableObserver = new FlatMapCompletableObserver(completableObserver, this.mapper);
        completableObserver.onSubscribe(flatMapCompletableObserver);
        this.source.subscribe(flatMapCompletableObserver);
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeFlatMapCompletable$FlatMapCompletableObserver */
    /* loaded from: classes.dex */
    static final class FlatMapCompletableObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T>, CompletableObserver, Disposable {
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

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.replace(this, disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
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

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.downstream.onComplete();
        }
    }
}
