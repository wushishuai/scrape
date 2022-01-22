package p005io.reactivex.internal.operators.single;

import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.single.SingleFlatMapMaybe */
/* loaded from: classes.dex */
public final class SingleFlatMapMaybe<T, R> extends Maybe<R> {
    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
    final SingleSource<? extends T> source;

    public SingleFlatMapMaybe(SingleSource<? extends T> singleSource, Function<? super T, ? extends MaybeSource<? extends R>> function) {
        this.mapper = function;
        this.source = singleSource;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super R> maybeObserver) {
        this.source.subscribe(new FlatMapSingleObserver(maybeObserver, this.mapper));
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleFlatMapMaybe$FlatMapSingleObserver */
    /* loaded from: classes.dex */
    static final class FlatMapSingleObserver<T, R> extends AtomicReference<Disposable> implements SingleObserver<T>, Disposable {
        private static final long serialVersionUID = -5843758257109742742L;
        final MaybeObserver<? super R> downstream;
        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

        FlatMapSingleObserver(MaybeObserver<? super R> maybeObserver, Function<? super T, ? extends MaybeSource<? extends R>> function) {
            this.downstream = maybeObserver;
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
            if (DisposableHelper.setOnce(this, disposable)) {
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
        public void onSuccess(T t) {
            try {
                MaybeSource maybeSource = (MaybeSource) ObjectHelper.requireNonNull(this.mapper.apply(t), "The mapper returned a null MaybeSource");
                if (!isDisposed()) {
                    maybeSource.subscribe(new FlatMapMaybeObserver(this, this.downstream));
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
    }

    /* renamed from: io.reactivex.internal.operators.single.SingleFlatMapMaybe$FlatMapMaybeObserver */
    /* loaded from: classes.dex */
    static final class FlatMapMaybeObserver<R> implements MaybeObserver<R> {
        final MaybeObserver<? super R> downstream;
        final AtomicReference<Disposable> parent;

        FlatMapMaybeObserver(AtomicReference<Disposable> atomicReference, MaybeObserver<? super R> maybeObserver) {
            this.parent = atomicReference;
            this.downstream = maybeObserver;
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.replace(this.parent, disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(R r) {
            this.downstream.onSuccess(r);
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
