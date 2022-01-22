package p005io.reactivex.internal.operators.maybe;

import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.internal.fuseable.HasUpstreamMaybeSource;
import p005io.reactivex.internal.observers.DeferredScalarDisposable;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeToObservable */
/* loaded from: classes.dex */
public final class MaybeToObservable<T> extends Observable<T> implements HasUpstreamMaybeSource<T> {
    final MaybeSource<T> source;

    public MaybeToObservable(MaybeSource<T> maybeSource) {
        this.source = maybeSource;
    }

    @Override // p005io.reactivex.internal.fuseable.HasUpstreamMaybeSource
    public MaybeSource<T> source() {
        return this.source;
    }

    @Override // p005io.reactivex.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        this.source.subscribe(create(observer));
    }

    public static <T> MaybeObserver<T> create(Observer<? super T> observer) {
        return new MaybeToObservableObserver(observer);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.maybe.MaybeToObservable$MaybeToObservableObserver */
    /* loaded from: classes.dex */
    public static final class MaybeToObservableObserver<T> extends DeferredScalarDisposable<T> implements MaybeObserver<T> {
        private static final long serialVersionUID = 7603343402964826922L;
        Disposable upstream;

        MaybeToObservableObserver(Observer<? super T> observer) {
            super(observer);
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
            complete(t);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            error(th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            complete();
        }

        @Override // p005io.reactivex.internal.observers.DeferredScalarDisposable, p005io.reactivex.disposables.Disposable
        public void dispose() {
            super.dispose();
            this.upstream.dispose();
        }
    }
}
