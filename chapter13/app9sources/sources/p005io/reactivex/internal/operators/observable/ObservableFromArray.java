package p005io.reactivex.internal.operators.observable;

import p005io.reactivex.Observable;
import p005io.reactivex.Observer;
import p005io.reactivex.annotations.Nullable;
import p005io.reactivex.internal.functions.ObjectHelper;
import p005io.reactivex.internal.observers.BasicQueueDisposable;

/* renamed from: io.reactivex.internal.operators.observable.ObservableFromArray */
/* loaded from: classes.dex */
public final class ObservableFromArray<T> extends Observable<T> {
    final T[] array;

    public ObservableFromArray(T[] tArr) {
        this.array = tArr;
    }

    @Override // p005io.reactivex.Observable
    public void subscribeActual(Observer<? super T> observer) {
        FromArrayDisposable fromArrayDisposable = new FromArrayDisposable(observer, this.array);
        observer.onSubscribe(fromArrayDisposable);
        if (!fromArrayDisposable.fusionMode) {
            fromArrayDisposable.run();
        }
    }

    /* renamed from: io.reactivex.internal.operators.observable.ObservableFromArray$FromArrayDisposable */
    /* loaded from: classes.dex */
    static final class FromArrayDisposable<T> extends BasicQueueDisposable<T> {
        final T[] array;
        volatile boolean disposed;
        final Observer<? super T> downstream;
        boolean fusionMode;
        int index;

        FromArrayDisposable(Observer<? super T> observer, T[] tArr) {
            this.downstream = observer;
            this.array = tArr;
        }

        @Override // p005io.reactivex.internal.fuseable.QueueFuseable
        public int requestFusion(int i) {
            if ((i & 1) == 0) {
                return 0;
            }
            this.fusionMode = true;
            return 1;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        @Nullable
        public T poll() {
            int i = this.index;
            T[] tArr = this.array;
            if (i == tArr.length) {
                return null;
            }
            this.index = i + 1;
            return (T) ObjectHelper.requireNonNull(tArr[i], "The array element is null");
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.index == this.array.length;
        }

        @Override // p005io.reactivex.internal.fuseable.SimpleQueue
        public void clear() {
            this.index = this.array.length;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public void dispose() {
            this.disposed = true;
        }

        @Override // p005io.reactivex.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        void run() {
            T[] tArr = this.array;
            int length = tArr.length;
            for (int i = 0; i < length && !isDisposed(); i++) {
                T t = tArr[i];
                if (t == null) {
                    Observer<? super T> observer = this.downstream;
                    observer.onError(new NullPointerException("The " + i + "th element is null"));
                    return;
                }
                this.downstream.onNext(t);
            }
            if (!isDisposed()) {
                this.downstream.onComplete();
            }
        }
    }
}
