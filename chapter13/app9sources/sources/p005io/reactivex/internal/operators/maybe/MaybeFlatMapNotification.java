package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.CompositeException;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Function;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.functions.ObjectHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeFlatMapNotification */
/* loaded from: classes.dex */
public final class MaybeFlatMapNotification<T, R> extends AbstractMaybeWithUpstream<T, R> {
    final Callable<? extends MaybeSource<? extends R>> onCompleteSupplier;
    final Function<? super Throwable, ? extends MaybeSource<? extends R>> onErrorMapper;
    final Function<? super T, ? extends MaybeSource<? extends R>> onSuccessMapper;

    public MaybeFlatMapNotification(MaybeSource<T> maybeSource, Function<? super T, ? extends MaybeSource<? extends R>> function, Function<? super Throwable, ? extends MaybeSource<? extends R>> function2, Callable<? extends MaybeSource<? extends R>> callable) {
        super(maybeSource);
        this.onSuccessMapper = function;
        this.onErrorMapper = function2;
        this.onCompleteSupplier = callable;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super R> maybeObserver) {
        this.source.subscribe(new FlatMapMaybeObserver(maybeObserver, this.onSuccessMapper, this.onErrorMapper, this.onCompleteSupplier));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeFlatMapNotification$FlatMapMaybeObserver */
    /* loaded from: classes.dex */
    static final class FlatMapMaybeObserver<T, R> extends AtomicReference<Disposable> implements MaybeObserver<T>, Disposable {
        private static final long serialVersionUID = 4375739915521278546L;
        final MaybeObserver<? super R> downstream;
        final Callable<? extends MaybeSource<? extends R>> onCompleteSupplier;
        final Function<? super Throwable, ? extends MaybeSource<? extends R>> onErrorMapper;
        final Function<? super T, ? extends MaybeSource<? extends R>> onSuccessMapper;
        Disposable upstream;

        FlatMapMaybeObserver(MaybeObserver<? super R> maybeObserver, Function<? super T, ? extends MaybeSource<? extends R>> function, Function<? super Throwable, ? extends MaybeSource<? extends R>> function2, Callable<? extends MaybeSource<? extends R>> callable) {
            this.downstream = maybeObserver;
            this.onSuccessMapper = function;
            this.onErrorMapper = function2;
            this.onCompleteSupplier = callable;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            DisposableHelper.dispose(this);
            this.upstream.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            try {
                ((MaybeSource) ObjectHelper.requireNonNull(this.onSuccessMapper.apply(t), "The onSuccessMapper returned a null MaybeSource")).subscribe(new InnerObserver());
            } catch (Exception e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(e);
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            try {
                ((MaybeSource) ObjectHelper.requireNonNull(this.onErrorMapper.apply(th), "The onErrorMapper returned a null MaybeSource")).subscribe(new InnerObserver());
            } catch (Exception e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(new CompositeException(th, e));
            }
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            try {
                ((MaybeSource) ObjectHelper.requireNonNull(this.onCompleteSupplier.call(), "The onCompleteSupplier returned a null MaybeSource")).subscribe(new InnerObserver());
            } catch (Exception e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(e);
            }
        }

        /* renamed from: io.reactivex.internal.operators.maybe.MaybeFlatMapNotification$FlatMapMaybeObserver$InnerObserver */
        /* loaded from: classes.dex */
        final class InnerObserver implements MaybeObserver<R> {
            InnerObserver() {
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSubscribe(Disposable disposable) {
                DisposableHelper.setOnce(FlatMapMaybeObserver.this, disposable);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onSuccess(R r) {
                FlatMapMaybeObserver.this.downstream.onSuccess(r);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onError(Throwable th) {
                FlatMapMaybeObserver.this.downstream.onError(th);
            }

            @Override // p005io.reactivex.MaybeObserver
            public void onComplete() {
                FlatMapMaybeObserver.this.downstream.onComplete();
            }
        }
    }
}
