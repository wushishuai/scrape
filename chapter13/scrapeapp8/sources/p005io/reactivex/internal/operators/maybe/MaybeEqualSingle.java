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

    public MaybeEqualSingle(MaybeSource<? extends T> source1, MaybeSource<? extends T> source2, BiPredicate<? super T, ? super T> isEqual) {
        this.source1 = source1;
        this.source2 = source2;
        this.isEqual = isEqual;
    }

    @Override // p005io.reactivex.Single
    protected void subscribeActual(SingleObserver<? super Boolean> observer) {
        EqualCoordinator<T> parent = new EqualCoordinator<>(observer, this.isEqual);
        observer.onSubscribe(parent);
        parent.subscribe(this.source1, this.source2);
    }

    /* renamed from: io.reactivex.internal.operators.maybe.MaybeEqualSingle$EqualCoordinator */
    /* loaded from: classes.dex */
    static final class EqualCoordinator<T> extends AtomicInteger implements Disposable {
        final SingleObserver<? super Boolean> downstream;
        final BiPredicate<? super T, ? super T> isEqual;
        final EqualObserver<T> observer1 = new EqualObserver<>(this);
        final EqualObserver<T> observer2 = new EqualObserver<>(this);

        EqualCoordinator(SingleObserver<? super Boolean> actual, BiPredicate<? super T, ? super T> isEqual) {
            super(2);
            this.downstream = actual;
            this.isEqual = isEqual;
        }

        void subscribe(MaybeSource<? extends T> source1, MaybeSource<? extends T> source2) {
            source1.subscribe(this.observer1);
            source2.subscribe(this.observer2);
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
                Object o1 = this.observer1.value;
                Object o2 = this.observer2.value;
                if (o1 == null || o2 == null) {
                    this.downstream.onSuccess(Boolean.valueOf(o1 == null && o2 == null));
                    return;
                }
                try {
                    this.downstream.onSuccess(Boolean.valueOf(this.isEqual.test(o1, o2)));
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    this.downstream.onError(ex);
                }
            }
        }

        void error(EqualObserver<T> sender, Throwable ex) {
            if (getAndSet(0) > 0) {
                EqualObserver<T> equalObserver = this.observer1;
                if (sender == equalObserver) {
                    this.observer2.dispose();
                } else {
                    equalObserver.dispose();
                }
                this.downstream.onError(ex);
                return;
            }
            RxJavaPlugins.onError(ex);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.internal.operators.maybe.MaybeEqualSingle$EqualObserver */
    /* loaded from: classes.dex */
    public static final class EqualObserver<T> extends AtomicReference<Disposable> implements MaybeObserver<T> {
        private static final long serialVersionUID = -3031974433025990931L;
        final EqualCoordinator<T> parent;
        Object value;

        EqualObserver(EqualCoordinator<T> parent) {
            this.parent = parent;
        }

        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onSuccess(T value) {
            this.value = value;
            this.parent.done();
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onError(Throwable e) {
            this.parent.error(this, e);
        }

        @Override // p005io.reactivex.MaybeObserver
        public void onComplete() {
            this.parent.done();
        }
    }
}
