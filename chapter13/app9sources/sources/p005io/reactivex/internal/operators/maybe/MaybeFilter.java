package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeFilter */
/* loaded from: classes.dex */
public final class MaybeFilter<T> extends AbstractMaybeWithUpstream<T, T> {
    final Predicate<? super T> predicate;

    public MaybeFilter(MaybeSource<T> maybeSource, Predicate<? super T> predicate) {
        super(maybeSource);
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new FilterMaybeObserver(maybeObserver, this.predicate));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeFilter$FilterMaybeObserver */
    /* loaded from: classes.dex */
    static final class FilterMaybeObserver<T> implements MaybeObserver<T>, Disposable {
        final MaybeObserver<? super T> downstream;
        final Predicate<? super T> predicate;
        Disposable upstream;

        FilterMaybeObserver(MaybeObserver<? super T> maybeObserver, Predicate<? super T> predicate) {
            this.downstream = maybeObserver;
            this.predicate = predicate;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            Disposable disposable = this.upstream;
            this.upstream = DisposableHelper.DISPOSED;
            disposable.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
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
                if (this.predicate.test(t)) {
                    this.downstream.onSuccess(t);
                } else {
                    this.downstream.onComplete();
                }
            } catch (Throwable th) {
                Exceptions.throwIfFatal(th);
                this.downstream.onError(th);
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
