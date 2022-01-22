package p005io.reactivex.internal.operators.maybe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import p005io.reactivex.MaybeObserver;
import p005io.reactivex.MaybeSource;
import p005io.reactivex.Single;
import p005io.reactivex.SingleObserver;
import p005io.reactivex.disposables.Disposable;
import p005io.reactivex.exceptions.Exceptions;
import p005io.reactivex.functions.BiPredicate;
import p005io.reactivex.internal.disposables.DisposableHelper;
import p005io.reactivex.plugins.RxJavaPlugins;

/* renamed from: io.reactivex.internal.operators.maybe.MaybeEqualSingle */
/* loaded from: classes.dex */
public final class MaybeEqualSingle<T> extends Single<Boolean> {
    final BiPredicate<? super T, ? super T> isEqual;
    final MaybeSource<? extends T> source1;
    final MaybeSource<? extends T> source2;

    public MaybeEqualSingle(MaybeSource<? extends T> maybeSource, MaybeSource<? extends T> maybeSource2, BiPredicate<? super T, ? super T> biPredicate) {
        this.source1 = maybeSource;
        this.source2 = maybeSource2;
        this.isEqual = biPredicate;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Boolean> singleObserver) {
        EqualCoordinator equalCoordinator = new EqualCoordinator(singleObserver, this.isEqual);
        singleObserver.onSubscribe(equalCoordinator);
        equalCoordinator.subscribe(this.source1, this.source2);
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeEqualSingle$EqualCoordinator */
    /* loaded from: classes.dex */
    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {
        final SingleObserver<? super Boolean> downstream;
        final BiPredicate<? super T, ? super T> isEqual;
        final EqualObserver<T> observer1 = new EqualObserver<>(this);
        final EqualObserver<T> observer2 = new EqualObserver<>(this);

        EqualCoordinator(SingleObserver<? super Boolean> singleObserver, BiPredicate<? super T, ? super T> biPredicate) {
            super(2);
            this.downstream = singleObserver;
            this.isEqual = biPredicate;
        }

        void subscribe(MaybeSource<? extends T> maybeSource, MaybeSource<? extends T> maybeSource2) {
            maybeSource.subscribe(this.observer1);
            maybeSource2.subscribe(this.observer2);
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.observer1.dispose();
            this.observer2.dispose();
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(this.observer1.get());
        }

        void done() {
            if (decrementAndGet() == 0) {
                Object obj = this.observer1.value;
                Object obj2 = this.observer2.value;
                if (obj == null || obj2 == null) {
                    this.downstream.onSuccess(Boolean.valueOf(obj == null && obj2 == null));
                    return;
                }
                try {
                    this.downstream.onSuccess(Boolean.valueOf(this.isEqual.test(obj, obj2)));
                } catch (Throwable th) {
                    Exceptions.throwIfFatal(th);
                    this.downstream.onError(th);
                }
            }
        }

        void error(EqualObserver<T> equalObserver, Throwable th) {
            if (getAndSet(0) > 0) {
                EqualObserver<T> equalObserver2 = this.observer1;
                if (equalObserver == equalObserver2) {
                    this.observer2.dispose();
                } else {
                    equalObserver2.dispose();
                }
                this.downstream.onError(th);
                return;
            }
            RxJavaPlugins.onError(th);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.maybe.MaybeEqualSingle$EqualObserver */
    /* loaded from: classes.dex */
    public static final class EqualObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T> {
        private static final long serialVersionUID = -3031974433025990931L;
        final EqualCoordinator<T> parent;
        Object value;

        EqualObserver(EqualCoordinator<T> equalCoordinator) {
            this.parent = equalCoordinator;
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable disposable) {
            DisposableHelper.setOnce(this, disposable);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T t) {
            this.value = t;
            this.parent.done();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable th) {
            this.parent.error(this, th);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.parent.done();
        }
    }
}
