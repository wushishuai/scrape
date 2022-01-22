package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.Maybe;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.SingleSource;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.Predicate;
import p005io.reactivex.internal.disposables.DisposableHelper;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeFilterSingle */
/* loaded from: classes.dex */
public final class MaybeFilterSingle<T> extends Maybe<T> {
    final Predicate<? super T> predicate;
    final SingleSource<T> source;

    public MaybeFilterSingle(SingleSource<T> singleSource, Predicate<? super T> predicate) {
        this.source = singleSource;
        this.predicate = predicate;
    }

    @Override // p005io.reactivex.Maybe
    protected void subscribeActual(MaybeObserver<? super T> maybeObserver) {
        this.source.subscribe(new FilterMaybeObserver(maybeObserver, this.predicate));
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeFilterSingle$FilterMaybeObserver */
    /* loaded from: classes.dex */
    static final class FilterMaybeObserver<T> implements SingleObserver<T>, Disposable {
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

        @Override // p005io.reactivex.SingleObserver
        public void onSubscribe(Disposable disposable) {
            if (DisposableHelper.validate(this.upstream, disposable)) {
                this.upstream = disposable;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // p005io.reactivex.SingleObserver
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

        @Override // p005io.reactivex.SingleObserver
        public void onError(Throwable th) {
            this.downstream.onError(th);
        }
    }
}
